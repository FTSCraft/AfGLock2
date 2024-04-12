package de.afgmedia.afglock2.locks;

import de.afgmedia.afglock2.locks.settings.AllowSetting;
import de.afgmedia.afglock2.main.AfGLock;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BookshelfProtection extends Protection {

    private final int protectionTier;
    private final int id;
    private final UUID owner;
    private final List<AllowSetting> allowSettings;
    private final Location location;

    public BookshelfProtection(AfGLock instance, UUID owner, int id, Location location, int tier) {
        super(instance);
        this.protectionTier = tier;
        this.allowSettings = new ArrayList<>();
        this.id = id;
        this.owner = owner;
        this.location = location;
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
        return ProtectionType.BOOKSHELF;
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
