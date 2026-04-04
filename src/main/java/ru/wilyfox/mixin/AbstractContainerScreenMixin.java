package ru.wilyfox.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.wilyfox.client.recipe.PotionRecipeTracker;
import ru.wilyfox.client.recipe.CraftRecipeTracker;
import ru.wilyfox.client.boss.BossMenuIconCollector;
import ru.wilyfox.client.rune.PetExperienceOverlay;
import ru.wilyfox.client.rune.RuneSetEffectOverlay;
import ru.wilyfox.client.rune.RuneSetSwitcher;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {
    @Shadow
    protected int leftPos;

    @Shadow
    protected int topPos;

    @Shadow
    protected AbstractContainerMenu menu;

    @Shadow
    protected Slot hoveredSlot;

    @Inject(method = "mouseClicked", at = @At("HEAD"))
    private void froghelper$inspectRecipes(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (button != 1) {
            return;
        }

        if (hoveredSlot == null || !hoveredSlot.hasItem()) {
            PotionRecipeTracker.getInstance().clear();
            CraftRecipeTracker.getInstance().clear();
            return;
        }

        ItemStack stack = hoveredSlot.getItem();
        PotionRecipeTracker.getInstance().inspect(stack, Minecraft.getInstance().player);
        CraftRecipeTracker.getInstance().inspect(stack, Minecraft.getInstance().player);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void froghelper$handleRuneSetSwitch(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        Screen screen = (Screen) (Object) this;
        if (RuneSetSwitcher.handleScreenKeyPressed(screen.getTitle(), menu, keyCode, scanCode)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void froghelper$renderRuneSetEffect(GuiGraphics context, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        Screen screen = (Screen) (Object) this;
        BossMenuIconCollector.inspect(screen.getTitle(), menu);

        if (RuneSetEffectOverlay.isRuneBagScreen(screen.getTitle())) {
            RuneSetEffectOverlay.updateCache(menu);
            return;
        }

        if (!RuneSetEffectOverlay.isPlayerInventoryScreen(screen)) {
            return;
        }

        RuneSetEffectOverlay.OverlayData data = RuneSetEffectOverlay.collectFromInventory(menu);
        if (data == null) {
            data = RuneSetEffectOverlay.getCached();
        }
        if (data == null) {
            return;
        }

        int panelX = leftPos - 8;
        int widestLine = Minecraft.getInstance().font.width(data.title());
        for (String line : data.lines()) {
            widestLine = Math.max(widestLine, Minecraft.getInstance().font.width(line));
        }

        panelX -= widestLine + 12;
        int panelY = topPos + 8;

        RuneSetEffectOverlay.render(context, panelX, panelY, data);

        PetExperienceOverlay.OverlayData petExpData = PetExperienceOverlay.collect(menu);
        if (petExpData == null) {
            return;
        }

        int rightPanelX = leftPos + 176 + 8;
        int rightPanelY = topPos + 8;
        PetExperienceOverlay.render(context, rightPanelX, rightPanelY, petExpData);
    }
}
