package ru.wilyfox.client.rune;

import java.util.ArrayList;
import java.util.List;

public class ActiveRunesStore {
    private List<String> runes = new ArrayList<>();

    public void replace(List<String> updatedRunes) {
        this.runes = new ArrayList<>(updatedRunes);
    }

    public List<String> getAll() {
        return List.copyOf(runes);
    }

    public boolean isEmpty() {
        return runes.isEmpty();
    }

    public void clear() {
        runes.clear();
    }
}
