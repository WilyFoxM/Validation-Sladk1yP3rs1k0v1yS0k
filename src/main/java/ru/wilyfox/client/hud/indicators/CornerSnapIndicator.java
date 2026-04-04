package ru.wilyfox.client.hud.indicators;

import ru.wilyfox.client.hud.widget.Widget;
import ru.wilyfox.client.hud.widget.WidgetCorner;

public class CornerSnapIndicator {
    private final Widget widget;
    private final WidgetCorner corner;

    public CornerSnapIndicator(Widget widget, WidgetCorner corner) {
        this.widget = widget;
        this.corner = corner;
    }

    public Widget getWidget() {
        return widget;
    }

    public WidgetCorner getCorner() {
        return corner;
    }
}
