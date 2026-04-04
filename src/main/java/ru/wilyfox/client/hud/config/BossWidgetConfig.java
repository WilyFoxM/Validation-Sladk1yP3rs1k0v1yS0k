package ru.wilyfox.client.hud.config;

public class BossWidgetConfig {
    public boolean active = true;
    public boolean fullAligment = false;
    public BossTimerSourceMode sourceMode = BossTimerSourceMode.PROTOCOL_PREFERRED;

    public int maxBosses = 5;

    public int minLevel = 15;
    public int maxLevel = 520;

    public boolean showName = true;
    public boolean showIcons = true;
    public boolean showLevel = true;
    public boolean showTimer = true;
}
