package de.afgmedia.afglock2.listener;

import de.afgmedia.afglock2.main.AfGLock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class WindchargeListener implements Listener {

    private final AfGLock instance;

    public WindchargeListener(AfGLock instance) {
        this.instance = instance;
        instance.getServer().getPluginManager().registerEvents(this, instance);
    }

    @EventHandler
    public void onWind(EntityExplodeEvent event) {

        event.blockList().removeIf(block -> instance.getProtectionManager().getByBlock(block) != null);

    }

}
