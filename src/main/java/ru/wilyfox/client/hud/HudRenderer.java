package ru.wilyfox.client.hud;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import ru.wilyfox.client.hud.config.ConfigManager;
import ru.wilyfox.client.hud.config.WidgetLayoutConfig;
import ru.wilyfox.client.hud.fishing.FishingSpotOverlayRenderer;
import ru.wilyfox.client.alchemy.AlchemyIngredientOverlayRenderer;
import ru.wilyfox.client.hud.internal.HudEditorOverlayHost;
import ru.wilyfox.client.hud.internal.HudEditorOverlayRenderer;
import ru.wilyfox.client.hud.internal.HudGroupDragController;
import ru.wilyfox.client.hud.internal.HudSnapGraphNormalizer;
import ru.wilyfox.client.hud.internal.HudSnapLayoutEngine;
import ru.wilyfox.client.hud.internal.HudSnapLayoutHost;
import ru.wilyfox.client.hud.internal.HudScreenAnchorHelper;
import ru.wilyfox.client.hud.internal.HudSnapGroupResolver;
import ru.wilyfox.client.hud.indicators.ScreenAnchor;
import ru.wilyfox.client.hud.indicators.CornerSnapIndicator;
import ru.wilyfox.client.hud.layer.HudLayer;
import ru.wilyfox.client.hud.menu.HudSettingsPanel;
import ru.wilyfox.client.ping.PingMarkerOverlayRenderer;
import ru.wilyfox.client.hud.widget.AbstractWidget;
import ru.wilyfox.client.hud.widget.BossHudWidget;
import ru.wilyfox.client.hud.widget.Widget;
import ru.wilyfox.client.hud.widget.WidgetTheme;
import ru.wilyfox.utils.MouseUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HudRenderer {
    private final List<Widget> widgets = new ArrayList<>();
    private HudSettingsPanel settingsPanel;

    private ScreenAnchor activeScreenAnchor = null;
    private CornerSnapIndicator activeDraggedCornerIndicator = null;
    private CornerSnapIndicator activeTargetCornerIndicator = null;

    private final int SCREEN_SNAP_MARGIN = 8;
    private final int SCREEN_SNAP_DISTANCE = 12;
    private final int WIDGET_SNAP_DISTANCE = 6;
    private final int GROUP_GAP = 5;
    private final int WIDGET_FRAME_PADDING = 4;
    private final int HOTBAR_WIDTH = 182;
    private final int HOTBAR_HEIGHT = 22;
    private final int HOTBAR_ANCHOR_GAP = 6;
    private final int OFFHAND_SLOT_WIDTH = 29;

    private boolean editing = false;
    private boolean settingsOpen = false;
    private int lastScreenWidth = -1;
    private int lastScreenHeight = -1;

    private Widget draggedWidget = null;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;
    private boolean draggingWidgetGroup = false;
    private final List<AbstractWidget> draggedGroupWidgets = new ArrayList<>();
    private final Map<String, HudGroupDragController.GroupDragState> draggedGroupStates = new HashMap<>();
    private final HudSnapLayoutEngine snapLayoutEngine = new HudSnapLayoutEngine(
            widgets,
            SCREEN_SNAP_MARGIN,
            SCREEN_SNAP_DISTANCE,
            WIDGET_SNAP_DISTANCE,
            GROUP_GAP,
            HOTBAR_WIDTH,
            HOTBAR_HEIGHT,
            HOTBAR_ANCHOR_GAP,
            OFFHAND_SLOT_WIDTH
    );
    private final HudEditorOverlayHost overlayHost = new HudEditorOverlayHost() {
        @Override
        public ScreenAnchor getActiveScreenAnchor() {
            return activeScreenAnchor;
        }

        @Override
        public CornerSnapIndicator getActiveDraggedCornerIndicator() {
            return activeDraggedCornerIndicator;
        }

        @Override
        public CornerSnapIndicator getActiveTargetCornerIndicator() {
            return activeTargetCornerIndicator;
        }

        @Override
        public boolean isDraggingWidgetGroup() {
            return draggingWidgetGroup;
        }

        @Override
        public Widget getDraggedWidget() {
            return draggedWidget;
        }

        @Override
        public List<AbstractWidget> getDraggedGroupWidgets() {
            return draggedGroupWidgets;
        }

        @Override
        public List<AbstractWidget> getSnappedDescendants(AbstractWidget rootWidget) {
            return HudRenderer.this.getSnappedDescendants(rootWidget);
        }

        @Override
        public boolean isScreenAnchorOccupied(ScreenAnchor anchor, Widget ignoredWidget) {
            return HudRenderer.this.isScreenAnchorOccupied(anchor, ignoredWidget);
        }

        @Override
        public boolean isScreenAnchorCovered(int anchorX, int anchorY) {
            return HudRenderer.this.isScreenAnchorCovered(anchorX, anchorY);
        }

        @Override
        public int getHotbarLeftAnchorX(int screenWidth) {
            return HudRenderer.this.getHotbarLeftAnchorX(screenWidth);
        }

        @Override
        public int getHotbarRightAnchorX(int screenWidth) {
            return HudRenderer.this.getHotbarRightAnchorX(screenWidth);
        }

        @Override
        public int getHotbarAnchorY(int screenHeight) {
            return HudRenderer.this.getHotbarAnchorY(screenHeight);
        }

        @Override
        public boolean isCenterSideAnchor(ScreenAnchor anchor) {
            return HudRenderer.this.isCenterSideAnchor(anchor);
        }
    };
    private final HudSnapLayoutHost snapLayoutHost = new HudSnapLayoutHost() {
        @Override
        public Widget getDraggedWidget() {
            return draggedWidget;
        }

        @Override
        public void setActiveScreenAnchor(ScreenAnchor anchor) {
            activeScreenAnchor = anchor;
        }

        @Override
        public void setActiveDraggedCornerIndicator(CornerSnapIndicator indicator) {
            activeDraggedCornerIndicator = indicator;
        }

        @Override
        public void setActiveTargetCornerIndicator(CornerSnapIndicator indicator) {
            activeTargetCornerIndicator = indicator;
        }

        @Override
        public int getLastScreenWidth() {
            return lastScreenWidth;
        }

        @Override
        public int getLastScreenHeight() {
            return lastScreenHeight;
        }

        @Override
        public void setLastScreenWidth(int width) {
            lastScreenWidth = width;
        }

        @Override
        public void setLastScreenHeight(int height) {
            lastScreenHeight = height;
        }

        @Override
        public boolean isScreenAnchorOccupied(ScreenAnchor anchor, Widget ignoredWidget) {
            return HudRenderer.this.isScreenAnchorOccupied(anchor, ignoredWidget);
        }

        @Override
        public boolean isCenterSideAnchor(ScreenAnchor anchor) {
            return HudRenderer.this.isCenterSideAnchor(anchor);
        }
    };

    public HudRenderer(HudSettingsPanel s) {
        this.settingsPanel = s;
    }

    public boolean isEditing() {
        return editing;
    }

    public void setEditing(boolean editing) {
        this.editing = editing;

        if (!editing) {
            draggedWidget = null;
            draggingWidgetGroup = false;
            draggedGroupWidgets.clear();
            draggedGroupStates.clear();
            activeScreenAnchor = null;
            activeDraggedCornerIndicator = null;
            activeTargetCornerIndicator = null;
        }
    }

    public void toggleSettings() {
        this.settingsOpen = !this.settingsOpen;
    }

    public void setSettings(boolean settings) {
        this.settingsOpen = settings;
    }

    public boolean isSettingsOpen() {
        return this.settingsOpen;
    }

    public void registerWidget(Widget widget) {
        registerWidget(widget, null);
    }

    public void registerWidget(Widget widget, ScreenAnchor defaultAnchor) {
        widgets.add(widget);

        if (widget instanceof AbstractWidget abstractWidget) {
            abstractWidget.setConfigKey(widget.getClass().getSimpleName());

            WidgetLayoutConfig storedLayout = ConfigManager.getWidgetLayout(abstractWidget.getConfigKey());
            if (storedLayout != null) {
                if (storedLayout.scale != null) {
                    abstractWidget.setScale(storedLayout.scale);
                }
                if (storedLayout.anchor != null) {
                    abstractWidget.setScreenAnchor(storedLayout.anchor);
                }
                if (storedLayout.x != null) {
                    abstractWidget.setStartX(storedLayout.x);
                }
                if (storedLayout.y != null) {
                    abstractWidget.setStartY(storedLayout.y);
                }
                if (storedLayout.snapTarget != null && storedLayout.snapOwnCorner != null && storedLayout.snapTargetCorner != null) {
                    abstractWidget.setWidgetSnap(storedLayout.snapTarget, storedLayout.snapOwnCorner, storedLayout.snapTargetCorner);
                } else {
                    abstractWidget.clearWidgetSnap();
                }
            } else if (defaultAnchor != null) {
                abstractWidget.setScreenAnchor(defaultAnchor);
            }
        }
    }

    public List<Widget> getWidgets() {
        return List.copyOf(widgets);
    }

    public void render(GuiGraphics context, DeltaTracker tickCounter) {
        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        double mouseX = MouseUtils.getMouseX();
        double mouseY = MouseUtils.getMouseY();
        Widget hoveredWidget = editing ? findTopHoveredWidget(mouseX, mouseY) : null;

        handleScreenResize(screenWidth, screenHeight);
        updateAnchoredWidgets(screenWidth, screenHeight);
        updateSnappedWidgets();

        for (Widget widget : widgets) {
            if (!widget.isVisible()) {
                continue;
            }

            widget.render(context, tickCounter);
        }

        if (editing) {
            if (hoveredWidget != null) {
                renderHoveredWidgetOutline(context, hoveredWidget);

                if (Screen.hasAltDown()) {
                    renderGroupTooltip(context, hoveredWidget);
                } else if (Screen.hasControlDown()) {
                    renderScaleTooltip(context, hoveredWidget);
                }
            }

            renderScreenAnchors(context, screenWidth, screenHeight);
            renderWidgetSnapIndicators(context);
        }

        if (settingsOpen) {
            settingsPanel.render(context, MouseUtils.getMouseX(), MouseUtils.getMouseY());
        }

        if (ConfigManager.get().fishing.showFishingMarkers) {
            FishingSpotOverlayRenderer.render(context);
        }

        if (ConfigManager.get().render.showAlchemyIngredientMarkers) {
            AlchemyIngredientOverlayRenderer.render(context);
        }

        PingMarkerOverlayRenderer.render(context, tickCounter.getGameTimeDeltaPartialTick(true));
    }

    public void renderLayer(HudLayer layer, GuiGraphics context, DeltaTracker tickCounter) {
        for (Widget widget : widgets) {
            if (!widget.isVisible()) {
                continue;
            }

            if (widget.getLayer() == layer) {
                widget.render(context, tickCounter);
            }
        }
    }

    public void onMousePressed(double mouseX, double mouseY, int button) {
        if (settingsOpen && settingsPanel.mousePressed(mouseX, mouseY, button)) {
            return;
        }

        if (!editing || button != 0) {
            return;
        }


        Widget hovered = findTopHoveredWidget(mouseX, mouseY);
        if (hovered == null) {
            return;
        }

        draggedWidget = hovered;
        dragOffsetX = (int) (mouseX - hovered.getStartX());
        dragOffsetY = (int) (mouseY - hovered.getStartY());

        if (hovered instanceof AbstractWidget abstractWidget) {
            abstractWidget.setScreenAnchor(null);
            abstractWidget.clearWidgetSnap();

            if (Screen.hasAltDown()) {
                beginGroupDrag(abstractWidget);
            } else {
                detachSnappedDescendants(abstractWidget);
            }
        }
    }

    public void onMouseReleased(int button, int screenWidth, int screenHeight, double mouseX, double mouseY) {
        if (button != 0) {
            return;
        }

        if (settingsOpen && settingsPanel.mouseReleased(mouseX, mouseY, button)) {
            return;
        }

        Widget releasedWidget = draggedWidget;
        draggedWidget = null;
        if (draggingWidgetGroup) {
            finishGroupDrag();
        }
        activeScreenAnchor = null;
        activeDraggedCornerIndicator = null;
        activeTargetCornerIndicator = null;

        if (editing && releasedWidget instanceof AbstractWidget) {
            normalizeWidgetSnapParents();
            saveAllWidgetLayouts();
        }
    }

    public void onMouseDragged(double mouseX, double mouseY, int screenWidth, int screenHeight, int button) {
        if (settingsOpen && settingsPanel.mouseDragged(mouseX, mouseY, button)) {
            return;
        }

        if (!editing || draggedWidget == null) {
            return;
        }

        activeScreenAnchor = null;
        activeDraggedCornerIndicator = null;
        activeTargetCornerIndicator = null;

        int newX = (int) mouseX - dragOffsetX;
        int newY = (int) mouseY - dragOffsetY;

        newX = clamp(newX, 0, screenWidth - draggedWidget.getWidth());
        newY = clamp(newY, 0, screenHeight - draggedWidget.getHeight());

        if (draggedWidget instanceof AbstractWidget abstractWidget) {
            abstractWidget.setScreenAnchor(null);
        }

        draggedWidget.setStartX(newX);
        draggedWidget.setStartY(newY);
        if (draggingWidgetGroup && draggedWidget instanceof AbstractWidget abstractWidget) {
            updateDraggedGroupPositions(abstractWidget);
        }

        boolean snappedToAnchor = applyScreenAnchorSnapping(draggedWidget, screenWidth, screenHeight, Screen.hasShiftDown());
        if (!snappedToAnchor) {
            applyWidgetSnapping(screenWidth, screenHeight);
        } else if (draggedWidget instanceof AbstractWidget abstractWidget) {
            abstractWidget.clearWidgetSnap();
        }

        resolveWidgetOverlap(screenWidth, screenHeight);

        if (draggingWidgetGroup && draggedWidget instanceof AbstractWidget abstractWidget) {
            updateDraggedGroupPositions(abstractWidget);
        }
    }

    public boolean onMouseScrolled(double mouseX, double mouseY, double scrollY, boolean ctrlHeld, boolean shiftHeld) {
        if (settingsOpen && settingsPanel.mouseScrolled(mouseX, mouseY, scrollY)) {
            return true;
        }

        if (!editing || !ctrlHeld) {
            return false;
        }

        Widget hovered = findTopHoveredWidget(mouseX, mouseY);
        if (hovered == null) {
            return false;
        }

        if (hovered instanceof AbstractWidget scalableWidget) {
            float step = shiftHeld ? 0.02f : 0.10f;
            scalableWidget.adjustScale(scrollY > 0 ? step : -step);
            if (scalableWidget.getScreenAnchor() != null) {
                int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
                int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
                applyStoredScreenAnchor(scalableWidget, scalableWidget.getScreenAnchor(), screenWidth, screenHeight);
            }
            ConfigManager.saveWidgetLayout(scalableWidget);
            return true;
        }

        return false;
    }

    public boolean onKeyPressed(int keyCode, int scanCode, int modifiers) {
        return settingsOpen && settingsPanel.keyPressed(keyCode, scanCode, modifiers);
    }

    public boolean onCharTyped(char codePoint, int modifiers) {
        return settingsOpen && settingsPanel.charTyped(codePoint, modifiers);
    }

    public boolean handleChatClick(double mouseX, double mouseY, int button) {
        if (button != 0 || editing || settingsOpen) {
            return false;
        }

        Widget hovered = findTopHoveredWidget(mouseX, mouseY);
        if (hovered instanceof BossHudWidget bossHudWidget) {
            return bossHudWidget.handleChatClick(mouseX, mouseY);
        }

        return false;
    }

    private Widget findTopHoveredWidget(double mouseX, double mouseY) {
        for (int i = widgets.size() - 1; i >= 0; i--) {
            Widget widget = widgets.get(i);

            if (!widget.isVisible()) {
                continue;
            }

            if (widget.isHovered(mouseX, mouseY)) {
                return widget;
            }
        }

        return null;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private void handleScreenResize(int screenWidth, int screenHeight) {
        snapLayoutEngine.handleScreenResize(snapLayoutHost, screenWidth, screenHeight);
    }

    private void updateAnchoredWidgets(int screenWidth, int screenHeight) {
        snapLayoutEngine.updateAnchoredWidgets(snapLayoutHost, screenWidth, screenHeight);
    }

    private void updateSnappedWidgets() {
        snapLayoutEngine.updateSnappedWidgets(snapLayoutHost);
    }

    private boolean applyScreenAnchorSnapping(Widget widget, int screenWidth, int screenHeight, boolean shiftHeld) {
        return snapLayoutEngine.applyScreenAnchorSnapping(snapLayoutHost, widget, screenWidth, screenHeight, shiftHeld);
    }

    private void applyStoredScreenAnchor(Widget widget, ScreenAnchor anchor, int screenWidth, int screenHeight) {
        snapLayoutEngine.applyStoredScreenAnchor(widget, anchor, screenWidth, screenHeight);
    }

    private void applyWidgetSnapping(int screenWidth, int screenHeight) {
        snapLayoutEngine.applyWidgetSnapping(snapLayoutHost, screenWidth, screenHeight);
    }

    private Map<String, AbstractWidget> getAbstractWidgetMap() {
        return snapLayoutEngine.getAbstractWidgetMap();
    }

    private AbstractWidget findAbstractWidget(String configKey) {
        return snapLayoutEngine.findAbstractWidget(configKey);
    }

    private void renderHoveredWidgetOutline(GuiGraphics context, Widget hoveredWidget) {
        HudEditorOverlayRenderer.renderHoveredWidgetOutline(overlayHost, context, hoveredWidget);
    }

    private void resolveWidgetOverlap(int screenWidth, int screenHeight) {
        snapLayoutEngine.resolveWidgetOverlap(snapLayoutHost, draggingWidgetGroup, screenWidth, screenHeight);
    }

    private void saveAllWidgetLayouts() {
        for (Widget widget : widgets) {
            if (widget instanceof AbstractWidget abstractWidget) {
                ConfigManager.saveWidgetLayout(abstractWidget);
            }
        }
    }

    private void normalizeWidgetSnapParents() {
        HudSnapGraphNormalizer.normalize(getAbstractWidgetMap());
    }

    private void beginGroupDrag(AbstractWidget rootWidget) {
        List<AbstractWidget> descendants = getSnappedDescendants(rootWidget);
        draggingWidgetGroup = HudGroupDragController.beginGroupDrag(
                rootWidget,
                descendants,
                draggedGroupWidgets,
                draggedGroupStates
        );
    }

    private void finishGroupDrag() {
        draggingWidgetGroup = false;
        HudGroupDragController.finishGroupDrag(
                draggedWidget instanceof AbstractWidget abstractWidget ? abstractWidget : null,
                draggedGroupWidgets,
                draggedGroupStates
        );
    }

    private void updateDraggedGroupPositions(AbstractWidget rootWidget) {
        HudGroupDragController.updateDraggedGroupPositions(rootWidget, draggedGroupWidgets, draggedGroupStates);
    }

    private List<AbstractWidget> getSnappedDescendants(AbstractWidget rootWidget) {
        return HudSnapGroupResolver.getSnappedDescendants(rootWidget, widgets);
    }

    private List<AbstractWidget> getConnectedSnapGroup(AbstractWidget startWidget) {
        return HudSnapGroupResolver.getConnectedSnapGroup(startWidget, widgets);
    }

    private void detachSnappedDescendants(AbstractWidget rootWidget) {
        for (AbstractWidget widget : getSnappedDescendants(rootWidget)) {
            widget.clearWidgetSnap();
        }
    }

    private void renderScreenAnchors(GuiGraphics context, int screenWidth, int screenHeight) {
        HudEditorOverlayRenderer.renderScreenAnchors(
                overlayHost,
                context,
                screenWidth,
                screenHeight,
                SCREEN_SNAP_MARGIN
        );
    }

    private boolean isScreenAnchorOccupied(ScreenAnchor anchor, Widget ignoredWidget) {
        for (Widget widget : widgets) {
            if (widget == ignoredWidget || !widget.isVisible() || !(widget instanceof AbstractWidget abstractWidget)) {
                continue;
            }

            if (anchor == abstractWidget.getScreenAnchor()) {
                return true;
            }
        }

        return false;
    }

    private boolean isScreenAnchorCovered(int anchorX, int anchorY) {
        for (Widget widget : widgets) {
            if (!widget.isVisible()) {
                continue;
            }

            if (anchorX >= widget.getStartX()
                    && anchorX <= widget.getStartX() + widget.getWidth()
                    && anchorY >= widget.getStartY()
                    && anchorY <= widget.getStartY() + widget.getHeight()) {
                return true;
            }
        }

        return false;
    }

    private int getHotbarLeftAnchorX(int screenWidth) {
        return HudScreenAnchorHelper.getHotbarLeftAnchorX(screenWidth, HOTBAR_WIDTH, HOTBAR_ANCHOR_GAP, OFFHAND_SLOT_WIDTH);
    }

    private int getHotbarRightAnchorX(int screenWidth) {
        return HudScreenAnchorHelper.getHotbarRightAnchorX(screenWidth, HOTBAR_WIDTH, HOTBAR_ANCHOR_GAP, OFFHAND_SLOT_WIDTH);
    }

    private int getHotbarAnchorY(int screenHeight) {
        return HudScreenAnchorHelper.getHotbarAnchorY(screenHeight, HOTBAR_HEIGHT);
    }

    private boolean isCenterSideAnchor(ScreenAnchor anchor) {
        return anchor == ScreenAnchor.TOP_CENTER
                || anchor == ScreenAnchor.LEFT_CENTER
                || anchor == ScreenAnchor.RIGHT_CENTER;
    }

    private void renderWidgetSnapIndicators(GuiGraphics context) {
        HudEditorOverlayRenderer.renderWidgetSnapIndicators(overlayHost, context);
    }

    private void renderGroupTooltip(GuiGraphics context, Widget hovered) {
        if (!(hovered instanceof AbstractWidget abstractWidget)) {
            return;
        }

        int groupSize = draggingWidgetGroup && draggedGroupWidgets.contains(abstractWidget)
                ? draggedGroupWidgets.size()
                : getSnappedDescendants(abstractWidget).size() + 1;
        if (groupSize <= 1) {
            return;
        }

        float labelScale = 0.95f;
        String text = "Group: " + groupSize;

        int paddingX = Math.round(6 * labelScale);
        int baseHeight = Minecraft.getInstance().font.lineHeight + 4;
        int height = Math.round(baseHeight * labelScale);
        int textWidth = Math.round(Minecraft.getInstance().font.width(text) * labelScale);
        int width = textWidth + paddingX * 2;

        int x = hovered.getStartX() + (hovered.getWidth() - width) / 2;
        int y = hovered.getStartY() - height - 4;

        if (y < 2) {
            y = hovered.getStartY() + hovered.getHeight() + 4;
        }

        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        if (x + width > screenWidth - 2) {
            x = screenWidth - width - 2;
        }
        if (x < 2) {
            x = 2;
        }

        context.fill(x, y, x + width, y + height, WidgetTheme.TOOLTIP_BG);
        context.fill(x, y, x + width, y + 1, WidgetTheme.ACCENT_LINE);

        context.pose().pushPose();
        context.pose().translate(x + paddingX, y + (height - Minecraft.getInstance().font.lineHeight * labelScale) / 2.0f, 0);
        context.pose().scale(labelScale, labelScale, 1.0f);

        context.drawString(
                Minecraft.getInstance().font,
                text,
                0,
                0,
                WidgetTheme.TOOLTIP_TEXT
        );

        context.pose().popPose();
    }

    private void renderScaleTooltip(GuiGraphics context, Widget hovered) {
        if (!(hovered instanceof AbstractWidget scalableWidget)) {
            return;
        }

        float labelScale = 0.85f + (scalableWidget.getScale() - 1.0f) * 0.35f;
        labelScale = Math.max(0.7f, Math.min(1.1f, labelScale));
        String text = hovered.getDisplayName() + " x" + String.format("%.2f", scalableWidget.getScale());

        int paddingX = Math.round(6 * labelScale);
        int baseHeight = Minecraft.getInstance().font.lineHeight + 4;
        int height = Math.round(baseHeight * labelScale);
        int textWidth = Math.round(Minecraft.getInstance().font.width(text) * labelScale);
        int width = textWidth + paddingX * 2;

        int x = hovered.getStartX() + (hovered.getWidth() - width) / 2;
        int y = hovered.getStartY() - height - 4;

        if (y < 2) {
            y = hovered.getStartY() + hovered.getHeight() + 4;
        }

        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        if (x + width > screenWidth - 2) {
            x = screenWidth - width - 2;
        }
        if (x < 2) {
            x = 2;
        }

        context.fill(x, y, x + width, y + height, WidgetTheme.TOOLTIP_BG);
        context.fill(x, y, x + width, y + 1, WidgetTheme.ACCENT_LINE);

        context.pose().pushPose();
        context.pose().translate(x + paddingX, y + (height - Minecraft.getInstance().font.lineHeight * labelScale) / 2.0f, 0);
        context.pose().scale(labelScale, labelScale, 1.0f);

        context.drawString(
                Minecraft.getInstance().font,
                text,
                0,
                0,
                WidgetTheme.TOOLTIP_TEXT
        );

        context.pose().popPose();
    }
}
