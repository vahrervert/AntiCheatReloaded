package net.gravitydevelopment.anticheat.check.movement;

import org.bukkit.entity.Player;

import net.gravitydevelopment.anticheat.check.CheckResult;
import net.gravitydevelopment.anticheat.util.Distance;

/**
 * TODO
 */
public class ElytraFly {

	private static final CheckResult PASS = new CheckResult(CheckResult.Result.PASSED);
	
	public static CheckResult runCheck(Player player, Distance distance) {
		return PASS;
	}
	
}
