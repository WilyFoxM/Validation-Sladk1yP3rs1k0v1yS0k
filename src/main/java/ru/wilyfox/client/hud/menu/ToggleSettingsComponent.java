package ru.wilyfox.client.hud.menu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import ru.wilyfox.client.hud.config.ConfigManager;
import ru.wilyfox.client.hud.widget.WidgetTheme;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ToggleSettingsComponent extends SettingsComponent {
    private final Supplier<Boolean> getter;
    private final Consumer<Boolean> setter;

    protected ToggleSettingsComponent(int x, int y, int width, int height, String label,
                                      Supplier<Boolean> g,
                                      Consumer<Boolean> s) {
        super(x, y, width, height, label);
        this.getter = g;
        this.setter = s;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();

        boolean hovered = isHovered(mouseX, mouseY);
        boolean value = getter.get();

        int rowBg = hovered ? WidgetTheme.PANEL_BG : WidgetTheme.PANEL_BG_SOFT;
        int textColor = hovered ? WidgetTheme.TITLE : WidgetTheme.TEXT_PRIMARY;

        context.fill(x, y, x + width, y + height, rowBg);

        int textY = y + (height - mc.font.lineHeight) / 2;
        context.drawString(mc.font, label, x + 8, textY, textColor);

        String stateText = value ? "ON" : "OFF";
        int pillWidth = 30;
        int pillHeight = 12;
        int pillX = x + width - 8 - pillWidth;
        int pillY = y + (height - pillHeight) / 2;

        int pillBg;
        int pillTextColor;

        if (value) {
            pillBg = hovered ? WidgetTheme.PANEL_BG : WidgetTheme.PANEL_BG_SOFT;
            pillTextColor = WidgetTheme.TITLE;
            context.fill(pillX, pillY, pillX + pillWidth, pillY + 1, WidgetTheme.ACCENT_LINE);
        } else {
            pillBg = hovered ? WidgetTheme.BAR_BG : WidgetTheme.PANEL_BG_SOFT;
            pillTextColor = WidgetTheme.TEXT_SECONDARY;
        }

        context.fill(pillX, pillY, pillX + pillWidth, pillY + pillHeight, pillBg);
        context.drawCenteredString(mc.font, stateText, pillX + pillWidth / 2, pillY + 2, pillTextColor);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || !isHovered(mouseX, mouseY)) {
            return false;
        }

        setter.accept(!getter.get());
        ConfigManager.save();
        return true;
    }
}
