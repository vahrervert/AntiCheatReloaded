package net.gravitydevelopment.anticheat.check.combat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

import net.gravitydevelopment.anticheat.AntiCheat;
import net.gravitydevelopment.anticheat.check.CheckResult;
import net.gravitydevelopment.anticheat.check.CheckType;
import net.gravitydevelopment.anticheat.event.EventListener;
import net.gravitydevelopment.anticheat.util.CheckerNPCS;
import net.gravitydevelopment.anticheat.util.VersionUtil;

/**
 * @author Marco
 * STILL IN HEAVY DEVELOPMENT!
 */
public class KillAuraCheck {

	private static final Map<UUID, CheckerNPCS> CHECKER_NPCS = new HashMap<UUID, CheckerNPCS>(); 
	private static final Map<UUID, Integer> VL_COUNT = new HashMap<UUID, Integer>();
	
	public static void cleanPlayer(Player p) {
		CHECKER_NPCS.remove(p.getUniqueId());
	}
	
	public static void listenPackets() {
		AntiCheat.getProtocolManager().addPacketListener(new PacketAdapter(AntiCheat.getPlugin(), ListenerPriority.NORMAL, new PacketType[] {PacketType.Play.Client.USE_ENTITY}) {
			@Override
			public void onPacketReceiving(PacketEvent e) {
				Player p = e.getPlayer();
				if (!CHECKER_NPCS.containsKey(p.getUniqueId()))
					return;
				int entityId = ((Integer)e.getPacket().getIntegers().read(0));
				boolean isBot = CHECKER_NPCS.get(p.getUniqueId()).onHit(entityId);
				if (isBot) {
					if (!VL_COUNT.containsKey(p.getUniqueId()))
						VL_COUNT.put(p.getUniqueId(), 1);
					else {
						VL_COUNT.put(p.getUniqueId(), VL_COUNT.get(p.getUniqueId()) + 1);
						if (VL_COUNT.get(p.getUniqueId()) >= AntiCheat.getManager().getBackend().getMagic().KILLAURA_BOTHITS())
							EventListener.log(new CheckResult(CheckResult.Result.FAILED, p.getName() + " failed Killaura, hit the bot " + VL_COUNT.get(p.getUniqueId()) + " times (max=" + AntiCheat.getManager().getBackend().getMagic().KILLAURA_BOTHITS() + ")").getMessage(), p, CheckType.KILLAURA);
					}
				}
			}
		});
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
