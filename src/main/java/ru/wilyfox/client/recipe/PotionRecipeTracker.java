package ru.wilyfox.client.recipe;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.ArrayList;
import java.util.List;

public final class PotionRecipeTracker {
    private static final PotionRecipeTracker INSTANCE = new PotionRecipeTracker();

    private String title;
    private List<String> recipeLines = List.of();

    private PotionRecipeTracker() {
    }

    public static PotionRecipeTracker getInstance() {
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

    public void inspect(ItemStack stack, Player player) {
        if (stack == null || stack.isEmpty() || player == null) {
            clear();
            return;
        }

        String itemName = stack.getHoverName().getString().trim();
        if (!itemName.startsWith("Зелье ")) {
            clear();
            return;
        }

        List<Component> tooltip = stack.getTooltipLines(Item.TooltipContext.of(player.level()), player, TooltipFlag.NORMAL);
        List<String> parsedRecipe = parseRecipeLines(tooltip);

        if (parsedRecipe.isEmpty()) {
            clear();
            return;
        }

        this.title = itemName;
        this.recipeLines = parsedRecipe;
    }

    public void clear() {
        this.title = null;
        this.recipeLines = List.of();
    }

    private List<String> parseRecipeLines(List<Component> tooltip) {
        List<String> result = new ArrayList<>();
        boolean readingRecipe = false;

        for (Component line : tooltip) {
            String text = line.getString().replace('\u00A0', ' ').trim();

            if (!readingRecipe) {
                if (text.equals("Рецепт:")) {
                    readingRecipe = true;
                }
                continue;
            }

            if (text.startsWith("Ваш уровень мастерства:")) {
                break;
            }

            if (!text.isEmpty()) {
                result.add(text);
            }
        }

        return result;
    }
}
