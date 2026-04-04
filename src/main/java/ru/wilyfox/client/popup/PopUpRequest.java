package ru.wilyfox.client.popup;

public record PopUpRequest(
        String source,
        String title,
        String message,
        PopUpSeverity severity,
        Integer fadeInMs,
        Integer holdMs,
        Integer fadeOutMs
) {
    public static PopUpRequest of(String source, String title, String message, PopUpSeverity severity) {
        return new PopUpRequest(source, title, message, severity, null, null, null);
    }
}
