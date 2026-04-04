package ru.wilyfox.utils;

import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Formatting {
    private static final Pattern COLON_TIME = Pattern.compile("(\\d{1,2}):(\\d{2})(?::(\\d{2}))?");
    private static final Pattern WORD_TIME = Pattern.compile(
            "(\\d+)\\s*(ч|час|часа|часов|h|hr|hrs|мин|min|m|сек|с|sec|secs|seconds)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );

    private Formatting() {
    }

    public static String sanitize(String text) {
        String stripped = stripMinecraftFormatting(text);
        if (stripped.isBlank()) {
            return "";
        }

        return stripped
                .replace("\r", "")
                .replace("\n", "")
                .replace('\u00A0', ' ')
                .replaceAll("[^\\p{L}\\p{N} ]+", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    public static String stripMinecraftFormatting(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        String normalized = text
                .replace("Р’В§", "§")
                .replace("В§", "§")
                .replace("Â§", "§");

        StringBuilder result = new StringBuilder(normalized.length());

        for (int i = 0; i < normalized.length(); i++) {
            char current = normalized.charAt(i);

            if (current == '§') {
                if (i + 1 < normalized.length()) {
                    char next = normalized.charAt(i + 1);

                    if (next == 'x' || next == 'X') {
                        i++;

                        for (int hexIndex = 0; hexIndex < 6 && i + 1 < normalized.length(); hexIndex++) {
                            if (normalized.charAt(i + 1) == '§') {
                                i += 2;
                            } else {
                                break;
                            }
                        }
                    } else {
                        i++;
                    }
                }

                continue;
            }

            result.append(current);
        }

        return result.toString();
    }

    public static long parseTimeToMillis(String text) {
        if (text == null || text.isBlank()) {
            return -1L;
        }

        String clean = stripMinecraftFormatting(text).replace('\u00A0', ' ');

        Matcher colonMatcher = COLON_TIME.matcher(clean);
        if (colonMatcher.find()) {
            long first = Long.parseLong(colonMatcher.group(1));
            long second = Long.parseLong(colonMatcher.group(2));
            String thirdGroup = colonMatcher.group(3);

            if (thirdGroup != null) {
                long third = Long.parseLong(thirdGroup);
                return first * 3_600_000L + second * 60_000L + third * 1000L;
            }

            return first * 60_000L + second * 1000L;
        }

        Matcher matcher = WORD_TIME.matcher(clean);
        long totalMillis = 0L;
        boolean found = false;

        while (matcher.find()) {
            found = true;

            long value = Long.parseLong(matcher.group(1));
            String unit = matcher.group(2).toLowerCase();

            if (unit.startsWith("ч") || unit.startsWith("час") || unit.startsWith("h")) {
                totalMillis += value * 3_600_000L;
            } else if (unit.startsWith("мин") || unit.equals("m") || unit.equals("min")) {
                totalMillis += value * 60_000L;
            } else if (unit.startsWith("сек") || unit.equals("с") || unit.startsWith("sec")) {
                totalMillis += value * 1000L;
            }
        }

        return found ? totalMillis : -1L;
    }

    public static String formatMillis(long millis) {
        long diff = millis - Instant.now().toEpochMilli();
        if (diff < 0L) {
            diff = 0L;
        }

        long totalSeconds = diff / 1000L;
        long hours = totalSeconds / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        long seconds = totalSeconds % 60L;

        if (hours > 0L) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }

        return String.format("%02d:%02d", minutes, seconds);
    }

    public static String formatMillisSigned(long millis) {
        long diff = millis - Instant.now().toEpochMilli();
        boolean negative = diff < 0L;
        long absolute = Math.abs(diff);

        long totalSeconds = absolute / 1000L;
        long hours = totalSeconds / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        long seconds = totalSeconds % 60L;

        String formatted = hours > 0L
                ? String.format("%02d:%02d:%02d", hours, minutes, seconds)
                : String.format("%02d:%02d", minutes, seconds);

        return negative ? "-" + formatted : formatted;
    }
}
