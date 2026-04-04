package ru.wilyfox.client.target;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import ru.wilyfox.client.hud.healthbar.PlayerHealthBarRenderer;
import ru.wilyfox.utils.WorldToScreen;

public final class TargetHighlightRenderHook {
    private static final float RED = 1.0F;
    private static final float GREEN = 0.15F;
    private static final float BLUE = 0.15F;
    private static final float LINE_ALPHA = 1.0F;
    private static final double BOX_INFLATE = 0.08D;

    private TargetHighlightRenderHook() {
    }

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(TargetHighlightRenderHook::onAfterEntities);
    }

    private static void onAfterEntities(WorldRenderContext context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || context.matrixStack() == null) {
            return;
        }

        if (TargetListStore.getTargets().isEmpty()) {
            return;
        }

        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
        PoseStack poseStack = context.matrixStack();
        MultiBufferSource consumers = context.consumers();
        if (consumers == null) {
            return;
        }

        VertexConsumer lineConsumer = consumers.getBuffer(RenderType.lines());
        float partialTick = context.tickCounter().getGameTimeDeltaPartialTick(true);

        for (Player target : mc.level.players()) {
            if (target == mc.player) {
                continue;
            }

            if (!target.isAlive() || target.isRemoved() || target.isInvisible()) {
                continue;
            }

            if (!TargetListStore.isTarget(target.getGameProfile().getName())) {
                continue;
            }

            if (!PlayerHealthBarRenderer.isVisibleToCamera(target)) {
                continue;
            }

            Vec3 screenProbe = new Vec3(
                    Mth.lerp(partialTick, target.xOld, target.getX()),
                    Mth.lerp(partialTick, target.yOld, target.getY()) + target.getBbHeight() * 0.5D,
                    Mth.lerp(partialTick, target.zOld, target.getZ())
            );
            if (WorldToScreen.project(screenProbe) == null) {
                continue;
            }

            AABB box = target.getBoundingBox()
                    .inflate(BOX_INFLATE)
                    .move(-cameraPos.x, -cameraPos.y, -cameraPos.z);

            ShapeRenderer.renderLineBox(poseStack, lineConsumer, box, RED, GREEN, BLUE, LINE_ALPHA);
        }
    }
}
