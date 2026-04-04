package ru.wilyfox.client.utility;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import ru.wilyfox.bridge.PlayerFishingAccessor;
import ru.wilyfox.client.hud.config.ConfigManager;

public final class AutoFish {
    private static final long REEL_COOLDOWN_MS = 250L;

    private static int consecutiveTouchTicks = 0;
    private static boolean reeledCurrentHook = false;
    private static int currentHookId = Integer.MIN_VALUE;
    private static long lastReelAt = 0L;

    private AutoFish() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!ConfigManager.get().fishing.autoFish) {
                resetHookState();
                return;
            }

            if (client.player == null || client.level == null || client.gameMode == null) {
                resetHookState();
                return;
            }

            FishingHook hook = getFishingHook(client);
            if (hook == null || !hook.isAlive()) {
                resetHookState();
                return;
            }

            if (hook.getId() != currentHookId) {
                currentHookId = hook.getId();
                consecutiveTouchTicks = 0;
                reeledCurrentHook = false;
            }

            if (!isTouchingWaterOrLava(hook)) {
                consecutiveTouchTicks = 0;
                return;
            }

            consecutiveTouchTicks++;
            int requiredTicks = Math.max(1, ConfigManager.get().fishing.autoFishDelayTicks);
            if (!reeledCurrentHook && consecutiveTouchTicks >= requiredTicks && System.currentTimeMillis() - lastReelAt >= REEL_COOLDOWN_MS) {
                InteractionHand hand = findRodHand(client);
                if (hand != null) {
                    client.gameMode.useItem(client.player, hand);
                    lastReelAt = System.currentTimeMillis();
                    reeledCurrentHook = true;
                }
            }
        });
    }

    private static FishingHook getFishingHook(Minecraft client) {
        if (client.player instanceof PlayerFishingAccessor accessor) {
            return accessor.froghelper$getFishingHook();
        }
        return null;
    }

    private static void resetHookState() {
        consecutiveTouchTicks = 0;
        reeledCurrentHook = false;
        currentHookId = Integer.MIN_VALUE;
    }

    private static boolean isTouchingWaterOrLava(FishingHook hook) {
        BlockPos center = BlockPos.containing(hook.getX(), hook.getY(), hook.getZ());
        BlockPos below = center.below();
        return isWaterOrLava(hook, center) || isWaterOrLava(hook, below);
    }

    private static boolean isWaterOrLava(FishingHook hook, BlockPos pos) {
        return hook.level().getFluidState(pos).is(FluidTags.WATER) || hook.level().getFluidState(pos).is(FluidTags.LAVA);
    }

    private static InteractionHand findRodHand(Minecraft client) {
        if (isFishingRod(client.player.getMainHandItem())) {
            return InteractionHand.MAIN_HAND;
        }
        if (isFishingRod(client.player.getOffhandItem())) {
            return InteractionHand.OFF_HAND;
        }
        return null;
    }

    private static boolean isFishingRod(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.getItem() == Items.FISHING_ROD;
    }
}
