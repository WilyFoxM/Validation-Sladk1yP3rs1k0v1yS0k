package ru.wilyfox.client.protocol;

public record DwLevelInfoPacket(
        int level,
        double money,
        int blocks,
        double requiredMoney,
        int requiredBlocks,
        boolean maxLevel
) {
}
