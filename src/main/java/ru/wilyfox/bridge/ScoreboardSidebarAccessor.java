package ru.wilyfox.bridge;

import net.minecraft.client.gui.GuiGraphics;

public interface ScoreboardSidebarAccessor {
    void froghelper$renderAt(GuiGraphics context, int x, int y);
    int froghelper$getRenderedWidth();
    int froghelper$getRenderedHeight();
    int froghelper$getDefaultX();
    int froghelper$getDefaultY();
}
