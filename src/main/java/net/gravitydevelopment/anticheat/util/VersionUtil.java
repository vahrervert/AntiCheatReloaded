package net.gravitydevelopment.anticheat.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class VersionUtil {

	public static String getVersion() {
		return Bukkit.getServer().getClass().getPackage().getName().replace(".",  ",").split(",")[3];
	}
	
	public static boolean isFlying(Player p) {
		if (getVersion().equals("v1_8_R3")) {
			return p.isFlying();
		}else {
			return p.isFlying() || p.isGliding();
		}
	}
	
}
