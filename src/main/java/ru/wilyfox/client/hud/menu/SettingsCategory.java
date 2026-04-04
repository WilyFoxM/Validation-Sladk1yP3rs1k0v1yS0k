package ru.wilyfox.client.hud.menu;

public enum SettingsCategory {
    QUICK_ACCESS("Quick Access"),
    AUTO_MESSAGES("Auto Messages"),
    BOSS_TIMERS("Boss Timers"),
    BOSS_RESPAWN_MESSAGES("Boss Messages"),
    PLAYER_HEALTH_BARS("HP Bars"),
    BLOCKS_PER_SECOND("Block Counter"),
    FISHING("Fishing"),
    WAYPOINTS("WayPoints"),
    POP_UPS("Pop-Ups"),
    THEME("Theme"),
    RENDER("Render"),
    CLICKER("Clicker");

    private final String title;

    SettingsCategory(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
