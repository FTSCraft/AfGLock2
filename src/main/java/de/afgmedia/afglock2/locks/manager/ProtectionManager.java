package de.afgmedia.afglock2.locks.manager;

import com.Acrobot.ChestShop.Plugins.ChestShop;
import de.afgmedia.afglock2.locks.*;
import de.afgmedia.afglock2.locks.group.LockGroup;
import de.afgmedia.afglock2.locks.lockpick.Lockpick;
import de.afgmedia.afglock2.locks.settings.*;
import de.afgmedia.afglock2.main.AfGLock;
import de.afgmedia.afglock2.utils.Utils;
import de.afgmedia.afglock2.utils.Values;
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
import java.util.logging.Logger;

public class ProtectionManager {

    private final AfGLock instance;
    private int latestID = 0;
    private final HashMap<Location, Protection> protections = new HashMap<>();
    private final HashMap<Player, ProtectionSetting> playerSetting = new HashMap<>();
    private final HashMap<String, LockGroup> lockGroups = new HashMap<>();
    private final HashMap<Player, Lockpick> lockPicking = new HashMap<>();

    public ProtectionManager(AfGLock instance) {
        this.instance = instance;
    }

    public ReturnType createLock(Block block, Player player, int protectionTier) {

        Material material = block.getType();
        Location location = block.getLocation();

        if (!Utils.isLockable(material))
            return ReturnType.NOT_LOCKABLE;

        ProtectionType type;
        if (material == Material.CHEST || material == Material.TRAPPED_CHEST) {
            if (Utils.isDoubleChest(block))
                type = ProtectionType.DOUBLE_CHEST;
            else type = ProtectionType.CHEST;
        } else if (Utils.isDoor(material))
            type = ProtectionType.DOOR;
        else if (Utils.isTrapDoor(material))
            type = ProtectionType.TRAP_DOOR;
        else if (Utils.isFenceGate(material))
            type = ProtectionType.GATE;
        else if (Utils.isBarrel(material))
            type = ProtectionType.BARREL;
        else if (material == Material.NOTE_BLOCK) {
            type = ProtectionType.NOTE_BLOCK;
        } else if (material == Material.LECTERN) {
            type = ProtectionType.NOTE_BLOCK;
        } else return ReturnType.FAIL;

        if (type == ProtectionType.DOUBLE_CHEST) {
            location = Utils.getLeftLocationOfDoubleChest(block);
        } else if (type == ProtectionType.DOOR) {
            location = Utils.getLowerLocationOfDoor(block);
        }

        if (protections.get(location) != null)
            return ReturnType.ALREADY_LOCKED;
        if (!ChestShop.canAccess(player, block))
            return ReturnType.ALREADY_LOCKED;



        Protection protection = switch (type) {
            case DOOR -> new DoorProtection(instance, player.getUniqueId(), latestID, location, protectionTier);
            case CHEST -> new ChestProtection(instance, player.getUniqueId(), latestID, location, protectionTier);
            case DOUBLE_CHEST -> new DoubleChestProtection(instance, player.getUniqueId(), latestID, location, protectionTier);
            case TRAP_DOOR -> new TrapDoorProtection(instance, player.getUniqueId(), latestID, location, protectionTier);
            case GATE -> new TrapDoorProtection(instance, player.getUniqueId(), latestID, location, protectionTier);
            case NOTE_BLOCK -> new NoteBlockProtection(instance, player.getUniqueId(), latestID, location, protectionTier);
            case LECTERN -> new LecternProtection(instance, latestID, player.getUniqueId(), location, protectionTier);
            default -> null;
        };

        if (protection == null) {
            return ReturnType.FAIL;
        }

        protections.put(location, protection);


        protection.saveToFile();

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

        return protections.get(location);
    }

