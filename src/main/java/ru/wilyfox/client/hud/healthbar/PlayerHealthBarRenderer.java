package ru.wilyfox.client.hud.healthbar;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import ru.wilyfox.client.hud.config.ConfigManager;
import ru.wilyfox.client.hud.widget.WidgetTheme;

public final class PlayerHealthBarRenderer {
    private static final float WORLD_SCALE = 0.025f;
    private static final int BAR_WIDTH = 44;
    private static final int BAR_HEIGHT = 5;
    private static final int BG_PADDING = 2;
    private static final int ACCENT_HEIGHT = 1;
    private static final float PANEL_Z = 0.0f;
    private static final float ACCENT_Z = 0.001f;
    private static final float FILL_Z = 0.002f;
    private static final double BAR_Y_OFFSET = 0.85;
    private static final double FADE_START_DISTANCE = 8.0;
    private static final double FADE_END_DISTANCE = 20.0;
    private static final float MIN_DISTANCE_ALPHA = 0.12f;
    private PlayerHealthBarRenderer() {
    }

    public static void render(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.gameRenderer == null) {
            return;
        }

        if (!ConfigManager.get().playerHealthBars.active) {
            return;
        }

        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
        Camera camera = dispatcher.camera;
        if (camera == null) {
            return;
        }

        Vec3 cameraPos = camera.getPosition();

