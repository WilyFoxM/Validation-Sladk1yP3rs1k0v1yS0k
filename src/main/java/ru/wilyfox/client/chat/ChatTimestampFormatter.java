package ru.wilyfox.client.chat;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public final class ChatTimestampFormatter {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final Pattern TIMESTAMP_PREFIX = Pattern.compile("^\\[\\d{2}:\\d{2}:\\d{2}]\\s*");
    private static final int TIMESTAMP_COLOR = 0xFF8C8C8C;

    private ChatTimestampFormatter() {
    }

    public static MutableComponent createTimestampPrefix() {
        String value = "[" + LocalTime.now().format(TIME_FORMAT) + "] ";
        return Component.literal(value).withColor(TIMESTAMP_COLOR);
    }

    public static String stripTimestampPrefix(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        return TIMESTAMP_PREFIX.matcher(text.stripLeading()).replaceFirst("");
    }
}
