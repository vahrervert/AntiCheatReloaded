package net.gravitydevelopment.anticheat.util;

import org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_9_R1.EntityPlayer;

public class ElytraUtil_R1 {

	public static boolean isGliding(Player p) {
		EntityPlayer player = ((CraftPlayer)p).getHandle();
		return player.cB();
	}

}
