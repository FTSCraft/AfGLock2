package de.afgmedia.afglock2.locks.lockpick;

import com.sun.org.apache.xpath.internal.SourceTree;
import de.afgmedia.afglock2.listener.InventoryClickListener;
import de.afgmedia.afglock2.locks.Protection;
import de.afgmedia.afglock2.main.AfGLock;
import de.afgmedia.afglock2.utils.Utils;
import de.afgmedia.afglock2.utils.Values;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Lockpick {

    private Protection protection;
    private Player player;
    private int protectionTier;
    private int kolben;
    private List<Integer> solution;
    private int lastKolben = 0;

    private Inventory inventory;
    private int progress = 0;
    private AfGLock plugin;
    private boolean wait = false;

    public Lockpick(Protection protection, Player player, AfGLock plugin)
    {
        this.plugin = plugin;
        this.protection = protection;
        this.protectionTier = protection.getProtectionTier();
        this.player = player;
        //Tier 1 = 2 Kolben | Tier 2 = 3 Kolben | Tier 3 = 4 Kolben
        this.kolben = protectionTier + 1;
        this.solution = new ArrayList<>();
        for (int i = 0; i < this.kolben; i++) {
            int number = Utils.getOneOrTwo();
            solution.add(number);
        }
        this.inventory = Bukkit.createInventory(null, 3 * 9, Values.DIETRICH_INVENTORY_NAME);
        setupInventory();
    }

    private void setupInventory()
    {

        ItemStack goal = new ItemStack(Material.CHEST, 1);
        ItemStack piston = new ItemStack(Material.IRON_BLOCK, 1);
        ItemStack filler = new ItemStack(Material.IRON_BARS, 1);
        ItemMeta goalM = goal.getItemMeta();
        ItemMeta pistonM = piston.getItemMeta();
        ItemMeta fillerM = filler.getItemMeta();
        goalM.setDisplayName("§5Ziel");
        goalM.setLore(Arrays.asList("§cWenn du es bis hier hin schaffst, hast du das Schloss geknackt.", "§cWenn du nicht weißt wie das System funktioniert, guck auf unsere Website!"));
        pistonM.setDisplayName("§6Kolben");
        pistonM.setLore(Arrays.asList("§cZiehe diesen Kolben nach oben oder unten.",
                "§cEs ist eine 50:50 Chance",
                "§cWenn du es nicht schaffst, bricht der Dietrich",
                "§cWenn du den Kolben richtig verschiebst, musst du den Nächsten verschieben...",
                "§c...Bis du die Kiste erreichst"));
        fillerM.setDisplayName(" ");
        goal.setItemMeta(goalM);
        piston.setItemMeta(pistonM);
        filler.setItemMeta(fillerM);

        for (int i = 0; i < kolben; i++) {
            inventory.setItem(9 + i, piston.clone());
        }

        inventory.setItem(9 + kolben, goal);

        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null)
                inventory.setItem(i, filler);
        }

        int kolben[] = {9, 10, 11, 12, 13};

        for (int i = 0; i < kolben.length; i++) {
            int k = kolben[i];
            if (inventory.getItem(k).equals(piston)) {
                try {
                    inventory.setItem(k + 9, new ItemStack(Material.AIR));
                    inventory.setItem(k - 9, new ItemStack(Material.AIR));
                } catch (Exception ignored) {

                }
            }
        }

    }

    public void openInventory(Player player)
    {
        player.openInventory(inventory);
    }

    public void lockPick(InventoryClickEvent event)
    {
        if (wait) {
            player.sendMessage("§cBitte warte ein Moment");
            event.setCancelled(true);
            return;
        }
        Player p = (Player) event.getWhoClicked();

        int slot = event.getSlot();
        ItemStack itemStack = event.getCurrentItem();

        if (itemStack == null || itemStack.getType() == Material.AIR) {
            itemStack = event.getCursor();
        }

        if (itemStack.getType() == Material.AIR) {
            event.setCancelled(true);
            return;
        }

        if (itemStack.getItemMeta().getDisplayName().equalsIgnoreCase("§6Kolben")) {

            if (slot == -999) {
                return;
            }

            List<Integer> kolbenSlots = Arrays.asList(9, 10, 11, 12, 13);
            int currentUpperSlot = lastKolben;
            int currentLowerSlot = lastKolben + 18;

            if (slot == currentUpperSlot) {
                wait = true;
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    wait = false;
                }, 20);
                if (solution.get(lastKolben) != 1) {
                    fail(event);
                    return;
                }

            }
            else if (slot == currentLowerSlot) {
                wait = true;
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    wait = false;
                }, 20);
                if (solution.get(lastKolben) != 2) {
                    fail(event);
                    return;
                }

            }
            else {
                if (kolbenSlots.get(lastKolben) == slot) {
                    event.setCancelled(false);
                    return;
                }
                event.setCancelled(true);
                return;
            }


            if (lastKolben == protectionTier) {
                success();
                return;
            }
            lastKolben++;

        }
        else {
            event.setCancelled(true);
            return;
        }

    }

    private void fail(InventoryClickEvent event)
    {
        Player p = (Player) event.getWhoClicked();
        event.setCancelled(false);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            p.closeInventory();
            p.sendMessage("§cDein Dietrich ist abgebrochen!");
            p.getInventory().getItemInMainHand().setAmount(p.getInventory().getItemInMainHand().getAmount() - 1);
            p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_LAND, 100, 30);
        }, 2);

        plugin.getProtectionManager().lockPickFail(player);
    }

    private void success()
    {
        plugin.getProtectionManager().removeLock(protection);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.closeInventory();
            player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
        }, 2);
        player.sendMessage("§cDu hast es geschafft! Die Sicherung ist in seine Einzelteile zersprungen.");
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 100, 50);
        OfflinePlayer op = Bukkit.getOfflinePlayer(protection.getOwner());
        if (op.isOnline())
            op.getPlayer().sendMessage("§c" + "Ein Sloss von dir wurde geknackt! (" + protection.getLocation().getX() + " " + protection.getLocation().getY() + " " + protection.getLocation().getZ() + ")");
        else
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mail " + op.getName() + " Ein Sloss von dir wurde geknackt! (" + protection.getLocation().getX() + " " + protection.getLocation().getY() + " " + protection.getLocation().getZ() + ")");
    }

}
