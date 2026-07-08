package com.mondraq.packet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public final class PacketHelper {

    private PacketHelper() {}

    public static final Material SICK_BLOCK = resolveMaterial();

    private static Material resolveMaterial() {
        try {
            return Material.valueOf("PALE_OAK_SAPLING");
        } catch (IllegalArgumentException e) {
            return Material.OAK_SAPLING;
        }
    }

    public static void sendFakeBlock(Player p, Location loc, Material mat) {
        p.sendBlockChange(loc, mat.createBlockData());
    }

    public static void resendSickBlock(Player p, Location loc) {
        p.sendBlockChange(loc, SICK_BLOCK.createBlockData());
    }

    public static void restoreBlock(Player p, Location loc) {
        p.sendBlockChange(loc, loc.getBlock().getBlockData());
    }
}
