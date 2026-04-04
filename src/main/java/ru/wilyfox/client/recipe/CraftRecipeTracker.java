package ru.wilyfox.client.recipe;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import ru.wilyfox.utils.Formatting;

import java.util.ArrayList;
import java.util.List;

public final class CraftRecipeTracker {
    private static final String START_MARKER_PRIMARY = "Ваши ингридиенты:";
    private static final String START_MARKER_SECONDARY = "Ваши ингредиенты:";
    private static final String END_MARKER = "Нажмите для начала крафта";
    private static final String TIME_MARKER = "Время крафта:";
    private static final CraftRecipeTracker INSTANCE = new CraftRecipeTracker();

    private String title;
    private String craftTimeLine;
    private List<String> recipeLines = List.of();
    private ItemStack icon = ItemStack.EMPTY;

    private CraftRecipeTracker() {
    }

    public static CraftRecipeTracker getInstance() {
        return INSTANCE;
    }

    public boolean hasRecipe() {
        return title != null && !recipeLines.isEmpty();
    }

    public String getTitle() {
        return title == null ? "" : title;
    }

    public List<String> getRecipeLines() {
        return List.copyOf(recipeLines);
    }

    public String getCraftTimeLine() {
        return craftTimeLine == null ? "" : craftTimeLine;
    }

    public ItemStack getIcon() {
        return icon.isEmpty() ? ItemStack.EMPTY : icon.copy();
    }

    public void inspect(ItemStack stack, Player player) {
        if (stack == null || stack.isEmpty() || player == null) {
            clear();
            return;
        }

        List<Component> tooltip = stack.getTooltipLines(Item.TooltipContext.of(player.level()), player, TooltipFlag.NORMAL);
        List<String> parsedRecipe = parseRecipeLines(tooltip);

        if (parsedRecipe.isEmpty()) {
            clear();
            return;
        }

        this.title = sanitize(stack.getHoverName().getString());
        this.craftTimeLine = parseCraftTimeLine(tooltip);
        this.recipeLines = parsedRecipe;
        this.icon = stack.copy();
        this.icon.setCount(1);
    }

    public void clear() {
        this.title = null;
        this.craftTimeLine = null;
        this.recipeLines = List.of();
        this.icon = ItemStack.EMPTY;
    }

    private List<String> parseRecipeLines(List<Component> tooltip) {
        List<String> result = new ArrayList<>();
        boolean readingRecipe = false;

        for (Component line : tooltip) {
            String text = sanitize(line.getString());

            if (!readingRecipe) {
                if (text.equals(START_MARKER_PRIMARY) || text.equals(START_MARKER_SECONDARY)) {
                    readingRecipe = true;
                }
                continue;
            }

            if (text.startsWith(END_MARKER)) {
                break;
            }

            if (!text.isEmpty()) {
                result.add(text);
            }
        }

        return result;
    }

    private String parseCraftTimeLine(List<Component> tooltip) {
        for (Component line : tooltip) {
            String text = sanitize(line.getString());
            if (text.startsWith(TIME_MARKER)) {
                return text;
            }
        }

        return "";
    }

    private String sanitize(String text) {
        return Formatting.stripMinecraftFormatting(text)
                .replace('\u00A0', ' ')
                .replace('\r', ' ')
                .replace('\n', ' ')
                .replaceAll("\\s+", " ")
                .trim();
    }
}
