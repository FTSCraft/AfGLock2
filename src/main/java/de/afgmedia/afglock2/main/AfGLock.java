package de.afgmedia.afglock2.main;

import de.afgmedia.afglock2.commands.CMDlock;
import de.afgmedia.afglock2.commands.TabLock;
import de.afgmedia.afglock2.database.DatabaseManager;
import de.afgmedia.afglock2.items.ItemStacks;
import de.afgmedia.afglock2.listener.BlockBreakListener;
import de.afgmedia.afglock2.listener.CraftingListener;
import de.afgmedia.afglock2.listener.InteractListener;
import de.afgmedia.afglock2.locks.manager.ProtectionManager;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class AfGLock extends JavaPlugin {

    private ProtectionManager protectionManager;
    private AfGFileManager fileManager;
    private ItemStacks itemStacks;

    private static AfGLock instance;
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        instance = this;
        init();
    }

    @Override
    public void onDisable() {
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

        itemStacks = new ItemStacks(this);

        new CMDlock(this);
        new TabLock(this);

        fileManager = new AfGFileManager(this);
        fileManager.loadGroups();
        databaseManager = new DatabaseManager();

    }

    public ProtectionManager getProtectionManager() {
        return protectionManager;
    }

    public ItemStacks getItemStacks() {
        return itemStacks;
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
