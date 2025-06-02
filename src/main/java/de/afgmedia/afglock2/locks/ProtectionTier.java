package de.afgmedia.afglock2.locks;

public enum ProtectionTier {
    STONE(1.0F),
    COPPER(0.5F),
    IRON(0.25F),
    DIAMOND(0.2F),
    EMERALD(0.15F);

    private final float chance;

    ProtectionTier(float chance) {
        this.chance = chance;
    }

    public float getChance() {
        return this.chance;
    }
}
