package ru.wilyfox.client.protocol;

public record DwComboPacket(
        double booster,
        double nextBooster,
        int blocks,
        int requiredBlocks
) {
}
