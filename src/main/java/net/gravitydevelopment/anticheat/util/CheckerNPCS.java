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
		Vector vec = e.getFrom().getDirection();
		Vector newVec = new Vector(vec.getX() * -2, vec.getY() * -2, vec.getZ() * -2);
		Location backLocation = eyeLocation.add(newVec);
		if (e.getPlayer().getLocation().getPitch() <= -45) {// TODO better fix for this
			Vector direction = e.getFrom().getDirection();
			Vector addVector = direction.setX(direction.getX() * -2).setY(direction.getY() * -2).setZ(direction.getZ() * -2);
			backLocation.add(addVector);
		}
		one.move(backLocation, e.getPlayer());
	}

	public boolean onHit(int entityId) {
		for (NPC npc : NPCS)
			if (npc.getEntityId() == entityId)
				return true;
		return false;
	}

	public void clean(Player p) {
		if (!p.isOnline())
			return;
		for (NPC npc : NPCS)
			npc.destroy(p);
		NPCS.clear();
	}
	
}
