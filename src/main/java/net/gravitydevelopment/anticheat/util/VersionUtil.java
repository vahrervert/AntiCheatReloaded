package net.gravitydevelopment.anticheat.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class VersionUtil {

	public static String getVersion() {
		return Bukkit.getServer().getClass().getPackage().getName().replace(".",  ",").split(",")[3];
	}
	
	public static boolean isFlying(Player p) {
		if (getVersion().equals("v1_9_R1") || getVersion().equals("v1_10_R1") || getVersion().equals("v1_11_R1")) {
			return p.isFlying() || p.isGliding();
		} else {
			return p.isFlying();
		}
	}
	
}
