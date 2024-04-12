package de.afgmedia.afglock2.locks.manager;

import com.Acrobot.ChestShop.Plugins.ChestShop;
import de.afgmedia.afglock2.locks.*;
import de.afgmedia.afglock2.locks.group.LockGroup;
import de.afgmedia.afglock2.locks.lochkarte.Lochkarte;
import de.afgmedia.afglock2.locks.lockpick.Lockpick;
import de.afgmedia.afglock2.locks.settings.*;
import de.afgmedia.afglock2.main.AfGLock;
import de.afgmedia.afglock2.utils.Utils;
import de.afgmedia.afglock2.utils.Values;
import de.ftscraft.ftsutils.items.ItemReader;
import de.ftscraft.ftsutils.uuidfetcher.UUIDFetcher;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class ProtectionManager {

    private final AfGLock instance;
    private int latestID = 0;
    private final HashMap<Location, Protection> protections = new HashMap<>();
    private final HashMap<Player, ProtectionSetting> playerSetting = new HashMap<>();
    private final HashMap<String, LockGroup> lockGroups = new HashMap<>();
    private final HashMap<Player, Lockpick> lockPicking = new HashMap<>();
    private final HashMap<Integer, Lochkarte> lochkartenCache = new HashMap<>();

    public ProtectionManager(AfGLock instance) {
        this.instance = instance;
    }

    public ReturnType createLock(Block block, Player player, int protectionTier) {

        Material material = block.getType();
        Location location = block.getLocation();

        if (!Utils.isLockable(material)) return ReturnType.NOT_LOCKABLE;

        ProtectionType type;
        if (material == Material.CHEST || material == Material.TRAPPED_CHEST)
            if (Utils.isDoubleChest(block)) type = ProtectionType.DOUBLE_CHEST;
            else type = ProtectionType.CHEST;
        else if (Utils.isDoor(material)) type = ProtectionType.DOOR;
        else if (Utils.isTrapDoor(material)) type = ProtectionType.TRAP_DOOR;
        else if (Utils.isFenceGate(material)) type = ProtectionType.GATE;
        else if (Utils.isBarrel(material)) type = ProtectionType.BARREL;
        else if (material == Material.NOTE_BLOCK) type = ProtectionType.NOTE_BLOCK;
        else if (material == Material.LECTERN) type = ProtectionType.LECTERN;
        else if (material == Material.CHISELED_BOOKSHELF) type = ProtectionType.BOOKSHELF;
        else return ReturnType.FAIL;

        if (type == ProtectionType.DOUBLE_CHEST) location = Utils.getLeftLocationOfDoubleChest(block);
        else if (type == ProtectionType.DOOR) location = Utils.getLowerLocationOfDoor(block);

        if (protections.get(location) != null) return ReturnType.ALREADY_LOCKED;
        if (!ChestShop.canAccess(player, block)) return ReturnType.ALREADY_LOCKED;


        Protection protection = switch (type) {
            case DOOR -> new DoorProtection(instance, player.getUniqueId(), latestID, location, protectionTier);
            case CHEST -> new ChestProtection(instance, player.getUniqueId(), latestID, location, protectionTier);
            case DOUBLE_CHEST ->
                    new DoubleChestProtection(instance, player.getUniqueId(), latestID, location, protectionTier);
            case TRAP_DOOR, GATE ->
                    new TrapDoorProtection(instance, player.getUniqueId(), latestID, location, protectionTier);
            case BARREL -> new BarrelProtection(instance, player.getUniqueId(), latestID, location, protectionTier);
            case NOTE_BLOCK ->
                    new NoteBlockProtection(instance, player.getUniqueId(), latestID, location, protectionTier);
            case LECTERN -> new LecternProtection(instance, latestID, player.getUniqueId(), location, protectionTier);
            case BOOKSHELF ->
                    new BookshelfProtection(instance, player.getUniqueId(), latestID, location, protectionTier);
            default -> null;
        };

        protections.put(location, protection);

        protection.save();

        latestID++;

        return ReturnType.DONE;
    }

    public Protection getByBlock(Block block) {

        Location location = block.getLocation();

        if (Utils.isDoubleChest(block)) {
            location = Utils.getLeftLocationOfDoubleChest(block);
        } else if (Utils.isDoor(block.getType())) {
            location = Utils.getLowerLocationOfDoor(block);
        }

        Protection protection = protections.get(location);
        if (protection == null) {
            protection = AfGLock.getInstance().getDatabaseManager().loadLock(location);
        }

        return protection;
    }

    //TODO: Code auslagern
    public void handleInteractEvent(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block block = event.getClickedBlock();

        if (!ChestShop.canAccess(p, block)) {
            event.setCancelled(true);
            return;
        }

        // Für den Fall, dass der Spieler gerade eine Einstellung vornehmen möchte...
        if (playerSetting.containsKey(p)) {
            event.setCancelled(true);
            ProtectionSetting ps = playerSetting.get(p);

            if (ps instanceof InfoSetting) {

                Protection protection = getByBlock(block);
                if (protection == null) {
                    p.sendMessage(Values.PREFIX + "Dieser Block ist nicht gesichert!");
                    playerSetting.remove(p);
                    return;
                }

                new Thread(() -> {
                    p.sendMessage("§e========");
                    p.sendMessage("§eBesitzer: §c" + Bukkit.getOfflinePlayer(protection.getOwner()).getName());
                    p.sendMessage("§eSicherungslevel: §c" + protection.getProtectionTier());
                    p.sendMessage("§eID: §c" + protection.getId());
                    p.sendMessage("§eGruppen: ");
                    for (AllowSetting setting : protection.getAllowSettings()) {
                        if (setting.getType() == AllowSetting.AllowSettingType.GROUP) {
                            p.sendMessage("§c- " + setting.getGroup());
                        }
                    }
                    p.sendMessage("§eSpieler: ");
                    for (AllowSetting setting : protection.getAllowSettings()) {
                        if (setting.getType() == AllowSetting.AllowSettingType.PLAYER)
                            p.sendMessage("§c- " + UUIDFetcher.getName(UUID.fromString(setting.getUuid())));
                    }
                }).start();

            } else if (ps instanceof AllowSetting allowSetting) {

                Protection protection = getByBlock(block);
                if (protection == null) {
                    p.sendMessage(Values.PREFIX + "Dieser Block ist nicht gesichert!");
                    playerSetting.remove(p);
                    return;
                }

                if (!protection.getOwner().toString().equalsIgnoreCase(p.getUniqueId().toString())) {
                    p.sendMessage(Values.PREFIX + "Diese Sicherung gehört dir nicht");
                    playerSetting.remove(p);
                    return;
                }

                if (allowSetting.getType() == AllowSetting.AllowSettingType.PLAYER) {

                    protection.addAllowSetting(allowSetting);
                    p.sendMessage(Values.PREFIX + "Du hast den Spieler " + Bukkit.getOfflinePlayer(UUID.fromString(allowSetting.getUuid())).getName() + " hinzugefügt");

                } else if (allowSetting.getType() == AllowSetting.AllowSettingType.GROUP) {

                    protection.addAllowSetting(allowSetting);
                    p.sendMessage(Values.PREFIX + "Du hast die Gruppe " + allowSetting.getGroup() + " hinzugefügt");

                }


            } else if (ps instanceof DenySetting denySetting) {

                Protection protection = getByBlock(block);
                if (protection == null) {
                    p.sendMessage(Values.PREFIX + "Dieser Block ist nicht gesichert!");
                    playerSetting.remove(p);
                    return;
                }

                if (!protection.getOwner().toString().equalsIgnoreCase(p.getUniqueId().toString())) {
                    p.sendMessage(Values.PREFIX + "Diese Sicherung gehört dir nicht");
                    playerSetting.remove(p);
                    return;
                }

                protection.removeAllowSetting(denySetting);


            } else if (ps instanceof RemoveSetting) {

                Protection protection = getByBlock(block);
                if (protection == null) {
                    p.sendMessage(Values.PREFIX + "Dieser Block ist nicht gesichert");
                    playerSetting.remove(p);
                    return;
                }

                if (!protection.getOwner().toString().equalsIgnoreCase(p.getUniqueId().toString())) {
                    if (p.hasPermission("afglock.admin")) {
                        removeLock(protection);
                        playerSetting.remove(p);
                        p.sendMessage(Values.PREFIX + "Du hast die Sicherung mit deinen Rechten entfernt");
                        return;
                    }
                    p.sendMessage(Values.PREFIX + "Diese Sicherung gehört dir nicht");
                    playerSetting.remove(p);
                    return;
                }

                p.sendMessage(Values.PREFIX + "Du hast die Sicherung entfernt");
                removeLock(protection);
                playerSetting.remove(p);
            }

            playerSetting.remove(p);
            event.setCancelled(true);
        }

        Protection protection = getByBlock(block);

        // Für den Fall, dass die Protection noch nicht da ist
        if (protection == null) {

            ItemStack itemStack = p.getInventory().getItemInMainHand();
            boolean hasLochkarte = false;

            if (Lochkarte.holdsLochkarte(p)) {
                itemStack = p.getInventory().getItemInOffHand();
                hasLochkarte = true;
            }

            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta != null) {
                String sign = ItemReader.getSign(itemMeta);
                if (sign == null) return;
                if (Utils.isLock(sign)) {

                    if (!Utils.isLockable(block.getType())) {
                        p.sendMessage(Values.PREFIX + "Dieser Block kann nicht gesichert werden");
                        event.setCancelled(true);
                        return;
                    }

                    int tier;
                    switch (sign) {
                        case "IRON_LOCK" -> tier = 1;
                        case "DIAMOND_LOCK" -> tier = 2;
                        case "EMERALD_LOCK" -> tier = 3;
                        case "STONE_LOCK" -> tier = 4;
                        case "COPPER_LOCK" -> tier = 5;
                        default -> {
                            return;
                        }
                    }

                    itemStack.setAmount(itemStack.getAmount() - 1);
                    event.setCancelled(true);
                    createLock(block, p, tier);
                    p.sendMessage(Values.PREFIX + "Du hast die Sicherung erfolgreich erstellt");
                    if (hasLochkarte) {
                        protection = getByBlock(block);
                        Lochkarte lochkarte = getLochkarte(Lochkarte.getLochkarteId(p.getInventory().getItemInMainHand()));
                        protection.applyLochkarte(lochkarte);
                        event.setCancelled(true);
                        p.sendMessage(Component.text(Values.PREFIX + "Du hast die Lochkarte auf das Schloss angewandt"));
                    }
                }

            }

            return;

        }


        if (!protection.isAllowedToAccess(p.getUniqueId())) {
            if (p.hasPermission("ftslock.openlock")) {
                p.sendMessage(Values.PREFIX + "Du hast diese Sicherung von §c" + Utils.getName(protection.getOwner()) + " §7mit deinen Rechten geöffnet!");
                event.setCancelled(false);
                return;
            }
            event.setCancelled(true);

            ItemStack itemStack = p.getInventory().getItemInMainHand();
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta != null) {
                String sign = ItemReader.getSign(itemMeta);
                if (sign.equals("DIETRICH") && p.hasPermission("afglock.dietrich")) {
                    startLockPicking(protection, p);
                    return;
                }
            }
            p.sendMessage(Values.PREFIX + "Dieser Block ist gesichert!");

        } else {
            //Für den Fall, dass der Spieler auf den Block zugreifen kann
            //Spieler muss Owner von Lock sein für Lochkarte
            if (!protection.isOwner(p.getUniqueId())) {
                return;
            }

            if (!Lochkarte.holdsLochkarte(p)) {
                return;
            }

            Lochkarte lochkarte = getLochkarte(Lochkarte.getLochkarteId(p.getInventory().getItemInMainHand()));
            protection.applyLochkarte(lochkarte);
            event.setCancelled(true);
            p.sendMessage(Component.text(Values.PREFIX + "Du hast die Lochkarte auf das Schloss angewandt"));

        }

    }

    private void startLockPicking(Protection check, Player p) {
        int tier = check.getProtectionTier();

        double d = Math.random();

        switch (tier) {
            case 1 -> {
                if (d <= 0.25) {
                    success(p, check);
                } else fail(p);
            }
            case 2 -> {
                if (d <= 0.125) {
                    success(p, check);
                } else fail(p);
            }
            case 3 -> {
                if (d <= 0.0625) success(p, check);
                else fail(p);
            }
            case 4 -> success(p, check);
            case 5 -> {
                if (d <= 0.5) {
                    success(p, check);
                } else fail(p);
            }
            default -> throw new IllegalStateException("Unexpected value: " + tier);
        }

    }

    private void success(Player p, Protection protection) {
        instance.getProtectionManager().removeLock(protection);
        Bukkit.getScheduler().runTaskLater(instance, () -> {
            p.closeInventory();
            if (protection.getProtectionTier() != 4)
                p.getInventory().getItemInMainHand().setAmount(p.getInventory().getItemInMainHand().getAmount() - 1);
        }, 2);
        p.sendMessage(Values.PREFIX + "Du hast es geschafft! Die Sicherung ist in seine Einzelteile zersprungen.");
        p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, 100, 50);
        OfflinePlayer op = Bukkit.getOfflinePlayer(protection.getOwner());
        if (op.isOnline())
            op.getPlayer().sendMessage(Values.PREFIX + "Ein Sloss von dir wurde geknackt! (" + protection.getLocation().getX() + " " + protection.getLocation().getY() + " " + protection.getLocation().getZ() + ")");
        else
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mail " + op.getName() + " Ein Sloss von dir wurde geknackt! (" + protection.getLocation().getX() + " " + protection.getLocation().getY() + " " + protection.getLocation().getZ() + ")");

    }

    private void fail(Player p) {
        Bukkit.getScheduler().runTaskLater(instance, () -> {
            p.closeInventory();
            p.sendMessage(Values.PREFIX + "Dein Dietrich ist abgebrochen!");
            p.getInventory().getItemInMainHand().setAmount(p.getInventory().getItemInMainHand().getAmount() - 1);
            p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_LAND, 100, 30);
        }, 2);
    }

    public void lockPick(InventoryClickEvent event) {
        Player p = (Player) event.getWhoClicked();
        lockPicking.get(p).lockPick(event);
    }

    public HashMap<String, LockGroup> getLockGroups() {
        return lockGroups;
    }

    public void lockPickFail(Player player) {
        lockPicking.remove(player);
    }

    public void removeLock(Protection protection) {
        protections.remove(protection.getLocation());
        protection.delete();
    }

    public Protection addLock(Location loc, UUID owner, ProtectionType type, int protectionTier, List<AllowSetting> allowSettings, int id) {

        Protection protection = null;

        if (type == ProtectionType.CHEST) {
            protection = new ChestProtection(instance, owner, id, loc, protectionTier);
        } else if (type == ProtectionType.DOUBLE_CHEST) {
            protection = new DoubleChestProtection(instance, owner, id, loc, protectionTier);
        } else if (type == ProtectionType.TRAP_DOOR) {
            protection = new TrapDoorProtection(instance, owner, id, loc, protectionTier);
        } else if (type == ProtectionType.DOOR) {
            protection = new DoorProtection(instance, owner, id, loc, protectionTier);
        } else if (type == ProtectionType.BARREL) {
            protection = new BarrelProtection(instance, owner, id, loc, protectionTier);
        }

        if (protection == null) {
            instance.getLogger().log(Level.WARNING, "Protection " + id + " could not be loaded (Null)");
            return null;
        }

        for (AllowSetting allowSetting : allowSettings) {
            protection.addAllowSetting(allowSetting);
        }

        if (latestID < id) {
            latestID = id;
        }

        protections.put(loc, protection);
        return protection;
    }

    public boolean isLocked(Block block) {
        return getByBlock(block) != null;
    }

    public void latestIDPlus() {
        latestID++;
    }

    public enum ReturnType {
        DONE, NOT_LOCKABLE, ALREADY_LOCKED, FAIL
    }

    public void setProtectionSetting(Player player, ProtectionSetting protectionSetting) {
        playerSetting.put(player, protectionSetting);
    }

    public boolean isInSetting(Player player) {
        return playerSetting.containsKey(player);
    }

    public HashMap<Location, Protection> getProtections() {
        return protections;
    }

    public Lochkarte getLochkarte(int id) {
        if (!lochkartenCache.containsKey(id)) {
            System.out.println("Cache " + lochkartenCache.keySet());
            Lochkarte lochkarte = instance.getFileManager().loadLochkarte(id);
            lochkartenCache.put(lochkarte.getId(), lochkarte);
            return lochkarte;
        }
        return lochkartenCache.get(id);
    }

    public void saveLochkartenFromCache() {
        for (Lochkarte value : lochkartenCache.values()) {

            instance.getFileManager().saveLochkarte(value);
        }
    }

}
