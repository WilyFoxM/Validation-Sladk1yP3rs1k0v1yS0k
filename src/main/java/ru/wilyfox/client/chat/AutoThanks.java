package ru.wilyfox.client.chat;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import ru.wilyfox.client.hud.config.ConfigManager;
import ru.wilyfox.utils.Formatting;

import java.util.regex.Pattern;

public final class AutoThanks {
    private static final Pattern BOOSTER_MESSAGE_PATTERN = Pattern.compile("^[\\w\\s]+ \\u0430\\u043a\\u0442\\u0438\\u0432\\u0438\\u0440\\u043e\\u0432\\u0430\\u043b \\u0433\\u043b\\u043e\\u0431\\u0430\\u043b\\u044c\\u043d\\u044b\\u0439 \\u0431\\u0443\\u0441\\u0442\\u0435\\u0440");
    private static final long COMMAND_COOLDOWN_MS = 3_000L;

    private static long lastSentAt = 0L;

    private AutoThanks() {
    }

    public static void onIncomingMessage(Component component) {
        if (component == null || !ConfigManager.get().render.autoThanks) {
            return;
        }

        String text = Formatting.stripMinecraftFormatting(component.getString())
                .replace('\u00A0', ' ')
                .trim();

        if (!BOOSTER_MESSAGE_PATTERN.matcher(text).find()) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastSentAt < COMMAND_COOLDOWN_MS) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.player.connection == null) {
            return;
        }

        ChatDispatchQueue.enqueueCommand("thx", COMMAND_COOLDOWN_MS);
        lastSentAt = now;
    }
}
