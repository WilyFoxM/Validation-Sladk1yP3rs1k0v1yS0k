package ru.wilyfox.client.alchemy;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec3;
import ru.wilyfox.client.hud.widget.WidgetUtils;
import ru.wilyfox.utils.WorldToScreen;

public final class AlchemyIngredientOverlayRenderer {
    private static final long LIFETIME_MS = 1000L;
    private static final int MARKER_RGB = 0x85F6A0;

    private AlchemyIngredientOverlayRenderer() {
    }

    public static void render(GuiGraphics context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.gameRenderer == null) {
            return;
        }

        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();
        long now = System.currentTimeMillis();

        for (AlchemyIngredientSpot spot : AlchemyIngredientTracker.getInstance().getActiveSpots()) {
            Vec3 renderPos = spot.center().add(0.0, 0.15, 0.0);
            WorldToScreen.ScreenPoint point = WorldToScreen.project(renderPos);
            if (point == null) {
                continue;
            }

            double distance = cameraPos.distanceTo(renderPos);
            int size = getPulsingMarkerSize(spot, distance);
            int alpha = getMarkerAlpha(spot.particleCount(), now - spot.latestTimestamp());
            if (alpha <= 8) {
                continue;
            }

            int half = size / 2;
            int color = withAlpha(MARKER_RGB, alpha);
            WidgetUtils.drawCorners(context, point.x() - half, point.y() - half, size, size, color);
        }
    }

    private static int getMarkerAlpha(int particleCount, long ageMs) {
        float particleFactor = Math.max(0.0f, Math.min(1.0f, particleCount / 16.0f));
        float ageFactor = 1.0f - Math.max(0.0f, Math.min(1.0f, ageMs / (float) LIFETIME_MS));
        ageFactor = 0.35f + ageFactor * 0.65f;
        return Math.round(255.0f * particleFactor * ageFactor);
    }

    private static int withAlpha(int rgb, int alpha) {
        return ((alpha & 0xFF) << 24) | (rgb & 0xFFFFFF);
    }

    private static int getPulsingMarkerSize(AlchemyIngredientSpot spot, double distance) {
        float t = (float) Math.max(0.0, Math.min(1.0, (distance - 2.0) / 18.0));
        float baseSize = 22.0f - t * 10.0f;

        double time = System.currentTimeMillis() / 1000.0;
        double phase = (spot.center().x + spot.center().z) * 0.65;
        double pulse = Math.sin(time * 2.0 + phase);

        float amplitude = 1.6f - t * 0.4f;
        return Math.max(7, Math.round(baseSize + (float) pulse * amplitude));
    }
}
