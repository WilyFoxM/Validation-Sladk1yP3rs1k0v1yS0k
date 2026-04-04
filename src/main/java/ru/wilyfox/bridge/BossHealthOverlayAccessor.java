package ru.wilyfox.bridge;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.LerpingBossEvent;

import java.util.List;

public interface BossHealthOverlayAccessor {
    void froghelper$renderAt(GuiGraphics context, int x, int y);
    int froghelper$getRenderedHeight();
    int froghelper$getRenderedWidth();
    List<LerpingBossEvent> froghelper$getEvents();
}
