package ru.wilyfox.client.ping;

import net.minecraft.client.Minecraft;
import ru.wilyfox.client.hud.config.ConfigManager;
import ru.wilyfox.client.hud.widget.WidgetTheme;

final class PingMarkerPresentation {
    static final int PRIMARY_TEXT_COLOR = WidgetTheme.TEXT_SOFT;
    static final int SECONDARY_TEXT_COLOR = WidgetTheme.TEXT_SECONDARY;
    static final int OWN_BLOCK_ACCENT_COLOR = WidgetTheme.ACCENT_LINE;
    static final int OTHER_BLOCK_ACCENT_COLOR = WidgetTheme.TEXT_PRIMARY;
    static final int OWN_ENTITY_ACCENT_COLOR = WidgetTheme.TEXT_ACCENT;
    static final int OTHER_ENTITY_ACCENT_COLOR = WidgetTheme.TITLE;
    static final int BLOCK_BACKGROUND_COLOR = WidgetTheme.PANEL_BG;
    static final int ENTITY_BACKGROUND_COLOR = WidgetTheme.PANEL_BG_SOFT;

    private PingMarkerPresentation() {
    }

    static String buildPrimaryLine(PingPayload payload) {
        if (payload.label() != null && !payload.label().isBlank()) {
            return payload.label();
        }

        boolean showAuthor = ConfigManager.get().wayPoints.showAuthor;
        boolean showEntityName = ConfigManager.get().wayPoints.showEntityName;
        boolean showLocation = ConfigManager.get().wayPoints.showLocation;

        String author = showAuthor && payload.author() != null && !payload.author().isBlank()
                ? payload.author()
                : null;
        String entityName = showEntityName && payload.entityName() != null && !payload.entityName().isBlank()
                ? payload.entityName()
                : null;
        String location = showLocation && payload.location() != null && !payload.location().isBlank()
                ? payload.location()
                : null;

        if (entityName != null && author != null) {
            return entityName + " - " + author;
        }

        if (entityName != null && location != null) {
            return entityName + " - " + location;
        }

        if (author != null && location != null) {
            return author + " - " + location;
        }

        if (entityName != null) {
            return entityName;
        }

        if (author != null) {
            return author;
        }

        if (location != null) {
            return location;
        }

        return "Ping";
    }

    static String buildSecondaryLine(double distance) {
        if (!ConfigManager.get().wayPoints.showDistance) {
            return "";
        }

        return String.format("%.1fm", distance);
    }

    static int getAccentColor(PingPayload payload) {
        boolean entityMarker = PingPayload.TYPE_ENTITY.equalsIgnoreCase(payload.type());
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null
                && payload.author() != null
                && payload.author().equalsIgnoreCase(mc.player.getGameProfile().getName())) {
            return entityMarker ? OWN_ENTITY_ACCENT_COLOR : OWN_BLOCK_ACCENT_COLOR;
        }

        return entityMarker ? OTHER_ENTITY_ACCENT_COLOR : OTHER_BLOCK_ACCENT_COLOR;
    }

    static int getBackgroundColor(PingPayload payload) {
        return PingPayload.TYPE_ENTITY.equalsIgnoreCase(payload.type())
                ? ENTITY_BACKGROUND_COLOR
                : BLOCK_BACKGROUND_COLOR;
    }
}
