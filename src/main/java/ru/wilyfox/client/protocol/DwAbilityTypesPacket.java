package ru.wilyfox.client.protocol;

import java.util.Map;

public record DwAbilityTypesPacket(Map<String, DwAbilityType> types) {
}
