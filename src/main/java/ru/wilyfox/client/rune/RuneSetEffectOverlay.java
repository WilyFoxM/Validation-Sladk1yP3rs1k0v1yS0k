package ru.wilyfox.client.rune;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import ru.wilyfox.client.hud.widget.WidgetTheme;
import ru.wilyfox.utils.Formatting;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RuneSetEffectOverlay {
    private static final String MENU_NAME = "\uE962";
    private static final List<Integer> RUNE_BAG_SLOTS = List.of(11, 13, 15, 27, 35);
    private static final List<Integer> RUNE_SET_SLOTS = List.of(0, 1, 3, 4, 5, 6, 8);
    private static final Pattern PROPERTY_LINE = Pattern.compile("(.*): (.*)");
    private static OverlayData cachedOverlay;

    private RuneSetEffectOverlay() {
    }

    public static boolean isRuneBagScreen(Component title) {
        return title != null && title.getString().contains(MENU_NAME);
    }

    public static OverlayData collect(AbstractContainerMenu menu) {
        String setName = findSelectedSetName(menu);
        Map<String, AggregatedProperty> properties = collectProperties(menu);

        if (setName == null && properties.isEmpty()) {
            return null;
        }

        List<String> lines = properties.values().stream()
                .filter(property -> !property.isEmpty())
                .sorted((a, b) -> Double.compare(Math.abs(b.value), Math.abs(a.value)))
                .map(AggregatedProperty::format)
                .toList();

        return new OverlayData(setName == null ? "Set Effect" : setName, lines);
    }

    public static OverlayData collectFromInventory(AbstractContainerMenu menu) {
        for (Slot slot : menu.slots) {
            if (!slot.hasItem()) {
                continue;
            }

            OverlayData data = extractOverlayFromItem(slot.getItem());
            if (data != null) {
                return data;
            }
        }

        return null;
    }

    public static void updateCache(AbstractContainerMenu menu) {
        cachedOverlay = collect(menu);
        updateCooldown(menu);
    }

    public static OverlayData getCached() {
        return cachedOverlay;
    }

    public static boolean isPlayerInventoryScreen(Object screen) {
        return screen instanceof InventoryScreen;
    }

    public static void render(GuiGraphics context, int x, int y, OverlayData data) {
        Minecraft mc = Minecraft.getInstance();
        int lineHeight = mc.font.lineHeight + 1;

        int maxWidth = mc.font.width(data.title());
        for (String line : data.lines()) {
            maxWidth = Math.max(maxWidth, mc.font.width(line));
        }

        int width = maxWidth + 12;
        int height = 12 + lineHeight + Math.max(0, data.lines().size()) * lineHeight;

        context.fill(x, y, x + width, y + height, WidgetTheme.PANEL_BG);
        context.fill(x, y, x + width, y + 1, WidgetTheme.ACCENT_LINE);

        int textY = y + 5;
        context.drawString(mc.font, data.title(), x + 6, textY, WidgetTheme.TITLE);
        textY += lineHeight + 2;

        for (String line : data.lines()) {
            context.drawString(mc.font, line, x + 6, textY, WidgetTheme.TEXT_SOFT);
            textY += lineHeight;
        }
    }

    private static String findSelectedSetName(AbstractContainerMenu menu) {
        for (int slotIndex : RUNE_SET_SLOTS) {
            if (slotIndex < 0 || slotIndex >= menu.slots.size()) {
                continue;
            }

            Slot slot = menu.getSlot(slotIndex);
            if (!slot.hasItem()) {
                continue;
            }

            ItemStack stack = slot.getItem();
            List<String> lore = getLoreLines(stack);
            if (!lore.isEmpty() && lore.get(0).contains("Используется")) {
                return Formatting.stripMinecraftFormatting(stack.getHoverName().getString()).trim();
            }
        }

        return null;
    }

    private static void updateCooldown(AbstractContainerMenu menu) {
        long longestCooldown = 0L;

        for (int slotIndex : RUNE_SET_SLOTS) {
            if (slotIndex < 0 || slotIndex >= menu.slots.size()) {
                continue;
            }

            Slot slot = menu.getSlot(slotIndex);
            if (!slot.hasItem()) {
                continue;
            }

            for (String line : getLoreLines(slot.getItem())) {
                long parsed = Formatting.parseTimeToMillis(line);
                if (parsed > longestCooldown) {
                    longestCooldown = parsed;
                }
            }
        }

        if (longestCooldown > 0L) {
            RuneSetCooldownStore.update(longestCooldown);
        } else {
            RuneSetCooldownStore.clear();
        }
    }

    private static Map<String, AggregatedProperty> collectProperties(AbstractContainerMenu menu) {
        Map<String, AggregatedProperty> properties = new LinkedHashMap<>();

        for (int slotIndex : RUNE_BAG_SLOTS) {
            if (slotIndex < 0 || slotIndex >= menu.slots.size()) {
                continue;
            }

            Slot slot = menu.getSlot(slotIndex);
            if (!slot.hasItem()) {
                continue;
            }

            ItemStack stack = slot.getItem();
            if (!stack.is(Items.PAPER)) {
                continue;
            }

            for (Map.Entry<String, String> entry : extractRawProperties(stack).entrySet()) {
                AggregatedProperty property = properties.computeIfAbsent(entry.getKey(), key -> new AggregatedProperty(key));
                property.append(entry.getValue());
            }
        }

        return properties;
    }

    private static Map<String, String> extractRawProperties(ItemStack stack) {
        Map<String, String> result = new LinkedHashMap<>();
        List<String> lore = getLoreLines(stack);

        boolean afterDivider = false;
        for (String line : lore) {
            if (line.contains("-----")) {
                afterDivider = true;
                continue;
            }

            if (!afterDivider) {
                continue;
            }

            Matcher matcher = PROPERTY_LINE.matcher(line);
            if (matcher.matches()) {
                result.put(matcher.group(1).trim(), matcher.group(2).trim());
            }
        }

        return result;
    }

    private static OverlayData extractOverlayFromItem(ItemStack stack) {
        String itemName = Formatting.stripMinecraftFormatting(stack.getHoverName().getString()).trim();
        if (!itemName.toLowerCase().contains("рун")) {
            return null;
        }

        List<String> lore = getLoreLines(stack);
        if (lore.isEmpty()) {
            return null;
        }

        List<String> lines = new ArrayList<>();

        for (String line : lore) {
            String clean = line.trim();
            if (clean.isEmpty()) {
                continue;
            }

            String lower = clean.toLowerCase();
            if (lower.contains("используется для активации")) {
                continue;
            }

            if (lower.contains("нельзя передать")) {
                continue;
            }

            if (!clean.contains(":")) {
                continue;
            }

            lines.add(clean);
        }

        if (lines.isEmpty()) {
            return null;
        }

        return new OverlayData("Эффект сета рун", lines);
    }

    private static List<String> getLoreLines(ItemStack stack) {
        List<Component> tooltip = stack.getTooltipLines(
                Item.TooltipContext.of(Minecraft.getInstance().player.level()),
                Minecraft.getInstance().player,
                Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL
        );
        List<String> lines = new ArrayList<>();

        for (int i = 1; i < tooltip.size(); i++) {
            String text = Formatting.stripMinecraftFormatting(tooltip.get(i).getString()).trim();
            if (!text.isEmpty()) {
                lines.add(text);
            }
        }

        return lines;
    }

    public record OverlayData(String title, List<String> lines) {
    }

    private static final class AggregatedProperty {
        private static final Pattern FLAT = Pattern.compile("^([-+][\\d.,]+)(?: \\(([-+][\\d.,]+)\\))?(?: \\| ([-+][\\d.,]+))?$");
        private static final Pattern PERCENT = Pattern.compile("^([-+][\\d.,]+)%(?: \\(([-+][\\d.,]+)%\\))?(?: \\| ([-+][\\d.,]+)%)?$");
        private static final Pattern MULTIPLY = Pattern.compile("^x([\\d.,]+)(?: \\| x([\\d.,]+))?$");
        private static final Pattern MINER = Pattern.compile("^1 к (\\d+)$");

        private final String name;
        private Kind kind = Kind.PRESENCE;
        private double value = 0.0;
        private boolean present;

        private AggregatedProperty(String name) {
            this.name = name;
        }

        private void append(String rawValue) {
            if (rawValue.equals("+")) {
                present = true;
                kind = Kind.PRESENCE;
                return;
            }

            Matcher percent = PERCENT.matcher(rawValue);
            if (percent.matches()) {
                kind = Kind.PERCENT;
                value += parseGroup(percent, 1) + parseGroup(percent, 2) + parseGroup(percent, 3);
                return;
            }

            Matcher flat = FLAT.matcher(rawValue);
            if (flat.matches()) {
                kind = Kind.FLAT;
                value += parseGroup(flat, 1) + parseGroup(flat, 2) + parseGroup(flat, 3);
                return;
            }

            Matcher multiply = MULTIPLY.matcher(rawValue);
            if (multiply.matches()) {
                kind = Kind.MULTIPLY;
                value += parseGroup(multiply, 1) + parseGroup(multiply, 2) - 1.0;
                return;
            }

            Matcher miner = MINER.matcher(rawValue);
            if (miner.matches()) {
                kind = Kind.MINER;
                value += 1.0 / Double.parseDouble(miner.group(1));
            }
        }

        private boolean isEmpty() {
            return !present && Math.abs(value) < 0.0001;
        }

        private String format() {
            return name + ": " + switch (kind) {
                case PRESENCE -> "+";
                case FLAT -> signed(value);
                case PERCENT -> signed(value) + "%";
                case MULTIPLY -> "x" + trim(value);
                case MINER -> "1 к " + (value == 0.0 ? "0" : String.valueOf((int) Math.round(1.0 / value)));
            };
        }

        private static double parseGroup(Matcher matcher, int group) {
            String value = matcher.group(group);
            if (value == null || value.isBlank()) {
                return 0.0;
            }

            return Double.parseDouble(value.replace(',', '.'));
        }

        private static String signed(double value) {
            String formatted = trim(value);
            return value >= 0 ? "+" + formatted : formatted;
        }

        private static String trim(double value) {
            String formatted = String.format(java.util.Locale.US, "%.2f", value);
            formatted = formatted.replaceAll("0+$", "").replaceAll("\\.$", "");
            return formatted;
        }
    }

    private enum Kind {
        PRESENCE,
        FLAT,
        PERCENT,
        MULTIPLY,
        MINER
    }
}
