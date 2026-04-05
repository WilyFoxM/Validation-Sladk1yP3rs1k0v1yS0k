package ru.wilyfox.client.discord;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

final class DiscordIpcClient implements AutoCloseable {
    private static final int OPCODE_HANDSHAKE = 0;
    private static final int OPCODE_FRAME = 1;
    private static final int OPCODE_CLOSE = 2;
    private static final int MIN_PIPE_INDEX = 0;
    private static final int MAX_PIPE_INDEX = 9;
    private static final int CONNECT_ATTEMPTS = 3;
    private static final long CONNECT_RETRY_DELAY_MS = 250L;

    private RandomAccessFile pipe;
    private String pipePath;

    void connect(String clientId) throws IOException {
        IOException bestFailure = null;
        for (int attempt = 0; attempt < CONNECT_ATTEMPTS; attempt++) {
            IOException attemptFailure = null;
            for (int index = MIN_PIPE_INDEX; index <= MAX_PIPE_INDEX; index++) {
                try {
                    pipe = openPipe(index);
                    writeFrame(OPCODE_HANDSHAKE, "{\"v\":1,\"client_id\":\"" + escapeJson(clientId) + "\"}");

                    Response response = readFrame();
                    if (response.opcode == OPCODE_CLOSE) {
                        throw new IOException("Discord closed IPC: " + describePayload(response.payload));
                    }

                    if (!response.payload.contains("\"evt\":\"READY\"")) {
                        throw new IOException("Unexpected handshake response: " + response.payload);
                    }

                    return;
                } catch (IOException exception) {
                    if (!isPipeNotFound(exception)) {
                        bestFailure = exception;
                    }
                    attemptFailure = exception;
                    closeQuietly();
                }
            }

            if (attempt + 1 < CONNECT_ATTEMPTS) {
                sleepBeforeRetry();
            }

            if (bestFailure == null) {
                bestFailure = attemptFailure;
            }
        }

        if (bestFailure != null) {
            throw bestFailure;
        }

        throw new IOException("Discord IPC pipe not found");
    }

    void setActivity(
            String details,
            String state,
            long startedAtSeconds,
            String largeImageKey,
            String largeImageText,
            String smallImageKey,
            String smallImageText
    ) throws IOException {
        ensureConnected();

        StringBuilder activity = new StringBuilder();
        activity.append('{');
        activity.append("\"details\":\"").append(escapeJson(details)).append('"');

        if (state != null && !state.isBlank()) {
            activity.append(",\"state\":\"").append(escapeJson(state)).append('"');
        }

        if (startedAtSeconds > 0L) {
            activity.append(",\"timestamps\":{\"start\":").append(startedAtSeconds).append('}');
        }

        if ((largeImageKey != null && !largeImageKey.isBlank())
                || (largeImageText != null && !largeImageText.isBlank())
                || (smallImageKey != null && !smallImageKey.isBlank())
                || (smallImageText != null && !smallImageText.isBlank())) {
            activity.append(",\"assets\":{");
            boolean hasPreviousField = false;
            if (largeImageKey != null && !largeImageKey.isBlank()) {
                activity.append("\"large_image\":\"").append(escapeJson(largeImageKey)).append('"');
                hasPreviousField = true;
            }
            if (largeImageText != null && !largeImageText.isBlank()) {
                if (hasPreviousField) {
                    activity.append(',');
                }
                activity.append("\"large_text\":\"").append(escapeJson(largeImageText)).append('"');
                hasPreviousField = true;
            }
            if (smallImageKey != null && !smallImageKey.isBlank()) {
                if (hasPreviousField) {
                    activity.append(',');
                }
                activity.append("\"small_image\":\"").append(escapeJson(smallImageKey)).append('"');
                hasPreviousField = true;
            }
            if (smallImageText != null && !smallImageText.isBlank()) {
                if (hasPreviousField) {
                    activity.append(',');
                }
                activity.append("\"small_text\":\"").append(escapeJson(smallImageText)).append('"');
            }
            activity.append('}');
        }

        activity.append('}');

        String payload = "{\"cmd\":\"SET_ACTIVITY\",\"args\":{\"pid\":" + ProcessHandle.current().pid()
                + ",\"activity\":" + activity + "},\"nonce\":\"" + UUID.randomUUID() + "\"}";
        writeFrame(OPCODE_FRAME, payload);
    }

    void clearActivity() throws IOException {
        ensureConnected();
        String payload = "{\"cmd\":\"SET_ACTIVITY\",\"args\":{\"pid\":" + ProcessHandle.current().pid()
                + ",\"activity\":null},\"nonce\":\"" + UUID.randomUUID() + "\"}";
        writeFrame(OPCODE_FRAME, payload);
    }

