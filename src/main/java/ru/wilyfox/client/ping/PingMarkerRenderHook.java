package ru.wilyfox.client.ping;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;

public final class PingMarkerRenderHook {
    private PingMarkerRenderHook() {
    }

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(PingMarkerRenderHook::onAfterEntities);
    }

    private static void onAfterEntities(WorldRenderContext context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || context.matrixStack() == null) {
            return;
        }

        if (PingMarkerManager.getActiveMarkers().isEmpty()) {
            return;
        }

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        PingMarkerRenderer.render(
                context.matrixStack(),
                bufferSource,
                context.tickCounter().getGameTimeDeltaPartialTick(true)
        );
        bufferSource.endBatch();
    }
}
