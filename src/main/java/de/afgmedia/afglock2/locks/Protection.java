package de.afgmedia.afglock2.locks;

import de.afgmedia.afglock2.locks.group.LockGroup;
import de.afgmedia.afglock2.locks.lochkarte.Lochkarte;
import de.afgmedia.afglock2.locks.settings.AllowSetting;
import de.afgmedia.afglock2.locks.settings.DenySetting;
import de.afgmedia.afglock2.main.AfGLock;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
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
        if(getAllowSettings().contains(setting))
            return;

        getAllowSettings().add(setting);

        saveToFile();
    }

    public void applyLochkarte(Lochkarte lochkarte) {
        for (UUID uuid : lochkarte.getUuids()) {
            AllowSetting allowSetting = new AllowSetting(AllowSetting.AllowSettingType.PLAYER);
            allowSetting.setUuid(uuid.toString());
            addAllowSetting(allowSetting);
        }
        for (String group : lochkarte.getGroups()) {
            AllowSetting allowSetting = new AllowSetting(AllowSetting.AllowSettingType.GROUP);
            allowSetting.setGroup(group);
            addAllowSetting(allowSetting);
        }
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

        saveToFile();

    }

    public void saveToFile() {

        File lockFolder = new File(instance.getDataFolder() + "//locks//");

        File file = new File(lockFolder + "//" + getId() + ".yml");
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        final String w = getLocation().getWorld().getName();

        cfg.set("location.x", getLocation().getX());
        cfg.set("location.y", getLocation().getY());
        cfg.set("location.z", getLocation().getZ());
        cfg.set("location.world", w);
        cfg.set("owner", getOwner().toString());
        cfg.set("type", getProtectionType().toString());
        cfg.set("tier", getProtectionTier());
        cfg.set("id", getId());

        for (int i = 0; i < getAllowSettings().size(); i++) {
            AllowSetting allowSetting = getAllowSettings().get(i);
            cfg.set("allow." + i + ".type", allowSetting.getType().toString());
            if (allowSetting.getType() == AllowSetting.AllowSettingType.PLAYER) {
                cfg.set("allow." + i + ".value", allowSetting.getUuid());
            }
            if (allowSetting.getType() == AllowSetting.AllowSettingType.GROUP) {
                cfg.set("allow." + i + ".value", allowSetting.getGroup());
            }
        }

        try {
            cfg.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void delete() {

        File lockFolder = new File(instance.getDataFolder() + "//locks//");
        File file = new File(lockFolder + "//" + getId() + ".yml");
        file.delete();

    }

}
