package ru.wilyfox.client.protocol;

import java.util.Map;

public record DwStaffTimersPacket(Map<Integer, Long> timers) {
}
