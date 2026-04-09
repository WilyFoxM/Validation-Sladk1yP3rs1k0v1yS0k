package ru.wilyfox.client.ping;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import ru.wilyfox.client.profiler.ModProfiler;

public final class PingMarkerRenderHook {
    private PingMarkerRenderHook() {
    }

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(PingMarkerRenderHook::onAfterEntities);
    }

    private static void onAfterEntities(WorldRenderContext context) {
        try (ModProfiler.Scope ignored = ModProfiler.getInstance().scope("render/PingMarkerRenderHook")) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null || mc.player == null || context.matrixStack() == null) {
                ModProfiler.getInstance().incrementCounter("render/PingMarkerRenderHook/skippedNoContext");
                return;
            }

            if (PingMarkerManager.getActiveMarkers().isEmpty()) {
                ModProfiler.getInstance().incrementCounter("render/PingMarkerRenderHook/skippedNoMarkers");
                return;
            }

            MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
            try (ModProfiler.Scope renderScope = ModProfiler.getInstance().scope("render/PingMarkerRenderHook/renderMarkers")) {
                PingMarkerRenderer.render(
                        context.matrixStack(),
                        bufferSource,
                        context.tickCounter().getGameTimeDeltaPartialTick(true)
                );
            }
            try (ModProfiler.Scope batchScope = ModProfiler.getInstance().scope("render/PingMarkerRenderHook/endBatch")) {
                bufferSource.endBatch();
            }
        }
    }
}
