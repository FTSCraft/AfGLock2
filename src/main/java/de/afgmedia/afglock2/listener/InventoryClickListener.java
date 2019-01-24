package de.afgmedia.afglock2.listener;

import de.afgmedia.afglock2.main.AfGLock;
import de.afgmedia.afglock2.utils.Values;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

public class InventoryClickListener implements Listener {

    private AfGLock plugin;

    public InventoryClickListener(AfGLock plugin)
    {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event)
    {
        Player p = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();

        if (inventory.getName().equalsIgnoreCase(Values.DIETRICH_INVENTORY_NAME)) {
            plugin.getProtectionManager().lockPick(event);
        }


    }

}
