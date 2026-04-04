package ru.wilyfox.client.quickaccess;

import java.util.ArrayList;
import java.util.List;

public class QuickAccessConfig {
    public boolean active = true;
    public List<QuickAccessSectionConfig> sections = createDefaultSections();

    public static List<QuickAccessSectionConfig> createDefaultSections() {
        List<QuickAccessSectionConfig> sections = new ArrayList<>();

        QuickAccessSectionConfig common = new QuickAccessSectionConfig();
        common.title = "Common";
        common.items.add(item("Shop", "shop", "minecraft:emerald", 0));
        common.items.add(item("Upgrades", "upgrades", "minecraft:anvil", 0));
        common.items.add(item("Runes", "runesbag", "minecraft:command_block", 54));
        common.items.add(item("Collections", "collections", "minecraft:bone", 0));
        common.items.add(item("Pets", "pets", "minecraft:spider_spawn_egg", 0));
        sections.add(common);

        QuickAccessSectionConfig travel = new QuickAccessSectionConfig();
        travel.title = "Travel";
        travel.items.add(item("Spawn", "spawn", "minecraft:compass", 0));
        travel.items.add(item("Mine", "mine", "minecraft:diamond_pickaxe", 0));
        travel.items.add(item("Menu", "menu", "minecraft:nether_star", 0));
        travel.items.add(item("Bosses", "bosses", "minecraft:zombie_head", 0));
        travel.items.add(item("Fishing", "warp fish", "minecraft:fishing_rod", 0));
        sections.add(travel);

        QuickAccessSectionConfig player = new QuickAccessSectionConfig();
        player.title = "Player";
        player.items.add(item("Stats {player}", "statistics {player}", "minecraft:book", 0));
        player.items.add(item("Trade {player}", "trade {player}", "minecraft:gold_ingot", 0));
        player.items.add(item("Backpack", "backpack", "minecraft:command_block", 1));
        sections.add(player);

        return sections;
    }

    private static QuickAccessItemConfig item(String title, String command, String itemId, int customModelData) {
        QuickAccessItemConfig item = new QuickAccessItemConfig();
        item.title = title;
        item.command = command;
        item.itemId = itemId;
        item.customModelData = customModelData;
        return item;
    }
}
