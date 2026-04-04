package ru.wilyfox.boss;

import net.minecraft.world.item.ItemStack;
import ru.wilyfox.utils.BossLevel;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class BossRepository {
    private final Map<String, BossInfo> worldBosses = new LinkedHashMap<>();
    private final Map<String, BossInfo> protocolBosses = new LinkedHashMap<>();
    private final Map<String, ItemStack> discoveredBossIcons = new LinkedHashMap<>();

    public void upsert(String bossName, long respawnAtMillis) {
        upsert(worldBosses, bossName, bossName, respawnAtMillis, Objects.requireNonNullElse(BossLevel.getBossLevel(bossName), 0));
    }

    public void upsertProtocol(String bossId, String bossName, long respawnAtMillis, int level) {
        upsert(protocolBosses, bossId, bossName, respawnAtMillis, level);
    }

    public void clearProtocol() {
        protocolBosses.clear();
        discoveredBossIcons.clear();
    }

    public Collection<BossInfo> getAll() {
        return getAllMerged();
    }

    public Collection<BossInfo> getAllWorld() {
        return worldBosses.values().stream()
                .sorted(Comparator.comparingLong(BossInfo::getRespawnAt))
                .collect(Collectors.toList());
    }

    public Collection<BossInfo> getAllProtocol() {
        return protocolBosses.values().stream()
                .sorted(Comparator.comparingLong(BossInfo::getRespawnAt))
                .collect(Collectors.toList());
    }

    public Collection<BossInfo> getAllMerged() {
        Map<String, BossInfo> merged = new LinkedHashMap<>();

        for (BossInfo boss : protocolBosses.values()) {
            merged.put(mergeKey(boss), boss);
        }

        for (BossInfo boss : worldBosses.values()) {
            merged.putIfAbsent(mergeKey(boss), boss);
        }

        return merged.values().stream()
                .sorted(Comparator.comparingLong(BossInfo::getRespawnAt))
                .collect(Collectors.toList());
    }

    public void replaceProtocol(Map<String, BossInfo> bosses) {
        protocolBosses.clear();
        protocolBosses.putAll(bosses);
    }

    public ItemStack getDiscoveredIcon(BossInfo boss) {
        if (boss.getLevel() > 0) {
            ItemStack byLevel = discoveredBossIcons.get(levelKey(boss.getLevel()));
            if (byLevel != null) {
                return byLevel.copy();
            }
        }

        ItemStack byName = discoveredBossIcons.get(iconKey(boss.getName()));
        return byName != null ? byName.copy() : null;
    }

    public void rememberDiscoveredIcon(String bossName, int level, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        ItemStack icon = stack.copy();
        icon.setCount(1);

        if (bossName != null && !bossName.isBlank()) {
            discoveredBossIcons.put(iconKey(bossName), icon.copy());
        }

        if (level > 0) {
            discoveredBossIcons.put(levelKey(level), icon.copy());
        }
    }

    public String findBossNameByLevel(int level) {
        for (BossInfo boss : protocolBosses.values()) {
            if (boss.getLevel() == level) {
                return boss.getName();
            }
        }

        for (BossInfo boss : worldBosses.values()) {
            if (boss.getLevel() == level) {
                return boss.getName();
            }
        }

        return null;
    }

    private void upsert(Map<String, BossInfo> storage, String key, String bossName, long respawnAtMillis, int level) {
        storage.compute(key, (ignored, oldBoss) -> {
            if (oldBoss == null) {
                return new BossInfo(bossName, respawnAtMillis, level);
            }

            oldBoss.setRespawnAt(respawnAtMillis);
            return oldBoss;
        });
    }

    private String mergeKey(BossInfo boss) {
        if (boss.getLevel() > 0) {
            return levelKey(boss.getLevel());
        }

        return iconKey(boss.getName());
    }

    private String levelKey(int level) {
        return "level:" + level;
    }

    private String iconKey(String bossName) {
        return "name:" + bossName.trim().toLowerCase(Locale.ROOT);
    }
}
