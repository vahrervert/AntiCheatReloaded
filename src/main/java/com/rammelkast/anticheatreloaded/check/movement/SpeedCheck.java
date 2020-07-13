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
import com.rammelkast.anticheatreloaded.check.CheckType;
import com.rammelkast.anticheatreloaded.config.providers.Checks;
import com.rammelkast.anticheatreloaded.util.Distance;
import com.rammelkast.anticheatreloaded.util.MovementManager;
import com.rammelkast.anticheatreloaded.util.Utilities;
import com.rammelkast.anticheatreloaded.util.VersionUtil;

/**
 * @author Rammelkast
 */
public class SpeedCheck {

	private static final CheckResult PASS = new CheckResult(CheckResult.Result.PASSED);

	public static boolean isSpeedExempt(Player player, Backend backend) {
		return backend.isMovingExempt(player) || backend.justVelocity(player) || VersionUtil.isFlying(player);
	}

	public static CheckResult checkXZSpeed(Player player, double x, double z, Location movingTowards) {
		Backend backend = AntiCheatReloaded.getManager().getBackend();
		if (isSpeedExempt(player, backend) || player.getVehicle() != null || Utilities.isInWater(player))
			return PASS;

		MovementManager movementManager = AntiCheatReloaded.getManager().getUserManager().getUser(player.getUniqueId())
				.getMovementManager();
		Checks checksConfig = AntiCheatReloaded.getManager().getConfiguration().getChecks();
		double distanceXZ = movementManager.distanceXZ;
		boolean boxedIn = movementManager.topSolid && movementManager.bottomSolid;
		
		// AirSpeed
		if (checksConfig.isSubcheckEnabled(CheckType.SPEED, "airSpeed") && movementManager.airTicks > 1 && movementManager.elytraEffectTicks <= 0) {
			double multiplier = 0.985D;
			double predict = 0.36 * Math.pow(multiplier, movementManager.airTicks + 1);
			double limit = checksConfig.getDouble(CheckType.SPEED, "airSpeed", "baseLimit"); // Default 0.03125
			// Adjust for ice
			if (movementManager.iceInfluenceTicks > 0) {
				double iceIncrement = 0.025 * Math.pow(1.038, movementManager.iceInfluenceTicks);
				// Clamp to max value
				if (iceIncrement > 0.18D)
					iceIncrement = 0.18D;
				if (boxedIn)
					iceIncrement += 0.45D;
				if (!Utilities.couldBeOnIce(movingTowards))
					 iceIncrement *= 2.5D;
				predict += iceIncrement;
			}
			// Leniency when boxed in
			if (boxedIn && movementManager.airTicks < 3)
				limit *= 1.2D;
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
				predict += (player.getPotionEffect(PotionEffectType.SPEED).getAmplifier() + 1) * 0.05D;
			// Adjust for jump boost effects
			if (player.hasPotionEffect(PotionEffectType.JUMP))
				predict += (player.getPotionEffect(PotionEffectType.JUMP).getAmplifier() + 1) * 0.05D;
			// Adjust for custom walking speed
			double walkSpeedMultiplier = checksConfig.getDouble(CheckType.SPEED, "airSpeed", "walkSpeedMultiplier"); // Default 1.4
			predict += walkSpeedMultiplier * (Math.pow(1.1, ((player.getWalkSpeed() / 0.20) - 1)) - 1);
			// Slabs sometimes allow for a slight boost after jump
			if (movementManager.halfMovementHistoryCounter > 0)
				predict *= 2.125D;
			if (Utilities.couldBeOnHalfblock(movingTowards) && movementManager.halfMovementHistoryCounter == 0)
				predict *= 1.25D;
			// Boats sometimes give a false positive
			if (Utilities.couldBeOnBoat(player, 0.5))
				predict *= 1.25D;
			// Strafing in air when nearing terminal velocity gives false positives
			// This fixes the issue but gives hackers some leniency which means we need another check for this
			double deltaMotionY = movementManager.motionY - movementManager.lastMotionY;
			if (deltaMotionY < 0 && deltaMotionY >= -0.06)
				predict *= 1.5D;
			// Players can move faster in air with slow falling
			if (VersionUtil.isSlowFalling(player))
				predict *= 1.25D;
			// Prevent NoSlow
			// TODO better way for this
			if (player.isBlocking() && movementManager.airTicks > 2)
				predict *= 0.8D;
			
			if (distanceXZ - predict > limit) {
				return new CheckResult(CheckResult.Result.FAILED, "moved too fast in air (speed=" + distanceXZ
						+ ", limit=" + predict + ", block=" + player.isBlocking() + ", box=" + boxedIn + ", at=" + movementManager.airTicks + ")");
			}
		}

		// AirAcceleration
		// As of right now, this falses sometimes, dont know why
		if (checksConfig.isSubcheckEnabled(CheckType.SPEED, "airAcceleration") && movementManager.airTicks > 1
				&& movementManager.iceInfluenceTicks <= 0 && movementManager.slimeInfluenceTicks <= 0
				&& movementManager.elytraEffectTicks <= 0) {
			double initialAcceleration = movementManager.acceleration;
			double limit = checksConfig.getDouble(CheckType.SPEED, "airAcceleration", "baseLimit"); // Default 0.3725
			// Slight increase when boxed in
			if (boxedIn)
				limit *= 1.05D;
			// Adjust for speed effects
			if (player.hasPotionEffect(PotionEffectType.SPEED))
				limit += (player.getPotionEffect(PotionEffectType.SPEED).getAmplifier() + 1) * 0.0225D;
			// Adjust for slabs
			if (movementManager.halfMovementHistoryCounter > 15)
				limit *= 2.0D;
			// Adjust for custom walking speed
			double walkSpeedMultiplier = checksConfig.getDouble(CheckType.SPEED, "airAcceleration", "walkSpeedMultiplier"); // Default 1.4
			limit += walkSpeedMultiplier * (Math.pow(1.1, ((player.getWalkSpeed() / 0.20) - 1)) - 1);
			// Boats sometimes give a false positive
			if (Utilities.couldBeOnBoat(player))
				limit *= 1.25D;
			if (initialAcceleration > limit) {
				return new CheckResult(CheckResult.Result.FAILED,
						"exceeded acceleration limits (acceleration=" + initialAcceleration + ", max=" + limit + ")");
			}
		}

		// JumpBehaviour
		// Works against YPorts and mini jumps
		if (checksConfig.isSubcheckEnabled(CheckType.SPEED, "jumpBehaviour") && movementManager.touchedGroundThisTick
				&& !boxedIn && movementManager.slimeInfluenceTicks <= 10) {
			// This happens naturally
			if (movementManager.airTicksBeforeGrounded == movementManager.groundTicks) {
				double minimumDistXZ = checksConfig.getDouble(CheckType.SPEED, "jumpBehaviour", "minimumDistXZ"); // Default 0.42
				if (distanceXZ >= minimumDistXZ) {
					return new CheckResult(CheckResult.Result.FAILED, "had unexpected jumping behaviour");
				}
			}
		}
		
		// GroundSpeed
		if (checksConfig.isSubcheckEnabled(CheckType.SPEED, "groundSpeed") && movementManager.groundTicks > 1) {
			double initialLimit = checksConfig.getDouble(CheckType.SPEED, "groundSpeed", "initialLimit"); // Default 0.34
			double limit = initialLimit - 0.0055 * Math.min(9, movementManager.groundTicks);
			// Leniency when moving back on ground
			if (movementManager.groundTicks < 5)
				limit += 0.1D;
			// Slab leniency
			if (movementManager.halfMovementHistoryCounter > 8)
				limit += 0.2D;
			// Leniency when boxed in
			if (boxedIn)
				limit *= 1.1D;
			// Adjust for speed effects
			if (player.hasPotionEffect(PotionEffectType.SPEED))
				limit += (player.getPotionEffect(PotionEffectType.SPEED).getAmplifier() + 1) * 0.06D;
			if (movementManager.iceInfluenceTicks >= 50) {
				// When moving off ice
				if (!Utilities.couldBeOnIce(movingTowards))
					limit *= 2.5D;
				else {
					// When boxed in and spamming space for boost
					if (movementManager.topSolid && movementManager.bottomSolid)
						limit *= 3.0D;
					else
						limit *= 1.25D;
				}
			}
			// Increased speed when stepping on/off half blocks
			if (Utilities.isNearBed(movingTowards) || Utilities.couldBeOnHalfblock(movingTowards)
					|| Utilities.isNearBed(movingTowards.clone().add(0, -0.5, 0)))
				limit *= 2.0D;
			// Increased speed when stepping on/off boat
			if (Utilities.couldBeOnBoat(player))
				limit += 0.2D;
			// Prevent NoSlow
			if (player.isBlocking() && movementManager.groundTicks > 2)
				limit *= 0.45D;
			if (distanceXZ - limit > 0) {
				return new CheckResult(CheckResult.Result.FAILED,
						"moved too fast on ground (speed=" + distanceXZ + ", limit=" + limit + ", blocking=" + player.isBlocking() + ")");
			}
		}
		return PASS;
	}