        for (Player target : mc.level.players()) {
            if (target == mc.player) {
                continue;
            }

            if (!target.isAlive() || target.isRemoved()) {
                continue;
            }

            if (target.isInvisible()) {
                continue;
            }

            if (!isVisibleToCamera(target)) {
                continue;
            }

            double x = Mth.lerp(partialTick, target.xOld, target.getX());
            double y = Mth.lerp(partialTick, target.yOld, target.getY()) + target.getBbHeight() + BAR_Y_OFFSET;
            double z = Mth.lerp(partialTick, target.zOld, target.getZ());

            double distance = cameraPos.distanceTo(new Vec3(x, y, z));
            float distanceAlpha = ConfigManager.get().playerHealthBars.distanceFade
                    ? getDistanceAlpha(distance)
                    : 1.0f;
            if (distanceAlpha <= 0.01f) {
                continue;
            }

            poseStack.pushPose();
            poseStack.translate(x - cameraPos.x, y - cameraPos.y, z - cameraPos.z);
            poseStack.mulPose(dispatcher.cameraOrientation());
            poseStack.scale(-WORLD_SCALE, -WORLD_SCALE, WORLD_SCALE);

            renderHealthBar(poseStack, bufferSource, target, distanceAlpha);

            poseStack.popPose();
        }
    }

    private static void renderHealthBar(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, Player target, float distanceAlpha) {
        float health = target.getHealth();
        float maxHealth = target.getMaxHealth();
        float progress = maxHealth <= 0.0f ? 0.0f : Mth.clamp(health / maxHealth, 0.0f, 1.0f);
        boolean lowHealth = progress < 0.5f;
        float opacityMultiplier = ConfigManager.get().playerHealthBars.opacityPercent / 100.0f;
        int verticalOffset = ConfigManager.get().playerHealthBars.verticalOffset;

        int x1 = -BAR_WIDTH / 2;
        int y1 = verticalOffset;
        int x2 = x1 + BAR_WIDTH;
        int y2 = y1 + BAR_HEIGHT;
        int fillWidth = Math.round(BAR_WIDTH * progress);
        int fillStartX = x2 - fillWidth;

        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.debugQuads());

        float finalAlpha = distanceAlpha * opacityMultiplier;
        int panelBaseColor = lowHealth
                ? WidgetTheme.withAlpha(WidgetTheme.PANEL_BG, 0x9E)
                : WidgetTheme.withAlpha(WidgetTheme.PANEL_BG_SOFT, 0x82);
        int panelColor = applyAlpha(panelBaseColor, finalAlpha);
        int accentColor = applyAlpha(getAccentColor(progress), finalAlpha);
        int fillColor = applyAlpha(getFillColor(progress), finalAlpha);

        fillQuad(vertexConsumer, matrix, x1 - BG_PADDING, y1 - BG_PADDING, x2 + BG_PADDING, y2 + BG_PADDING, PANEL_Z, panelColor);
        fillQuad(vertexConsumer, matrix, x1 - BG_PADDING, y1 - BG_PADDING, x2 + BG_PADDING, y1 - BG_PADDING + ACCENT_HEIGHT, ACCENT_Z, accentColor);

        if (fillWidth > 0) {
            fillQuad(vertexConsumer, matrix, fillStartX, y1, x2, y2, FILL_Z, fillColor);
        }
    }

    private static void fillQuad(VertexConsumer vertexConsumer, Matrix4f matrix, int x1, int y1, int x2, int y2, float z, int argb) {
        int a = (argb >> 24) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;

        vertexConsumer.addVertex(matrix, x1, y2, z).setColor(r, g, b, a);
        vertexConsumer.addVertex(matrix, x2, y2, z).setColor(r, g, b, a);
        vertexConsumer.addVertex(matrix, x2, y1, z).setColor(r, g, b, a);
        vertexConsumer.addVertex(matrix, x1, y1, z).setColor(r, g, b, a);
    }

    private static int applyAlpha(int argb, float alphaMultiplier) {
        int alpha = (argb >>> 24) & 0xFF;
        int scaledAlpha = Mth.clamp(Math.round(alpha * alphaMultiplier), 0, 255);
        return (scaledAlpha << 24) | (argb & 0x00FFFFFF);
    }

    private static int getFillColor(float progress) {
        float lowHealthBlend = Mth.clamp((0.50f - progress) / 0.35f, 0.0f, 1.0f);
        return lerpArgb(
                WidgetTheme.withAlpha(WidgetTheme.BAR_FILL, 0xB8),
                WidgetTheme.withAlpha(WidgetTheme.STATUS_ERROR, 0xD8),
                lowHealthBlend
        );
    }

    private static int getAccentColor(float progress) {
        float lowHealthBlend = Mth.clamp((0.40f - progress) / 0.20f, 0.0f, 1.0f);
        return lerpArgb(
                WidgetTheme.withAlpha(WidgetTheme.ACCENT_LINE, 0xAA),
                WidgetTheme.withAlpha(WidgetTheme.STATUS_ERROR, 0xF0),
                lowHealthBlend
        );
    }

    private static int lerpArgb(int fromArgb, int toArgb, float t) {
        int fromA = (fromArgb >>> 24) & 0xFF;
        int fromR = (fromArgb >> 16) & 0xFF;
        int fromG = (fromArgb >> 8) & 0xFF;
        int fromB = fromArgb & 0xFF;

        int toA = (toArgb >>> 24) & 0xFF;
        int toR = (toArgb >> 16) & 0xFF;
        int toG = (toArgb >> 8) & 0xFF;
        int toB = toArgb & 0xFF;

        int a = Mth.lerpInt(t, fromA, toA);
        int r = Mth.lerpInt(t, fromR, toR);
        int g = Mth.lerpInt(t, fromG, toG);
        int b = Mth.lerpInt(t, fromB, toB);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static float getDistanceAlpha(double distance) {
        if (distance <= FADE_START_DISTANCE) {
            return 1.0f;
        }

        if (distance >= FADE_END_DISTANCE) {
            return MIN_DISTANCE_ALPHA;
        }

        float progress = (float) ((distance - FADE_START_DISTANCE) / (FADE_END_DISTANCE - FADE_START_DISTANCE));
        return 1.0f - (1.0f - MIN_DISTANCE_ALPHA) * progress;
    }

    public static boolean isVisibleToCamera(Player target) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.gameRenderer == null || mc.player == null) {
            return false;
        }

        Vec3 from = mc.gameRenderer.getMainCamera().getPosition();
        Vec3 to = target.position().add(0.0, target.getBbHeight() * 0.85, 0.0);

        ClipContext context = new ClipContext(
                from,
                to,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                mc.player
        );

        BlockHitResult hit = mc.level.clip(context);
        return hit.getType() == HitResult.Type.MISS;
    }
}
