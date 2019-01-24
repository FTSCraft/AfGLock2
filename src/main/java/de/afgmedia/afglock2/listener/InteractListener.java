package de.afgmedia.afglock2.listener;

import de.afgmedia.afglock2.main.AfGLock;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class InteractListener implements Listener {

    private AfGLock plugin;

    public InteractListener(AfGLock plugin)
    {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
        public void onInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        Block block = event.getClickedBlock();

        plugin.getProtectionManager().handleInteractEvent(event);
    }

}
