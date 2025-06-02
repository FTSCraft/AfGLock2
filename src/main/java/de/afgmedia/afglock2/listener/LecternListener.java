package de.afgmedia.afglock2.listener;

import de.afgmedia.afglock2.locks.Protection;
import de.afgmedia.afglock2.main.AfGLock;
import de.afgmedia.afglock2.utils.Values;
import de.ftscraft.ftsutils.misc.MiniMsg;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;

public class LecternListener implements Listener {

    private final AfGLock plugin;

    public LecternListener(AfGLock plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onLecternTake(PlayerTakeLecternBookEvent event) {
        Protection protection = plugin.getProtectionManager().getByBlock(event.getLectern().getBlock());

        if (protection.isAllowedToAccess(event.getPlayer().getUniqueId())) {
            return;
        }

        event.setCancelled(true);
        MiniMsg.msg(event.getPlayer(), Values.MM_PREFIX+"Dieser Block ist gesichert.");

    }

}
