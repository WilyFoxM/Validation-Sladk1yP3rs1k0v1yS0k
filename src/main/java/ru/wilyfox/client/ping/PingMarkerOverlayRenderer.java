package ru.wilyfox.client.ping;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import ru.wilyfox.client.hud.config.ConfigManager;
import ru.wilyfox.utils.WorldToScreen;

public final class PingMarkerOverlayRenderer {
    private static final double Y_OFFSET = 1.15D;
    private static final double FADE_START_DISTANCE = 10.0D;
    private static final double FADE_END_DISTANCE = 64.0D;
    private static final float MIN_ALPHA = 0.20f;

    private PingMarkerOverlayRenderer() {
    }

    public static void render(GuiGraphics context) {
        if (PingMarkerRenderer.isEnabled()) {
            return;
        }

        if (!ConfigManager.get().wayPoints.overlayRender) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.gameRenderer == null) {
            return;
        }

        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();

        for (PingMarker marker : PingMarkerManager.getActiveMarkers()) {
            Vec3 renderPos = marker.position().add(0.0D, Y_OFFSET, 0.0D);
            WorldToScreen.ScreenPoint point = WorldToScreen.project(renderPos);
            if (point == null) {
                continue;
            }

            double distance = cameraPos.distanceTo(marker.position());
            float alpha = getDistanceAlpha(distance);
            if (alpha <= 0.01f) {
                continue;
            }

            String line1 = PingMarkerPresentation.buildPrimaryLine(marker.payload());
            String line2 = PingMarkerPresentation.buildSecondaryLine(distance);
            boolean hasSecondaryLine = !line2.isBlank();

            int line1Width = mc.font.width(line1);
            int line2Width = hasSecondaryLine ? mc.font.width(line2) : 0;
            int width = Math.max(line1Width, line2Width);

            int x = point.x() - width / 2;
            int y = point.y() - (hasSecondaryLine ? 18 : 13);
            int height = hasSecondaryLine ? 18 : 9;
            int backgroundColor = applyAlpha(PingMarkerPresentation.getBackgroundColor(marker.payload()), alpha);
            int primaryColor = applyAlpha(PingMarkerPresentation.PRIMARY_TEXT_COLOR, alpha);
            int accentColor = applyAlpha(PingMarkerPresentation.getAccentColor(marker.payload()), alpha);

            context.fill(x - 3, y - 2, x + width + 3, y + height, backgroundColor);
            context.fill(x - 3, y - 2, x + width + 3, y, accentColor);
            context.drawString(mc.font, line1, x, y, primaryColor);
            if (hasSecondaryLine) {
                int secondaryColor = applyAlpha(PingMarkerPresentation.SECONDARY_TEXT_COLOR, alpha);
                context.drawString(mc.font, line2, point.x() - line2Width / 2, y + 9, secondaryColor);
            }
        }
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

    private static int applyAlpha(int argb, float alphaMultiplier) {
        int alpha = (argb >>> 24) & 0xFF;
        int scaledAlpha = Mth.clamp(Math.round(alpha * alphaMultiplier), 0, 255);
        return (scaledAlpha << 24) | (argb & 0x00FFFFFF);
    }
}
