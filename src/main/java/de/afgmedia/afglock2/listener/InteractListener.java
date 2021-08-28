package de.afgmedia.afglock2.listener;

import de.afgmedia.afglock2.main.AfGLock;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.type.Gate;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;

public class InteractListener implements Listener {

    private final AfGLock plugin;

    public InteractListener(AfGLock plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        Block block = event.getClickedBlock();

        plugin.getProtectionManager().handleInteractEvent(event);
    }

    @EventHandler
    public void onOpen(BlockRedstoneEvent event) {

        if (event.getBlock().getBlockData() instanceof Openable || event.getBlock().getBlockData() instanceof Gate) {
            if (plugin.getProtectionManager().isLocked(event.getBlock())) {
                event.setNewCurrent(event.getOldCurrent());
            }
        }

    }

    @EventHandler
    public void onHopper(InventoryMoveItemEvent event) {


        InventoryHolder invH = event.getSource().getHolder();

        if (invH instanceof Container) {

            Container container = (Container) invH;

            if (container.getType() == Material.HOPPER) {
                return;
            }

            if (plugin.getProtectionManager().isLocked(container.getBlock()))
                event.setCancelled(true);

        } else if (invH instanceof DoubleChest) {

            DoubleChest doubleChest = (DoubleChest) invH;
            if (plugin.getProtectionManager().isLocked(doubleChest.getLocation().getBlock())) {
                event.setCancelled(true);
            }

        }


    }

}
