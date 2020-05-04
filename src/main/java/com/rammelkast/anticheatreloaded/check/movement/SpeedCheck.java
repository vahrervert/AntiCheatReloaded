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
import org.bukkit.potion.PotionEffectType;

import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.check.Backend;
import com.rammelkast.anticheatreloaded.check.CheckResult;
import com.rammelkast.anticheatreloaded.util.Distance;
import com.rammelkast.anticheatreloaded.util.Utilities;
import com.rammelkast.anticheatreloaded.util.VersionUtil;

/**
 * This check is an outdated mess which needs to be replaced
 */
public class SpeedCheck {

	public static final Map<UUID, Integer> SPEED_VIOLATIONS = new HashMap<UUID, Integer>();
	private static final CheckResult PASS = new CheckResult(CheckResult.Result.PASSED);

	public static boolean isSpeedExempt(Player player, Backend backend) {
		return backend.isMovingExempt(player) || backend.justVelocity(player) || VersionUtil.isFlying(player);
	}

	public static CheckResult checkXZSpeed(Player player, double x, double z) {
		Backend backend = AntiCheatReloaded.getManager().getBackend();
		if (!isSpeedExempt(player, backend) && player.getVehicle() == null) {
			String reason = "";
			double max = backend.getMagic().XZ_SPEED_MAX();
			if (player.getLocation().getBlock().getType() == Material.SOUL_SAND) {
				if (player.isSprinting()) {
					reason = "on soulsand while sprinting ";
					max = backend.getMagic().XZ_SPEED_MAX_SOULSAND_SPRINT();
				} else if (player.hasPotionEffect(PotionEffectType.SPEED)) {
					reason = "on soulsand with speed potion ";
					max = backend.getMagic().XZ_SPEED_MAX_SOULSAND_POTION();
				} else {
					reason = "on soulsand ";
					max = backend.getMagic().XZ_SPEED_MAX_SOULSAND();
				}
			} else if (VersionUtil.isFlying(player)) {
				reason = "while flying ";
				max = backend.getMagic().XZ_SPEED_MAX_FLY();
			} else if (player.hasPotionEffect(PotionEffectType.SPEED)) {
				if (player.isSprinting()) {
					reason = "with speed potion while sprinting ";
					max = backend.getMagic().XZ_SPEED_MAX_POTION_SPRINT();
				} else {
					reason = "with speed potion ";
					max = backend.getMagic().XZ_SPEED_MAX_POTION();
				}
			} else if (player.isSprinting()) {
				String blockDownName = player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().name();
				if (blockDownName.endsWith("ICE")
						&& (player.getEyeLocation().getBlock().getRelative(BlockFace.UP).getType() != Material.AIR)) {
					if (player.getLocation().getBlock().getType().name().endsWith("TRAPDOOR")) {
						reason = "while boosting on " + blockDownName.toLowerCase().replaceAll("_", " ")
								+ " and trapdoor";
						max = backend.getMagic().XZ_SPEED_MAX_SPRINT()
								+ (blockDownName.equals("BLUE_ICE") ? 0.35925D : 0.35D);
					} else {
						reason = "while boosting on " + blockDownName.toLowerCase().replaceAll("_", " ") + " ";
						max = backend.getMagic().XZ_SPEED_MAX_SPRINT()
								+ (blockDownName.equals("BLUE_ICE") ? 0.20925D : 0.2D);
					}
				} else {
					reason = "while sprinting ";
					max = backend.getMagic().XZ_SPEED_MAX_SPRINT();
				}
			}

			float speed = player.getWalkSpeed();
			max += speed > 0 ? player.getWalkSpeed() - 0.2f : 0;

			if (x > max || z > max) {
				int num = backend.increment(player, SPEED_VIOLATIONS, backend.getMagic().SPEED_MAX());
				if (num >= backend.getMagic().SPEED_MAX()) {
					return new CheckResult(CheckResult.Result.FAILED,
							"speed was too high " + reason + num + " times in a row (max="
									+ backend.getMagic().SPEED_MAX() + ", speed=" + (x > z ? x : z) + ", max speed="
									+ max + ")");
				} else {
					return PASS;
				}
			} else {
				SPEED_VIOLATIONS.put(player.getUniqueId(), 0);
				return PASS;
			}
		} else {
			return PASS;
		}
	}

	public static CheckResult checkYSpeed(Player player, Distance distance) {
		Backend backend = AntiCheatReloaded.getManager().getBackend();
		if (!backend.isMovingExempt(player) && !player.isInsideVehicle() && !player.isSleeping()
				&& (distance.getYDifference() > backend.getMagic().Y_SPEED_MAX())
				&& !backend.isDoing(player, backend.velocitized, backend.getMagic().VELOCITY_TIME())
				&& !player.hasPotionEffect(PotionEffectType.JUMP) && !VersionUtil.isFlying(player)
				&& !Utilities.isNearBed(distance.getTo())
				&& !Utilities.isSlime(AntiCheatReloaded.getManager().getUserManager().getUser(player.getUniqueId())
						.getGoodLocation(player.getLocation()).getBlock())) {
			return new CheckResult(CheckResult.Result.FAILED,
					"y speed was too high (speed=" + distance.getYDifference() + ", max=" + backend.getMagic().Y_SPEED_MAX() + ")");
		} else {
			return PASS;
		}
	}

}
