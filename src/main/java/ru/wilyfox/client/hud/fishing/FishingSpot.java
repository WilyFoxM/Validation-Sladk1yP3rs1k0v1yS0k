package ru.wilyfox.client.hud.fishing;

import net.minecraft.world.phys.Vec3;

public record FishingSpot(Vec3 center, int bubbleCount, long latestTimestamp) {
}
