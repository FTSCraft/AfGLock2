package de.afgmedia.afglock2.locks;

import de.afgmedia.afglock2.locks.group.LockGroup;
import de.afgmedia.afglock2.locks.lochkarte.Lochkarte;
import de.afgmedia.afglock2.locks.settings.AllowSetting;
import de.afgmedia.afglock2.locks.settings.DenySetting;
import de.afgmedia.afglock2.main.AfGLock;
import org.bukkit.Location;

import java.io.File;
import java.util.List;
import java.util.UUID;

public abstract class Protection {

    abstract public int getId();

    abstract public UUID getOwner();

    abstract public Location getLocation();

    abstract public ProtectionType getProtectionType();

    abstract public List<AllowSetting> getAllowSettings();

    abstract public int getProtectionTier();

    private final AfGLock instance;

    public Protection(AfGLock instance) {
        this.instance = instance;
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

    /**
     * Checks for duplicated AllowSettings
     * @param allowSetting - AllowSetting to check if it's already in
     * @return true when AllowSetting is a duplicate, otherwise false
     */
    private boolean checkForDuplicates(AllowSetting allowSetting) {

        for (AllowSetting setting : getAllowSettings()) {
            if (setting.getType() == allowSetting.getType()) {
                if(setting.getType() == AllowSetting.AllowSettingType.PLAYER) {
                    if(setting.getUuid().equals(allowSetting.getUuid()))
                        return true;
                } else {
                    if(setting.getGroup().equals(allowSetting.getGroup()))
                        return true;
                }
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
            } else if (setting.getType() == AllowSetting.AllowSettingType.GROUP) {
                LockGroup group = instance.getProtectionManager().getLockGroups().get(setting.getGroup());
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
                if (setting.getType() == AllowSetting.AllowSettingType.PLAYER) {

                    if (setting.getUuid().equalsIgnoreCase(denySetting.getUuid())) {
                        remove = setting;
                    }

                }
            } else if (denySetting.getType() == AllowSetting.AllowSettingType.GROUP) {
                if (setting.getType() == AllowSetting.AllowSettingType.GROUP) {

                    if (setting.getGroup().equalsIgnoreCase(denySetting.getGroup())) {
                        remove = setting;
                    }

                }

            }

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

}
