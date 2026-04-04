package ru.wilyfox.client.boss;

import net.minecraft.core.component.DataComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.TooltipFlag;
import ru.wilyfox.boss.BossRepository;
import ru.wilyfox.utils.BossLevel;
import ru.wilyfox.utils.BossName;
import ru.wilyfox.utils.Formatting;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class BossMenuIconCollector {
    private static final int[] KNOWN_BOSS_LEVELS = {
            15, 20, 25, 30, 35, 40, 45, 50, 55, 60,
            65, 70, 75, 80, 90, 100, 105, 110, 115, 120,
            125, 130, 140, 150, 160, 170, 180, 190, 200, 210,
            220, 230, 240, 250, 260, 270, 280, 300, 320, 330,
            340, 345, 350, 360, 370, 380, 390, 400, 410, 420,
            430, 440, 450, 460, 470, 480, 490, 500, 510, 520
    };
    private static final Pattern LEVEL_PATTERN = Pattern.compile("(\\d+)");

    private static BossRepository repository;

    private BossMenuIconCollector() {
    }

    public static void bindRepository(BossRepository bossRepository) {
        repository = bossRepository;
    }

    public static void inspect(Component title, AbstractContainerMenu menu) {
        if (repository == null || menu == null) {
            return;
        }

        if (!isBossMenu(title)) {
            return;
        }

        for (Slot slot : menu.slots) {
            if (slot == null || !slot.hasItem()) {
                continue;
            }

            ItemStack stack = slot.getItem();
            String bossName = resolveBossName(stack);
            if (bossName == null) {
                continue;
            }

            int level = resolveBossLevel(stack, bossName);
            if (level <= 0) {
                continue;
            }

            repository.rememberDiscoveredIcon(bossName, level, stack);
        }
    }

    private static String resolveBossName(ItemStack stack) {
        String cleanName = Formatting.sanitize(stack.getHoverName().getString());
        if (cleanName.isBlank()) {
            return null;
        }

        String normalizedByTracker = BossName.getBossName(cleanName.toUpperCase(Locale.ROOT));
        if (normalizedByTracker != null) {
            return normalizedByTracker;
        }

        Integer exactLevel = BossLevel.getBossLevel(cleanName);
        if (exactLevel != null) {
            return cleanName;
        }

        for (int level : KNOWN_BOSS_LEVELS) {
            String knownBossName = BossLevel.getBossNameByLevel(level);
            if (knownBossName == null || knownBossName.isBlank()) {
                continue;
            }

            String upperKnownBossName = knownBossName.toUpperCase(Locale.ROOT);
            if (cleanName.equals(knownBossName) || cleanName.equals(upperKnownBossName)) {
                return knownBossName;
            }
        }

        return null;
    }

    private static int resolveBossLevel(ItemStack stack, String bossName) {
        Integer levelFromName = bossName != null ? BossLevel.getBossLevel(bossName) : null;
        if (levelFromName != null && levelFromName > 0) {
            return levelFromName;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return 0;
        }

        for (Component line : stack.getTooltipLines(Item.TooltipContext.of(minecraft.player.level()), minecraft.player, TooltipFlag.NORMAL)) {
            String cleanLine = Formatting.sanitize(line.getString());
            if (!cleanLine.contains("уровень")) {
                continue;
            }

            Matcher matcher = LEVEL_PATTERN.matcher(cleanLine);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        }

        return 0;
    }

    private static boolean isBossMenu(Component title) {
        String cleanTitle = Formatting.sanitize(title != null ? title.getString() : "");
        if (cleanTitle.isBlank()) {
            return false;
        }

        String normalized = cleanTitle.toLowerCase(Locale.ROOT);
        return normalized.contains("босс");
    }
}
