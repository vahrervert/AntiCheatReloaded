package net.gravitydevelopment.anticheat.util;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.mojang.authlib.GameProfile;

import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_8_R3.PlayerInteractManager;

public class NPC {

	private final EntityPlayer npc;
	
	public NPC(String name, World w, Player owner) {
		this.npc = new EntityPlayer(((CraftServer) Bukkit.getServer()).getServer(), ((CraftWorld) w).getHandle(), new GameProfile(UUID.randomUUID(), name), new PlayerInteractManager(((CraftWorld) w).getHandle()));
		((CraftPlayer)owner).getHandle().playerConnection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, new EntityPlayer[]{this.npc}));
		this.npc.setInvisible(true);
		Location eyeLocation = owner.getEyeLocation();
		Vector vec = owner.getLocation().getDirection();
		Vector newVec = new Vector(vec.getX() * -2, vec.getY() * -2, vec.getZ() * -2);
		Location backLocation = eyeLocation.add(newVec);
		if (owner.getLocation().getPitch() < -50 && owner.getLocation().getPitch() > -75)
			this.npc.setLocation(backLocation.getX(), backLocation.getY() + 1, backLocation.getZ(), 0, 0);
		else if (owner.getLocation().getPitch() >= -50)
			this.npc.setLocation(backLocation.getX(), backLocation.getY() - 0.25, backLocation.getZ(), 0, 0);
		else
			this.npc.setLocation(backLocation.getX(), backLocation.getY() + 1.75, backLocation.getZ(), 0, 0);
		((CraftPlayer)owner).getHandle().playerConnection.sendPacket(new PacketPlayOutNamedEntitySpawn(this.npc));
	}
	
	public int getEntityId() {
		return npc.getId();
	}
	
	public void move(Location loc, Player owner) {
		this.npc.setLocation(loc.getX(), loc.getY(), loc.getZ(), 0, 0);
		((CraftPlayer)owner).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityTeleport(this.npc));
	}
	
}
