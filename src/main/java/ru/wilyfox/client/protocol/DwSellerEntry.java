package ru.wilyfox.client.protocol;

public record DwSellerEntry(
        String id,
        String name,
        long remainingMillis
) {
}
