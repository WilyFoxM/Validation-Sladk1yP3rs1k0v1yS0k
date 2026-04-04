package ru.wilyfox.client.hud.config;

public enum ThemePreset {
    FROG("Frog", 0xD0D0D0),
    MINT("Mint", 0x9FD7C4),
    EMBER("Ember", 0xD9A07A),
    OCEAN("Ocean", 0x8FB7D9),
    CUSTOM("Custom", 0xD0D0D0);

    private final String title;
    private final int accentRgb;

    ThemePreset(String title, int accentRgb) {
        this.title = title;
        this.accentRgb = accentRgb;
    }

    public String getTitle() {
        return title;
    }

    public int getAccentRgb() {
        return accentRgb;
    }
}