	public static CheckResult checkVerticalSpeed(Player player, Distance distance) {
		Backend backend = AntiCheatReloaded.getManager().getBackend();
		MovementManager movementManager = AntiCheatReloaded.getManager().getUserManager().getUser(player.getUniqueId())
				.getMovementManager();
		Checks checksConfig = AntiCheatReloaded.getManager().getConfiguration().getChecks();
		if (!checksConfig.isSubcheckEnabled(CheckType.SPEED, "verticalSpeed"))
			return PASS;
		
		if (player.isInsideVehicle() || player.isSleeping()
				|| backend.isDoing(player, backend.velocitized, backend.getMagic().VELOCITY_SCHETIME()) || VersionUtil.isFlying(player)
				|| VersionUtil.isRiptiding(player)) {
			return PASS;
		}
		
		double maxMotionY = getMaxAcceptableMotionY(player, Utilities.isNearBed(distance.getTo()),
				Utilities.couldBeOnBoat(player), Utilities.isClimbableBlock(distance.getFrom().getBlock())
						|| Utilities.isClimbableBlock(distance.getFrom().getBlock().getRelative(BlockFace.DOWN)), movementManager.halfMovement, checksConfig);
		if (movementManager.motionY > maxMotionY && movementManager.slimeInfluenceTicks <= 0) {
			return new CheckResult(CheckResult.Result.FAILED,
					"exceeded vertical speed limit (mY=" + movementManager.motionY + ", max=" + maxMotionY + ")");
		}
		return PASS;
	}
	
	private static double getMaxAcceptableMotionY(Player player, boolean nearBed, boolean couldBeOnBoat, boolean fromClimbable, boolean halfMovement, Checks checksConfig) { 
		double base = couldBeOnBoat ? 0.600000025 : (nearBed ? 0.5625 : (halfMovement ? 0.6 : 0.42));
		if (fromClimbable)
			base += checksConfig.getDouble(CheckType.SPEED, "verticalSpeed", "climbableCompensation"); // Default 0.04
		if (player.hasPotionEffect(PotionEffectType.JUMP)) {
			base += player.getPotionEffect(PotionEffectType.JUMP).getAmplifier() * 0.2D;
		}
		return base;
	}

}
