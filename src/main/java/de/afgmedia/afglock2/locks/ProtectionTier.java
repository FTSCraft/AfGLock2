package de.afgmedia.afglock2.locks;

public enum ProtectionTier {
    STONE(1.0F),
    COPPER(0.25F),
    IRON(0.2F),
    DIAMOND(0.1F),
    EMERALD(0.05F);

    private final float chance;

    ProtectionTier(float chance) {
        this.chance = chance;
    }

    public float getChance() {
        return this.chance;
    }
}
