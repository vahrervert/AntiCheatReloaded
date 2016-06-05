package net.gravitydevelopment.anticheat.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class VersionUtil {

	public static String getVersion() {
		String version = "";
        try {
            version = Bukkit.getServer().getClass().getPackage().getName().replace(".",  ",").split(",")[3];
        } catch (ArrayIndexOutOfBoundsException whatVersionAreYouUsingException) {}
        return version;
	}
	
	public static boolean isFlying(Player p) {
		if (getVersion().equals("v1_8_R3")) {
			return p.isFlying();
		}else if (getVersion().equals("v1_9_R1")) {
			return p.isFlying() || ElytraUtil.isGliding(p);
		}else {
			return p.isFlying();
		}
	}
	
}
