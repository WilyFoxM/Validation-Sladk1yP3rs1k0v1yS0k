package ru.wilyfox.client.protocol;

public record DwPotionTimerEntry(
        int id,
        long remainedMillis,
        int quality
) {
}
