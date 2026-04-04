package ru.wilyfox.client.pet;

public record ActivePetInfo(
        String id,
        String name,
        int level,
        double exp,
        double energy
) {
}
