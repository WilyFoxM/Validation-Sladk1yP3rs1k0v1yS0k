package ru.wilyfox.client.hud.internal;

import ru.wilyfox.client.hud.indicators.CornerSnapIndicator;
import ru.wilyfox.client.hud.indicators.ScreenAnchor;
import ru.wilyfox.client.hud.widget.Widget;

public interface HudSnapLayoutHost {
    Widget getDraggedWidget();

    void setActiveScreenAnchor(ScreenAnchor anchor);

    void setActiveDraggedCornerIndicator(CornerSnapIndicator indicator);

    void setActiveTargetCornerIndicator(CornerSnapIndicator indicator);

    int getLastScreenWidth();

    int getLastScreenHeight();

    void setLastScreenWidth(int width);

    void setLastScreenHeight(int height);

    boolean isScreenAnchorOccupied(ScreenAnchor anchor, Widget ignoredWidget);

    boolean isCenterSideAnchor(ScreenAnchor anchor);
}
