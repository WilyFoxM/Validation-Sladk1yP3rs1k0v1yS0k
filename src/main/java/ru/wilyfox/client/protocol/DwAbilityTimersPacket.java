package ru.wilyfox.client.protocol;

import java.util.Map;

public record DwAbilityTimersPacket(Map<String, Long> timers) {
}
