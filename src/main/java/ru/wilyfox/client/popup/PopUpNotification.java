package ru.wilyfox.client.popup;

public record PopUpNotification(
        String source,
        String title,
        String message,
        PopUpSeverity severity,
        long createdAtMs,
        int fadeInMs,
        int holdMs,
        int fadeOutMs
) {
    public long expiresAtMs() {
        return createdAtMs + fadeInMs + holdMs + fadeOutMs;
    }
}
