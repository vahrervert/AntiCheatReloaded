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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.check.CheckResult;
import com.rammelkast.anticheatreloaded.util.Distance;
import com.rammelkast.anticheatreloaded.util.Utilities;
import com.rammelkast.anticheatreloaded.util.VersionUtil;

public class GlideCheck {

	public static final Map<UUID, Double> LAST_MOTION_Y = new HashMap<UUID, Double>();
	public static final Map<UUID, Float> LAST_FALL_DISTANCE = new HashMap<UUID, Float>();
	public static final Map<UUID, Integer> AIR_TICKS = new HashMap<UUID, Integer>();
	public static final Map<UUID, Integer> VIOLATIONS = new HashMap<UUID, Integer>();
	private static final CheckResult PASS = new CheckResult(CheckResult.Result.PASSED);

	public static CheckResult runCheck(Player player, Distance distance) {
		if (distance.getYDifference() > AntiCheatReloaded.getManager().getBackend().getMagic().TELEPORT_MIN()
				|| VersionUtil.isFlying(player) || FlightCheck.isMovingExempt(player)) {
			VIOLATIONS.put(player.getUniqueId(), 0);
			return PASS;
		}
		
		if (player.getLocation().getBlock().getType() == Material.LADDER
				|| player.getLocation().getBlock().getRelative(0, 1, 0).getType() == Material.LADDER) {
			return PASS;
		}
		
		if (!LAST_MOTION_Y.containsKey(player.getUniqueId())) {
			LAST_MOTION_Y.put(player.getUniqueId(), distance.getYDifference());
			return PASS;
		}
		
		if (!LAST_FALL_DISTANCE.containsKey(player.getUniqueId())) {
			LAST_FALL_DISTANCE.put(player.getUniqueId(), player.getFallDistance());
			return PASS;
		}
		
		int violations = VIOLATIONS.getOrDefault(player.getUniqueId(), 0);
		double motionY = distance.getYDifference();
		float fallDistance = player.getFallDistance();
		if (motionY == LAST_MOTION_Y.get(player.getUniqueId())
				&& fallDistance > LAST_FALL_DISTANCE.get(player.getUniqueId())) {
			if (violations + 1 >= AntiCheatReloaded.getManager().getBackend().getMagic().GLIDE_LIMIT()) {
				VIOLATIONS.put(player.getUniqueId(), 0);
				if (!AntiCheatReloaded.getManager().getBackend().silentMode()) {
					Location prev = player.getLocation().clone();
					prev.setY(prev.getY() - distanceToFall(prev));
					player.teleport(AntiCheatReloaded.getManager().getUserManager().getUser(player.getUniqueId())
							.getGoodLocation(prev));
				}
				return new CheckResult(CheckResult.Result.FAILED,
						"had gliding behaviour (amt=" + Utilities.roundDouble(motionY, 5) + ")");
			} else {
				VIOLATIONS.put(player.getUniqueId(), violations + 1);
				return PASS;
			}
		}
		LAST_MOTION_Y.put(player.getUniqueId(), distance.getYDifference());
		LAST_FALL_DISTANCE.put(player.getUniqueId(), player.getFallDistance());
		if (violations > Math.round(AntiCheatReloaded.getManager().getBackend().getMagic().GLIDE_LIMIT() / 2)) {
			VIOLATIONS.put(player.getUniqueId(), violations - 1);
		}
		return PASS;
	}

	private static double distanceToFall(Location location) {
		location = location.clone();
		double highestY = location.getWorld().getHighestBlockYAt(location) + 1.02D;
		return location.getY() - highestY;
	}

}
