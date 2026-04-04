package ru.wilyfox.client.ping;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import ru.wilyfox.client.chat.FrogChatProtocol;
import ru.wilyfox.client.hud.config.ConfigManager;
import ru.wilyfox.client.protocol.CurrentServerInfo;
import ru.wilyfox.client.protocol.DiamondWorldProtocolClient;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public final class PingMarkerManager {
    private static final long MARKER_LIFETIME_MS = 30_000L;
    private static final double BASE_RENDER_Y_OFFSET = 1.15D;
    private static final double PIXEL_TO_WORLD_SCALE = 0.025D;
    private static final List<PingMarker> MARKERS = new ArrayList<>();
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
        purgeExpired();
        refreshTrackedEntityPositions();
        return List.copyOf(MARKERS);
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
            MARKERS.removeIf(existing -> isSameMarker(existing.payload(), payload));
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
        long now = System.currentTimeMillis();
        synchronized (MARKERS) {
            Iterator<PingMarker> iterator = MARKERS.iterator();
            while (iterator.hasNext()) {
                PingMarker marker = iterator.next();
                if (now - marker.receivedAt() > MARKER_LIFETIME_MS) {
                    iterator.remove();
                }
            }
        }
    }

    private static void refreshTrackedEntityPositions() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        synchronized (MARKERS) {
            for (int index = 0; index < MARKERS.size(); index++) {
                PingMarker marker = MARKERS.get(index);
                UUID entityUuid = marker.entityUuid();
                if (entityUuid == null) {
                    continue;
                }

                Entity entity = findEntityByUuid(entityUuid);
                if (entity == null) {
                    continue;
                }

                MARKERS.set(index, new PingMarker(
                        marker.payload(),
                        entity.position().add(0.0D, getEntityMarkerHeightOffset(entity), 0.0D),
                        entityUuid,
                        marker.receivedAt()
                ));
            }
        }
    }

    private static Entity findEntityByUuid(UUID entityUuid) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return null;
        }

        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (entityUuid.equals(entity.getUUID())) {
                return entity;
            }
        }

        return null;
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
}
