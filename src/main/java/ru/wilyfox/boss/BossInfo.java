package ru.wilyfox.boss;

public class BossInfo {
    private final String name;
    private long respawnAt;
    private final int level;

    public BossInfo(String n, long r, int l) {
        this.name = n;
        this.respawnAt = r;
        this.level = l;
    }

    public String getName() {
        return name;
    }

    public long getRespawnAt() {
        return respawnAt;
    }

    public int getLevel() {
        return level;
    }

    public void setRespawnAt(long respawnAt) {
        this.respawnAt = respawnAt;
    }
}
