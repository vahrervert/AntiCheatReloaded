package net.gravitydevelopment.anticheat.check.combat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

import net.gravitydevelopment.anticheat.AntiCheat;
import net.gravitydevelopment.anticheat.check.CheckResult;
import net.gravitydevelopment.anticheat.check.CheckType;
import net.gravitydevelopment.anticheat.event.EventListener;

public class VelocityCheck {

	private static final Map<UUID, Integer> VL_COUNT = new HashMap<UUID, Integer>();
	
	public static void cleanPlayer(Player p) {
		VL_COUNT.remove(p.getUniqueId());
	}
	
	public static void runCheck(EntityDamageByEntityEvent e, final Player p) {
		if (AntiCheat.getManager().getCheckManager().isOpExempt(p) || AntiCheat.getManager().getCheckManager().isExempt(p, CheckType.VELOCITY))
			return;
		final Location then = p.getLocation();
		new BukkitRunnable() {
			@Override
			public void run() {
				if (then.distance(p.getLocation()) < 0.125) {
					if (!VL_COUNT.containsKey(p.getUniqueId()))
						VL_COUNT.put(p.getUniqueId(), 1);
					else {
						VL_COUNT.put(p.getUniqueId(), VL_COUNT.get(p.getUniqueId()) + 1);
						if (VL_COUNT.get(p.getUniqueId()) > AntiCheat.getManager().getBackend().getMagic().VELOCITY_AMT()) {
							VL_COUNT.remove(p.getUniqueId());
							EventListener.log(new CheckResult(CheckResult.Result.FAILED, p.getName() + " failed Velocity, had zero/low velocity " + VL_COUNT.get(p.getUniqueId()) + " times (max=" + AntiCheat.getManager().getBackend().getMagic().VELOCITY_AMT() + ")").getMessage(), p, CheckType.VELOCITY);
						}
					}
				}else {
					VL_COUNT.remove(p.getUniqueId());
				}
			}
		}.runTaskLater(AntiCheat.getPlugin(), 4);
	}
	
}
