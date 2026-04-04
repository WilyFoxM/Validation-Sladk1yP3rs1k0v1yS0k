package ru.wilyfox.client.seller;

import ru.wilyfox.client.protocol.DwSellerEntry;
import ru.wilyfox.utils.Formatting;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SellerCooldownStore {
    private final Map<String, Entry> entries = new LinkedHashMap<>();

    public void replace(List<DwSellerEntry> sellers) {
        long now = System.currentTimeMillis();
        entries.clear();

        for (DwSellerEntry seller : sellers) {
            long remaining = seller.remainingMillis();
            boolean ready = remaining < 0L;
            long clampedRemaining = Math.max(0L, remaining);
            entries.put(seller.id(), new Entry(
                    seller.id(),
                    sanitizeName(seller.name(), seller.id()),
                    ready,
                    now + clampedRemaining
            ));
        }
    }

    public List<Entry> getEntries() {
        List<Entry> result = new ArrayList<>(entries.values());
        result.sort(Comparator.comparing(Entry::name, String.CASE_INSENSITIVE_ORDER));
        return result;
    }

    public boolean hasEntries() {
        return !entries.isEmpty();
    }

    public void clear() {
        entries.clear();
    }

    private static String sanitizeName(String name, String fallbackId) {
        String stripped = Formatting.stripMinecraftFormatting(name == null ? "" : name)
                .replaceAll("(?i)&[0-9A-FK-ORX]", "")
                .trim();
        return stripped.isBlank() ? fallbackId : stripped;
    }

    public record Entry(
            String id,
            String name,
            boolean ready,
            long endsAt
    ) {
        public long remainingMillis() {
            return Math.max(0L, endsAt - System.currentTimeMillis());
        }
    }
}
