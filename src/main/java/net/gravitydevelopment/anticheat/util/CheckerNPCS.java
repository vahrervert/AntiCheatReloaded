package net.gravitydevelopment.anticheat.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

public class CheckerNPCS {
	
	private List<NPC> NPCS = new ArrayList<NPC>();
	
	public CheckerNPCS(Player p) {
		NPC npcOne = new NPC(NameUtil.getRandomName(), p.getWorld(), p);
		NPCS.add(npcOne);
	}

	public void doMove(PlayerMoveEvent e) {
		NPC one = NPCS.get(0);
		Location eyeLocation = e.getPlayer().getEyeLocation();
		Vector vec = e.getPlayer().getLocation().getDirection();
		Vector newVec = new Vector(vec.getX() * -2, vec.getY() * -2, vec.getZ() * -2);
		Location backLocation = eyeLocation.add(newVec);
		one.move(backLocation, e.getPlayer());
	}
	
}
