package ru.wilyfox.client.chat;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import ru.wilyfox.client.hud.config.ConfigManager;

public final class ChatMessageDecorator {
    private ChatMessageDecorator() {
    }

    public static Component decorate(Component component) {
        if (component == null) {
            return Component.empty();
        }

        Component result = component;
        if (ConfigManager.get().render.toneDownChat) {
            result = ChatToneDownFormatter.format(result);
        }

        if (ConfigManager.get().render.chatTimestamps) {
            result = prependTimestamp(result);
        }

        return result;
    }

    private static Component prependTimestamp(Component component) {
        MutableComponent prefixed = Component.empty();
        prefixed.append(ChatTimestampFormatter.createTimestampPrefix());
        prefixed.append(component.copy());
        return prefixed;
    }
}
