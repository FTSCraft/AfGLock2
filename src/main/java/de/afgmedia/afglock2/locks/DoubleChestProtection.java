package de.afgmedia.afglock2.locks;

import de.afgmedia.afglock2.locks.settings.AllowSetting;
import de.afgmedia.afglock2.main.AfGLock;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DoubleChestProtection extends Protection {

    private final int id;
    private final UUID owner;
    private final Location location;
    private final int protectionTier;
    private final List<AllowSetting> allowSettings;

    public DoubleChestProtection(AfGLock instance, UUID owner, int id, Location location, int tier)
    {
        super(instance);
        this.protectionTier = tier;
        this.allowSettings = new ArrayList<>();
        this.id = id;
        this.owner = owner;
        this.location = location;
    }

    @Override
    public Location getLocation()
    {
        return location;
    }

    @Override
    public ProtectionType getProtectionType()
    {
        return ProtectionType.DOUBLE_CHEST;
    }

    @Override
    public List<AllowSetting> getAllowSettings()
    {
        return allowSettings;
    }

    @Override
    public UUID getOwner()
    {
        return owner;
    }

    @Override
    public int getId()
    {
        return id;
    }

    @Override
    public int getProtectionTier()
    {
        return protectionTier;
    }
}
