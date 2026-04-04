package ru.wilyfox.client.boss;

public record BossDamageInfo(
        String bossId,
        String bossName,
        int bossLevel,
        long damage,
        long updatedAt
) {
}
