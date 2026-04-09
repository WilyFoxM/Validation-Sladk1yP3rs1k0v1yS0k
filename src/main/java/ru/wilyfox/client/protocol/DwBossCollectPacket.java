package ru.wilyfox.client.protocol;

import java.util.Map;
import java.util.Set;

public record DwBossCollectPacket(Map<String, Set<String>> collectibles) {
}
