package net.gravitydevelopment.anticheat.check.combat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import net.gravitydevelopment.anticheat.util.CheckerNPCS;
import net.gravitydevelopment.anticheat.util.VersionUtil;

/**
 * @author Marco
 * STILL IN HEAVY DEVELOPMENT!
 */
public class KillAuraCheck {

	private static final Map<UUID, CheckerNPCS> CHECKER_NPCS = new HashMap<UUID, CheckerNPCS>(); 
	
	public static void cleanPlayer(Player p) {
		CHECKER_NPCS.remove(p.getUniqueId());
	}

	public static void doDamageEvent(EntityDamageByEntityEvent e, Player p) {
		if (VersionUtil.getVersion().equals("v1_8_R3")) { // TODO 1.9 support
			if (!CHECKER_NPCS.containsKey(p.getUniqueId())) { // Enable killaura check after player damaged other player, this is more efficient
				CHECKER_NPCS.put(p.getUniqueId(), new CheckerNPCS(p));
			}
		}
	}
	
	public static void doMove(PlayerMoveEvent e) {
		if (CHECKER_NPCS.containsKey(e.getPlayer().getUniqueId())) { 
			CHECKER_NPCS.get(e.getPlayer().getUniqueId()).doMove(e);
		}
	}
	
}
