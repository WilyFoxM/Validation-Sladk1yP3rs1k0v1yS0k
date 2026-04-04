package ru.wilyfox.client.protocol;

import java.util.List;

public record DwPotionTypesPacket(List<DwPotionTypeEntry> entries) {
}