    String getPipePath() {
        return pipePath == null ? "" : pipePath;
    }

    @Override
    public void close() throws IOException {
        closeQuietly();
    }

    private void closeQuietly() throws IOException {
        if (pipe != null) {
            pipe.close();
            pipe = null;
        }
        pipePath = null;
    }

    private RandomAccessFile openPipe(int index) throws IOException {
        String candidatePath = "\\\\.\\pipe\\discord-ipc-" + index;
        try {
            RandomAccessFile candidate = new RandomAccessFile(candidatePath, "rw");
            pipePath = candidatePath;
            return candidate;
        } catch (FileNotFoundException exception) {
            throw new IOException("Discord IPC pipe not found: " + candidatePath, exception);
        }
    }

    private void sleepBeforeRetry() {
        try {
            Thread.sleep(CONNECT_RETRY_DELAY_MS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    private static boolean isPipeNotFound(IOException exception) {
        if (exception instanceof FileNotFoundException) {
            return true;
        }

        Throwable cause = exception.getCause();
        return cause instanceof FileNotFoundException;
    }

    private void ensureConnected() throws IOException {
        if (pipe == null) {
            throw new IOException("Discord IPC is not connected");
        }
    }

    private void writeFrame(int opcode, String payload) throws IOException {
        ensureConnected();

        byte[] payloadBytes = payload.getBytes(StandardCharsets.UTF_8);
        pipe.write(intToLittleEndian(opcode));
        pipe.write(intToLittleEndian(payloadBytes.length));
        pipe.write(payloadBytes);
    }

    private Response readFrame() throws IOException {
        ensureConnected();

        byte[] header = new byte[8];
        pipe.readFully(header);

        int opcode = littleEndianToInt(header, 0);
        int length = littleEndianToInt(header, 4);
        if (length < 0) {
            throw new EOFException("Negative Discord IPC payload length");
        }

        byte[] payload = new byte[length];
        pipe.readFully(payload);
        return new Response(opcode, new String(payload, StandardCharsets.UTF_8));
    }

    private static byte[] intToLittleEndian(int value) {
        return new byte[] {
                (byte) (value & 0xFF),
                (byte) ((value >> 8) & 0xFF),
                (byte) ((value >> 16) & 0xFF),
                (byte) ((value >> 24) & 0xFF)
        };
    }

    private static int littleEndianToInt(byte[] bytes, int offset) {
        return (bytes[offset] & 0xFF)
                | ((bytes[offset + 1] & 0xFF) << 8)
                | ((bytes[offset + 2] & 0xFF) << 16)
                | ((bytes[offset + 3] & 0xFF) << 24);
    }

    private static String describePayload(String payload) {
        String message = extractJsonString(payload, "message");
        if (!message.isBlank()) {
            String code = extractJsonNumber(payload, "code");
            return code.isBlank() ? message : code + ": " + message;
        }

        return payload;
    }

    private static String extractJsonString(String json, String key) {
        String pattern = "\"" + key + "\":\"";
        int start = json.indexOf(pattern);
        if (start < 0) {
            return "";
        }

        int index = start + pattern.length();
        StringBuilder builder = new StringBuilder();
        boolean escaping = false;
        while (index < json.length()) {
            char current = json.charAt(index++);
            if (escaping) {
                builder.append(current);
                escaping = false;
                continue;
            }
            if (current == '\\') {
                escaping = true;
                continue;
            }
            if (current == '"') {
                break;
            }
            builder.append(current);
        }

        return builder.toString();
    }

    private static String extractJsonNumber(String json, String key) {
        String pattern = "\"" + key + "\":";
        int start = json.indexOf(pattern);
        if (start < 0) {
            return "";
        }

        int index = start + pattern.length();
        StringBuilder builder = new StringBuilder();
        while (index < json.length()) {
            char current = json.charAt(index++);
            if ((current >= '0' && current <= '9') || current == '-') {
                builder.append(current);
                continue;
            }
            if (builder.length() > 0) {
                break;
            }
        }

        return builder.toString();
    }

    private static String escapeJson(String value) {
        StringBuilder builder = new StringBuilder(value.length() + 8);
        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);
            switch (current) {
                case '\\' -> builder.append("\\\\");
                case '"' -> builder.append("\\\"");
                case '\b' -> builder.append("\\b");
                case '\f' -> builder.append("\\f");
                case '\n' -> builder.append("\\n");
                case '\r' -> builder.append("\\r");
                case '\t' -> builder.append("\\t");
                default -> {
                    if (current < 0x20) {
                        builder.append(String.format("\\u%04x", (int) current));
                    } else {
                        builder.append(current);
                    }
                }
            }
        }
        return builder.toString();
    }

    private record Response(int opcode, String payload) {
    }
}
