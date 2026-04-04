package ru.wilyfox.client.protocol;

import ru.wilyfox.client.booster.BoosterStore;

import java.util.List;
import java.util.Map;

public record DwBoostersPacket(Map<BoosterStore.Kind, List<BoosterStore.ProtocolEntry>> boosters) {
}
