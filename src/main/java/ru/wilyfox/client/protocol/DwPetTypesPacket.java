package ru.wilyfox.client.protocol;

import java.util.Map;

public record DwPetTypesPacket(Map<String, DwPetType> types) {
}
