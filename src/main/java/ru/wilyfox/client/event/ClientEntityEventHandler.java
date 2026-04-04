package ru.wilyfox.client.event;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import ru.wilyfox.boss.BossTracker;

public class ClientEntityEventHandler {
    private final BossTracker bossTracker;

    public ClientEntityEventHandler(BossTracker b) {
        this.bossTracker = b;
    }

    public void register() {
        ClientEntityEvents.ENTITY_LOAD.register((Entity entity, ClientLevel world) -> this.bossTracker.onEntityLoad(entity));

        ClientTickEvents.END_WORLD_TICK.register(bossTracker::onWorldTick);
    }
}
