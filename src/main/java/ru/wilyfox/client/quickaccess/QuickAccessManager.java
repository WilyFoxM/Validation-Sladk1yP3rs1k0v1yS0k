package ru.wilyfox.client.quickaccess;

import net.minecraft.client.Minecraft;
import ru.wilyfox.client.chat.ChatDispatchQueue;

public final class QuickAccessManager {
    private static final QuickAccessManager INSTANCE = new QuickAccessManager();

    private QuickAccessScreen activeScreen;
    private boolean heldOpen;

    private QuickAccessManager() {
    }

    public static QuickAccessManager getInstance() {
        return INSTANCE;
    }

    public void open(Minecraft client) {
        if (client == null || client.screen != null) {
            return;
        }

        QuickAccessScreen screen = new QuickAccessScreen();
        activeScreen = screen;
        heldOpen = true;
        client.setScreen(screen);
    }

    public void release(Minecraft client) {
        if (!heldOpen) {
            return;
        }

        heldOpen = false;
        if (!(client.screen instanceof QuickAccessScreen screen) || screen != activeScreen) {
            activeScreen = null;
            return;
        }

        QuickAccessItemConfig hovered = screen.getHoveredItem();
        client.setScreen(null);
        activeScreen = null;

        if (hovered == null) {
            return;
        }

        String resolved = QuickAccessCommandResolver.resolveCommand(hovered);
        if (resolved == null || resolved.isBlank()) {
            if (QuickAccessCommandResolver.requiresTarget(hovered)) {
                QuickAccessCommandResolver.showMissingTargetFeedback();
            }
            return;
        }

        ChatDispatchQueue.enqueueCommand(resolved, 0L);
    }

    public void forceClose(Minecraft client) {
        heldOpen = false;
        if (client.screen == activeScreen) {
            client.setScreen(null);
        }
        activeScreen = null;
    }
}
