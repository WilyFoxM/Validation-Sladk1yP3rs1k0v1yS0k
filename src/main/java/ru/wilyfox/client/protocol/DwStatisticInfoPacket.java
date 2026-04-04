package ru.wilyfox.client.protocol;

import java.util.Map;

public record DwStatisticInfoPacket(Map<String, String> values) {
}
