package de.afgmedia.afglock2.locks.manager;

import com.Acrobot.ChestShop.Plugins.ChestShop;
import de.afgmedia.afglock2.locks.Protection;
import de.afgmedia.afglock2.locks.ProtectionTier;
import de.afgmedia.afglock2.locks.ProtectionType;
import de.afgmedia.afglock2.locks.group.LockGroup;
import de.afgmedia.afglock2.locks.lochkarte.Lochkarte;
import de.afgmedia.afglock2.locks.settings.*;
import de.afgmedia.afglock2.main.AfGLock;
import de.afgmedia.afglock2.utils.Utils;
import de.afgmedia.afglock2.utils.Values;
import de.ftscraft.ftsutils.items.ItemReader;
import de.ftscraft.ftsutils.uuidfetcher.UUIDFetcher;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ProtectionManager {
    private final AfGLock instance;

    private int latestID = 0;

    private final HashMap<Location, Protection> protections = new HashMap<>();

    private final HashMap<Player, ProtectionSetting> playerSetting = new HashMap<>();

    private final HashMap<String, LockGroup> lockGroups = new HashMap<>();

    private final HashMap<Integer, Lochkarte> lochkartenCache = new HashMap<>();

    public ProtectionManager(AfGLock instance) {
        this.instance = instance;
    }

    public ReturnType createLock(Block block, Player player, ProtectionTier protectionTier) {
        ProtectionType type;
        Material material = block.getType();
        Location location = block.getLocation();
        if (!Utils.isLockable(material))
            return ReturnType.NOT_LOCKABLE;
        if (material == Material.CHEST || material == Material.TRAPPED_CHEST) {
            if (Utils.isDoubleChest(block)) {
                type = ProtectionType.DOUBLE_CHEST;
            } else {
                type = ProtectionType.CHEST;
            }
        } else if (Utils.isDoor(material)) {
            type = ProtectionType.DOOR;
        } else if (Utils.isTrapDoor(material)) {
            type = ProtectionType.TRAP_DOOR;
        } else if (Utils.isFenceGate(material)) {
            type = ProtectionType.GATE;
        } else if (Utils.isBarrel(material)) {
            type = ProtectionType.BARREL;
        } else if (material == Material.NOTE_BLOCK) {
            type = ProtectionType.NOTE_BLOCK;
        } else if (material == Material.LECTERN) {
            type = ProtectionType.LECTERN;
        } else if (material == Material.CHISELED_BOOKSHELF) {
            type = ProtectionType.BOOKSHELF;
        } else {
            return ReturnType.FAIL;
        }
        if (type == ProtectionType.DOUBLE_CHEST) {
            location = Utils.getLeftLocationOfDoubleChest(block);
        } else if (type == ProtectionType.DOOR) {
            location = Utils.getLowerLocationOfDoor(block);
        }
        if (this.protections.get(location) != null)
            return ReturnType.ALREADY_LOCKED;
        if (!ChestShop.canAccess(player, block))
            return ReturnType.ALREADY_LOCKED;
        Protection protection = this.instance.getDatabaseManager().createLock(player.getUniqueId(), location, type, protectionTier);
        this.protections.put(location, protection);
        return ReturnType.DONE;
    }

    public Protection getByBlock(Block block) {
        Location location = block.getLocation();
        if (Utils.isDoubleChest(block)) {
            location = Utils.getLeftLocationOfDoubleChest(block);
        } else if (Utils.isDoor(block.getType())) {
            location = Utils.getLowerLocationOfDoor(block);
        }
        Protection protection = this.protections.get(location);
        if (protection == null)
            protection = AfGLock.getInstance().getDatabaseManager().loadLock(location);
        return protection;
    }

    public void handleInteractEvent(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
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

                Bukkit.getScheduler().runTaskAsynchronously(instance, () ->{
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
                });

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
            this.playerSetting.remove(p);
            event.setCancelled(true);
        }
        Protection protection = getByBlock(block);
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
                if (sign == null)
                    return;
                if (Utils.isLock(sign)) {
                    ProtectionTier tier;
                    if (!Utils.isLockable(block.getType())) {
                        p.sendMessage("Dieser Block kann nicht gesichert werden");
                        event.setCancelled(true);
                        return;
                    }
                    switch (sign) {
                        case "IRON_LOCK":
                            tier = ProtectionTier.IRON;
                            break;
                        case "DIAMOND_LOCK":
                            tier = ProtectionTier.DIAMOND;
                            break;
                        case "EMERALD_LOCK":
                            tier = ProtectionTier.EMERALD;
                            break;
                        case "STONE_LOCK":
                            tier = ProtectionTier.STONE;
                            break;
                        case "COPPER_LOCK":
                            tier = ProtectionTier.COPPER;
                            break;
                        default:
                            return;
                    }
                    itemStack.setAmount(itemStack.getAmount() - 1);
                    event.setCancelled(true);
                    createLock(block, p, tier);
                    p.sendMessage("Du hast die Sicherung erfolgreich erstellt");
                    if (hasLochkarte) {
                        protection = getByBlock(block);
                        Lochkarte lochkarte = getLochkarte(Lochkarte.getLochkarteId(p.getInventory().getItemInMainHand()));
                        protection.applyLochkarte(lochkarte);
                        event.setCancelled(true);
                        p.sendMessage(Component.text("Du hast die Lochkarte auf das Schloss angewandt"));
                    }
                }
            }
            return;
        }
        if (!protection.isAllowedToAccess(p.getUniqueId())) {
            if (protection.getProtectionType() == ProtectionType.LECTERN) {
                return;
            }
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
            p.sendMessage("Dieser Block ist gesichert!");
        } else {
            if (!protection.isOwner(p.getUniqueId()))
                return;
            if (!Lochkarte.holdsLochkarte(p))
                return;
            Lochkarte lochkarte = getLochkarte(Lochkarte.getLochkarteId(p.getInventory().getItemInMainHand()));
            protection.applyLochkarte(lochkarte);
            event.setCancelled(true);
            p.sendMessage(Component.text("Du hast die Lochkarte auf das Schloss angewandt"));
        }
    }

    private void startLockPicking(Protection check, Player p) {
        ProtectionTier tier = check.getProtectionTier();
        double d = Math.random();
        if (d <= tier.getChance()) {
            success(p, check);
        } else {
            fail(p);
        }
    }

    private void success(Player p, Protection protection) {
        instance.getProtectionManager().removeLock(protection);
        Bukkit.getScheduler().runTaskLater(instance, () -> {
            p.closeInventory();
            if (protection.getProtectionTier() != ProtectionTier.STONE)
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
        Bukkit.getScheduler().runTaskLater(this.instance, () -> {
            p.closeInventory();
            p.sendMessage("Dein Dietrich ist abgebrochen!");
            p.getInventory().getItemInMainHand().setAmount(p.getInventory().getItemInMainHand().getAmount() - 1);
            p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_LAND, 100.0F, 30.0F);
        }, 2L);
    }

    public HashMap<String, LockGroup> getLockGroups() {
        return this.lockGroups;
    }

    public void removeLock(Protection protection) {
        this.protections.remove(protection.getLocation());
        protection.delete();
    }

    public Protection addLock(Location loc, UUID owner, ProtectionType type, ProtectionTier protectionTier, List<AllowSetting> allowSettings, int id) {
        Protection protection = new Protection(id, owner, loc, type, protectionTier);
        for (AllowSetting allowSetting : allowSettings)
            protection.addAllowSetting(allowSetting);
        if (this.latestID < id)
            this.latestID = id;
        this.protections.put(loc, protection);
        return protection;
    }

    public boolean isLocked(Block block) {
        return (getByBlock(block) != null);
    }

    public enum ReturnType {
        DONE, NOT_LOCKABLE, ALREADY_LOCKED, FAIL
    }

    public void setProtectionSetting(Player player, ProtectionSetting protectionSetting) {
        playerSetting.put(player, protectionSetting);
    }

    public boolean isInSetting(Player player) {
        return this.playerSetting.containsKey(player);
    }

    public Lochkarte getLochkarte(int id) {
        if (!this.lochkartenCache.containsKey(Integer.valueOf(id))) {
            System.out.println("Cache " + this.lochkartenCache.keySet());
            Lochkarte lochkarte = this.instance.getFileManager().loadLochkarte(id);
            this.lochkartenCache.put(Integer.valueOf(lochkarte.getId()), lochkarte);
            return lochkarte;
        }
        return this.lochkartenCache.get(Integer.valueOf(id));
    }

    public void saveLochkartenFromCache() {
        for (Lochkarte value : this.lochkartenCache.values())
            this.instance.getFileManager().saveLochkarte(value);
    }
}
