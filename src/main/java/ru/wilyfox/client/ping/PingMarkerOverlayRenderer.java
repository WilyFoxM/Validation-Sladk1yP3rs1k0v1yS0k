package ru.wilyfox.client.ping;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import ru.wilyfox.client.hud.config.ConfigManager;
import ru.wilyfox.client.profiler.ModProfiler;
import ru.wilyfox.utils.WorldToScreen;

public final class PingMarkerOverlayRenderer {
    private static final double Y_OFFSET = 1.15D;
    private static final double FADE_START_DISTANCE = 10.0D;
    private static final double FADE_END_DISTANCE = 64.0D;
    private static final float MIN_ALPHA = 0.20f;
    private static final int EDGE_MARGIN = 18;
    private static final int EDGE_ARROW_SIZE = 6;
    private static final int EDGE_PANEL_SIZE = 14;

    private PingMarkerOverlayRenderer() {
    }

    public static void render(GuiGraphics context, float partialTick) {
        try (ModProfiler.Scope ignored = ModProfiler.getInstance().scope("hud/PingMarkerOverlayRenderer/frame")) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.gameRenderer == null) {
            ModProfiler.getInstance().incrementCounter("hud/PingMarkerOverlayRenderer/skippedNoWorld");
            return;
        }

        boolean overlayLabelsEnabled = ConfigManager.get().wayPoints.overlayRender;
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int markers = 0;
        int projected = 0;
        int renderedOnscreen = 0;
        int renderedOffscreen = 0;
        int skippedNoPosition = 0;
        int skippedNoProjection = 0;
        int skippedAlpha = 0;
        int skippedOverlayDisabled = 0;
        try (ModProfiler.Scope iterateScope = ModProfiler.getInstance().scope("hud/PingMarkerOverlayRenderer/iterateMarkers")) {
            for (PingMarker marker : PingMarkerManager.getActiveMarkers()) {
                markers++;
                Vec3 markerPosition;
                try (ModProfiler.Scope resolveScope = ModProfiler.getInstance().scope("hud/PingMarkerOverlayRenderer/resolvePosition")) {
                    markerPosition = PingMarkerManager.getRenderPosition(marker, partialTick);
                }
                if (markerPosition == null) {
                    skippedNoPosition++;
                    continue;
                }

                Vec3 renderPos = markerPosition.add(0.0D, Y_OFFSET, 0.0D);
                WorldToScreen.Projection projection;
                try (ModProfiler.Scope projectionScope = ModProfiler.getInstance().scope("hud/PingMarkerOverlayRenderer/project")) {
                    projection = WorldToScreen.projectDetailed(renderPos);
                }
                if (projection == null) {
                    skippedNoProjection++;
                    continue;
                }
                projected++;

                double distance = cameraPos.distanceTo(markerPosition);
                float alpha = getDistanceAlpha(distance);
                if (alpha <= 0.01f) {
                    skippedAlpha++;
                    continue;
                }

                if (!projection.onScreen()) {
                    try (ModProfiler.Scope offscreenScope = ModProfiler.getInstance().scope("hud/PingMarkerOverlayRenderer/offscreenIndicator")) {
                        renderOffscreenIndicator(context, mc, marker, distance, alpha, projection, screenWidth, screenHeight);
                    }
                    renderedOffscreen++;
                    continue;
                }

                if (!overlayLabelsEnabled || PingMarkerRenderer.isEnabled()) {
                    skippedOverlayDisabled++;
                    continue;
                }

                WorldToScreen.ScreenPoint point = new WorldToScreen.ScreenPoint(
                        Math.round(projection.screenX()),
                        Math.round(projection.screenY())
                );

                String line1;
                String line2;
                try (ModProfiler.Scope textScope = ModProfiler.getInstance().scope("hud/PingMarkerOverlayRenderer/buildText")) {
                    line1 = PingMarkerPresentation.buildPrimaryLine(marker.payload());
                    line2 = PingMarkerPresentation.buildSecondaryLine(distance);
                }
                boolean hasSecondaryLine = !line2.isBlank();

                int line1Width;
                int line2Width;
                try (ModProfiler.Scope measureScope = ModProfiler.getInstance().scope("hud/PingMarkerOverlayRenderer/measureText")) {
                    line1Width = mc.font.width(line1);
                    line2Width = hasSecondaryLine ? mc.font.width(line2) : 0;
                }
                int width = Math.max(line1Width, line2Width);

                int x = point.x() - width / 2;
                int y = point.y() - (hasSecondaryLine ? 18 : 13);
                int height = hasSecondaryLine ? 18 : 9;
                int backgroundColor = applyAlpha(PingMarkerPresentation.getBackgroundColor(marker.payload()), alpha);
                int primaryColor = applyAlpha(PingMarkerPresentation.getPrimaryTextColor(), alpha);
                int accentColor = applyAlpha(PingMarkerPresentation.getAccentColor(marker.payload()), alpha);

                try (ModProfiler.Scope drawScope = ModProfiler.getInstance().scope("hud/PingMarkerOverlayRenderer/drawOnscreen")) {
                    context.fill(x - 3, y - 2, x + width + 3, y + height, backgroundColor);
                    context.fill(x - 3, y - 2, x + width + 3, y, accentColor);
                    context.drawString(mc.font, line1, x, y, primaryColor);
                    if (hasSecondaryLine) {
                        int secondaryColor = applyAlpha(PingMarkerPresentation.getSecondaryTextColor(), alpha);
                        context.drawString(mc.font, line2, point.x() - line2Width / 2, y + 9, secondaryColor);
                    }
                }
                renderedOnscreen++;
            }
        }
        ModProfiler.getInstance().incrementCounter("hud/PingMarkerOverlayRenderer/markers", markers);
        ModProfiler.getInstance().incrementCounter("hud/PingMarkerOverlayRenderer/projected", projected);
        ModProfiler.getInstance().incrementCounter("hud/PingMarkerOverlayRenderer/renderedOnscreen", renderedOnscreen);
        ModProfiler.getInstance().incrementCounter("hud/PingMarkerOverlayRenderer/renderedOffscreen", renderedOffscreen);
        ModProfiler.getInstance().incrementCounter("hud/PingMarkerOverlayRenderer/skippedNoPosition", skippedNoPosition);
        ModProfiler.getInstance().incrementCounter("hud/PingMarkerOverlayRenderer/skippedNoProjection", skippedNoProjection);
        ModProfiler.getInstance().incrementCounter("hud/PingMarkerOverlayRenderer/skippedAlpha", skippedAlpha);
        ModProfiler.getInstance().incrementCounter("hud/PingMarkerOverlayRenderer/skippedOverlayDisabled", skippedOverlayDisabled);
        }
    }

    private static void renderOffscreenIndicator(
            GuiGraphics context,
            Minecraft mc,
            PingMarker marker,
            double distance,
            float alpha,
            WorldToScreen.Projection projection,
            int screenWidth,
            int screenHeight
    ) {
        float centerX = screenWidth / 2.0f;
        float centerY = screenHeight / 2.0f;
        float dx = projection.indicatorX();
        float dy = projection.indicatorY();

        if (Math.abs(dx) < 0.001f && Math.abs(dy) < 0.001f) {
            return;
        }

        float availableHalfWidth = Math.max(1.0f, centerX - EDGE_MARGIN);
        float availableHalfHeight = Math.max(1.0f, centerY - EDGE_MARGIN);
        float scale = Math.min(
                availableHalfWidth / Math.max(Math.abs(dx), 1.0f),
                availableHalfHeight / Math.max(Math.abs(dy), 1.0f)
        );

        int edgeX = Math.round(centerX + dx * scale);
        int edgeY = Math.round(centerY + dy * scale);

        int backgroundColor = applyAlpha(PingMarkerPresentation.getBackgroundColor(marker.payload()), alpha);
        int accentColor = applyAlpha(PingMarkerPresentation.getAccentColor(marker.payload()), alpha);
        int textColor = applyAlpha(PingMarkerPresentation.getPrimaryTextColor(), alpha);
        int distanceColor = applyAlpha(PingMarkerPresentation.getSecondaryTextColor(), alpha);

        context.fill(
                edgeX - EDGE_PANEL_SIZE / 2,
                edgeY - EDGE_PANEL_SIZE / 2,
                edgeX + EDGE_PANEL_SIZE / 2,
                edgeY + EDGE_PANEL_SIZE / 2,
                backgroundColor
        );
        context.fill(
                edgeX - EDGE_PANEL_SIZE / 2,
                edgeY - EDGE_PANEL_SIZE / 2,
                edgeX + EDGE_PANEL_SIZE / 2,
                edgeY - EDGE_PANEL_SIZE / 2 + 1,
                accentColor
        );

        drawDirectionalArrow(context, edgeX, edgeY, dx, dy, textColor);

        String distanceText = PingMarkerPresentation.buildSecondaryLine(distance);
        if (!distanceText.isBlank()) {
            int textWidth = mc.font.width(distanceText);
            int textX = Mth.clamp(edgeX - textWidth / 2, 4, screenWidth - textWidth - 4);
            int textY = Mth.clamp(edgeY + EDGE_PANEL_SIZE / 2 + 2, 4, screenHeight - mc.font.lineHeight - 4);
            context.drawString(mc.font, distanceText, textX, textY, distanceColor);
        }
    }

    private static void drawDirectionalArrow(GuiGraphics context, int centerX, int centerY, float dx, float dy, int color) {
        if (Math.abs(dx) >= Math.abs(dy)) {
            if (dx >= 0.0f) {
                drawRightArrow(context, centerX, centerY, color);
            } else {
                drawLeftArrow(context, centerX, centerY, color);
            }
            return;
        }

        if (dy >= 0.0f) {
            drawDownArrow(context, centerX, centerY, color);
        } else {
            drawUpArrow(context, centerX, centerY, color);
        }
    }

    private static void drawRightArrow(GuiGraphics context, int centerX, int centerY, int color) {
        for (int offset = 0; offset < EDGE_ARROW_SIZE; offset++) {
            int x = centerX - EDGE_ARROW_SIZE / 2 + offset;
            context.fill(x, centerY - offset, x + 1, centerY + offset + 1, color);
        }
    }

    private static void drawLeftArrow(GuiGraphics context, int centerX, int centerY, int color) {
        for (int offset = 0; offset < EDGE_ARROW_SIZE; offset++) {
            int x = centerX + EDGE_ARROW_SIZE / 2 - offset;
            context.fill(x, centerY - offset, x + 1, centerY + offset + 1, color);
        }
    }

    private static void drawUpArrow(GuiGraphics context, int centerX, int centerY, int color) {
        for (int offset = 0; offset < EDGE_ARROW_SIZE; offset++) {
            int y = centerY + EDGE_ARROW_SIZE / 2 - offset;
            context.fill(centerX - offset, y, centerX + offset + 1, y + 1, color);
        }
    }

    private static void drawDownArrow(GuiGraphics context, int centerX, int centerY, int color) {
        for (int offset = 0; offset < EDGE_ARROW_SIZE; offset++) {
            int y = centerY - EDGE_ARROW_SIZE / 2 + offset;
            context.fill(centerX - offset, y, centerX + offset + 1, y + 1, color);
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