    public void handleInteractEvent(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block block = event.getClickedBlock();
        ItemStack inHand = p.getInventory().getItemInMainHand();
        if (!ChestShop.canAccess(p, block)) {
            event.setCancelled(true);
            return;
        }
        if (playerSetting.containsKey(p)) {
            event.setCancelled(true);
            ProtectionSetting ps = playerSetting.get(p);

            if (ps instanceof InfoSetting) {

                Protection protection = getByBlock(block);
                if (protection == null) {
                    p.sendMessage("§cDieser Block ist nicht gesichert!");
                    playerSetting.remove(p);
                    return;
                }

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
                        p.sendMessage("§c- " + Bukkit.getOfflinePlayer(UUID.fromString(setting.getUuid())).getName());
                }

            } else if (ps instanceof AllowSetting) {

                Protection protection = getByBlock(block);
                if (protection == null) {
                    p.sendMessage("§cDieser Block ist nicht gesichert!");
                    playerSetting.remove(p);
                    return;
                }

                if (!protection.getOwner().toString().equalsIgnoreCase(p.getUniqueId().toString())) {
                    p.sendMessage("§cDiese Sicherung gehört dir nicht");
                    playerSetting.remove(p);
                    return;
                }

                AllowSetting allowSetting = (AllowSetting) ps;

                if (allowSetting.getType() == AllowSetting.AllowSettingType.PLAYER) {

                    protection.addAllowSetting(allowSetting);
                    p.sendMessage("§cDu hast den Spieler " + Bukkit.getOfflinePlayer(UUID.fromString(allowSetting.getUuid())).getName() + " hinzugefügt");

                } else if (allowSetting.getType() == AllowSetting.AllowSettingType.GROUP) {

                    protection.addAllowSetting(allowSetting);
                    p.sendMessage("§cDu hast die Gruppe " + allowSetting.getGroup() + " hinzugefügt");

                }


            } else if (ps instanceof DenySetting) {

                Protection protection = getByBlock(block);
                if (protection == null) {
                    p.sendMessage("§cDieser Block ist nicht gesichert!");
                    playerSetting.remove(p);
                    return;
                }

                if (!protection.getOwner().toString().equalsIgnoreCase(p.getUniqueId().toString())) {
                    p.sendMessage("§cDiese Sicherung gehört dir nicht");
                    playerSetting.remove(p);
                    return;
                }

                DenySetting denySetting = (DenySetting) ps;

                protection.removeAllowSetting(denySetting);


            } else if (ps instanceof RemoveSetting) {

                Protection protection = getByBlock(block);
                if (protection == null) {
                    p.sendMessage("§cDieser Block ist nicht gesichert");
                    playerSetting.remove(p);
                    return;
                }

                if (!protection.getOwner().toString().equalsIgnoreCase(p.getUniqueId().toString())) {
                    if (p.hasPermission("afglock.admin")) {
                        removeLock(protection);
                        playerSetting.remove(p);
                        p.sendMessage("§cDu hast die Sicherung mit deinen Rechten entfernt");
                        return;
                    }
                    p.sendMessage("§cDiese Sicherung gehört dir nicht");
                    playerSetting.remove(p);
                    return;
                }

                p.sendMessage("§cDu hast die Sicherung entfernt");
                removeLock(protection);
                playerSetting.remove(p);
            }

            playerSetting.remove(p);
            event.setCancelled(true);
        }

        Protection protection = getByBlock(block);

        if (protection == null) {

            ItemStack itemStack = p.getInventory().getItemInMainHand();

            if (itemStack != null) {
                ItemMeta itemMeta = itemStack.getItemMeta();
                if (itemMeta != null) {

                    if (Utils.isLock(itemMeta.getDisplayName())) {

                        if (!Utils.isLockable(block.getType())) {
                            p.sendMessage("§cDieser Block kann nicht gesichert werden");
                            event.setCancelled(true);
                            return;
                        }

                        String displayName = itemMeta.getDisplayName();
                        int tier;
                        if (displayName.equalsIgnoreCase(Values.SCHLOSS_IRON_NAME)) {
                            tier = 1;
                        } else if (displayName.equalsIgnoreCase(Values.SCHLOSS_DIAMOND_NAME)) {
                            tier = 2;
                        } else if (displayName.equalsIgnoreCase(Values.SCHLOSS_EMERALD_NAME)) {
                            tier = 3;
                        } else if (displayName.equalsIgnoreCase(Values.SCHLOSS_STEIN_NAME)) {
                            tier = 4;
                        } else if(displayName.equalsIgnoreCase(Values.SCHLOSS_COPPER_NAME)) {
                            tier = 5;
                        } else {
                            return;
                        }

                        itemStack.setAmount(itemStack.getAmount() - 1);
                        event.setCancelled(true);
                        createLock(block, p, tier);
                        p.sendMessage("§cDu hast die Sicherung erfolgreich erstellt");

                    }

                }
            }

            return;

        }


