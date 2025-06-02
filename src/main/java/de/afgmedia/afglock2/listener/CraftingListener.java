package de.afgmedia.afglock2.listener;

import de.afgmedia.afglock2.main.AfGLock;
import de.ftscraft.ftsutils.items.ItemReader;
import de.ftscraft.ftsutils.misc.MiniMsg;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

public class CraftingListener implements Listener {

    public CraftingListener(AfGLock instance) {
        instance.getServer().getPluginManager().registerEvents(this, instance);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCraft(CraftItemEvent event) {
        ItemStack result = event.getInventory().getResult();
        if (result == null)
            return;
        String sign = ItemReader.getSign(result);
        if (sign != null && sign.equals("DIETRICH") &&
                !event.getWhoClicked().hasPermission("afglock.dietrich")) {
            event.setCancelled(true);
            MiniMsg.msg((Player) event.getWhoClicked(), "<blue>Das dürfen nur Räuber.</blue>");
        }
    }

}
