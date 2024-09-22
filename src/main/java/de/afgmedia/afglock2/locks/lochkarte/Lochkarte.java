package de.afgmedia.afglock2.locks.lochkarte;

import de.afgmedia.afglock2.locks.settings.AllowSetting;
import de.afgmedia.afglock2.locks.settings.DenySetting;
import de.ftscraft.ftsutils.items.ItemReader;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class Lochkarte {
    private final int id;
    private final ArrayList<UUID> uuids;
    private final ArrayList<String> groups;

    public Lochkarte(int id) {
        this.id = id;
        this.uuids = new ArrayList<>();
        this.groups = new ArrayList<>();
    }

    public boolean addPlayer(UUID player) {
        if (this.uuids.size() >= 7 || this.uuids.contains(player))
            return false;
        this.uuids.add(player);
        return true;
    }

    public boolean removePlayer(UUID player) {
        return this.uuids.remove(player);
    }

    public boolean addGroup(String group) {
        if (this.groups.size() >= 5 || this.groups.contains(group))
            return false;
        this.groups.add(group);
        return true;
    }

    public boolean removeGroup(String group) {
        return this.groups.remove(group);
    }

    public void generateItemMeta(ItemStack item) {
        ItemReader.addPDC(item, "LOCHKARTE_ID", this.id, PersistentDataType.INTEGER);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.lore(generateLore());
        item.setItemMeta(itemMeta);
    }

    private List<Component> generateLore() {
        List<Component> components = new ArrayList<>();
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
        components.add(Component.empty());
        components.add(Component.text("§7ID: §c" + this.id));
        return components;
    }

    public int getId() {
        return this.id;
    }

    public ArrayList<UUID> getUuids() {
        return this.uuids;
    }

    public ArrayList<String> getGroups() {
        return this.groups;
    }

    public boolean addAllowSetting(AllowSetting allowSetting) {
        if (allowSetting.getType() == AllowSetting.AllowSettingType.PLAYER)
            return addPlayer(UUID.fromString(allowSetting.getUuid()));
        if (allowSetting.getType() == AllowSetting.AllowSettingType.GROUP)
            return addGroup(allowSetting.getGroup());
        return false;
    }

    public boolean removeAllowSetting(DenySetting denySetting) {
        if (denySetting.getType() == AllowSetting.AllowSettingType.PLAYER)
            return removePlayer(UUID.fromString(denySetting.getUuid()));
        return removeGroup(denySetting.getGroup());
    }

    public static boolean holdsLochkarte(Player player) {
        return isLochkarte(player.getInventory().getItemInMainHand());
    }

    private static boolean isLochkarte(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null)
            return false;
        String sign;
        if ((sign = ItemReader.getSign(itemMeta)) != null)
            return sign.equals("LOCHKARTE");
        return false;
    }

    public static int getLochkarteId(ItemStack itemStack) {
        if (!isLochkarte(itemStack))
            throw new IllegalArgumentException("ItemStack is not a Lochkarte");
        return ItemReader.getPDC(itemStack, "LOCHKARTE_ID", PersistentDataType.INTEGER);
    }
}
