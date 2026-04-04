package ru.wilyfox.client.hud.widget;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import ru.wilyfox.client.hud.layer.HudLayer;

public class PlainTextWidget extends AbstractWidget {
    private final String text;

    public PlainTextWidget(int x, int y, HudLayer layer, String text) {
        super(x, y, layer);
        this.text = text;
    }

    @Override
    public void render(GuiGraphics context, DeltaTracker tickCounter) {
        if (!isVisible()) {
            return;
        }

        context.pose().pushPose();
        context.pose().translate(startX, startY, 0);
        context.pose().scale(scale, scale, 1.0f);

        context.drawString(Minecraft.getInstance().font, text, 0, 0, WidgetTheme.TEXT_SOFT);

        context.pose().popPose();
    }

    @Override
    public int getWidth() {
        return Math.round(Minecraft.getInstance().font.width(text) * getScale());
    }

    @Override
    public int getHeight() {
        return Math.round(Minecraft.getInstance().font.lineHeight * getScale());
    }

    @Override
    public String getDisplayName() {
        return "Text";
    }
}
