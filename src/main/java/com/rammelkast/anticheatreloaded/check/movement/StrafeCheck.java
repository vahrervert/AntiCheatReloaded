/*
 * AntiCheatReloaded for Bukkit and Spigot.
 * Copyright (c) 2012-2015 AntiCheat Team
 * Copyright (c) 2016-2020 Rammelkast
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.rammelkast.anticheatreloaded.check.movement;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.check.CheckResult;
import com.rammelkast.anticheatreloaded.check.CheckResult.Result;
import com.rammelkast.anticheatreloaded.util.MovementManager;
import com.rammelkast.anticheatreloaded.util.Utilities;

/**
 * 
 * @author Rammelkast
 *
 */
public class StrafeCheck {

	private static final CheckResult PASS = new CheckResult(CheckResult.Result.PASSED);

	public static CheckResult runCheck(Player player, double x, double z, Location from, Location to) {
		if (!Utilities.cantStandAtExp(from) || !Utilities.cantStandAtExp(to))
			return PASS;
		
		MovementManager movementManager = AntiCheatReloaded.getManager().getUserManager().getUser(player.getUniqueId())
				.getMovementManager();
		
		Vector oldAcceleration = new Vector(movementManager.lastDistanceX, 0, movementManager.lastDistanceZ);
		Vector newAcceleration = new Vector(x, 0, z);
		
		// TODO fix false with bouncing off walls
		float angle = newAcceleration.angle(oldAcceleration);
		double distance = newAcceleration.lengthSquared();
		if (angle > 0.25 && distance > 0.025)
			return new CheckResult(Result.FAILED, "switched angle in air (angle=" + angle + ", dist=" + distance + ")");
		return PASS;
	}
	
}
