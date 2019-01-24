package de.afgmedia.afglock2.main;

import de.afgmedia.afglock2.commands.CMDlock;
import de.afgmedia.afglock2.items.ItemStacks;
import de.afgmedia.afglock2.listener.BlockBreakListener;
import de.afgmedia.afglock2.listener.InteractListener;
import de.afgmedia.afglock2.listener.InventoryClickListener;
import de.afgmedia.afglock2.locks.manager.ProtectionManager;
import de.afgmedia.afglock2.utils.AfGFileManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.WorldCreator;
import org.bukkit.event.Listener;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class AfGLock extends JavaPlugin implements Listener {

    private ProtectionManager protectionManager;
    private AfGFileManager fileManager;

    @Override
    public void onEnable()
    {
        init();
    }

    @Override
    public void onDisable()
    {
        fileManager.saveGroups();
        fileManager.saveLocks();
    }

    private void init()
    {
        protectionManager = new ProtectionManager(this);
        new InteractListener(this);
        new InventoryClickListener(this);
        new BlockBreakListener(this);
        new ItemStacks(this);
        new CMDlock(this);
        fileManager = new AfGFileManager(this);
        fileManager.loadGroups();
        fileManager.loadLocks();

    }

    public ProtectionManager getProtectionManager()
    {
        return protectionManager;
    }

}
