package ru.wilyfox.client.clan;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class PlayerClanNameFormatter {
    private static final int CLAN_COLOR = 0xFF7FD0A6;

    private PlayerClanNameFormatter() {
    }

    public static Component apply(Component baseComponent, String playerName) {
        String clanName = PlayerClanStorage.getClan(playerName);
        if (clanName == null || clanName.isBlank()) {
            return baseComponent;
        }

        MutableComponent result = Component.empty();
        result.append(Component.literal("[" + clanName + "] ").withColor(CLAN_COLOR));
        result.append(baseComponent != null ? baseComponent.copy() : Component.literal(playerName));
        return result;
    }
}
