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

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.check.CheckResult;
import com.rammelkast.anticheatreloaded.util.Distance;
import com.rammelkast.anticheatreloaded.util.Utilities;
import com.rammelkast.anticheatreloaded.util.VersionUtil;

/**
 * @author Rammelkast TODO rewrite this check
 */
public class FlightCheck {

	public static final Map<UUID, Double> BLOCKS_OVER_FLIGHT = new HashMap<UUID, Double>();
	public static final Map<UUID, Long> MOVING_EXEMPT = new HashMap<UUID, Long>();
	private static final CheckResult PASS = new CheckResult(CheckResult.Result.PASSED);

	public static CheckResult runCheck(Player player, Distance distance) {
		if (distance.getYDifference() > AntiCheatReloaded.getManager().getBackend().getMagic().TELEPORT_MIN()
				|| VersionUtil.isFlying(player)) {
			// This was a teleport or user is flying/using elytra, so we don't care
			// about it.
			return PASS;
		}
		final UUID uuid = player.getUniqueId();
		final double y1 = distance.fromY();
		final double y2 = distance.toY();
		if (!isMovingExempt(player) && !Utilities.isHoveringOverWater(player.getLocation(), 1)
				&& Utilities.cantStandAtExp(player.getLocation())
				&& Utilities.blockIsnt(player.getLocation().getBlock().getRelative(BlockFace.DOWN),
						new Material[] { Material.IRON_BARS })
				&& Utilities.blockIsnt(player.getLocation().getBlock().getRelative(BlockFace.DOWN), new String[] {
					"FENCE",
					"FENCE_GATE"
				})) {

			if (!BLOCKS_OVER_FLIGHT.containsKey(uuid)) {
				BLOCKS_OVER_FLIGHT.put(uuid, 0D);
			}

			BLOCKS_OVER_FLIGHT.put(uuid, (BLOCKS_OVER_FLIGHT.get(uuid) + distance.getXDifference()
					+ distance.getYDifference() + distance.getZDifference()));

			if (y1 > y2) {
				BLOCKS_OVER_FLIGHT.put(uuid, (BLOCKS_OVER_FLIGHT.get(uuid) - distance.getYDifference()));
			}

			if (BLOCKS_OVER_FLIGHT.get(uuid) > AntiCheatReloaded.getManager().getBackend().getMagic().FLIGHT_BLOCK_LIMIT()
					&& (y1 <= y2)) {
				return new CheckResult(CheckResult.Result.FAILED,
						player.getName() + " flew over " + BLOCKS_OVER_FLIGHT.get(uuid) + " blocks (max="
								+ AntiCheatReloaded.getManager().getBackend().getMagic().FLIGHT_BLOCK_LIMIT() + ")");
			}
		} else {
			BLOCKS_OVER_FLIGHT.put(uuid, 0D);
		}

		return PASS;
	}

	public static boolean isMovingExempt(Player player) {
		return isDoing(player, MOVING_EXEMPT, -1);
	}

	private static boolean isDoing(Player player, Map<UUID, Long> map, double max) {
		if (map.containsKey(player.getUniqueId())) {
			if (max != -1) {
				if (((System.currentTimeMillis() - map.get(player.getUniqueId())) / 1000) > max) {
					map.remove(player.getUniqueId());
					return false;
				} else {
					return true;
				}
			} else {
				// Termination time has already been calculated
				if (map.get(player.getUniqueId()) < System.currentTimeMillis()) {
					map.remove(player.getUniqueId());
					return false;
				} else {
					return true;
				}
			}
		} else {
			return false;
		}
	}

}
