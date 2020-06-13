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
import org.bukkit.block.BlockFace;
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
 * + VerticalSpeed
 * 
 * Planned:
 * + GroundSpeed A
 * + GroundAcceleration A
 * + AirSpeed B
 * + JumpBehaviour B
 */
public class SpeedCheck {

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
		boolean boxedIn = movementManager.topSolid && movementManager.bottomSolid;
		
		// AirSpeed A
		if (movementManager.airTicks > 1) {
			double multiplier = 0.9808305131D;
			double predict = 0.3597320645 * Math.pow(multiplier, movementManager.airTicks + 1);
			double limit = 0.03075D;
			// Adjust for ice
			if (movementManager.iceInfluenceTicks > 0) {
				double iceIncrement = 0.02 * Math.pow(1.0375, movementManager.iceInfluenceTicks);
				// Clamp to max value
				if (iceIncrement > 0.18D)
					iceIncrement = 0.18D;
				if (boxedIn)
					iceIncrement += 0.5D;
				predict += iceIncrement;
			}
			// Adjust for slime
			if (movementManager.slimeInfluenceTicks > 0) {
				double slimeIncrement = 0.022 * Math.pow(1.0375, movementManager.slimeInfluenceTicks);
				// Clamp to max value
				if (slimeIncrement > 0.12D)
					slimeIncrement = 0.12D;
				predict += slimeIncrement;
			}
			// Adjust for speed effects
			if (player.hasPotionEffect(PotionEffectType.SPEED))
				predict += player.getPotionEffect(PotionEffectType.SPEED).getAmplifier() * 0.05D;
			// Adjust for custom walking speed
			predict += 1.4D * (Math.pow(1.1, ((player.getWalkSpeed() / 0.20) - 1)) - 1);
			// Slabs sometimes allow for a slight boost after jump
			if (movementManager.halfMovementHistoryCounter > 0)
				predict *= 2.125D;
			if (Utilities.couldBeOnHalfblock(movingTowards) && movementManager.halfMovementHistoryCounter == 0)
				predict *= 1.25D;
			if (distanceXZ - predict > limit) {
				return new CheckResult(CheckResult.Result.FAILED,
						"moved too fast in air (speed=" + distanceXZ + ", limit=" + predict + ")");
			}
		}

		// AirAcceleration A
		// As of right now, this falses sometimes, dont know why
		if (movementManager.airTicks > 1 && movementManager.iceInfluenceTicks <= 0 && movementManager.slimeInfluenceTicks <= 0) {
			double initialAcceleration = movementManager.acceleration;
			double limit = 0.3725D;
			// Adjust for speed effects
			if (player.hasPotionEffect(PotionEffectType.SPEED) )
				limit += player.getPotionEffect(PotionEffectType.SPEED).getAmplifier() * 0.0225D;
			// Adjust for slabs
			if (movementManager.halfMovementHistoryCounter > 15)
				limit *= 2.0D;
			// Adjust for custom walking speed
			limit += 1.4D * (Math.pow(1.1, ((player.getWalkSpeed() / 0.20) - 1)) - 1);
			if (initialAcceleration > limit) {
				return new CheckResult(CheckResult.Result.FAILED,
						"exceeded acceleration limits (acceleration=" + initialAcceleration + ", max=" + limit + ")");
			}
		}

		// JumpBehaviour A
		// Works against YPorts and mini jumps
		if (movementManager.touchedGroundThisTick && !boxedIn && movementManager.slimeInfluenceTicks <= 10) {
			// This happens naturally as well when walking next to walls
			if (movementManager.airTicksBeforeGrounded == movementManager.groundTicks) {
				boolean movingFreely = Utilities.cantStandClose(movingTowards.getBlock())
						&& Utilities.cantStandFar(movingTowards.getBlock());
				// TODO calculation for distanceXZ value
				if (movingFreely && distanceXZ >= 0.42) {
					return new CheckResult(CheckResult.Result.FAILED,
							"had unexpected jumping behaviour");
				}
			}
		}
		
		// GroundSpeed A
		if (movementManager.groundTicks > 1) {
			 double limit = 0.34 - 0.0055 * Math.min(9, movementManager.groundTicks);
			 if (movementManager.groundTicks < 5)
				 limit += 0.1D;
			 if (!Utilities.cantStandAtExp(movingTowards.clone().add(0, 0.51, 0))
						|| !Utilities.cantStandAtExp(movingTowards.clone().add(0, 0.11, 0)))
				 limit *= 2.2D;
			 if (movementManager.halfMovementHistoryCounter > 8)
				 limit += 0.2D;
			 if (movementManager.iceInfluenceTicks >= 55) {
				 if (!Utilities.isIce(movingTowards.getBlock().getRelative(BlockFace.DOWN)))
					 limit += 0.75D;
				 else {
					 if (movementManager.topSolid && movementManager.bottomSolid)
						 limit *= 2.5D;
					 else
						 limit += 0.03D;
				 }
			 }
			 if (Utilities.isNearBed(movingTowards) || Utilities.couldBeOnHalfblock(movingTowards)
						|| Utilities.isNearBed(movingTowards.clone().add(0, -0.5, 0)))
				 limit *= 2.0D;
			 if (Utilities.couldBeOnBoat(player))
				 limit += 0.2D;
			 if (movementManager.signInfluenceTicks > 0)
				 limit += 0.08D;
			 if (distanceXZ - limit > 0) {
				 return new CheckResult(CheckResult.Result.FAILED,
							"moved too fast on ground (speed=" + distanceXZ + ", limit=" + limit + ")");
			 }
		}
		return PASS;
	}

	public static CheckResult checkVerticalSpeed(Player player, Distance distance) {
		Backend backend = AntiCheatReloaded.getManager().getBackend();
		MovementManager movementManager = AntiCheatReloaded.getManager().getUserManager().getUser(player.getUniqueId())
				.getMovementManager();
		if (player.isInsideVehicle() || player.isSleeping()
				|| backend.isDoing(player, backend.velocitized, backend.getMagic().VELOCITY_TIME()) || VersionUtil.isFlying(player)
				|| VersionUtil.isRiptiding(player)) {
			return PASS;
		}
		
		double maxMotionY = getMaxAcceptableMotionY(player, Utilities.isNearBed(distance.getTo()),
				Utilities.couldBeOnBoat(player), Utilities.isClimbableBlock(distance.getFrom().getBlock())
						|| Utilities.isClimbableBlock(distance.getFrom().getBlock().getRelative(BlockFace.DOWN)));
		if (movementManager.motionY > maxMotionY && movementManager.slimeInfluenceTicks <= 0
				&& !movementManager.halfMovement) {
			return new CheckResult(CheckResult.Result.FAILED,
					"exceeded vertical speed limit (mY=" + movementManager.motionY + ", max=" + maxMotionY + ")");
		}
		return PASS;
	}
	
	private static double getMaxAcceptableMotionY(Player player, boolean nearBed, boolean couldBeOnBoat, boolean fromClimbable) { 
		double base = couldBeOnBoat ? 0.600000025 : (nearBed ? 0.5625 : 0.42);
		if (fromClimbable)
			base += 0.04;
		if (player.hasPotionEffect(PotionEffectType.JUMP) ) {
			base += player.getPotionEffect(PotionEffectType.JUMP).getAmplifier() * 0.15D;
		}
		return base;
	}

}
