package net.gravitydevelopment.anticheat.util;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.mojang.authlib.GameProfile;

import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_8_R3.PlayerInteractManager;

public class NPC {

	private final EntityPlayer npc;
	
	public NPC(String name, World w, Player owner) {
		this.npc = new EntityPlayer(((CraftServer) Bukkit.getServer()).getServer(), ((CraftWorld) w).getHandle(), new GameProfile(UUID.randomUUID(), name), new PlayerInteractManager(((CraftWorld) w).getHandle()));
		((CraftPlayer)owner).getHandle().playerConnection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, new EntityPlayer[]{this.npc}));
		((CraftPlayer)owner).getHandle().playerConnection.sendPacket(new PacketPlayOutNamedEntitySpawn(this.npc));
	}
	
	public void teleport(Location loc) {
		this.npc.teleportTo(loc, false);
	}
	
}
