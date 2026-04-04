package ru.wilyfox.client.protocol;

public record DwPetType(
        String id,
        String name,
        String rarity,
        String material,
        int customModelData
) {
}
