package com.rammelkast.anticheatreloaded.util;

import java.util.EnumSet;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class VersionUtil {

	public static String getVersion() {
		return Bukkit.getServer().getClass().getPackage().getName().replace(".",  ",").split(",")[3];
	}
	
	private static final EnumSet<Material> MOVE_UP_BLOCKS_1_8 = EnumSet.of(Material.ACACIA_STAIRS,
			Material.BIRCH_WOOD_STAIRS, Material.BRICK_STAIRS, Material.COBBLESTONE_STAIRS, Material.DARK_OAK_STAIRS,
			Material.JUNGLE_WOOD_STAIRS, Material.NETHER_BRICK_STAIRS, Material.QUARTZ_STAIRS,
			Material.RED_SANDSTONE_STAIRS, Material.SANDSTONE_STAIRS, Material.SMOOTH_STAIRS,
			Material.SPRUCE_WOOD_STAIRS, Material.WOOD_STAIRS);
	
	public static boolean isFlying(Player p) {
		if (getVersion().equals("v1_9_R1") || getVersion().equals("v1_10_R1") || getVersion().equals("v1_11_R1")) {
			return p.isFlying() || p.isGliding() || p.hasPotionEffect(NMS_1_9_PLUS.LEVITATION);
		} else {
			return p.isFlying();
		}
	}

	public static EnumSet<Material> getMoveUpBlocks() {
		if (getVersion().equals("v1_9_R1") || getVersion().equals("v1_10_R1") || getVersion().equals("v1_11_R1")) {
			return NMS_1_9_PLUS.MOVE_UP_BLOCKS_1_9;
		} else {
			return MOVE_UP_BLOCKS_1_8;
		}
	}

	public static boolean isNewYSpeed() {
		if (getVersion().equals("v1_9_R1") || getVersion().equals("v1_10_R1") || getVersion().equals("v1_11_R1")) {
			return true;
		} else {
			return false;
		}
	}

	public static long getHealTime() {
		if (getVersion().equals("v1_9_R1") || getVersion().equals("v1_10_R1") || getVersion().equals("v1_11_R1")) {
			return 495;
		} else {
			return 1995;
		}
	}
	
}
