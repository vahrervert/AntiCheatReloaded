package com.rammelkast.anticheatreloaded.check.movement;

import org.bukkit.entity.Player;

import com.rammelkast.anticheatreloaded.check.CheckResult;
import com.rammelkast.anticheatreloaded.util.Distance;

/**
 * TODO
 */
public class ElytraFly {

	private static final CheckResult PASS = new CheckResult(CheckResult.Result.PASSED);
	
	public static CheckResult runCheck(Player player, Distance distance) {
		return PASS;
	}
	
}
