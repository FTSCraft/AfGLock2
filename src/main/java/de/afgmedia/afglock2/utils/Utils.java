package de.afgmedia.afglock2.utils;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Door;
import org.bukkit.inventory.DoubleChestInventory;

public class Utils {

    public static Location getLeftLocationOfDoubleChest(Block block) {
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

    public static Location getLowerLocationOfDoor(Block block) {
        Location lower;
        if (!isDoor(block.getType()))
            throw new IllegalArgumentException("given block is no door " + block.getLocation());
        Door door = (Door) block.getBlockData();
        if (door.getHalf() == Bisected.Half.TOP) {
            lower = block.getLocation().subtract(0, 1, 0);
        } else {
            if (!door.isOpen()) {
                lower = block.getLocation().subtract(0, 1, 0);
                if (isDoor(lower.getBlock().getType())) return lower;
                else return block.getLocation();
            }
            lower = block.getLocation();
        }
        return lower;
    }

    public static boolean isDoubleChest(Block block) {
        BlockState blockState = block.getState();
        if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) {
            Chest chest = (Chest) blockState;
            return chest.getInventory() instanceof DoubleChestInventory;
        }
        return false;
    }

    public static boolean isLock(String string) {
        return string.endsWith("_LOCK");
    }

    public static boolean isLockable(Material material) {

        return material == Material.CHEST || isDoor(material) || isTrapDoor(material) || material == Material.TRAPPED_CHEST || isFenceGate(material) || isBarrel(material) || material == Material.NOTE_BLOCK || material == Material.CHISELED_BOOKSHELF || material == Material.LECTERN;

    }

    public static boolean isFenceGate(Material mat) {
        return mat.toString().contains("FENCE_GATE");
    }

    public static boolean isTrapDoor(Material mat) {
        return mat.toString().contains("_TRAPDOOR");
    }

    public static boolean isDoor(Material material) {
        return material.toString().contains("_DOOR");
    }

    public static String getName(UUID uuid) {
        return Bukkit.getOfflinePlayer(uuid).getName();
    }

    public static boolean isBarrel(Material material) {
        return material == Material.BARREL;
    }
}
