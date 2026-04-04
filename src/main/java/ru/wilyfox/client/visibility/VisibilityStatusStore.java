package ru.wilyfox.client.visibility;

public class VisibilityStatusStore {
    private boolean clanVisible = true;
    private boolean playersVisible = true;
    private PetsVisibility petsVisibility = PetsVisibility.ENABLED;

    public boolean isClanVisible() {
        return clanVisible;
    }

    public boolean isPlayersVisible() {
        return playersVisible;
    }

    public PetsVisibility getPetsVisibility() {
        return petsVisibility;
    }

    public void setClanVisible(boolean clanVisible) {
        this.clanVisible = clanVisible;
    }

    public void setPlayersVisible(boolean playersVisible) {
        this.playersVisible = playersVisible;
    }

    public void setPetsVisibility(PetsVisibility petsVisibility) {
        if (petsVisibility != null) {
            this.petsVisibility = petsVisibility;
        }
    }

    public enum PetsVisibility {
        ENABLED("Enabled"),
        ONLY_OWN("Only Own"),
        DISABLED("Disabled");

        private final String displayName;

        PetsVisibility(String displayName) {
            this.displayName = displayName;
        }

        public String displayName() {
            return displayName;
        }

        public String compactName() {
            return switch (this) {
                case ENABLED -> "On";
                case ONLY_OWN -> "Own";
                case DISABLED -> "Off";
            };
        }

        public static PetsVisibility fromName(String value) {
            if (value == null || value.isBlank()) {
                return ENABLED;
            }

            try {
                return PetsVisibility.valueOf(value.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                return ENABLED;
            }
        }
    }
}
