package ru.wilyfox.client.target;

import ru.wilyfox.client.hud.config.ConfigManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class TargetListStore {
    private static final Map<String, String> TARGETS = new LinkedHashMap<>();
    private static boolean loaded;

    private TargetListStore() {
    }

    public static synchronized boolean isTarget(String name) {
        String normalized = normalize(name);
        if (normalized == null) {
            return false;
        }

        ensureLoaded();
        return TARGETS.containsKey(normalized);
    }

    public static synchronized boolean add(String name) {
        String cleaned = clean(name);
        String normalized = normalize(cleaned);
        if (normalized == null) {
            return false;
        }

        ensureLoaded();
        if (TARGETS.containsKey(normalized)) {
            return false;
        }

        TARGETS.put(normalized, cleaned);
        persist();
        return true;
    }

    public static synchronized boolean remove(String name) {
        String normalized = normalize(name);
        if (normalized == null) {
            return false;
        }

        ensureLoaded();
        if (TARGETS.remove(normalized) == null) {
            return false;
        }

        persist();
        return true;
    }

    public static synchronized List<String> getTargets() {
        ensureLoaded();
        return List.copyOf(TARGETS.values());
    }

    private static void ensureLoaded() {
        if (loaded) {
            return;
        }

        TARGETS.clear();
        if (ConfigManager.get().targets.players != null) {
            for (String entry : ConfigManager.get().targets.players) {
                String cleaned = clean(entry);
                String normalized = normalize(cleaned);
                if (normalized != null) {
                    TARGETS.putIfAbsent(normalized, cleaned);
                }
            }
        }
        loaded = true;
    }

    private static void persist() {
        ConfigManager.get().targets.players = new ArrayList<>(TARGETS.values());
        ConfigManager.save();
    }

    private static String clean(String value) {
        if (value == null) {
            return null;
        }

        String cleaned = value.trim().replaceAll("\\s+", "");
        if (cleaned.isBlank()) {
            return null;
        }

        return cleaned;
    }

    private static String normalize(String value) {
        String cleaned = clean(value);
        if (cleaned == null) {
            return null;
        }

        return cleaned.toLowerCase(Locale.ROOT);
    }
}
