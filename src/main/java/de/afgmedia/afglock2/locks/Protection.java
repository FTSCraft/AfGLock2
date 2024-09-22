package de.afgmedia.afglock2.locks;

import de.afgmedia.afglock2.locks.group.LockGroup;
import de.afgmedia.afglock2.locks.lochkarte.Lochkarte;
import de.afgmedia.afglock2.locks.settings.AllowSetting;
import de.afgmedia.afglock2.locks.settings.DenySetting;
import de.afgmedia.afglock2.main.AfGLock;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Protection {

    private final int id;

    private final UUID owner;

    private final Location location;

    private final ProtectionType type;

    private final List<AllowSetting> allowSettings;

    private final ProtectionTier tier;

    public Protection(int id, UUID owner, Location location, ProtectionType type, ProtectionTier tier) {
        this.id = id;
        this.owner = owner;
        this.location = location;
        this.type = type;
        this.allowSettings = new ArrayList<>();
        this.tier = tier;
    }

    public Protection(int id, UUID owner, Location location, ProtectionType type, ProtectionTier tier, List<AllowSetting> allowSettings) {
        this.id = id;
        this.owner = owner;
        this.location = location;
        this.type = type;
        this.allowSettings = allowSettings;
        this.tier = tier;
    }

    public void addAllowSetting(AllowSetting setting) {
        addAllowSetting(setting, true);
    }

    public void addAllowSetting(AllowSetting setting, boolean saveToFile) {
        if (checkForDuplicates(setting))
            return;
        getAllowSettings().add(setting);
        if (saveToFile)
            save();
    }

    private boolean checkForDuplicates(AllowSetting allowSetting) {
        for (AllowSetting setting : getAllowSettings()) {
            if (setting.getType() == allowSetting.getType()) {
                if (setting.getType() == AllowSetting.AllowSettingType.PLAYER) {
                    if (setting.getUuid().equals(allowSetting.getUuid()))
                        return true;
                    continue;
                }
                if (setting.getGroup().equals(allowSetting.getGroup()))
                    return true;
            }
        }
        return false;
    }

    public void applyLochkarte(Lochkarte lochkarte) {
        for (UUID uuid : lochkarte.getUuids()) {
            AllowSetting allowSetting = new AllowSetting(AllowSetting.AllowSettingType.PLAYER);
            allowSetting.setUuid(uuid.toString());
            addAllowSetting(allowSetting, false);
        }
        for (String group : lochkarte.getGroups()) {
            AllowSetting allowSetting = new AllowSetting(AllowSetting.AllowSettingType.GROUP);
            allowSetting.setGroup(group);
            addAllowSetting(allowSetting, false);
        }
        save();
    }

    public boolean isAllowedToAccess(UUID uuid) {
        if (getOwner().toString().equalsIgnoreCase(uuid.toString()))
            return true;
        for (AllowSetting setting : getAllowSettings()) {
            if (setting.getType() == AllowSetting.AllowSettingType.PLAYER) {
                if (setting.getUuid().equalsIgnoreCase(uuid.toString()))
                    return true;
                continue;
            }
            if (setting.getType() == AllowSetting.AllowSettingType.GROUP) {
                LockGroup group = AfGLock.getInstance().getProtectionManager().getLockGroups().get(setting.getGroup());
                if (group.isMember(uuid))
                    return true;
            }
        }
        return false;
    }

    public boolean isOwner(UUID uuid) {
        return getOwner().toString().equalsIgnoreCase(uuid.toString());
    }

    public void removeAllowSetting(DenySetting denySetting) {
        AllowSetting remove = null;
        for (AllowSetting setting : getAllowSettings()) {
            if (denySetting.getType() == AllowSetting.AllowSettingType.PLAYER) {
                if (setting.getType() == AllowSetting.AllowSettingType.PLAYER)
                    if (setting.getUuid().equalsIgnoreCase(denySetting.getUuid()))
                        remove = setting;
                continue;
            }
            if (denySetting.getType() == AllowSetting.AllowSettingType.GROUP &&
                    setting.getType() == AllowSetting.AllowSettingType.GROUP)
                if (setting.getGroup().equalsIgnoreCase(denySetting.getGroup()))
                    remove = setting;
        }
        if (remove == null)
            return;
        getAllowSettings().remove(remove);
        save();
    }

    public void save() {
        AfGLock.getInstance().getDatabaseManager().saveLock(this);
    }

    public void delete() {
        AfGLock.getInstance().getDatabaseManager().deleteLock(this);
    }

    public int getId() {
        return this.id;
    }

    public UUID getOwner() {
        return this.owner;
    }

    public Location getLocation() {
        return this.location;
    }

    public ProtectionType getProtectionType() {
        return this.type;
    }

    public List<AllowSetting> getAllowSettings() {
        return this.allowSettings;
    }

    public ProtectionTier getProtectionTier() {
        return this.tier;
    }
}
