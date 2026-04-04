package ru.wilyfox.client.hud.internal;

import ru.wilyfox.client.hud.widget.AbstractWidget;
import ru.wilyfox.client.hud.widget.Widget;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class HudSnapGroupResolver {
    private HudSnapGroupResolver() {
    }

    public static List<AbstractWidget> getSnappedDescendants(AbstractWidget rootWidget, List<Widget> widgets) {
        List<AbstractWidget> result = new ArrayList<>();
        collectSnappedDescendants(rootWidget, widgets, result);
        return result;
    }

    public static List<AbstractWidget> getConnectedSnapGroup(AbstractWidget startWidget, List<Widget> widgets) {
        List<AbstractWidget> result = new ArrayList<>();
        List<AbstractWidget> queue = new ArrayList<>();
        Set<String> visited = new HashSet<>();

        queue.add(startWidget);
        if (startWidget.getConfigKey() != null) {
            visited.add(startWidget.getConfigKey());
        }

        for (int index = 0; index < queue.size(); index++) {
            AbstractWidget current = queue.get(index);
            result.add(current);

            AbstractWidget parent = findAbstractWidget(current.getSnapTargetKey(), widgets);
            if (parent != null && parent.getConfigKey() != null && visited.add(parent.getConfigKey())) {
                queue.add(parent);
            }

            for (Widget widget : widgets) {
                if (!(widget instanceof AbstractWidget abstractWidget) || abstractWidget == current || abstractWidget.getConfigKey() == null) {
                    continue;
                }

                if (!current.getConfigKey().equals(abstractWidget.getSnapTargetKey()) || !visited.add(abstractWidget.getConfigKey())) {
                    continue;
                }

                queue.add(abstractWidget);
            }
        }

        return result;
    }

    private static void collectSnappedDescendants(AbstractWidget parent, List<Widget> widgets, List<AbstractWidget> result) {
        String parentKey = parent.getConfigKey();
        if (parentKey == null) {
            return;
        }

        for (Widget widget : widgets) {
            if (!(widget instanceof AbstractWidget abstractWidget) || abstractWidget == parent || result.contains(abstractWidget)) {
                continue;
            }

            if (!parentKey.equals(abstractWidget.getSnapTargetKey())) {
                continue;
            }

            result.add(abstractWidget);
            collectSnappedDescendants(abstractWidget, widgets, result);
        }
    }

    private static AbstractWidget findAbstractWidget(String configKey, List<Widget> widgets) {
        if (configKey == null) {
            return null;
        }

        for (Widget widget : widgets) {
            if (widget instanceof AbstractWidget abstractWidget && configKey.equals(abstractWidget.getConfigKey())) {
                return abstractWidget;
            }
        }
        return null;
    }
}
