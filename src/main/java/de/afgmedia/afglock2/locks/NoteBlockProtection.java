package de.afgmedia.afglock2.locks;

import de.afgmedia.afglock2.locks.settings.AllowSetting;
import de.afgmedia.afglock2.main.AfGLock;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NoteBlockProtection extends Protection{
    private final int id;
    private final UUID owner;
    private final Location location;
    private final int protectionTier;
    private final List<AllowSetting> allowSettings;

    public NoteBlockProtection(AfGLock instance, UUID owner, int id, Location location, int tier) {
        super(instance);
        this.protectionTier = tier;
        this.id = id;
        this.owner = owner;
        this.location = location;
        this.allowSettings = new ArrayList<>();

    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public UUID getOwner() {
        return owner;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public ProtectionType getProtectionType() {
        return ProtectionType.NOTE_BLOCK;
    }

    @Override
    public List<AllowSetting> getAllowSettings() {
        return allowSettings;
    }

    @Override
    public int getProtectionTier() {
        return protectionTier;
    }
}
