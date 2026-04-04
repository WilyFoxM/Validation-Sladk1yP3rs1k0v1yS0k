package ru.wilyfox.client.chat;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import ru.wilyfox.client.ping.PingPayload;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FrogChatProtocol {
    private static final String CLAN_PREFIX = "@";
    private static final String PING_PREFIX = "{fhping:";
    private static final String TOKEN_SUFFIX = "}";
    private static final Pattern SINGLE_PART_PATTERN = Pattern.compile("\\{fhping:([A-Za-z0-9_-]+)}");
    private static final Pattern MULTI_PART_PATTERN = Pattern.compile("\\{fhping:([A-Za-z0-9_-]+):([1-9]\\d*):([1-9]\\d*):([A-Za-z0-9_-]+)}");
    private static final int MAX_CHAT_LENGTH = 240;
    private static final long SEND_INTERVAL_MS = 5_000L;

    private static final Map<String, IncomingPingBuffer> incomingBuffers = new HashMap<>();
    private static final List<Consumer<String>> pingListeners = new ArrayList<>();

    private static boolean initialized = false;
    private FrogChatProtocol() {
    }

    public static void init() {
        if (initialized) {
            return;
        }

        initialized = true;

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            incomingBuffers.clear();
        });
    }

    public static void registerPingListener(Consumer<String> listener) {
        if (listener != null) {
            pingListeners.add(listener);
        }
    }

    public static void sendPingPayload(String rawPayload) {
        String payload = encodePayload(rawPayload);
        if (payload == null || payload.isBlank()) {
            return;
        }

        List<String> tokens = splitPayload(payload);
        if (tokens.isEmpty()) {
            return;
        }

        for (String token : tokens) {
            ChatDispatchQueue.enqueueChat(CLAN_PREFIX + token, SEND_INTERVAL_MS);
        }

        if (tokens.size() == 1) {
            showLocalMessage("Метка поставлена.");
        } else {
            showLocalMessage("Метка поставлена (" + tokens.size() + " ч.).");
        }
    }

    public static boolean handleIncomingProtocol(Component component) {
        if (component == null) {
            return false;
        }

        String text = component.getString();
        if (text == null || !text.contains(PING_PREFIX)) {
            return false;
        }

        Matcher multiMatcher = MULTI_PART_PATTERN.matcher(text);
        if (multiMatcher.find()) {
            String sender = extractSender(text, multiMatcher.start());
            String messageId = multiMatcher.group(1);
            int partIndex = Integer.parseInt(multiMatcher.group(2));
            int totalParts = Integer.parseInt(multiMatcher.group(3));
            String payloadPart = multiMatcher.group(4);

            String bufferKey = normalizeSender(sender) + ":" + messageId;
            IncomingPingBuffer buffer = incomingBuffers.computeIfAbsent(
                    bufferKey,
                    ignored -> new IncomingPingBuffer(totalParts)
            );
            buffer.put(partIndex, payloadPart);

            if (!buffer.isComplete()) {
                return true;
            }

            incomingBuffers.remove(bufferKey);
            dispatchPingPayload(decodePayload(buffer.join()), sender);
            return true;
        }

        Matcher singleMatcher = SINGLE_PART_PATTERN.matcher(text);
        if (!singleMatcher.find()) {
            return false;
        }

        dispatchPingPayload(decodePayload(singleMatcher.group(1)), extractSender(text, singleMatcher.start()));
        return true;
    }

    private static void dispatchPingPayload(String payload, String sender) {
        if (payload == null || payload.isBlank()) {
            return;
        }

        PingPayload pingPayload = PingPayload.fromJson(payload);
        String effectiveSender = resolveSender(sender, pingPayload);
        if (!isOwnSender(effectiveSender)) {
            showIncomingMessage(effectiveSender, pingPayload);
        }

        for (Consumer<String> listener : List.copyOf(pingListeners)) {
            listener.accept(payload);
        }
    }

    private static List<String> splitPayload(String payload) {
        int singlePartLimit = MAX_CHAT_LENGTH - CLAN_PREFIX.length() - PING_PREFIX.length() - TOKEN_SUFFIX.length();
        if (payload.length() <= singlePartLimit) {
            return List.of(PING_PREFIX + payload + TOKEN_SUFFIX);
        }

        String messageId = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        int multipartOverhead = CLAN_PREFIX.length()
                + PING_PREFIX.length()
                + messageId.length()
                + ":1:1:".length()
                + TOKEN_SUFFIX.length();
        int partPayloadLimit = MAX_CHAT_LENGTH - multipartOverhead;
        if (partPayloadLimit <= 0) {
            return List.of();
        }

        int totalParts = (payload.length() + partPayloadLimit - 1) / partPayloadLimit;
        List<String> tokens = new ArrayList<>(totalParts);

        for (int part = 0; part < totalParts; part++) {
            int from = part * partPayloadLimit;
            int to = Math.min(payload.length(), from + partPayloadLimit);
            tokens.add(PING_PREFIX + messageId + ":" + (part + 1) + ":" + totalParts + ":" + payload.substring(from, to) + TOKEN_SUFFIX);
        }

        return tokens;
    }

    private static String encodePayload(String rawPayload) {
        if (rawPayload == null || rawPayload.isBlank()) {
            return null;
        }

        return Base64.getUrlEncoder().withoutPadding().encodeToString(rawPayload.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private static String decodePayload(String encodedPayload) {
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(encodedPayload);
            return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static String extractSender(String text, int tokenIndex) {
        if (tokenIndex <= 0) {
            return null;
        }

        String prefix = text.substring(0, tokenIndex).trim();
        int pipeIndex = prefix.indexOf('|');
        if (pipeIndex >= 0) {
            prefix = prefix.substring(pipeIndex + 1).trim();
        }

        prefix = prefix.replace("В» РЇ", "")
                .replace("РЇ:", "")
                .replace(":", "")
                .trim();

        String[] parts = prefix.split("\\s+");
        for (int i = parts.length - 1; i >= 0; i--) {
            String candidate = parts[i].replaceAll("[^A-Za-z0-9_]", "");
            if (!candidate.isBlank()) {
                return candidate;
            }
        }

        return null;
    }

    private static String normalizeSender(String sender) {
        if (sender == null || sender.isBlank()) {
            return "unknown";
        }

        return sender.toLowerCase(Locale.ROOT);
    }

    private static boolean isOwnSender(String sender) {
        if (sender == null || sender.isBlank()) {
            return false;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return false;
        }

        return sender.equalsIgnoreCase(minecraft.player.getGameProfile().getName());
    }

    private static String resolveSender(String fallbackSender, PingPayload payload) {
        if (payload != null && payload.author() != null && !payload.author().isBlank()) {
            return payload.author();
        }

        return fallbackSender;
    }

    private static void showIncomingMessage(String sender, PingPayload payload) {
        String message = sender == null || sender.isBlank()
                ? "Получена метка."
                : "Получена метка от " + sender + ".";

        if (payload != null && payload.location() != null && !payload.location().isBlank()) {
            message += " Локация: " + payload.location();
        }

        showLocalMessage(message);
    }

    private static void showLocalMessage(String message) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gui != null) {
            minecraft.gui.getChat().addMessage(Component.literal(message));
        }
    }

    private static final class IncomingPingBuffer {
        private final String[] parts;

        private IncomingPingBuffer(int totalParts) {
            this.parts = new String[Math.max(1, totalParts)];
        }

        private void put(int partIndex, String payloadPart) {
            if (partIndex <= 0 || partIndex > parts.length) {
                return;
            }

            parts[partIndex - 1] = payloadPart;
        }

        private boolean isComplete() {
            for (String part : parts) {
                if (part == null) {
                    return false;
                }
            }

            return true;
        }

        private String join() {
            StringBuilder builder = new StringBuilder();
            for (String part : parts) {
                builder.append(part);
            }
            return builder.toString();
        }
    }
}