        if (!protection.isAllowedToAccess(p.getUniqueId())) {
            if (p.hasPermission("ftslock.openlock")) {
                p.sendMessage("§cDu hast diese Sicherung von §e" + Utils.getName(protection.getOwner()) + " §cmit deinen Rechten geöffnet!");
                event.setCancelled(false);
                return;
            }
            event.setCancelled(true);

            ItemStack itemStack = p.getInventory().getItemInMainHand();
            if (itemStack != null) {
                ItemMeta itemMeta = itemStack.getItemMeta();
                if (itemMeta != null) {
                    if (itemMeta.getDisplayName().equalsIgnoreCase("§5Dietrich") && itemStack.getType() == Material.BLAZE_ROD) {
                        //p.sendMessage("§cDiese Funktion ist noch nicht freigeschaltet weil es noch verbuggt ist!");
                        //return;

                        startLockPicking(protection, p);
                        return;

                    }
                }
            }
            p.sendMessage("§cDieser Block ist gesichert!");

        }

    }

    private void startLockPicking(Protection check, Player p) {
        int tier = check.getProtectionTier();

        double d = Math.random();

        if (tier == 1) {

            if (d <= 0.25) {
                sucess(p, check);
            } else fail(p);

        } else if (tier == 2) {

            if (d <= 0.125) {
                sucess(p, check);
            } else fail(p);

        } else if (tier == 3) {
            if (d <= 0.0625)
                sucess(p, check);
            else fail(p);
        } else if (tier == 4) {
            sucess(p, check);
        }

        /*Lockpick lockpick = new Lockpick(check, p, instance);
        lockPicking.put(p, lockpick);
        lockpick.openInventory(p);
*/
    }

    private void sucess(Player p, Protection protection) {
        instance.getProtectionManager().removeLock(protection);
        Bukkit.getScheduler().runTaskLater(instance, () -> {
            p.closeInventory();
            if (protection.getProtectionTier() != 4)
                p.getInventory().getItemInMainHand().setAmount(p.getInventory().getItemInMainHand().getAmount() - 1);
        }, 2);
        p.sendMessage("§cDu hast es geschafft! Die Sicherung ist in seine Einzelteile zersprungen.");
        p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, 100, 50);
        OfflinePlayer op = Bukkit.getOfflinePlayer(protection.getOwner());
        if (op.isOnline())
            op.getPlayer().sendMessage("§c" + "Ein Sloss von dir wurde geknackt! (" + protection.getLocation().getX() + " " + protection.getLocation().getY() + " " + protection.getLocation().getZ() + ")");
        else
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mail " + op.getName() + " Ein Sloss von dir wurde geknackt! (" + protection.getLocation().getX() + " " + protection.getLocation().getY() + " " + protection.getLocation().getZ() + ")");

    }

    private void fail(Player p) {
        Bukkit.getScheduler().runTaskLater(instance, () -> {
            p.closeInventory();
            p.sendMessage("§cDein Dietrich ist abgebrochen!");
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

    public void addLock(Location loc, UUID owner, ProtectionType type, int protectionTier, List<AllowSetting> allowSettings, int id) {

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
            return;
        }

        for (AllowSetting allowSetting : allowSettings) {
            protection.addAllowSetting(allowSetting);
        }

        if (latestID < id) {
            latestID = id;
        }

        protections.put(loc, protection);

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
}
