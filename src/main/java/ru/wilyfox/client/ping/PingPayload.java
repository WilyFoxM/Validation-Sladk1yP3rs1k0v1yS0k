package ru.wilyfox.client.ping;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import ru.wilyfox.client.hud.config.ConfigManager;
import ru.wilyfox.client.protocol.CurrentServerInfo;
import ru.wilyfox.client.protocol.DiamondWorldProtocolClient;

import java.util.Optional;
import java.util.UUID;

public record PingPayload(
        int version,
        String type,
        String author,
        double x,
        double y,
        double z,
        String dimension,
        String family,
        int server,
        int mirror,
        String entityUuid,
        String entityName,
        String location,
        String label,
        long timestamp
) {
    private static final int CURRENT_VERSION = 1;
    private static final String DEFAULT_TYPE = "point";
    public static final String TYPE_BLOCK = "block";
    public static final String TYPE_ENTITY = "entity";
    private static final double BLOCK_MARKER_NUDGE = 1.0D;

    public static PingPayload captureCurrent() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return null;
        }

        Player player = minecraft.player;
        CurrentServerInfo serverInfo = DiamondWorldProtocolClient.getCurrentServerInfo();
        TargetCapture targetCapture = resolveMarkerPosition(player);
        if (targetCapture == null) {
            return null;
        }

        return new PingPayload(
                CURRENT_VERSION,
                targetCapture.type(),
                player.getGameProfile().getName(),
                targetCapture.position().x,
                targetCapture.position().y,
                targetCapture.position().z,
                minecraft.level.dimension().location().toString(),
                serverInfo.family(),
                serverInfo.serverNumber(),
                serverInfo.mirror(),
                targetCapture.entityUuid() != null ? targetCapture.entityUuid().toString() : null,
                targetCapture.entityName(),
                DiamondWorldProtocolClient.getCurrentGameLocation(),
                null,
                System.currentTimeMillis()
        );
    }

    private static TargetCapture resolveMarkerPosition(Player player) {
        double pickRange = Math.max(1.0D, ConfigManager.get().wayPoints.maxDistance);
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getViewVector(0.0F).scale(pickRange));
        HitResult hitResult = player.pick(pickRange, 0.0F, false);

        double blockDistance = hitResult != null && hitResult.getType() == HitResult.Type.BLOCK
                ? start.distanceToSqr(hitResult.getLocation())
                : Double.MAX_VALUE;

        Entity nearestEntity = null;
        double nearestEntityDistance = blockDistance;

        AABB searchBox = player.getBoundingBox().expandTowards(end.subtract(start)).inflate(1.0D);
        for (Entity entity : player.level().getEntities(player, searchBox, candidate -> candidate.isPickable() && !candidate.isSpectator())) {
            AABB bounds = entity.getBoundingBox().inflate(entity.getPickRadius());
            Optional<Vec3> hitPoint = bounds.clip(start, end);
            if (hitPoint.isEmpty()) {
                if (!bounds.contains(start)) {
                    continue;
                }
            }

            double distance = hitPoint.map(point -> start.distanceToSqr(point)).orElse(0.0D);
            if (distance >= nearestEntityDistance) {
                continue;
            }

            nearestEntity = entity;
            nearestEntityDistance = distance;
        }

        if (nearestEntity != null) {
            return new TargetCapture(
                    nearestEntity.position(),
                    TYPE_ENTITY,
                    nearestEntity.getUUID(),
                    nearestEntity.getCustomName() != null ? nearestEntity.getCustomName().getString() : nearestEntity.getName().getString()
            );
        }

        if (hitResult instanceof BlockHitResult blockHitResult && hitResult.getType() == HitResult.Type.BLOCK) {
            Vec3 blockCenter = Vec3.atCenterOf(blockHitResult.getBlockPos());
            Vec3 directionToPlayer = player.position().subtract(blockCenter);
            Vec3 shiftedPosition = directionToPlayer.lengthSqr() > 1.0E-6D
                    ? blockCenter.add(directionToPlayer.normalize().scale(BLOCK_MARKER_NUDGE))
                    : blockCenter;
            return new TargetCapture(shiftedPosition, TYPE_BLOCK, null, null);
        }

        return null;
    }

    public String toJson() {
        JsonObject root = new JsonObject();
        root.addProperty("v", version);
        root.addProperty("t", type);
        if (author != null && !author.isBlank()) {
            root.addProperty("author", author);
        }
        root.addProperty("x", x);
        root.addProperty("y", y);
        root.addProperty("z", z);
        root.addProperty("dim", dimension);
        root.addProperty("family", family);
        root.addProperty("server", server);
        root.addProperty("mirror", mirror);
        if (entityUuid != null && !entityUuid.isBlank()) {
            root.addProperty("entityUuid", entityUuid);
        }
        if (entityName != null && !entityName.isBlank()) {
            root.addProperty("entityName", entityName);
        }
        if (location != null && !location.isBlank()) {
            root.addProperty("loc", location);
        }
        if (label != null && !label.isBlank()) {
            root.addProperty("label", label);
        }
        root.addProperty("ts", timestamp);
        return root.toString();
    }

    public static PingPayload fromJson(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }

        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            return new PingPayload(
                    getInt(root, "v", CURRENT_VERSION),
                    getString(root, "t", DEFAULT_TYPE),
                    getOptionalString(root, "author"),
                    getDouble(root, "x", 0.0D),
                    getDouble(root, "y", 0.0D),
                    getDouble(root, "z", 0.0D),
                    getString(root, "dim", "minecraft:overworld"),
                    getString(root, "family", "UNKNOWN"),
                    getInt(root, "server", 0),
                    getInt(root, "mirror", 0),
                    getOptionalString(root, "entityUuid"),
                    getOptionalString(root, "entityName"),
                    getOptionalString(root, "loc"),
                    getOptionalString(root, "label"),
                    getLong(root, "ts", 0L)
            );
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String getString(JsonObject root, String key, String fallback) {
        String value = getOptionalString(root, key);
        return value != null ? value : fallback;
    }

    private static String getOptionalString(JsonObject root, String key) {
        return root.has(key) && !root.get(key).isJsonNull() ? root.get(key).getAsString() : null;
    }

    private static int getInt(JsonObject root, String key, int fallback) {
        return root.has(key) && !root.get(key).isJsonNull() ? root.get(key).getAsInt() : fallback;
    }

    private static long getLong(JsonObject root, String key, long fallback) {
        return root.has(key) && !root.get(key).isJsonNull() ? root.get(key).getAsLong() : fallback;
    }

    private static double getDouble(JsonObject root, String key, double fallback) {
        return root.has(key) && !root.get(key).isJsonNull() ? root.get(key).getAsDouble() : fallback;
    }

    public UUID getEntityUuidValue() {
        if (entityUuid == null || entityUuid.isBlank()) {
            return null;
        }

        try {
            return UUID.fromString(entityUuid);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private record TargetCapture(
            Vec3 position,
            String type,
            UUID entityUuid,
            String entityName
    ) {
    }
}
