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
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.check.CheckResult;
import com.rammelkast.anticheatreloaded.util.Distance;
import com.rammelkast.anticheatreloaded.util.Utilities;
import com.rammelkast.anticheatreloaded.util.VersionUtil;

public class YAxisCheck {

	public static final Map<UUID, Double> LAST_Y_COORD_CACHE = new HashMap<UUID, Double>();
	public static final Map<UUID, Long> LAST_Y_TIME = new HashMap<UUID, Long>();
	public static final Map<UUID, Integer> Y_AXIS_VIOLATIONS = new HashMap<UUID, Integer>();
	public static final Map<UUID, Long> LAST_Y_AXIS_VIOLATION = new HashMap<UUID, Long>();
	private static final CheckResult PASS = new CheckResult(CheckResult.Result.PASSED);

	private static boolean hasJumpPotion(Player player) {
		return player.hasPotionEffect(PotionEffectType.JUMP);
	}

	public static CheckResult runCheck(Player player, Distance distance) {
		if (distance.getYDifference() > AntiCheatReloaded.getManager().getBackend().getMagic().TELEPORT_MIN()
				|| distance.getYDifference() < 0 || VersionUtil.isFlying(player)) {
			return PASS;
		}
		if (!FlightCheck.isMovingExempt(player) && !Utilities.isClimbableBlock(player.getLocation().getBlock())
				&& !Utilities.isClimbableBlock(player.getLocation().add(0, -1, 0).getBlock())
				&& !player.isInsideVehicle() && !Utilities.isInWater(player) && !hasJumpPotion(player)
				&& !isMoveUpBlock(player.getLocation().add(0, -1, 0).getBlock())
				&& !isMoveUpBlock(player.getLocation().add(0, -1.5, 0).getBlock())) {
			double y1 = player.getLocation().getY();
			UUID uuid = player.getUniqueId();
			// Fix Y axis spam.
			if (!LAST_Y_COORD_CACHE.containsKey(uuid) || !LAST_Y_TIME.containsKey(uuid)
					|| !LAST_Y_AXIS_VIOLATION.containsKey(uuid)) {
				LAST_Y_COORD_CACHE.put(uuid, y1);
				Y_AXIS_VIOLATIONS.put(uuid, 0);
				LAST_Y_AXIS_VIOLATION.put(uuid, 0L);
				LAST_Y_TIME.put(uuid, System.currentTimeMillis());
			} else {
				if (y1 > LAST_Y_COORD_CACHE.get(uuid)
						&& Y_AXIS_VIOLATIONS.get(uuid) > AntiCheatReloaded.getManager().getBackend().getMagic()
								.Y_MAXVIOLATIONS()
						&& (System.currentTimeMillis() - LAST_Y_AXIS_VIOLATION.get(uuid)) < AntiCheatReloaded
								.getManager().getBackend().getMagic().Y_MAXVIOTIME()) {
					Location g = player.getLocation();
					Y_AXIS_VIOLATIONS.put(uuid, Y_AXIS_VIOLATIONS.get(uuid) + 1);
					LAST_Y_AXIS_VIOLATION.put(uuid, System.currentTimeMillis());
					if (!AntiCheatReloaded.getManager().getBackend().silentMode()) {
						g.setY(LAST_Y_COORD_CACHE.get(uuid));
						if (g.getBlock().getType() == Material.AIR || g.getBlock().getType() == Material.CAVE_AIR) {
							player.teleport(g);
						}
					}
					return new CheckResult(CheckResult.Result.FAILED,
							"tried to fly on y-axis " + Y_AXIS_VIOLATIONS.get(uuid) + " times (max ="
									+ AntiCheatReloaded.getManager().getBackend().getMagic().Y_MAXVIOLATIONS() + ")");
				} else {
					if (Y_AXIS_VIOLATIONS.get(uuid) > AntiCheatReloaded.getManager().getBackend().getMagic()
							.Y_MAXVIOLATIONS()
							&& (System.currentTimeMillis() - LAST_Y_AXIS_VIOLATION.get(uuid)) > AntiCheatReloaded
									.getManager().getBackend().getMagic().Y_MAXVIOTIME()) {
						Y_AXIS_VIOLATIONS.put(uuid, 0);
						LAST_Y_AXIS_VIOLATION.put(uuid, 0L);
					}
				}
				long i = System.currentTimeMillis() - LAST_Y_TIME.get(uuid);
				double diff = AntiCheatReloaded.getManager().getBackend().getMagic().Y_MAXDIFF()
						+ (Utilities.isStair(player.getLocation().add(0, -1, 0).getBlock()) ? 0.5 : 0.0);
				if ((y1 - LAST_Y_COORD_CACHE.get(uuid)) > diff
						&& i < AntiCheatReloaded.getManager().getBackend().getMagic().Y_TIME()) {
					Location g = player.getLocation();
					Y_AXIS_VIOLATIONS.put(uuid, Y_AXIS_VIOLATIONS.get(uuid) + 1);
					LAST_Y_AXIS_VIOLATION.put(uuid, System.currentTimeMillis());
					if (!AntiCheatReloaded.getManager().getBackend().silentMode()) {
						g.setY(LAST_Y_COORD_CACHE.get(uuid));
						if (g.getBlock().getType() == Material.AIR || g.getBlock().getType() == Material.CAVE_AIR) {
							player.teleport(g);
						}
					}
					return new CheckResult(CheckResult.Result.FAILED, "tried to fly on y-axis in " + i + " ms (min ="
							+ AntiCheatReloaded.getManager().getBackend().getMagic().Y_TIME() + ")");
				} else {
					if ((y1 - LAST_Y_COORD_CACHE.get(uuid)) > AntiCheatReloaded.getManager().getBackend().getMagic()
							.Y_MAXDIFF() + 1
							|| (System.currentTimeMillis() - LAST_Y_TIME.get(uuid)) > AntiCheatReloaded.getManager()
									.getBackend().getMagic().Y_TIME()) {
						LAST_Y_TIME.put(uuid, System.currentTimeMillis());
						LAST_Y_COORD_CACHE.put(uuid, y1);
					}
				}
			}
		}
		// Fix Y axis spam
		return PASS;
	}

	public static boolean isMoveUpBlock(Block block) {
		Material type = block.getType();
		return type.name().endsWith("STAIRS"); // TODO slabs/others?
	}

}
