package ru.wilyfox.client.miner;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ActiveMinersStore {
    private List<ActiveMinerInfo> miners = new ArrayList<>();

    public void replace(List<ActiveMinerInfo> updatedMiners) {
        miners = new ArrayList<>(updatedMiners);
    }

    public void merge(List<ActiveMinerInfo> updatedMiners) {
        if (updatedMiners.isEmpty()) {
            return;
        }

        Map<String, ActiveMinerInfo> merged = new LinkedHashMap<>();
        for (ActiveMinerInfo miner : miners) {
            merged.put(miner.resource(), miner);
        }
        for (ActiveMinerInfo miner : updatedMiners) {
            merged.put(miner.resource(), miner);
        }

        miners = new ArrayList<>(merged.values());
    }

    public List<ActiveMinerInfo> getAll() {
        return List.copyOf(miners);
    }

    public boolean isEmpty() {
        return miners.isEmpty();
    }

    public void clear() {
        miners.clear();
    }
}
