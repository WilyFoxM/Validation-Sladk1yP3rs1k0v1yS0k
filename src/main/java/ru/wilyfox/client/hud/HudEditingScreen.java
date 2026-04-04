package ru.wilyfox.client.hud;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class HudEditingScreen extends Screen {
    private final HudRenderer hudRenderer;

    public HudEditingScreen(HudRenderer hudRenderer) {
        super(Component.empty());
        this.hudRenderer = hudRenderer;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float partialTick) {
        // Не вызываем super.render(), чтобы не было затемнения/ванильного фона
    }

    @Override
    public void removed() {
        super.removed();
        hudRenderer.setEditing(false);
        hudRenderer.setSettings(false);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return hudRenderer.onMouseScrolled(mouseX, mouseY, scrollY, hasControlDown(), hasShiftDown());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return hudRenderer.onKeyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return hudRenderer.onCharTyped(codePoint, modifiers) || super.charTyped(codePoint, modifiers);
    }
}


