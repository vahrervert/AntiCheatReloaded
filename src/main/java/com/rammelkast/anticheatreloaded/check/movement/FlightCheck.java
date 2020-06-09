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
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.check.Backend;
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
	public static final Map<UUID, Integer> ASCENSION_COUNT = new HashMap<UUID, Integer>();
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
						new Material[] { Utilities.IRON_BARS })
				&& Utilities.blockIsnt(player.getLocation().getBlock().getRelative(BlockFace.DOWN),
						new String[] { "FENCE", "FENCE_GATE" })
				&& !Utilities.couldBeOnBoat(player)) {
			if (!BLOCKS_OVER_FLIGHT.containsKey(uuid)) {
				BLOCKS_OVER_FLIGHT.put(uuid, 0D);
			}

			BLOCKS_OVER_FLIGHT.put(uuid, (BLOCKS_OVER_FLIGHT.get(uuid) + distance.getXDifference()
					+ distance.getYDifference() + distance.getZDifference()));

			if (y1 > y2) {
				BLOCKS_OVER_FLIGHT.put(uuid, (BLOCKS_OVER_FLIGHT.get(uuid) - distance.getYDifference()));
			}

			if (BLOCKS_OVER_FLIGHT.get(uuid) > AntiCheatReloaded.getManager().getBackend().getMagic()
					.FLIGHT_BLOCK_LIMIT() && (y1 <= y2)) {
				return new CheckResult(CheckResult.Result.FAILED,
						"flew over " + BLOCKS_OVER_FLIGHT.get(uuid) + " blocks (max="
								+ AntiCheatReloaded.getManager().getBackend().getMagic().FLIGHT_BLOCK_LIMIT() + ")");
			}
		} else {
			BLOCKS_OVER_FLIGHT.put(uuid, 0D);
		}
		return PASS;
	}
	
	public static CheckResult checkAscension(Player player, double y1, double y2) {
		Backend backend = AntiCheatReloaded.getManager().getBackend();
		int max = backend.getMagic().ASCENSION_COUNT_MAX();
		String string = "";
		if (player.hasPotionEffect(PotionEffectType.JUMP)) {
			max += 12;
			string = " with jump potion";
		}
		Block block = player.getLocation().getBlock();
		if (!isMovingExempt(player) && !Utilities.isNearWater(player) && !VersionUtil.isFlying(player)
				&& !backend.justBroke(player) && !Utilities.isClimbableBlock(player.getLocation().getBlock())
				&& !Utilities.isClimbableBlock(player.getEyeLocation().getBlock())
				&& !Utilities.isClimbableBlock(player.getLocation().clone().add(0, -0.98, 0).getBlock())
				&& !player.isInsideVehicle() && !YAxisCheck.isMoveUpBlock(player.getLocation().add(0, -1, 0).getBlock())
				&& !YAxisCheck.isMoveUpBlock(player.getLocation().add(0, -0.5, 0).getBlock())) {
			// TODO isMoveUpBlock does not seem to be a success with stairs
			// I suddenly could not replicate this, man this check is a mess
			if (y1 < y2) {
				if (!block.getRelative(BlockFace.NORTH).isLiquid() && !block.getRelative(BlockFace.SOUTH).isLiquid()
						&& !block.getRelative(BlockFace.EAST).isLiquid()
						&& !block.getRelative(BlockFace.WEST).isLiquid()) {
					backend.increment(player, ASCENSION_COUNT, max);
					if (ASCENSION_COUNT.get(player.getUniqueId()) >= max) {
						return new CheckResult(CheckResult.Result.FAILED,
								"ascended " + ASCENSION_COUNT.get(player.getUniqueId()) + " times in a row (max = " + max
										+ string + ")");
					}
				}
			} else {
				ASCENSION_COUNT.put(player.getUniqueId(), 0);
			}
		}
		return PASS;
	}

	public static boolean isMovingExempt(Player player) {
		return isDoing(player, MOVING_EXEMPT, -1) || player.isInsideVehicle();
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
