package ru.wilyfox.client.protocol;

public enum DwGameEvent {
    NONE(""),
    MYTHICAL_EVENT("\u00A7d\u041c\u0438\u0444\u0438\u0447\u0435\u0441\u043a\u0438\u0439 \u044d\u0432\u0435\u043d\u0442"),
    GOLDEN_CHIN("\u00A76\u0417\u043e\u043b\u043e\u0442\u0430\u044f \u043b\u0438\u0445\u043e\u0440\u0430\u0434\u043a\u0430"),
    ANCIENT_GUARDIAN("\u00A7c\u0414\u0440\u0435\u0432\u043d\u0438\u0439 \u0441\u0442\u0440\u0430\u0436"),
    GENEROUS_OWL("\u00A7b\u0429\u0435\u0434\u0440\u0430\u044f \u0441\u043e\u0432\u0430");

    private final String displayName;

    DwGameEvent(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
