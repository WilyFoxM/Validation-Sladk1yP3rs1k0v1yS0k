package ru.wilyfox.client.pet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ActivePetsStore {
    private List<ActivePetInfo> pets = new ArrayList<>();

    public void replace(List<ActivePetInfo> updatedPets) {
        this.pets = new ArrayList<>(updatedPets);
    }

    public void merge(List<ActivePetInfo> updatedPets) {
        if (updatedPets.isEmpty()) {
            return;
        }

        Map<String, ActivePetInfo> merged = new LinkedHashMap<>();
        for (ActivePetInfo pet : pets) {
            merged.put(pet.id(), pet);
        }
        for (ActivePetInfo pet : updatedPets) {
            merged.put(pet.id(), pet);
        }

        pets = new ArrayList<>(merged.values());
    }

    public List<ActivePetInfo> getAll() {
        return List.copyOf(pets);
    }

    public boolean isEmpty() {
        return pets.isEmpty();
    }

    public void clear() {
        pets.clear();
    }
}
