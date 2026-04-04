package ru.wilyfox.client.combo;

public final class ComboProgressStore {
    private volatile Snapshot snapshot = Snapshot.empty();

    public void updateCombo(double booster, double nextBooster, int blocks, int requiredBlocks) {
        snapshot = new Snapshot(
                true,
                Math.max(1.0D, booster),
                Math.max(nextBooster, booster),
                Math.max(0, blocks),
                Math.max(0, requiredBlocks)
        );
    }

    public void updateBlocks(int blocks) {
        Snapshot current = snapshot;
        if (!current.available()) {
            snapshot = new Snapshot(true, 1.0D, 1.1D, Math.max(0, blocks), 1000);
            return;
        }

        snapshot = new Snapshot(
                true,
                current.booster(),
                current.nextBooster(),
                Math.max(0, blocks),
                current.requiredBlocks()
        );
    }

    public Snapshot getSnapshot() {
        return snapshot;
    }

    public void clear() {
        snapshot = Snapshot.empty();
    }

    public record Snapshot(
            boolean available,
            double booster,
            double nextBooster,
            int blocks,
            int requiredBlocks
    ) {
        private static final double EPSILON = 0.0001D;

        public static Snapshot empty() {
            return new Snapshot(false, 1.0D, 1.1D, 0, 1000);
        }

        public double progress() {
            if (requiredBlocks <= 0) {
                return 0.0D;
            }
            return Math.min(1.0D, blocks / (double) requiredBlocks);
        }

        public boolean maxed() {
            return nextBooster <= booster + EPSILON;
        }
    }
}
