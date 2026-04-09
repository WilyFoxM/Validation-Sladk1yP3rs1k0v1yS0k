package ru.wilyfox.client.ping;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import ru.wilyfox.client.chat.FrogChatProtocol;
import ru.wilyfox.client.hud.config.ConfigManager;
import ru.wilyfox.client.profiler.ModProfiler;
import ru.wilyfox.client.protocol.CurrentServerInfo;
import ru.wilyfox.client.protocol.DiamondWorldProtocolClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class PingMarkerManager {
    private static final long MARKER_LIFETIME_MS = 30_000L;
    private static final double BASE_RENDER_Y_OFFSET = 1.15D;
    private static final double PIXEL_TO_WORLD_SCALE = 0.025D;
    private static final List<PingMarker> MARKERS = new ArrayList<>();
    private static final Map<UUID, CachedEntityRef> ENTITY_CACHE = new HashMap<>();
    private static boolean initialized = false;

    private PingMarkerManager() {
    }

    public static void init() {
        if (initialized) {
            return;
        }

        initialized = true;
        FrogChatProtocol.registerPingListener(PingMarkerManager::handleIncomingPayload);
    }

    public static List<PingMarker> getActiveMarkers() {
        try (ModProfiler.Scope ignored = ModProfiler.getInstance().scope("ping/PingMarkerManager/getActiveMarkers")) {
            purgeExpired();
            List<PingMarker> snapshot = List.copyOf(MARKERS);
            ModProfiler.getInstance().incrementCounter("ping/PingMarkerManager/activeMarkers", snapshot.size());
            return snapshot;
        }
    }

    public static Vec3 getRenderPosition(PingMarker marker, float partialTick) {
        try (ModProfiler.Scope ignored = ModProfiler.getInstance().scope("ping/PingMarkerManager/getRenderPosition")) {
            if (marker == null) {
                ModProfiler.getInstance().incrementCounter("ping/PingMarkerManager/getRenderPositionNullMarker");
                return null;
            }

            UUID entityUuid = marker.entityUuid();
            if (entityUuid == null) {
                ModProfiler.getInstance().incrementCounter("ping/PingMarkerManager/getRenderPositionStatic");
                return marker.position();
            }

            Entity entity;
            try (ModProfiler.Scope findScope = ModProfiler.getInstance().scope("ping/PingMarkerManager/findEntityByUuid")) {
                entity = findEntityByUuid(entityUuid);
            }
            if (entity == null) {
                ModProfiler.getInstance().incrementCounter("ping/PingMarkerManager/getRenderPositionMissingEntity");
                return marker.position();
            }

            ModProfiler.getInstance().incrementCounter("ping/PingMarkerManager/getRenderPositionEntity");
            return new Vec3(
                    Mth.lerp(partialTick, entity.xOld, entity.getX()),
                    Mth.lerp(partialTick, entity.yOld, entity.getY()) + getEntityMarkerHeightOffset(entity),
                    Mth.lerp(partialTick, entity.zOld, entity.getZ())
            );
        }
    }

    public static void addLocalMarker(PingPayload payload) {
        addMarker(payload);
    }

    private static void handleIncomingPayload(String rawPayload) {
        PingPayload payload = PingPayload.fromJson(rawPayload);
        addMarker(payload);
    }

    private static void addMarker(PingPayload payload) {
        if (payload == null || !isRelevant(payload)) {
            return;
        }

        PingMarker marker = new PingMarker(
                payload,
                new Vec3(payload.x(), payload.y(), payload.z()),
                payload.getEntityUuidValue(),
                System.currentTimeMillis()
        );

        synchronized (MARKERS) {
            String ownerKey = markerOwnerKey(payload);
            if (ownerKey != null) {
                MARKERS.removeIf(existing -> ownerKey.equals(markerOwnerKey(existing.payload())));
            } else {
                MARKERS.removeIf(existing -> isSameMarker(existing.payload(), payload));
            }
            MARKERS.add(marker);
        }
    }

    private static boolean isRelevant(PingPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return false;
        }

        CurrentServerInfo serverInfo = DiamondWorldProtocolClient.getCurrentServerInfo();
        if (!equalsIgnoreCase(payload.family(), serverInfo.family())) {
            return false;
        }

        if (payload.server() != serverInfo.serverNumber()) {
            return false;
        }

        if (payload.mirror() != serverInfo.mirror()) {
            return false;
        }

        String currentDimension = minecraft.level.dimension().location().toString();
        if (!equalsIgnoreCase(payload.dimension(), currentDimension)) {
            return false;
        }

        String currentLocation = DiamondWorldProtocolClient.getCurrentGameLocation();
        if (currentLocation != null
                && !currentLocation.isBlank()
                && payload.location() != null
                && !payload.location().isBlank()
                && !equalsIgnoreCase(currentLocation, payload.location())) {
            return false;
        }

        return true;
    }

    private static void purgeExpired() {
        try (ModProfiler.Scope ignored = ModProfiler.getInstance().scope("ping/PingMarkerManager/purgeExpired")) {
            long now = System.currentTimeMillis();
            int removed = 0;
            Set<UUID> activeEntityUuids = new java.util.HashSet<>();
            synchronized (MARKERS) {
                Iterator<PingMarker> iterator = MARKERS.iterator();
                while (iterator.hasNext()) {
                    PingMarker marker = iterator.next();
                    if (now - marker.receivedAt() > MARKER_LIFETIME_MS) {
                        iterator.remove();
                        removed++;
                        continue;
                    }

                    if (marker.entityUuid() != null) {
                        activeEntityUuids.add(marker.entityUuid());
                    }
                }
            }
            ENTITY_CACHE.keySet().removeIf(entityUuid -> !activeEntityUuids.contains(entityUuid));
            ModProfiler.getInstance().incrementCounter("ping/PingMarkerManager/purgeRemoved", removed);
        }
    }

    private static Entity findEntityByUuid(UUID entityUuid) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            ModProfiler.getInstance().incrementCounter("ping/PingMarkerManager/findEntityNoLevel");
            return null;
        }

        CachedEntityRef cached = ENTITY_CACHE.get(entityUuid);
        if (cached != null) {
            Entity cachedEntity = validateCachedEntity(minecraft, entityUuid, cached);
            if (cachedEntity != null) {
                ModProfiler.getInstance().incrementCounter("ping/PingMarkerManager/findEntityCacheHit");
                return cachedEntity;
            }

            ENTITY_CACHE.remove(entityUuid);
            ModProfiler.getInstance().incrementCounter("ping/PingMarkerManager/findEntityCacheInvalid");
        }

        int scanned = 0;
        for (Entity entity : minecraft.level.entitiesForRendering()) {
            scanned++;
            if (entityUuid.equals(entity.getUUID())) {
                ENTITY_CACHE.put(entityUuid, new CachedEntityRef(entity.getId(), entity));
                ModProfiler.getInstance().incrementCounter("ping/PingMarkerManager/findEntityScanned", scanned);
                ModProfiler.getInstance().incrementCounter("ping/PingMarkerManager/findEntityCacheMiss");
                return entity;
            }
        }

        ModProfiler.getInstance().incrementCounter("ping/PingMarkerManager/findEntityScanned", scanned);
        ModProfiler.getInstance().incrementCounter("ping/PingMarkerManager/findEntityCacheMiss");
        ModProfiler.getInstance().incrementCounter("ping/PingMarkerManager/findEntityMiss");
        return null;
    }

    private static Entity validateCachedEntity(Minecraft minecraft, UUID entityUuid, CachedEntityRef cached) {
        Entity entity = cached.entity();
        if (isValidCachedEntity(minecraft, entityUuid, entity, cached.entityId())) {
            return entity;
        }

        Entity byId = minecraft.level.getEntity(cached.entityId());
        if (isValidCachedEntity(minecraft, entityUuid, byId, cached.entityId())) {
            ENTITY_CACHE.put(entityUuid, new CachedEntityRef(cached.entityId(), byId));
            ModProfiler.getInstance().incrementCounter("ping/PingMarkerManager/findEntityCacheRecoveredById");
            return byId;
        }

        return null;
    }

    private static boolean isValidCachedEntity(Minecraft minecraft, UUID entityUuid, Entity entity, int expectedEntityId) {
        return entity != null
                && entity.level() == minecraft.level
                && !entity.isRemoved()
                && entity.getId() == expectedEntityId
                && Objects.equals(entity.getUUID(), entityUuid);
    }

    private static double getEntityMarkerHeightOffset(Entity entity) {
        double offsetPixels = Math.max(0, ConfigManager.get().wayPoints.entityOffsetPixels);
        return entity.getBbHeight() + offsetPixels * PIXEL_TO_WORLD_SCALE - BASE_RENDER_Y_OFFSET;
    }

    private static boolean equalsIgnoreCase(String left, String right) {
        if (left == null || right == null) {
            return false;
        }

        return left.equalsIgnoreCase(right);
    }

    private static boolean isSameMarker(PingPayload left, PingPayload right) {
        return equalsIgnoreCase(left.author(), right.author())
                && left.timestamp() == right.timestamp();
    }

    private static String markerOwnerKey(PingPayload payload) {
        if (payload == null || payload.author() == null) {
            return null;
        }

        String author = payload.author().trim();
        return author.isBlank() ? null : author.toLowerCase(java.util.Locale.ROOT);
    }

    private record CachedEntityRef(int entityId, Entity entity) {
    }
}
