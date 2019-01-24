package de.afgmedia.afglock2.listener;

import de.afgmedia.afglock2.locks.Protection;
import de.afgmedia.afglock2.main.AfGLock;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakListener implements Listener {

    private AfGLock instance;

    public BlockBreakListener(AfGLock instance)
    {
        this.instance = instance;
        instance.getServer().getPluginManager().registerEvents(this, instance);
    }

    @EventHandler()
    public void onBreak(BlockBreakEvent event)
    {
        Player p = event.getPlayer();
        Block block = event.getBlock();
        Protection protection = instance.getProtectionManager().getByBlock(block);
        if(protection == null) {
            return;
        }

        if(p.hasPermission("afglock.admin")) {
            instance.getProtectionManager().removeLock(protection);
            p.sendMessage("§cDu hast die Sicherung mit deinen Rechten entfernt!");
            return;
        }

        if(!protection.getOwner().toString().equalsIgnoreCase(p.getUniqueId().toString())) {
            event.setCancelled(true);
            p.sendMessage("§cNur der Besitzer der Sicherung darf diesen Block abbauen!");
        } else {
            event.setCancelled(false);
            instance.getProtectionManager().removeLock(protection);
            p.sendMessage("§cDu hast die Sicherung entfernt");
        }

    }

}
