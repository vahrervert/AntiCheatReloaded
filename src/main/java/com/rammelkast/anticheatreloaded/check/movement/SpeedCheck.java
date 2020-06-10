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
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.check.Backend;
import com.rammelkast.anticheatreloaded.check.CheckResult;
import com.rammelkast.anticheatreloaded.util.Distance;
import com.rammelkast.anticheatreloaded.util.MovementManager;
import com.rammelkast.anticheatreloaded.util.Utilities;
import com.rammelkast.anticheatreloaded.util.VersionUtil;

/**
 * @author Rammelkast
 *
 * Features:
 * + AirSpeed A
 * + AirAcceleration A
 * + JumpBehaviour A
 * 
 * Planned:
 * + GroundSpeed A
 * + GroundAcceleration A
 * + AirSpeed B
 * + JumpBehaviour B
 */
public class SpeedCheck {

	public static final Map<UUID, Integer> JUMPBEHAVIOUR_VIOLATIONS = new HashMap<UUID, Integer>();
	private static final CheckResult PASS = new CheckResult(CheckResult.Result.PASSED);

	public static boolean isSpeedExempt(Player player, Backend backend) {
		return backend.isMovingExempt(player) || backend.justVelocity(player) || VersionUtil.isFlying(player);
	}

	public static CheckResult checkXZSpeed(Player player, double x, double z, Location movingTowards) {
		Backend backend = AntiCheatReloaded.getManager().getBackend();
		if (isSpeedExempt(player, backend) || player.getVehicle() != null)
			return PASS;

		MovementManager movementManager = AntiCheatReloaded.getManager().getUserManager().getUser(player.getUniqueId())
				.getMovementManager();
		double distanceXZ = Math.sqrt(x * x + z * z);

		// AirSpeed A
		// As of right now, this falses with speed effects and slimes
		if (movementManager.airTicks > 1) {
			double multiplier = 0.9808305131D;
			double predict = 0.3597320645 * Math.pow(multiplier, movementManager.airTicks + 1);
			// Adjust for ice boost
			if (movementManager.iceInfluenceTicks > 0) {
				predict += 0.105 * Math.pow(1.09, movementManager.iceInfluenceTicks);
			}
			if (distanceXZ - predict > 0.03075) {
				return new CheckResult(CheckResult.Result.FAILED,
						"moved too fast in air (speed=" + distanceXZ + ", predict=" + predict + ")");
			}
		}

		// AirAcceleration A
		// As of right now, this falses with speed effects and slimes
		if (movementManager.airTicks > 1 && movementManager.iceInfluenceTicks <= 0) {
			double initialAcceleration = movementManager.acceleration;
			// TODO calculate instead of defined value
			if (initialAcceleration > 0.36) {
				return new CheckResult(CheckResult.Result.FAILED,
						"exceeded acceleration limits (acceleration=" + initialAcceleration + ", max=0.36)");
			}
		}

		// JumpBehaviour A
		// Has a rare false positive when sprintjumping around corners
		// Works against YPorts and mini jumps
		if (movementManager.touchedGroundThisTick) {
			// This happens naturally as well when walking next to walls
			if (movementManager.airTicksBeforeGrounded == movementManager.groundTicks) {
				boolean movingFreely = Utilities.cantStandClose(movingTowards.getBlock())
						&& Utilities.cantStandFar(movingTowards.getBlock());
				if (movingFreely) {
					int vl = JUMPBEHAVIOUR_VIOLATIONS.getOrDefault(player.getUniqueId(), 0);
					// TODO config for this value
					if (vl++ > 1) {
						JUMPBEHAVIOUR_VIOLATIONS.remove(player.getUniqueId());
						return new CheckResult(CheckResult.Result.FAILED,
								"had unexpected jumping behaviour");
					}
					JUMPBEHAVIOUR_VIOLATIONS.put(player.getUniqueId(), vl);
					return PASS;
				} else {
					JUMPBEHAVIOUR_VIOLATIONS.remove(player.getUniqueId());
				}
			}
		}
		return PASS;
	}

	public static CheckResult checkYSpeed(Player player, Distance distance) {
		Backend backend = AntiCheatReloaded.getManager().getBackend();
		if (!backend.isMovingExempt(player) && !player.isInsideVehicle() && !player.isSleeping()
				&& (distance.getYDifference() > backend.getMagic().Y_SPEED_MAX())
				&& !backend.isDoing(player, backend.velocitized, backend.getMagic().VELOCITY_TIME())
				&& !player.hasPotionEffect(PotionEffectType.JUMP) && !VersionUtil.isFlying(player)
				&& !VersionUtil.isRiptiding(player) && !Utilities.isNearBed(distance.getTo())
				&& !Utilities.isSlime(AntiCheatReloaded.getManager().getUserManager().getUser(player.getUniqueId())
						.getGoodLocation(player.getLocation()).getBlock())
				&& !Utilities.couldBeOnBoat(player)) {
			return new CheckResult(CheckResult.Result.FAILED, "y speed was too high (speed=" + distance.getYDifference()
					+ ", max=" + backend.getMagic().Y_SPEED_MAX() + ")");
		} else {
			return PASS;
		}
	}

}
