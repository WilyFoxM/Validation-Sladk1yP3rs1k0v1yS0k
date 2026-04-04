package ru.wilyfox.client.rune;

public final class RuneSetCooldownStore {
    private static long remainingMillis = 0L;
    private static long totalMillis = 0L;
    private static long updatedAt = 0L;

    private RuneSetCooldownStore() {
    }

    public static void update(long newRemainingMillis) {
        long clamped = Math.max(0L, newRemainingMillis);
        long now = System.currentTimeMillis();
        long previousRemaining = getRemainingMillis();

        if (clamped <= 0L) {
            clear();
            return;
        }

        if (previousRemaining <= 0L || clamped > previousRemaining + 1_500L) {
            totalMillis = clamped;
        } else {
            totalMillis = Math.max(totalMillis, clamped);
        }

        remainingMillis = clamped;
        updatedAt = now;
    }

    public static long getRemainingMillis() {
        if (remainingMillis <= 0L || updatedAt <= 0L) {
            return 0L;
        }

        long elapsed = System.currentTimeMillis() - updatedAt;
        return Math.max(0L, remainingMillis - elapsed);
    }

    public static float getProgress() {
        long current = getRemainingMillis();
        if (current <= 0L || totalMillis <= 0L) {
            return 0.0F;
        }

        return Math.max(0.0F, Math.min(1.0F, current / (float) totalMillis));
    }

    public static boolean isActive() {
        return getRemainingMillis() > 0L;
    }

    public static void clear() {
        remainingMillis = 0L;
        totalMillis = 0L;
        updatedAt = 0L;
    }
}
