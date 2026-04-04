package ru.wilyfox.client.protocol;

import java.util.List;

public record DwPotionTimersPacket(List<DwPotionTimerEntry> entries) {
}
