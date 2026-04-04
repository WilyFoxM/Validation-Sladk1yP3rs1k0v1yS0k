package ru.wilyfox.client.boss;

public class BossDamageStore {
    private BossDamageInfo current;

    public void update(BossDamageInfo info) {
        current = info;
    }

    public BossDamageInfo getCurrent() {
        return current;
    }

    public boolean hasActiveEntry() {
        return getCurrent() != null;
    }

    public void clear() {
        current = null;
    }
}
