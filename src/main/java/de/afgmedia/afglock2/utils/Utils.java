package de.afgmedia.afglock2.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.material.Door;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Openable;
import sun.awt.SunHints;

import java.util.Random;
import java.util.UUID;

public class Utils {

    private static final Random random = new Random();

    public static Location getLeftLocationOfDoubleChest(Block block)
    {

        BlockState blockState = block.getState();

        if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) {
            Chest chest = (Chest) blockState;
            if (chest.getInventory() instanceof DoubleChestInventory) {
                DoubleChest doubleChest = (DoubleChest) chest.getInventory().getHolder();
                return ((Chest) doubleChest.getLeftSide()).getLocation();
            }
        }
        return block.getLocation();
    }

    public static Location getLowerLocationOfDoor(Block block)
    {
        if(!isDoor(block.getType())) {
            return null;
        }

        BlockState blockState = block.getState();
        Door door = ((Door) blockState.getData());
        Location lower;
        if (door.isTopHalf()) {
            lower = block.getLocation().subtract(0, 1, 0);
        }
        else {
            if(!door.isOpen()) {
                lower = block.getLocation().subtract(0, 1, 0);
                if(isDoor(lower.getBlock().getType()))
                    return lower;
                else return block.getLocation();
            }
            lower = block.getLocation();
        }
        return lower;

    }

    public static boolean isDoubleChest(Block block)
    {
        BlockState blockState = block.getState();

        if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) {
            Chest chest = (Chest) blockState;
            return chest.getInventory() instanceof DoubleChestInventory;
        }
        return false;
    }

    public static int getOneOrTwo()
    {

        if (random.nextBoolean())
            return 1;
        else return 2;

    }

    public static boolean isLock(String string)
    {
        return string.equalsIgnoreCase(Values.SCHLOSS_DIAMOND_NAME) || string.equalsIgnoreCase(Values.SCHLOSS_EMERALD_NAME) || string.equalsIgnoreCase(Values.SCHLOSS_IRON_NAME);
    }

    public static boolean isLockable(Material material)
    {

        return material == Material.CHEST || isDoor(material) || isTrapDoor(material) || material == Material.TRAPPED_CHEST;

    }

    public static boolean isTrapDoor(Material material)
    {
        return material == Material.ACACIA_TRAPDOOR || material == Material.BIRCH_TRAPDOOR || material == Material.DARK_OAK_TRAPDOOR || material == Material.JUNGLE_TRAPDOOR ||
                material == Material.OAK_TRAPDOOR || material == Material.SPRUCE_TRAPDOOR;
    }

    public static boolean isDoor(Material material)
    {
        return material == Material.OAK_DOOR || material == Material.ACACIA_DOOR || material == Material.BIRCH_DOOR || material == Material.DARK_OAK_DOOR ||
                material == Material.JUNGLE_DOOR || material == Material.SPRUCE_DOOR;
    }

    public static String getName(UUID uuid) {
        return Bukkit.getOfflinePlayer(uuid).getName();
    }

    public static UUID getUUID(String name) {
        return Bukkit.getOfflinePlayer(name).getUniqueId();
    }

}
