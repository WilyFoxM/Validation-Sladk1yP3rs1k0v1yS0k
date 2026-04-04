package ru.wilyfox.client.protocol;

import java.util.Map;

public record DwFishingSpotsPacket(Map<String, String> locations) {
}
