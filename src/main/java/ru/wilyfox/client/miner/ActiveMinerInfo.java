package ru.wilyfox.client.miner;

import net.minecraft.world.item.ItemStack;

public record ActiveMinerInfo(
        ItemStack icon,
        int level,
        String resource,
        String status,
        long homecomingAt
) {
}
