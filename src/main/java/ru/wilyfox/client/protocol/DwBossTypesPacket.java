package ru.wilyfox.client.protocol;

import java.util.LinkedHashMap;
import java.util.Map;

public record DwBossTypesPacket(Map<String, DwBossType> types) {
    public static DwBossTypesPacket empty() {
        return new DwBossTypesPacket(new LinkedHashMap<>());
    }
}
