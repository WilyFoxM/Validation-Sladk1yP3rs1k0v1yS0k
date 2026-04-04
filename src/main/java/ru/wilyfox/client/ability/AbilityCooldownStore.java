package ru.wilyfox.client.ability;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AbilityCooldownStore {
    private final Map<String, Entry> entries = new LinkedHashMap<>();
    private final Map<String, String> names = new LinkedHashMap<>();

    public void replaceTypes(Map<String, String> updatedNames) {
        names.clear();
        names.putAll(updatedNames);
    }

    public void replaceCooldowns(Map<String, Long> remainingMillisById) {
        cleanup();

        for (Map.Entry<String, Long> entry : remainingMillisById.entrySet()) {
            upsertCooldown(entry.getKey(), names.getOrDefault(entry.getKey(), entry.getKey()), entry.getValue());
        }
    }

    public void replaceExternalCooldown(String id, String name, long remainingMillis) {
        if (id == null || id.isBlank()) {
            return;
        }

        upsertCooldown(id, name, remainingMillis);
    }

    private void upsertCooldown(String id, String name, long remainingMillis) {
        long remaining = Math.max(0L, remainingMillis);
        if (remaining <= 0L) {
            entries.remove(id);
            return;
        }

        long now = System.currentTimeMillis();
        Entry previous = entries.get(id);
        long duration = previous != null ? Math.max(previous.durationMillis(), remaining) : remaining;
        long startedAt = now - Math.max(0L, duration - remaining);

        entries.put(id, new Entry(
                id,
                name != null && !name.isBlank() ? name : id,
                startedAt,
                now + duration,
                duration
        ));
    }

    public List<Entry> getActiveEntries() {
        cleanup();
        return new ArrayList<>(entries.values());
    }

    public boolean hasActiveEntries() {
        cleanup();
        return !entries.isEmpty();
    }

    public void clear() {
        entries.clear();
        names.clear();
    }

    private void cleanup() {
        long now = System.currentTimeMillis();
        entries.entrySet().removeIf(entry -> entry.getValue().endsAt() <= now);
    }

    public record Entry(
            String id,
            String name,
            long startedAt,
            long endsAt,
            long durationMillis
    ) {
        public long remainingMillis() {
            return Math.max(0L, endsAt - System.currentTimeMillis());
        }

        public float progress() {
            if (durationMillis <= 0L) {
                return 0.0F;
            }

            return remainingMillis() / (float) durationMillis;
        }
    }
}
