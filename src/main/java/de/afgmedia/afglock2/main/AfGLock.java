package de.afgmedia.afglock2.main;

import de.afgmedia.afglock2.commands.CMDlock;
import de.afgmedia.afglock2.commands.TabLock;
import de.afgmedia.afglock2.database.DatabaseManager;
import de.afgmedia.afglock2.items.ItemStacks;
import de.afgmedia.afglock2.listener.*;
import de.afgmedia.afglock2.locks.manager.ProtectionManager;
import org.bukkit.plugin.java.JavaPlugin;

public class AfGLock extends JavaPlugin {

    private ProtectionManager protectionManager;
    private AfGFileManager fileManager;

    private static AfGLock instance;
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        instance = this;
        init();
    }

    @Override
    public void onDisable() {
        protectionManager.getLockBreakNotifier().runThroughAllNotifies();
        protectionManager.saveLochkartenFromCache();
        fileManager.saveGroups();
        fileManager.saveConfig();
        databaseManager.shutdownConnection();
    }

    private void init() {
        protectionManager = new ProtectionManager(this);

        new InteractListener(this);
        new BlockBreakListener(this);
        new CraftingListener(this);
        new WindchargeListener(this);
        new LecternListener(this);

        new ItemStacks(this);

        new CMDlock(this);
        new TabLock(this);

        fileManager = new AfGFileManager(this);
        fileManager.loadGroups();
        databaseManager = new DatabaseManager();

    }

    public ProtectionManager getProtectionManager() {
        return protectionManager;
    }

    public AfGFileManager getFileManager() {
        return fileManager;
    }

    public static AfGLock getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}
