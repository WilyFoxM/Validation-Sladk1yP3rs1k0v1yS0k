package ru.wilyfox.client.chat;

import net.minecraft.network.chat.Component;
import ru.wilyfox.client.popup.PopUpManager;
import ru.wilyfox.client.popup.PopUpRequest;
import ru.wilyfox.client.popup.PopUpSeverity;
import ru.wilyfox.client.popup.PopUpSource;
import ru.wilyfox.utils.Formatting;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PrivateMessagePopUpNotifier {
    private static final Pattern CHAT_SPLIT = Pattern.compile("^([^:]{1,32})(:\\s+)(.+)$");
    private static final int PREVIEW_LIMIT = 36;

    private PrivateMessagePopUpNotifier() {
    }

    public static void onIncomingMessage(Component component) {
        if (component == null) {
            return;
        }

        String raw = Formatting.stripMinecraftFormatting(component.getString()).replace('\u00A0', ' ');
        ChatTab tab = ChatPrefixRouter.resolve(raw);
        if (tab != ChatTab.PRIVATE) {
            return;
        }

        String message = extractPreview(raw, tab);
        if (message.isBlank()) {
            return;
        }

        PopUpManager.getInstance().publish(PopUpRequest.of(
                PopUpSource.PRIVATE_MESSAGE,
                "Private Message",
                message,
                PopUpSeverity.INFO
        ));
    }

    private static String extractPreview(String raw, ChatTab tab) {
        String body = ChatPrefixRouter.stripKnownPrefix(raw, tab)
                .replace('\r', ' ')
                .replace('\n', ' ')
                .replaceAll("\\s+", " ")
                .trim();

        Matcher matcher = CHAT_SPLIT.matcher(body);
        if (matcher.matches()) {
            body = matcher.group(3).trim();
        }

        if (body.length() <= PREVIEW_LIMIT) {
            return body;
        }

        return body.substring(0, PREVIEW_LIMIT - 3).trim() + "...";
    }
}
