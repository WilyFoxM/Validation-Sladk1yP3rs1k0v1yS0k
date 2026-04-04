package ru.wilyfox.client.ping;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.Font;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class PingMarkerRenderer {
    private static final boolean WORLD_RENDER_ENABLED = true;
    private static final float WORLD_SCALE = 0.025f;
    private static final double Y_OFFSET = 1.15D;
    private static final double FADE_START_DISTANCE = 10.0D;
    private static final double FADE_END_DISTANCE = 64.0D;
    private static final float MIN_ALPHA = 0.20f;
    private static final int PANEL_HEIGHT = 23;
    private static final int SINGLE_LINE_PANEL_HEIGHT = 14;
    private static final int PANEL_PADDING_X = 4;
    private static final int PANEL_PADDING_Y = 3;
    private static final int ACCENT_HEIGHT = 2;
    private static final float PANEL_Z = 0.0f;
    private static final float ACCENT_Z = 0.001f;
    private static final int LIGHT_COLOR = 0x00F000F0;

    private PingMarkerRenderer() {
    }

    public static boolean isEnabled() {
        return WORLD_RENDER_ENABLED;
    }

    public static void render(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float partialTick) {
        if (!WORLD_RENDER_ENABLED) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }

        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
        Camera camera = dispatcher.camera;
        if (camera == null) {
            return;
        }

        Vec3 cameraPos = camera.getPosition();

        for (PingMarker marker : PingMarkerManager.getActiveMarkers()) {
            Vec3 position = marker.position();
            double distance = cameraPos.distanceTo(position);
            float alpha = getDistanceAlpha(distance);
            if (alpha <= 0.01f) {
                continue;
            }

            poseStack.pushPose();
            poseStack.translate(position.x - cameraPos.x, position.y - cameraPos.y + Y_OFFSET, position.z - cameraPos.z);
            poseStack.mulPose(dispatcher.cameraOrientation());
            poseStack.scale(WORLD_SCALE, -WORLD_SCALE, WORLD_SCALE);

            renderMarkerContents(poseStack, bufferSource, marker, distance, alpha);

            poseStack.popPose();
        }
    }

    private static void renderMarkerContents(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, PingMarker marker, double distance, float alpha) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        String primaryText = PingMarkerPresentation.buildPrimaryLine(marker.payload());
        String secondaryText = PingMarkerPresentation.buildSecondaryLine(distance);
        boolean hasSecondaryLine = !secondaryText.isBlank();
        int primaryWidth = font.width(primaryText);
        int secondaryWidth = hasSecondaryLine ? font.width(secondaryText) : 0;
        int panelWidth = Math.max(primaryWidth, secondaryWidth) + PANEL_PADDING_X * 2;
        int panelHeight = hasSecondaryLine ? PANEL_HEIGHT : SINGLE_LINE_PANEL_HEIGHT;
        int x1 = -panelWidth / 2;
        int y1 = -panelHeight / 2;
        int x2 = x1 + panelWidth;
        int y2 = y1 + panelHeight;

        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.debugQuads());

        int backgroundColor = applyAlpha(PingMarkerPresentation.getBackgroundColor(marker.payload()), alpha);
        int accentColor = applyAlpha(PingMarkerPresentation.getAccentColor(marker.payload()), alpha);

        fillQuad(vertexConsumer, matrix, x1, y1, x2, y2, PANEL_Z, backgroundColor);
        fillQuad(vertexConsumer, matrix, x1, y1, x2, y1 + ACCENT_HEIGHT, ACCENT_Z, accentColor);

        int primaryTextColor = applyAlpha(PingMarkerPresentation.PRIMARY_TEXT_COLOR, alpha);
        float primaryTextX = -primaryWidth / 2.0f;
        float primaryTextY = y1 + PANEL_PADDING_Y;
        Matrix4f textMatrix = poseStack.last().pose();
        Component primaryComponent = Component.literal(primaryText);
        int textBackground = (int) (Minecraft.getInstance().options.getBackgroundOpacity(0.25f) * 255.0f) << 24;

        font.drawInBatch(
                primaryComponent,
                primaryTextX,
                primaryTextY,
                primaryTextColor,
                false,
                textMatrix,
                bufferSource,
                Font.DisplayMode.SEE_THROUGH,
                textBackground,
                LIGHT_COLOR
        );
        font.drawInBatch(
                primaryComponent,
                primaryTextX,
                primaryTextY,
                0xFFFFFFFF,
                false,
                textMatrix,
                bufferSource,
                Font.DisplayMode.NORMAL,
                0,
                LightTexture.lightCoordsWithEmission(LIGHT_COLOR, 2)
        );
        if (hasSecondaryLine) {
            int secondaryTextColor = applyAlpha(PingMarkerPresentation.SECONDARY_TEXT_COLOR, alpha);
            float secondaryTextX = -secondaryWidth / 2.0f;
            float secondaryTextY = primaryTextY + font.lineHeight - 1.0f;
            Component secondaryComponent = Component.literal(secondaryText);

            font.drawInBatch(
                    secondaryComponent,
                    secondaryTextX,
                    secondaryTextY,
                    secondaryTextColor,
                    false,
                    textMatrix,
                    bufferSource,
                    Font.DisplayMode.SEE_THROUGH,
                    textBackground,
                    LIGHT_COLOR
            );
            font.drawInBatch(
                    secondaryComponent,
                    secondaryTextX,
                    secondaryTextY,
                    0xFFE4E4E4,
                    false,
                    textMatrix,
                    bufferSource,
                    Font.DisplayMode.NORMAL,
                    0,
                    LightTexture.lightCoordsWithEmission(LIGHT_COLOR, 2)
            );
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

    private static float getDistanceAlpha(double distance) {
        if (distance <= FADE_START_DISTANCE) {
            return 1.0f;
        }

        if (distance >= FADE_END_DISTANCE) {
            return MIN_ALPHA;
        }

        float progress = (float) ((distance - FADE_START_DISTANCE) / (FADE_END_DISTANCE - FADE_START_DISTANCE));
        return 1.0f - (1.0f - MIN_ALPHA) * progress;
    }

    private static int applyAlpha(int rgb, float alphaMultiplier) {
        int alpha = (rgb >>> 24) & 0xFF;
        int scaledAlpha = Mth.clamp(Math.round(alpha * alphaMultiplier), 0, 255);
        return (scaledAlpha << 24) | (rgb & 0x00FFFFFF);
    }
}
