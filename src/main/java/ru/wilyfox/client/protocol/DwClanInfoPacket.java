package ru.wilyfox.client.protocol;

import java.util.Map;

public record DwClanInfoPacket(Map<String, String> values) {
}
