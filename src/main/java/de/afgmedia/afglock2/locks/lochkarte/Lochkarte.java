package de.afgmedia.afglock2.locks.lochkarte;

import de.afgmedia.afglock2.items.ItemStacks;
import de.afgmedia.afglock2.locks.settings.AllowSetting;
import de.afgmedia.afglock2.utils.Values;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Lochkarte {

    /*
     *  Eine Lochkarte kann:
     *      * Bis zu 7 Personen zu einem Lock hinzufügen
     *      * Bis zu 5 Gruppen zu einem Lock hinzufügen
     *      * Ein Lock automatisch hinzufügen
     *
     *  Eine Lochkarte braucht:
     *      * Eine ID
     */

    private final int id;
    private int locktype;
    private final ArrayList<UUID> uuids;
    private final ArrayList<String> groups;


    public Lochkarte(int id) {
        this.id = id;
        uuids = new ArrayList<>();
        groups = new ArrayList<>();
    }

    public boolean addPlayer(UUID player) {
        if (uuids.size() >= Values.LOCHKARTE_PLAYER_LIMIT || uuids.contains(player)) {
            return false;
        }
        uuids.add(player);
        return true;
    }

    public boolean removePlayer(UUID player) {
        return uuids.remove(player);
    }

    public boolean addGroup(String group) {
        if (groups.size() >= Values.LOCHKARTE_GROUP_LIMIT || groups.contains(group))
            return false;

        groups.add(group);
        return true;
    }

    public boolean removeGroup(String group) {
        return groups.remove(group);
    }


    public void generateItemMeta(ItemStack item) {
        ItemMeta itemMeta = item.getItemMeta();
        PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();
        persistentDataContainer.set(ItemStacks.nameSpacedKeyId, PersistentDataType.INTEGER, id);
        itemMeta.lore(generateLore());
        item.setItemMeta(itemMeta);
    }

    private List<Component> generateLore() {
        List<Component> components = new ArrayList<>();

        components.add(Component.text("§7Lock: §c"));
        components.add(Component.empty());
        components.add(Component.text("§7Spieler:"));
        for (UUID uuid : uuids) {
            components.add(Component.text(ChatColor.RED + Bukkit.getOfflinePlayer(uuid).getName()));
        }
        components.add(Component.empty());
        components.add(Component.text("§7Gruppen:"));
        for (String group : groups) {
            components.add(Component.text(ChatColor.RED + group));
        }


        return components;
    }

    public int getId() {
        return id;
    }

    public int getLocktype() {
        return locktype;
    }

    public ArrayList<UUID> getUuids() {
        return uuids;
    }

    public ArrayList<String> getGroups() {
        return groups;
    }

    public void setLocktype(int locktype) {
        this.locktype = locktype;
    }

    public boolean addAllowSetting(AllowSetting allowSetting) {
        if (allowSetting.getType() == AllowSetting.AllowSettingType.PLAYER) {
            return addPlayer(UUID.fromString(allowSetting.getUuid()));
        } else if (allowSetting.getType() == AllowSetting.AllowSettingType.GROUP) {
            return addGroup(allowSetting.getGroup());
        } else return false;
    }

    public static boolean holdsLochkarte(Player player) {
        return isLochkarte(player.getInventory().getItemInMainHand());
    }

    private static boolean isLochkarte(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta == null)
            return false;

        PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();

        if (pdc.has(ItemStacks.nameSpacedKeyItem)) {
            return pdc.get(ItemStacks.nameSpacedKeyItem, PersistentDataType.STRING).equalsIgnoreCase("lochkarte");
        }
        return false;
    }

    public static int getLochkarteId(ItemStack itemStack) {
        if (!isLochkarte(itemStack)) {
            throw new IllegalArgumentException("ItemStack is not a Lochkarte");
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();
        return pdc.get(ItemStacks.nameSpacedKeyId, PersistentDataType.INTEGER);
    }


}
