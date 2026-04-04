package ru.wilyfox.client.hud.snap;

import ru.wilyfox.client.hud.widget.Widget;
import ru.wilyfox.client.hud.widget.WidgetCorner;

public class SnapCandidate {
    private final int distance;
    private final int snappedStartX;
    private final int snappedStartY;
    private final Widget targetWidget;
    private final WidgetCorner draggedCorner;
    private final WidgetCorner targetCorner;

    public SnapCandidate(
            int distance,
            int snappedStartX,
            int snappedStartY,
            Widget targetWidget,
            WidgetCorner draggedCorner,
            WidgetCorner targetCorner
    ) {
        this.distance = distance;
        this.snappedStartX = snappedStartX;
        this.snappedStartY = snappedStartY;
        this.targetWidget = targetWidget;
        this.draggedCorner = draggedCorner;
        this.targetCorner = targetCorner;
    }

    public int getDistance() {
        return distance;
    }

    public int getSnappedStartX() {
        return snappedStartX;
    }

    public int getSnappedStartY() {
        return snappedStartY;
    }

    public Widget getTargetWidget() {
        return targetWidget;
    }

    public WidgetCorner getDraggedCorner() {
        return draggedCorner;
    }

    public WidgetCorner getTargetCorner() {
        return targetCorner;
    }
}
