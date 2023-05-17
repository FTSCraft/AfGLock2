package de.afgmedia.afglock2.utils;

import de.afgmedia.afglock2.locks.ProtectionType;
import de.afgmedia.afglock2.locks.group.LockGroup;
import de.afgmedia.afglock2.locks.settings.AllowSetting;
import de.afgmedia.afglock2.main.AfGLock;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class AfGFileManager {

    private final File lockFolder;
    private final File groupFolder;
    private final AfGLock instance;

    public AfGFileManager(AfGLock instance) {
        this.instance = instance;
        this.lockFolder = new File(instance.getDataFolder() + "//locks//");
        this.groupFolder = new File(instance.getDataFolder() + "//groups");
        if (!lockFolder.exists())
            lockFolder.mkdirs();
    }

    public void loadLocks() {
        long timeBeginn = System.currentTimeMillis();
        System.out.println("Lade Locks...");
        String worldTest = "";
        try {

            for (File file : Objects.requireNonNull(lockFolder.listFiles())) {
                FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);

                long x = cfg.getLong("location.x");
                long y = cfg.getLong("location.y");
                long z = cfg.getLong("location.z");
                String world = cfg.getString("location.world");
                if (world.equalsIgnoreCase("abbauwelt"))
                    continue;
                worldTest = world;

                Location loc = new Location(Bukkit.getWorld(world), x, y, z);
                /*if(!Utils.isLockable(loc.getBlock().getType())) {
                    file.delete();
                    return;
                }*/

                UUID owner = UUID.fromString(cfg.getString("owner"));
                ProtectionType type = ProtectionType.valueOf(cfg.getString("type"));
                int protectionTier = cfg.getInt("tier");
                int id = cfg.getInt("id");

                List<AllowSetting> allowSettings = new ArrayList<>();

                if (cfg.contains("allow")) {
                    for (String s : cfg.getConfigurationSection("allow").getKeys(false)) {
                        AllowSetting.AllowSettingType allowSettingType = AllowSetting.AllowSettingType.valueOf(cfg.getString("allow." + s + ".type"));

                        AllowSetting allowSetting = new AllowSetting(allowSettingType);

                        if (allowSettingType == AllowSetting.AllowSettingType.PLAYER) {
                            allowSetting.setUuid(cfg.getString("allow." + s + ".value"));
                        } else {
                            allowSetting.setGroup(cfg.getString("allow." + s + ".value"));
                        }
                        allowSettings.add(allowSetting);
                    }
                }

                instance.getProtectionManager().addLock(loc, owner, type, protectionTier, allowSettings, id);

            }

            instance.getProtectionManager().latestIDPlus();

        } catch (NullPointerException ex) {
            ex.printStackTrace();
            System.out.println(worldTest);
        }
        long time = (System.currentTimeMillis() - timeBeginn);
        System.out.println("Zeit zum Laden: " + time + " Millisekunden");

    }

    public void saveLocks() {
/*

        for (Protection protection : instance.getProtectionManager().getProtections().values()) {

            try {
                File file = new File(lockFolder + "//" + protection.getId() + ".yml");
                FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);

                final String w = protection.getLocation().getWorld().getName();

                cfg.set("location.x", protection.getLocation().getX());
                cfg.set("location.y", protection.getLocation().getY());
                cfg.set("location.z", protection.getLocation().getZ());
                cfg.set("location.world", w);
                cfg.set("owner", protection.getOwner().toString());
                cfg.set("type", protection.getProtectionType().toString());
                cfg.set("tier", protection.getProtectionTier());
                cfg.set("id", protection.getId());

                for (int i = 0; i < protection.getAllowSettings().size(); i++) {
                    AllowSetting allowSetting = protection.getAllowSettings().get(i);
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
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
*/
    }

    public void saveGroups() {

        for (LockGroup group : instance.getProtectionManager().getLockGroups().values()) {
            File file = new File(groupFolder + "//" + group.getName() + ".yml");
            FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);

            cfg.set("owner", group.getOwner().toString());
            cfg.set("name", group.getName());
            cfg.set("member", group.getMembers().toArray());
            cfg.set("moderators", group.getModerators().toArray());

            try {
                cfg.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void loadGroups() {
        try {
            for (File file : Objects.requireNonNull(groupFolder.listFiles())) {
                final String fileName = file.getName();
                if (fileName.contains("Ä") || fileName.contains("ä") || fileName.contains("ö") || fileName.contains("Ö") || fileName.contains("Ü") || fileName.contains("ü") || fileName.contains("ß")) {
                    continue;
                }
                try {
                    FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);

                    UUID owner = UUID.fromString(cfg.getString("owner"));

                    String name = cfg.getString("name");

                    List<String> members = (List<String>) cfg.getList("member");

                    LockGroup group = new LockGroup(name, owner);

                    if (cfg.contains("moderators")) {

                        List<String> mods = (List<String>) cfg.getList("moderators");

                        for (String mod : mods) {
                            group.addModerator(UUID.fromString(mod));
                        }

                    }


                    for (String member : members) {
                        group.addMember(UUID.fromString(member));
                    }


                    instance.getProtectionManager().getLockGroups().put(name, group);
                } catch (Exception e) {

                    e.printStackTrace();
                }
            }
        } catch (NullPointerException ignored) {

        }

    }
}
