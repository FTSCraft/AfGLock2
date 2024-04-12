package de.afgmedia.afglock2.main;

import de.afgmedia.afglock2.locks.ProtectionType;
import de.afgmedia.afglock2.locks.group.LockGroup;
import de.afgmedia.afglock2.locks.lochkarte.Lochkarte;
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
    private final File lochkartenFolder;
    private int latestLochkartenId;
    private final AfGLock instance;

    AfGFileManager(AfGLock instance) {
        this.instance = instance;
        this.lockFolder = new File(instance.getDataFolder() + "//locks//");
        this.groupFolder = new File(instance.getDataFolder() + "//groups");
        this.lochkartenFolder = new File(instance.getDataFolder() + "//lochkarten");

        if (instance.getConfig().contains("latestLochkartenId")) {
            latestLochkartenId = instance.getConfig().getInt("latestLochkartenId");
        } else latestLochkartenId = 1;

        if (!lockFolder.exists())
            lockFolder.mkdirs();
        if (!lochkartenFolder.exists())
            lochkartenFolder.mkdirs();
    }

    void saveConfig() {
        instance.getConfig().set("latestLochkartenId", latestLochkartenId);
        instance.saveConfig();
    }


    void saveGroups() {

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

    void loadGroups() {
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

    public Lochkarte loadLochkarte(int id) {

        File file = new File(lochkartenFolder + "//lochkarte_" + id + ".yml");
        if (!file.exists()) {
            System.out.println(file.getAbsolutePath() + " " + id);
            return new Lochkarte(latestLochkartenId++);
        }

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        Lochkarte lochkarte = new Lochkarte(id);
        for (String group : cfg.getStringList("groups")) {
            lochkarte.addGroup(group);
        }
        for (String players : cfg.getStringList("players")) {
            lochkarte.addPlayer(UUID.fromString(players));
        }

        return lochkarte;
    }

    public void saveLochkarte(Lochkarte lochkarte) {
        int id = lochkarte.getId();
        File file = new File(lochkartenFolder + "//lochkarte_" + id + ".yml");
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        cfg.set("groups", lochkarte.getGroups());
        ArrayList<String> uuidStringList = new ArrayList<>(lochkarte.getUuids().size());
        for (UUID uuid : lochkarte.getUuids()) {
            uuidStringList.add(uuid.toString());
        }
        cfg.set("players", uuidStringList);

        try {
            cfg.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
