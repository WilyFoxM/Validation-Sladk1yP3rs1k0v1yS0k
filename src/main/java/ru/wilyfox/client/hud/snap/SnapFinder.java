package ru.wilyfox.client.hud.snap;

import ru.wilyfox.client.hud.widget.Widget;

public class SnapFinder {
    public static boolean overlapsVertically(Widget a, Widget b) {
        return a.getTop() < b.getBottom() && a.getBottom() > b.getTop();
    }

    public static boolean overlapsHorizontally(Widget a, Widget b) {
        return a.getLeft() < b.getRight() && a.getRight() > b.getLeft();
    }
}
