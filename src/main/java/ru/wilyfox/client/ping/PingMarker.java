package ru.wilyfox.client.ping;

import java.util.UUID;
import net.minecraft.world.phys.Vec3;

public record PingMarker(
        PingPayload payload,
        Vec3 position,
        UUID entityUuid,
        long receivedAt
) {
}
