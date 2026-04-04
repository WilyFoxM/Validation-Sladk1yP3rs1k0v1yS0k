package ru.wilyfox.client.chat;

import net.minecraft.network.chat.Component;
import ru.wilyfox.client.hud.config.ConfigManager;
import ru.wilyfox.client.hud.config.VisibilityStatusConfig;
import ru.wilyfox.client.visibility.VisibilityStatusStore;
import ru.wilyfox.utils.Formatting;

import java.util.Locale;

public final class VisibilityStatusTracker {
    private static VisibilityStatusStore store;

    private VisibilityStatusTracker() {
    }

    public static void bindStore(VisibilityStatusStore statusStore) {
        store = statusStore;
        applyConfigState();
    }

    public static void onIncomingMessage(Component component) {
        if (component == null || store == null) {
            return;
        }

        String text = Formatting.stripMinecraftFormatting(component.getString())
                .replace('\u00A0', ' ')
                .trim();
        if (text.isBlank()) {
            return;
        }

        String lower = text.toLowerCase(Locale.ROOT);

        if (lower.contains("участники клана:")) {
            if (lower.contains("скрыт")) {
                updateClanVisible(false);
            } else if (lower.contains("виден")) {
                updateClanVisible(true);
            }
            return;
        }

        if (lower.contains("теперь вы не видите игроков")) {
            updatePlayersVisible(false);
            return;
        }

        if (lower.contains("теперь вы видите игроков")) {
            updatePlayersVisible(true);
            return;
        }

        if (!lower.contains("режим отображения питомцев:")) {
            return;
        }

        if (lower.contains("только свои")) {
            updatePetsVisibility(VisibilityStatusStore.PetsVisibility.ONLY_OWN);
        } else if (lower.contains("выключено")) {
            updatePetsVisibility(VisibilityStatusStore.PetsVisibility.DISABLED);
        } else if (lower.contains("включено")) {
            updatePetsVisibility(VisibilityStatusStore.PetsVisibility.ENABLED);
        }
    }

    private static void applyConfigState() {
        if (store == null) {
            return;
        }

        VisibilityStatusConfig config = ConfigManager.get().visibilityStatus;
        store.setClanVisible(config.clanVisible);
        store.setPlayersVisible(config.playersVisible);
        store.setPetsVisibility(VisibilityStatusStore.PetsVisibility.fromName(config.petsVisibility));
    }

    private static void updateClanVisible(boolean visible) {
        if (store == null || store.isClanVisible() == visible) {
            return;
        }

        store.setClanVisible(visible);
        ConfigManager.get().visibilityStatus.clanVisible = visible;
        ConfigManager.save();
    }

    private static void updatePlayersVisible(boolean visible) {
        if (store == null || store.isPlayersVisible() == visible) {
            return;
        }

        store.setPlayersVisible(visible);
        ConfigManager.get().visibilityStatus.playersVisible = visible;
        ConfigManager.save();
    }

    private static void updatePetsVisibility(VisibilityStatusStore.PetsVisibility visibility) {
        if (store == null || store.getPetsVisibility() == visibility) {
            return;
        }

        store.setPetsVisibility(visibility);
        ConfigManager.get().visibilityStatus.petsVisibility = visibility.name();
        ConfigManager.save();
    }
}
