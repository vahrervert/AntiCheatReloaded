package net.gravitydevelopment.anticheat.check.movement;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import net.gravitydevelopment.anticheat.AntiCheat;
import net.gravitydevelopment.anticheat.check.CheckResult;
import net.gravitydevelopment.anticheat.util.Distance;

/**
 * 
 * @author Marco STILL IN ALPHA TESTING!
 */
public class GlideCheck {

	private static final CheckResult PASS = new CheckResult(CheckResult.Result.PASSED);
	
	public static CheckResult runCheck(Player player, Distance distance) {
		return PASS;
		// TODO
	}

}
