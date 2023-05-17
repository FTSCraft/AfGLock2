package de.afgmedia.afglock2.locks;

import de.afgmedia.afglock2.locks.settings.AllowSetting;
import de.afgmedia.afglock2.main.AfGLock;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LecternProtection extends Protection {
    private final int id;
    private final UUID owner;
    private final Location location;
    private final int protectionTier;
    private final List<AllowSetting> allowSettings;


    public LecternProtection(AfGLock instance, int id, UUID owner, Location location, int protectionTier) {
        super(instance);
        this.id = id;
        this.owner = owner;
        this.location = location;
        this.protectionTier = protectionTier;
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
        return ProtectionType.LECTERN;
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
