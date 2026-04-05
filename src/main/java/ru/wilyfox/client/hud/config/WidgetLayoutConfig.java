package ru.wilyfox.client.hud.config;

import ru.wilyfox.client.hud.indicators.ScreenAnchor;
import ru.wilyfox.client.hud.widget.WidgetCorner;

public class WidgetLayoutConfig {
    public Integer x;
    public Integer y;
    public Float scale;
    public ScreenAnchor anchor;
    public String snapTarget;
    public WidgetCorner snapOwnCorner;
    public WidgetCorner snapTargetCorner;
    public Boolean hiddenInGameplay;
}
