package ru.wilyfox.client.protocol;

import java.util.LinkedHashMap;
import java.util.Map;

public record DwBossTimersPacket(Map<String, Long> timers) {
    public static DwBossTimersPacket empty() {
        return new DwBossTimersPacket(new LinkedHashMap<>());
    }
}
