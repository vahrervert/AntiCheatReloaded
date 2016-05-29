package net.gravitydevelopment.anticheat.util;

import org.bukkit.Bukkit;

public class VersionUtil {

	public static String getVersion() {
		String version = "";
        try {
            version = Bukkit.getServer().getClass().getPackage().getName().replace(".",  ",").split(",")[3];
        } catch (ArrayIndexOutOfBoundsException whatVersionAreYouUsingException) {}
        return version;
	}
	
}
