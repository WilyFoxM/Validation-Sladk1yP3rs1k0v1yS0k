package ru.wilyfox.client.protocol;

import net.minecraft.network.chat.Component;
import ru.wilyfox.boss.BossRepository;
import ru.wilyfox.client.ability.AbilityCooldownStore;
import ru.wilyfox.client.boss.BossDamageStore;
import ru.wilyfox.client.booster.BoosterStore;
import ru.wilyfox.client.combo.ComboProgressStore;
import ru.wilyfox.client.level.LevelProgressStore;
import ru.wilyfox.client.miner.ActiveMinersStore;
import ru.wilyfox.client.pet.ActivePetsStore;
import ru.wilyfox.client.potion.PotionStore;
import ru.wilyfox.client.rune.ActiveRunesStore;
import ru.wilyfox.client.seller.SellerCooldownStore;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class DiamondWorldProtocolClient {
    private static final ProtocolState STATE = new ProtocolState();
    private static final ProtocolRouter ROUTER = new ProtocolRouter();

    private DiamondWorldProtocolClient() {
    }

    public static void init() {
        ProtocolTransport.init(STATE, ROUTER);
    }

    public static void bindBossRepository(BossRepository repository) {
        STATE.bossRepository = repository;
    }

    public static void bindActiveRunesStore(ActiveRunesStore store) {
        STATE.activeRunesStore = store;
    }

    public static void bindActivePetsStore(ActivePetsStore store) {
        STATE.activePetsStore = store;
    }

    public static void bindActiveMinersStore(ActiveMinersStore store) {
        STATE.activeMinersStore = store;
    }

    public static void bindAbilityCooldownStore(AbilityCooldownStore store) {
        STATE.abilityCooldownStore = store;
    }

    public static void bindBossDamageStore(BossDamageStore store) {
        STATE.bossDamageStore = store;
    }

    public static void bindLevelProgressStore(LevelProgressStore store) {
        STATE.levelProgressStore = store;
    }

    public static void bindPotionStore(PotionStore store) {
        STATE.potionStore = store;
    }

    public static void bindSellerCooldownStore(SellerCooldownStore store) {
        STATE.sellerCooldownStore = store;
    }

    public static void bindComboProgressStore(ComboProgressStore store) {
        STATE.comboProgressStore = store;
    }

    public static void bindBoosterStore(BoosterStore store) {
        STATE.boosterStore = store;
    }

    public static String getCurrentServerDisplayName(Component footer) {
        if (STATE.currentServerInfo != null && STATE.currentServerInfo.isKnown()) {
            return STATE.currentServerInfo.displayName();
        }

        if (footer != null) {
            CurrentServerInfo fallback = CurrentServerInfo.fromDisplayText(footer.getString());
            if (fallback.isKnown()) {
                return fallback.displayName();
            }
        }

        return "";
    }

    public static CurrentServerInfo getCurrentServerInfo() {
        return STATE.currentServerInfo != null ? STATE.currentServerInfo : CurrentServerInfo.unknown();
    }

    public static DwGameEvent getCurrentGameEvent() {
        return STATE.currentGameEvent != null ? STATE.currentGameEvent : DwGameEvent.NONE;
    }

    public static boolean isMythicalEventActive() {
        return getCurrentGameEvent() == DwGameEvent.MYTHICAL_EVENT;
    }

    public static boolean isRaidBossLevel(int level) {
        if (level <= 0) {
            return false;
        }

        for (DwBossType type : STATE.bossTypes.values()) {
            if (type.level() == level) {
                return type.raid();
            }
        }

        return false;
    }

    public static String getCurrentGameLocation() {
        return STATE.currentGameLocation;
    }

    public static boolean isDungeonLocation() {
        String location = normalizeLocationId(STATE.currentGameLocation);
        return location != null && location.contains("dungeon");
    }

    public static boolean isSiegeLocation() {
        String location = normalizeLocationId(STATE.currentGameLocation);
        return location != null && location.contains("siege");
    }

    public static boolean isDungeonOrSiegeLocation() {
        return isDungeonLocation() || isSiegeLocation();
    }

    public static Set<String> getFishingLocationIds() {
        return Set.copyOf(STATE.fishingLocationIds);
    }

    public static Map<String, Double> getFishingNibbles() {
        return Map.copyOf(STATE.fishingNibbles);
    }

    public static Map<String, String> getFishingLocationNames() {
        return Map.copyOf(STATE.fishingLocationNames);
    }

    public static boolean hasFishingNibbles() {
        return !STATE.fishingNibbles.isEmpty();
    }

    public static String getFishingLocationName(String locationId) {
        String normalized = normalizeLocationId(locationId);
        if (normalized == null) {
            return "";
        }

        String name = STATE.fishingLocationNames.get(normalized);
        return name != null && !name.isBlank() ? name : normalized;
    }

    public static String getBossNameByLevel(int level) {
        for (DwBossType type : STATE.bossTypes.values()) {
            if (type.level() == level) {
                return type.name();
            }
        }

        return null;
    }

    static String normalizeLocationId(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return normalized.isBlank() ? null : normalized;
    }
}
