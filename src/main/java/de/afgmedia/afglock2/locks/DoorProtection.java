package de.afgmedia.afglock2.locks;

import de.afgmedia.afglock2.locks.settings.AllowSetting;
import de.afgmedia.afglock2.main.AfGLock;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DoorProtection extends Protection {


    private int id;
    private UUID owner;
    private Location location;
    private int protectionTier;
    private List<AllowSetting> allowSettings;

    public DoorProtection(AfGLock instance, UUID owner, int id, Location location, int tier)
    {
        super(instance);
        this.protectionTier = tier;
        this.allowSettings = new ArrayList<>();
        this.id = id;
        this.owner = owner;
        this.location = location;
    }

    @Override
    public int getId()
    {
        return id;
    }

    @Override
    public Location getLocation()
    {
        return location;
    }

    @Override
    public List<AllowSetting> getAllowSettings()
    {
        return allowSettings;
    }

    @Override
    public ProtectionType getProtectionType()
    {
        return ProtectionType.DOOR;
    }

    @Override
    public UUID getOwner()
    {
        return owner;
    }

    @Override
    public int getProtectionTier()
    {
        return protectionTier;
    }
}
