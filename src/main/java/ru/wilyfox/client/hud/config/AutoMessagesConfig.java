package ru.wilyfox.client.hud.config;

import java.util.ArrayList;
import java.util.List;

public class AutoMessagesConfig {
    public boolean active = false;
    public List<AutoMessageEntryConfig> entries = createDefaultEntries();

    public static List<AutoMessageEntryConfig> createDefaultEntries() {
        List<AutoMessageEntryConfig> defaults = new ArrayList<>();
        defaults.add(new AutoMessageEntryConfig());
        return defaults;
    }
}
