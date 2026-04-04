package ru.wilyfox.client.hud.config;

public enum BossTimerSourceMode {
    WORLD_ONLY("World only"),
    PROTOCOL_PREFERRED("Protocol preferred"),
    PROTOCOL_ONLY("Protocol only");

    private final String title;

    BossTimerSourceMode(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
