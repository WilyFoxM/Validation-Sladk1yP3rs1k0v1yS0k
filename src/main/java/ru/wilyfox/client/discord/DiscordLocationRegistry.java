package ru.wilyfox.client.discord;

import java.util.Map;
import java.util.Set;

final class DiscordLocationRegistry {
    private static final Set<String> HUB_IDS = Set.of(
            "hub",
            "spawn",
            "lobby"
    );
    private static final Set<String> BOSS_IDS = Set.of(
            "bosskriger",
            "boss",
            "guardian"
    );
    private static final Map<String, DiscordRpcService.RpcMode> EXACT_MODES = Map.ofEntries(
            Map.entry("hub", DiscordRpcService.RpcMode.HUB),
            Map.entry("spawn", DiscordRpcService.RpcMode.HUB),
            Map.entry("lobby", DiscordRpcService.RpcMode.HUB),
            Map.entry("bosskriger", DiscordRpcService.RpcMode.BOSS),
            Map.entry("boss", DiscordRpcService.RpcMode.BOSS),
            Map.entry("guardian", DiscordRpcService.RpcMode.BOSS),
            Map.entry("dungeon", DiscordRpcService.RpcMode.DUNGEON),
            Map.entry("siege", DiscordRpcService.RpcMode.SIEGE)
    );

    private DiscordLocationRegistry() {
    }

    static DiscordRpcService.RpcMode resolveExact(String locationId) {
        if (locationId == null || locationId.isBlank()) {
            return null;
        }
        return EXACT_MODES.get(locationId);
    }

    static boolean isHub(String locationId) {
        return locationId != null && HUB_IDS.contains(locationId);
    }

    static boolean isBoss(String locationId) {
        return locationId != null && BOSS_IDS.contains(locationId);
    }
}
