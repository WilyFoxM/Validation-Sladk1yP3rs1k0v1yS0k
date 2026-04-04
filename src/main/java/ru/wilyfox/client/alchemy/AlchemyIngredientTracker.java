package ru.wilyfox.client.alchemy;

import net.minecraft.world.phys.Vec3;
import ru.wilyfox.client.hud.config.ConfigManager;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public final class AlchemyIngredientTracker {
    private static final AlchemyIngredientTracker INSTANCE = new AlchemyIngredientTracker();
    private static final long LIFETIME_MS = 1500L;
    private static final double GRID_SIZE = 0.5;
    private static final int MIN_PARTICLES_PER_SPOT = 3;

    private final List<AlchemyParticleEntry> particles = new ArrayList<>();

    private AlchemyIngredientTracker() {
    }

    public static AlchemyIngredientTracker getInstance() {
        return INSTANCE;
    }

    public void addParticle(double x, double y, double z) {
        if (!ConfigManager.get().render.showAlchemyIngredientMarkers) {
            clear();
            return;
        }

        long now = System.currentTimeMillis();
        particles.add(new AlchemyParticleEntry(new Vec3(x, y, z), now));
        cleanup(now);
    }

    public List<AlchemyIngredientSpot> getActiveSpots() {
        if (!ConfigManager.get().render.showAlchemyIngredientMarkers) {
            clear();
            return List.of();
        }

        cleanup(System.currentTimeMillis());

        Map<GridPos, CellData> cells = new HashMap<>();
        for (AlchemyParticleEntry particle : particles) {
            GridPos cell = GridPos.fromVec(particle.position(), GRID_SIZE);
            cells.computeIfAbsent(cell, ignored -> new CellData()).add(particle.position(), particle.timestamp());
        }

        List<AlchemyIngredientSpot> result = new ArrayList<>();
        Set<GridPos> visited = new HashSet<>();

        for (GridPos start : cells.keySet()) {
            if (visited.contains(start)) {
                continue;
            }

            Queue<GridPos> queue = new ArrayDeque<>();
            queue.add(start);
            visited.add(start);

            int totalCount = 0;
            long latestTimestamp = 0L;
            double sumX = 0.0;
            double sumY = 0.0;
            double sumZ = 0.0;

            while (!queue.isEmpty()) {
                GridPos current = queue.poll();
                CellData data = cells.get(current);
                if (data == null) {
                    continue;
                }

                totalCount += data.count;
                latestTimestamp = Math.max(latestTimestamp, data.latestTimestamp);
                sumX += data.sumX;
                sumY += data.sumY;
                sumZ += data.sumZ;

                for (GridPos neighbor : current.neighbors()) {
                    if (cells.containsKey(neighbor) && visited.add(neighbor)) {
                        queue.add(neighbor);
                    }
                }
            }

            if (totalCount < MIN_PARTICLES_PER_SPOT) {
                continue;
            }

            result.add(new AlchemyIngredientSpot(
                    new Vec3(sumX / totalCount, sumY / totalCount, sumZ / totalCount),
                    totalCount,
                    latestTimestamp
            ));
        }

        return result;
    }

    public void clear() {
        particles.clear();
    }

    private void cleanup(long now) {
        Iterator<AlchemyParticleEntry> iterator = particles.iterator();
        while (iterator.hasNext()) {
            AlchemyParticleEntry entry = iterator.next();
            if (now - entry.timestamp() > LIFETIME_MS) {
                iterator.remove();
            }
        }
    }

    private static final class CellData {
        private int count;
        private long latestTimestamp;
        private double sumX;
        private double sumY;
        private double sumZ;

        private void add(Vec3 pos, long timestamp) {
            count++;
            latestTimestamp = Math.max(latestTimestamp, timestamp);
            sumX += pos.x;
            sumY += pos.y;
            sumZ += pos.z;
        }
    }

    private record GridPos(int x, int y, int z) {
        static GridPos fromVec(Vec3 pos, double grid) {
            return new GridPos(
                    (int) Math.floor(pos.x / grid),
                    (int) Math.floor(pos.y / grid),
                    (int) Math.floor(pos.z / grid)
            );
        }

        List<GridPos> neighbors() {
            List<GridPos> result = new ArrayList<>(26);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) {
                            continue;
                        }

                        result.add(new GridPos(x + dx, y + dy, z + dz));
                    }
                }
            }
            return result;
        }
    }
}
