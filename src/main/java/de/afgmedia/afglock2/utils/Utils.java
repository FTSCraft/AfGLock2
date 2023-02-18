package de.afgmedia.afglock2.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.block.data.Bisected;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.block.data.type.Door;

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

        Door door = ((Door) block.getBlockData());
        Location lower;
        if (door.getHalf() == Bisected.Half.TOP) {
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
        return string.equalsIgnoreCase(Values.SCHLOSS_DIAMOND_NAME) || string.equalsIgnoreCase(Values.SCHLOSS_EMERALD_NAME) || string.equalsIgnoreCase(Values.SCHLOSS_IRON_NAME) || string.equalsIgnoreCase(Values.SCHLOSS_STEIN_NAME);
    }

    public static boolean isLockable(Material material)
    {

        return material == Material.CHEST || isDoor(material) || isTrapDoor(material) || material == Material.TRAPPED_CHEST || isFenceGate(material) || isBarrel(material) || material == Material.NOTE_BLOCK || material == Material.LECTERN;

    }

    public static boolean isFenceGate(Material mat) {
        return mat.toString().contains("FENCE_GATE");
    }

    public static boolean isTrapDoor(Material mat)
    {
        return mat.toString().contains("TRAPDOOR");
    }

    public static boolean isDoor(Material material)
    {
        return material.toString().contains("DOOR");
    }

    public static String getName(UUID uuid) {
        return Bukkit.getOfflinePlayer(uuid).getName();
    }

    public static UUID getUUID(String name) {
        return Bukkit.getOfflinePlayer(name).getUniqueId();
    }

    public static boolean isBarrel(Material material) {
        return material == Material.BARREL;
    }
}
