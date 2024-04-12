package de.afgmedia.afglock2.items;

import de.afgmedia.afglock2.main.AfGLock;
import de.afgmedia.afglock2.utils.Values;
import de.ftscraft.ftsutils.items.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;

public class ItemStacks {

    private final ItemStack schloss_iron;
    private final ItemStack schloss_diamond;
    private final ItemStack schloss_emerald;
    private final ItemStack schloss_stein;
    private final ItemStack schloss_copper;
    private final ItemStack dietrich;
    private final ItemStack lochkarte;

    private final AfGLock instance;

    public static NamespacedKey nameSpacedKeyId = null, nameSpacedKeyItem = null;

    public ItemStacks(AfGLock plugin) {
        this.instance = plugin;
        this.schloss_iron = new ItemBuilder(Material.NAME_TAG).name(Values.SCHLOSS_IRON_NAME).sign("IRON_LOCK").build();
        this.schloss_diamond = new ItemBuilder(Material.NAME_TAG).name(Values.SCHLOSS_DIAMOND_NAME).sign("DIAMOND_LOCK").build();
        this.schloss_emerald = new ItemBuilder(Material.NAME_TAG).name(Values.SCHLOSS_EMERALD_NAME).sign("EMERALD_LOCK").build();
        this.schloss_stein = new ItemBuilder(Material.FLINT).name(Values.SCHLOSS_STEIN_NAME).sign("STONE_LOCK").build();
        this.schloss_copper = new ItemBuilder(Material.NAME_TAG).name(Values.SCHLOSS_COPPER_NAME).sign("COPPER_LOCK").build();
        ItemBuilder lochkartenBuilder = new ItemBuilder(Material.PAPER).name(Values.LOCHKARTE).sign("LOCHKARTE");
        lochkartenBuilder.addPDC("LOCHKARTE_ID", -1, PersistentDataType.INTEGER);
        this.lochkarte = lochkartenBuilder.build();
        this.dietrich = new ItemBuilder(Material.BLAZE_ROD).name(Values.DIETRICH_ITEM_NAME).sign("DIETRICH").build();

        initCrafting();
    }

    private void initCrafting() {

        //Iron Lock
        NamespacedKey ironK = new NamespacedKey(instance, "AFGLOCK-IRONLOCK");
        ShapedRecipe ironR = new ShapedRecipe(ironK, schloss_iron);

        ironR.shape("III", "ICI", "III");
        ironR.setIngredient('I', Material.IRON_INGOT);
        ironR.setIngredient('C', Material.COPPER_INGOT);

        //Diamond Lock
        NamespacedKey diamondK = new NamespacedKey(instance, "AFGLOCK-DIAMONDLOCK");
        ShapedRecipe diamondR = new ShapedRecipe(diamondK, schloss_diamond);

        diamondR.shape("DDD", "DED", "DDD");
        diamondR.setIngredient('D', Material.DIAMOND);
        diamondR.setIngredient('E', Material.EMERALD);

        //Emerald Lock
        NamespacedKey emeraldK = new NamespacedKey(instance, "AFGLOCK-EMERALDLOCK");
        ShapedRecipe emeraldR = new ShapedRecipe(emeraldK, schloss_emerald);

        emeraldR.shape("EEE", "EDE", "EEE");
        emeraldR.setIngredient('E', Material.EMERALD);
        emeraldR.setIngredient('D', Material.DIAMOND);

        //Copper Lock
        NamespacedKey copperK = new NamespacedKey(instance, "AFGLOCK-COPPERLOCK");
        ShapedRecipe copperR = new ShapedRecipe(copperK, schloss_copper);

        copperR.shape("CCC", "CGC", "CCC");
        copperR.setIngredient('C', Material.COPPER_INGOT);
        copperR.setIngredient('G', Material.GOLD_NUGGET);

        //Stone Lock
        NamespacedKey steinK = new NamespacedKey(instance, "AFGLOCK-STONELOCK");
        ShapedRecipe steinR = new ShapedRecipe(steinK, schloss_stein);

        steinR.shape("SAS", "AAA", "SAS");
        steinR.setIngredient('A', Material.AIR);
        steinR.setIngredient('S', Material.COBBLESTONE);

        //Dietrich
        NamespacedKey dietrichK = new NamespacedKey(instance, "AFGLOCK-DIETRICH");
        ShapedRecipe dietrichR = new ShapedRecipe(dietrichK, dietrich);

        dietrichR.shape("EAA", "CCC", "AAD");
        dietrichR.setIngredient('A', Material.AIR);
        dietrichR.setIngredient('E', Material.EMERALD);
        dietrichR.setIngredient('C', Material.CHAIN);
        dietrichR.setIngredient('D', Material.DIAMOND);

        NamespacedKey lochkarteK = new NamespacedKey(instance, "AFGLOCK-LOCHKARTE");
        ShapedRecipe lochkarteR = new ShapedRecipe(lochkarteK, lochkarte);
        lochkarteR.shape("RIR", "RPR", "RRR");
        lochkarteR.setIngredient('R', Material.REDSTONE);
        lochkarteR.setIngredient('I', Material.IRON_INGOT);
        lochkarteR.setIngredient('P', Material.PAPER);

        instance.getServer().addRecipe(ironR);
        instance.getServer().addRecipe(diamondR);
        instance.getServer().addRecipe(emeraldR);
        instance.getServer().addRecipe(dietrichR);
        instance.getServer().addRecipe(steinR);
        instance.getServer().addRecipe(lochkarteR);
        instance.getServer().addRecipe(copperR);

    }

    public ItemStack getSchloss_diamond() {
        return schloss_diamond.clone();
    }

    public ItemStack getSchloss_emerald() {
        return schloss_emerald.clone();
    }

    public ItemStack getSchloss_iron() {
        return schloss_iron.clone();
    }

    public ItemStack getSchloss_stein() {
        return schloss_stein;
    }

    public ItemStack getSchloss_copper() {
        return schloss_copper;
    }

    public ItemStack getDietrich() {
        return dietrich;
    }
}
