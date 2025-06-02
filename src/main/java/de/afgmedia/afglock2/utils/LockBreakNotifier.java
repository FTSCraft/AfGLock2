package de.afgmedia.afglock2.utils;

import de.afgmedia.afglock2.main.AfGLock;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitTask;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

public class LockBreakNotifier {

    private final long DELAY_TO_SHOW = 3600000; // eine Stunde in ms

    private final AfGLock instance;

    private final Queue<LockBreakNotify> lockBreakNotifies = new LinkedBlockingQueue<>();
    private LockBreakNotify currentNotify;
    private BukkitTask task;

    public LockBreakNotifier(AfGLock instance) {
        this.instance = instance;
        startRunner();
    }

    private void startRunner() {

        task = Bukkit.getScheduler().runTaskTimerAsynchronously(instance, () -> {

            if (currentNotify == null)
                currentNotify = lockBreakNotifies.poll();

            while (currentNotify != null && currentNotify.brokeAt() + DELAY_TO_SHOW <= System.currentTimeMillis()) {
                Location loc = currentNotify.protectionLocation;

                OfflinePlayer op = Bukkit.getOfflinePlayer(currentNotify.player);

                if (op.isOnline())
                    //noinspection DataFlowIssue - player is online
                    op.getPlayer().sendMessage(Values.PREFIX + "Ein Schloss von dir wurde geknackt! " +
                            "(" + loc.getX() + " " + loc.getY() + " " + loc.getZ() + ")");
                else
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                            "mail " + op.getName() + " Ein Schloss von dir wurde geknackt! " +
                                    "(" + loc.getX() + " " + loc.getY() + " " + loc.getZ() + ")");

                instance.getLogger().finer("[LockBreakNotifier] Notified player " + op.getName());

                currentNotify = lockBreakNotifies.poll();

            }

        }, 0, 20 * 60 * 10);

    }

    public void runThroughAllNotifies() {
        task.cancel();
        if (currentNotify == null)
            currentNotify = lockBreakNotifies.poll();

        while (currentNotify != null) {

            OfflinePlayer op = Bukkit.getOfflinePlayer(currentNotify.player);
            Location loc = currentNotify.protectionLocation;

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    "mail " + op.getName() + " Ein Schloss von dir wurde geknackt! " +
                            "(" + loc.getX() + " " + loc.getY() + " " + loc.getZ() + ")");
            instance.getLogger().finer("[LockBreakNotifier] Notified player " + op.getName());
            currentNotify = lockBreakNotifies.poll();
        }


    }

    public record LockBreakNotify(long brokeAt, Location protectionLocation, UUID player) {
    }

    public void notifyLockBreak(Location location, UUID player) {
        lockBreakNotifies.add(new LockBreakNotify(System.currentTimeMillis(), location, player));
        instance.getLogger().finer("[LockBreakNotifier] Added lock break notify for " + player);
    }


}
