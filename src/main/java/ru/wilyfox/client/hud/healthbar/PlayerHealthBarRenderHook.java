package ru.wilyfox.client.hud.healthbar;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;

public final class PlayerHealthBarRenderHook {
    private PlayerHealthBarRenderHook() {
    }

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(PlayerHealthBarRenderHook::onAfterEntities);
    }

    private static void onAfterEntities(WorldRenderContext context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }

        if (context.matrixStack() == null) {
            return;
        }

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        PlayerHealthBarRenderer.render(
                context.matrixStack(),
                bufferSource,
                context.tickCounter().getGameTimeDeltaPartialTick(true)
        );

        bufferSource.endBatch();
    }
}
