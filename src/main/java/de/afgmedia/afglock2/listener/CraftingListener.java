package de.afgmedia.afglock2.listener;

import de.afgmedia.afglock2.main.AfGLock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;

public class CraftingListener implements Listener {

    private final AfGLock instance;

    public CraftingListener(AfGLock instance) {
        this.instance = instance;
        this.instance.getServer().getPluginManager().registerEvents(this, instance);
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {

        if (event.getRecipe().getResult().isSimilar(instance.getItemStacks().getDietrich())) {
            Player p = (Player) event.getWhoClicked();

            if (!p.hasPermission("afglock.dietrich")) {
                event.setCancelled(true);
                p.sendMessage("§cDieses Item können nur bestimmte Leute craften!");
            }
        }

    }

}
