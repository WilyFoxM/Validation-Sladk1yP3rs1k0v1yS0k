package ru.wilyfox.client.utility;

import java.util.ArrayDeque;
import java.util.Deque;

public final class BlockBreakCounter {
    private static final Deque<Long> BREAK_TIMESTAMPS = new ArrayDeque<>();

    private BlockBreakCounter() {}

    public static void recordBreak() {
        long now = System.currentTimeMillis();
        BREAK_TIMESTAMPS.addLast(now);
        cleanup(now);
    }

    public static int getBreakPerSecond() {
        long now = System.currentTimeMillis();
        cleanup(now);
        return BREAK_TIMESTAMPS.size();
    }

    private static void cleanup(long now) {
        long cutoff = now - 1000L;

        while (!BREAK_TIMESTAMPS.isEmpty() && BREAK_TIMESTAMPS.peekFirst() < cutoff) {
            BREAK_TIMESTAMPS.removeFirst();
        }
    }
}
