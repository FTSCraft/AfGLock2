package de.afgmedia.afglock2.listener;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import de.afgmedia.afglock2.locks.Protection;
import de.afgmedia.afglock2.main.AfGLock;
import de.afgmedia.afglock2.utils.Utils;

import java.util.ArrayList;

import de.afgmedia.afglock2.utils.Values;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.plugin.Plugin;

public class BlockBreakListener implements Listener {
    private final AfGLock instance;

    final ArrayList<OfflinePlayer> receivedMessage;

    public BlockBreakListener(AfGLock instance) {
        this.receivedMessage = new ArrayList<>();
        this.instance = instance;
        instance.getServer().getPluginManager().registerEvents(this, instance);
    }

    @EventHandler
    public void onZombie(EntityBreakDoorEvent event) {
        Block block = event.getBlock();
        Protection protection = this.instance.getProtectionManager().getByBlock(block);
        if (protection == null)
            return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player p = event.getPlayer();
        Block block = event.getBlock();
        Protection protection = instance.getProtectionManager().getByBlock(block);
        if (protection == null)
            return;
        if (p.hasPermission("afglock.admin")) {
            this.instance.getProtectionManager().removeLock(protection);
            p.sendMessage("§cDu hast die Sicherung mit deinen Rechten entfernt!");
            return;
        }
        if (!protection.getOwner().toString().equalsIgnoreCase(p.getUniqueId().toString())) {
            event.setCancelled(true);
            p.sendMessage("§cNur der Besitzer der Sicherung darf diesen Block abbauen!");
        } else {
            event.setCancelled(false);
            this.instance.getProtectionManager().removeLock(protection);
            p.sendMessage("§cDu hast die Sicherung entfernt");
        }
    }

    @EventHandler
    public void onDestroy(BlockDestroyEvent event) {
        if (this.instance.getProtectionManager().isLocked(event.getBlock()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        Player p = event.getPlayer();
        if (p.hasPermission("afglock.reisender") && !this.receivedMessage.contains(p))
            if (Utils.isLockable(event.getBlockPlaced().getType())) {
                p.sendMessage(Values.PREFIX + "Vergiss nicht deine Kisten zu sichern! Zumindest mit einem Steinschloss welches nur Räuber ohne Probleme aufbekommen. Das machst du indem du in jeder Ecke des Craftingfelds einen Cobblestone packst" +
                        "\n" +
                        "Für mehr Informationen klicke auf diesen Link: https://forum.ftscraft.de/afglocks");
                receivedMessage.add(p);
            }
    }

}
