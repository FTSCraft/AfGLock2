package de.afgmedia.afglock2.items;

import de.afgmedia.afglock2.main.AfGLock;
import de.afgmedia.afglock2.utils.Values;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemStacks {

    private ItemStack schloss_iron, schloss_diamond, schloss_emerald, dietrich;

    private AfGLock instance;

    public ItemStacks(AfGLock plugin)
    {
        this.instance = plugin;
        this.schloss_iron = new ItemStack(Material.NAME_TAG, 1);
        this.schloss_diamond = new ItemStack(Material.NAME_TAG, 1);
        this.schloss_emerald = new ItemStack(Material.NAME_TAG, 1);
        this.dietrich = new ItemStack(Material.BLAZE_ROD, 1);

        initItems();
        initCrafting();
    }

    private void initItems()
    {

        ItemMeta ironM = schloss_iron.getItemMeta();
        ItemMeta diamondM = schloss_diamond.getItemMeta();
        ItemMeta emeraldM = schloss_emerald.getItemMeta();
        ItemMeta dietrichM = dietrich.getItemMeta();

        ironM.setDisplayName(Values.SCHLOSS_IRON_NAME);
        diamondM.setDisplayName(Values.SCHLOSS_DIAMOND_NAME);
        emeraldM.setDisplayName(Values.SCHLOSS_EMERALD_NAME);
        dietrichM.setDisplayName(Values.DIETRICH_ITEM_NAME);

        schloss_iron.setItemMeta(ironM);
        schloss_diamond.setItemMeta(diamondM);
        schloss_emerald.setItemMeta(emeraldM);
        dietrich.setItemMeta(dietrichM);

    }

    private void initCrafting() {


        //Iron Lock
        NamespacedKey ironK = new NamespacedKey(instance, "AFGLOCK-IRONLOCK");
        ShapedRecipe ironR = new ShapedRecipe(ironK, schloss_iron);

        ironR.shape("III", "IGI", "III");
        ironR.setIngredient('I', Material.IRON_INGOT);
        ironR.setIngredient('G', Material.GOLD_NUGGET);

        //Diamond Lock
        NamespacedKey diamondK = new NamespacedKey(instance, "AFGLOCK-DIAMONDLOCK");
        ShapedRecipe diamondR = new ShapedRecipe(diamondK, schloss_diamond);

        diamondR.shape("DDD","DGD","DDD");
        diamondR.setIngredient('D', Material.DIAMOND);
        diamondR.setIngredient('G', Material.GOLD_INGOT);

        //Emerald Lock
        NamespacedKey emeraldK = new NamespacedKey(instance, "AFGLOCK-EMERALDLOCK");
        ShapedRecipe emeraldR = new ShapedRecipe(emeraldK, schloss_emerald);

        emeraldR.shape("EEE","EDE","EEE");
        emeraldR.setIngredient('E', Material.EMERALD);
        emeraldR.setIngredient('D', Material.DIAMOND);

        //Dietrich
        NamespacedKey dietrichK = new NamespacedKey(instance, "AFGLOCK-DIETRICH");
        ShapedRecipe dietrichR = new ShapedRecipe(dietrichK, dietrich);

        dietrichR.shape("EIE","IBI","EIE");
        dietrichR.setIngredient('E', Material.EMERALD);
        dietrichR.setIngredient('I', Material.IRON_INGOT);
        dietrichR.setIngredient('B', Material.BLAZE_ROD);

        instance.getServer().addRecipe(ironR);
        instance.getServer().addRecipe(diamondR);
        instance.getServer().addRecipe(emeraldR);
        instance.getServer().addRecipe(dietrichR);

    }

    public ItemStack getSchloss_diamond()
    {
        return schloss_diamond.clone();
    }

    public ItemStack getSchloss_emerald()
    {
        return schloss_emerald.clone();
    }

    public ItemStack getSchloss_iron()
    {
        return schloss_iron.clone();
    }

    public ItemStack getDietrich() {
        return dietrich;
    }
}
