package ru.wilyfox.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import ru.wilyfox.bridge.PlayerFishingAccessor;

@Mixin(Player.class)
public abstract class PlayerFishingAccessorMixin implements PlayerFishingAccessor {
    @Shadow public FishingHook fishing;

    @Override
    public FishingHook froghelper$getFishingHook() {
        return this.fishing;
    }
}
