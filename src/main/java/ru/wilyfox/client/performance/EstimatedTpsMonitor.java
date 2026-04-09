package ru.wilyfox.client.performance;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import ru.wilyfox.client.hud.config.ConfigManager;
import ru.wilyfox.client.profiler.ModProfiler;

import java.util.Arrays;

public final class EstimatedTpsMonitor {
    private static final Object LOCK = new Object();
    private static final int SAMPLE_CAPACITY = 2048;
    private static final int MIN_SAMPLES_FOR_ONE_PERCENT_LOW = 100;
    private static final int MIN_SAMPLES_FOR_POINT_ONE_PERCENT_LOW = 1000;
    private static final long BURST_GAP_NANOS = 16_000_000L;
    private static final long MIN_SAMPLE_INTERVAL_NANOS = 20_000_000L;
    private static final long MAX_SAMPLE_INTERVAL_NANOS = 500_000_000L;
    private static final long STALE_TIMEOUT_NANOS = 2_000_000_000L;
    private static final double MAX_TPS = 20.0D;

    private static boolean registered;
    private static final double[] samples = new double[SAMPLE_CAPACITY];
    private static int sampleCount;
    private static int sampleCursor;
    private static long lastPacketNanos;
    private static long lastBurstNanos;
    private static long lastSampleNanos;
    private static double smoothedTps = MAX_TPS;
    private static Snapshot cachedSnapshot = Snapshot.disabled();

    private EstimatedTpsMonitor() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        ClientTickEvents.END_CLIENT_TICK.register(EstimatedTpsMonitor::onClientTick);
    }

    public static void onClientboundPacket(Packet<?> packet) {
        if (!isMonitoringEnabled()) {
            return;
        }

        long now = System.nanoTime();
        synchronized (LOCK) {
            if (lastPacketNanos != 0L && now - lastPacketNanos <= BURST_GAP_NANOS) {
                lastPacketNanos = now;
                return;
            }

            if (lastBurstNanos != 0L) {
                long interval = now - lastBurstNanos;
                if (interval >= MIN_SAMPLE_INTERVAL_NANOS && interval <= MAX_SAMPLE_INTERVAL_NANOS) {
                    double instantTps = Math.min(MAX_TPS, 1_000_000_000.0D / interval);
                    appendSample(instantTps, now);
                }
            }

            lastBurstNanos = now;
            lastPacketNanos = now;
        }
    }

    public static Snapshot getSnapshot() {
        synchronized (LOCK) {
            return cachedSnapshot;
        }
    }

    private static void onClientTick(Minecraft client) {
        try (ModProfiler.Scope ignored = ModProfiler.getInstance().scope("tick/EstimatedTpsMonitor")) {
            if (!isMonitoringEnabled()) {
                reset();
                return;
            }

            if (client.player == null || client.level == null || client.getConnection() == null) {
                clearSamples();
                return;
            }

            recomputeSnapshot();
        }
    }

    private static boolean isMonitoringEnabled() {
        return ConfigManager.get() != null
                && ConfigManager.get().estimatedTps != null
                && ConfigManager.get().estimatedTps.enabled;
    }

    private static void appendSample(double tps, long now) {
        samples[sampleCursor] = tps;
        sampleCursor = (sampleCursor + 1) % SAMPLE_CAPACITY;
        if (sampleCount < SAMPLE_CAPACITY) {
            sampleCount++;
        }
        lastSampleNanos = now;
        smoothedTps = sampleCount == 1 ? tps : (smoothedTps * 0.82D + tps * 0.18D);
    }

    private static void recomputeSnapshot() {
        synchronized (LOCK) {
            if (sampleCount <= 0) {
                cachedSnapshot = Snapshot.waiting(true, 0);
                return;
            }

            long ageNanos = lastSampleNanos == 0L ? Long.MAX_VALUE : System.nanoTime() - lastSampleNanos;
            if (ageNanos > STALE_TIMEOUT_NANOS) {
                cachedSnapshot = Snapshot.waiting(true, sampleCount);
                return;
            }

            double[] sorted = copySamplesAscending();
            Double onePercentLow = sampleCount >= MIN_SAMPLES_FOR_ONE_PERCENT_LOW
                    ? computeLowAverage(sorted, 0.01D)
                    : null;
            Double pointOnePercentLow = sampleCount >= MIN_SAMPLES_FOR_POINT_ONE_PERCENT_LOW
                    ? computeLowAverage(sorted, 0.001D)
                    : null;

            cachedSnapshot = new Snapshot(
                    true,
                    true,
                    round2(smoothedTps),
                    onePercentLow != null ? round2(onePercentLow) : null,
                    pointOnePercentLow != null ? round2(pointOnePercentLow) : null,
                    sampleCount
            );
        }
    }

    private static double[] copySamplesAscending() {
        double[] copy = new double[sampleCount];
        if (sampleCount < SAMPLE_CAPACITY) {
            System.arraycopy(samples, 0, copy, 0, sampleCount);
        } else {
            int tailLength = SAMPLE_CAPACITY - sampleCursor;
            System.arraycopy(samples, sampleCursor, copy, 0, tailLength);
            System.arraycopy(samples, 0, copy, tailLength, sampleCursor);
        }
        Arrays.sort(copy);
        return copy;
    }

    private static double computeLowAverage(double[] sortedAscending, double fraction) {
        int bucketSize = Math.max(1, (int) Math.floor(sortedAscending.length * fraction));
        double total = 0.0D;
        for (int index = 0; index < bucketSize; index++) {
            total += sortedAscending[index];
        }
        return total / bucketSize;
    }

    private static double round2(double value) {
        return Math.round(value * 100.0D) / 100.0D;
    }

    private static void reset() {
        synchronized (LOCK) {
            clearSamplesLocked();
            cachedSnapshot = Snapshot.disabled();
        }
    }

    private static void clearSamples() {
        synchronized (LOCK) {
            clearSamplesLocked();
            cachedSnapshot = Snapshot.waiting(true, 0);
        }
    }

    private static void clearSamplesLocked() {
        sampleCount = 0;
        sampleCursor = 0;
        lastPacketNanos = 0L;
        lastBurstNanos = 0L;
        lastSampleNanos = 0L;
        smoothedTps = MAX_TPS;
    }

    public record Snapshot(
            boolean enabled,
            boolean available,
            double currentTps,
            Double onePercentLow,
            Double pointOnePercentLow,
            int sampleCount
    ) {
        public static Snapshot disabled() {
            return new Snapshot(false, false, 0.0D, null, null, 0);
        }

        public static Snapshot waiting(boolean enabled, int sampleCount) {
            return new Snapshot(enabled, false, 0.0D, null, null, sampleCount);
        }
    }
}
