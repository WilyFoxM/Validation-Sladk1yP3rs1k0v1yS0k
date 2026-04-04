package ru.wilyfox.client.hud.internal;

import ru.wilyfox.client.hud.indicators.CornerSnapIndicator;
import ru.wilyfox.client.hud.indicators.ScreenAnchor;
import ru.wilyfox.client.hud.widget.AbstractWidget;
import ru.wilyfox.client.hud.widget.Widget;

import java.util.List;

public interface HudEditorOverlayHost {
    ScreenAnchor getActiveScreenAnchor();

    CornerSnapIndicator getActiveDraggedCornerIndicator();

    CornerSnapIndicator getActiveTargetCornerIndicator();

    boolean isDraggingWidgetGroup();

    Widget getDraggedWidget();

    List<AbstractWidget> getDraggedGroupWidgets();

    List<AbstractWidget> getSnappedDescendants(AbstractWidget rootWidget);

    boolean isScreenAnchorOccupied(ScreenAnchor anchor, Widget ignoredWidget);

    boolean isScreenAnchorCovered(int anchorX, int anchorY);

    int getHotbarLeftAnchorX(int screenWidth);

    int getHotbarRightAnchorX(int screenWidth);

    int getHotbarAnchorY(int screenHeight);

    boolean isCenterSideAnchor(ScreenAnchor anchor);
}
