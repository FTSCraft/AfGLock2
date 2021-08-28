package de.afgmedia.afglock2.main;

import de.afgmedia.afglock2.commands.CMDlock;
import de.afgmedia.afglock2.commands.TabLock;
import de.afgmedia.afglock2.items.ItemStacks;
import de.afgmedia.afglock2.listener.BlockBreakListener;
import de.afgmedia.afglock2.listener.CraftingListener;
import de.afgmedia.afglock2.listener.InteractListener;
import de.afgmedia.afglock2.listener.InventoryClickListener;
import de.afgmedia.afglock2.locks.manager.ProtectionManager;
import de.afgmedia.afglock2.utils.AfGFileManager;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class AfGLock extends JavaPlugin implements Listener {

    private ProtectionManager protectionManager;
    private AfGFileManager fileManager;

    private ItemStacks itemStacks;

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
        new CraftingListener(this);

        itemStacks = new ItemStacks(this);

        new CMDlock(this);
        new TabLock(this);

        fileManager = new AfGFileManager(this);
        fileManager.loadGroups();
        fileManager.loadLocks();

    }

    public ProtectionManager getProtectionManager()
    {
        return protectionManager;
    }

    public ItemStacks getItemStacks() {
        return itemStacks;
    }
}
