package ru.wilyfox.client.protocol;

public record DwBossType(
        String id,
        String name,
        String material,
        int level,
        int capturePoints,
        int customModelData,
        boolean raid
) {
}
