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
package com.rammelkast.anticheatreloaded.check.combat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.check.CheckResult;
import com.rammelkast.anticheatreloaded.util.Utilities;

public class KillAuraCheck {

	// Angle check
	public static final Map<UUID, Integer> ANGLE_FLAGS = new HashMap<UUID, Integer>();
	// Aimbot check
	public static final Map<UUID, List<Float>> PITCH_MOVEMENTS_CACHE = new HashMap<UUID, List<Float>>();
	public static final Map<UUID, Float> GCD_CACHE = new HashMap<UUID, Float>();
	// Not used as of now, code not committed yet
	private static final CheckResult PASS = new CheckResult(CheckResult.Result.PASSED);
	
	public static CheckResult checkAngle(Player player, EntityDamageEvent event) {
		UUID uuid = player.getUniqueId();
		Entity entity = event.getEntity();
		if (entity instanceof LivingEntity) {
			LivingEntity living = (LivingEntity) entity;
			Location eyeLocation = player.getEyeLocation();

			double yawDifference = calculateYawDifference(eyeLocation, living.getLocation());
			double playerYaw = player.getEyeLocation().getYaw();

			double angleDifference = Math.abs(180 - Math.abs(Math.abs(yawDifference - playerYaw) - 180));
			if (Math.round(angleDifference) > AntiCheatReloaded.getManager().getConfiguration().getMagic().KILLAURA_MAX_ANGLE_DIFFERENCE()) {
				if (!ANGLE_FLAGS.containsKey(uuid)) {
					ANGLE_FLAGS.put(uuid, 1);
					return PASS;
				}
				
				int flags = ANGLE_FLAGS.get(uuid);
				if (flags >= AntiCheatReloaded.getManager().getConfiguration().getMagic().KILLAURA_MAX_ANGLE_VIOLATIONS()) {
					ANGLE_FLAGS.remove(uuid);
					return new CheckResult(CheckResult.Result.FAILED, "tried to attack from an illegal angle (angle=" + Math.round(angleDifference) + ")");
				}
				
				ANGLE_FLAGS.put(uuid, flags + 1);
			}
		}
		return PASS;
	}

	/**
	 * Check idea by Hawk AntiCheat (https://github.com/HawkAnticheat/Hawk)
	 */
	public static CheckResult checkAimbot(Player player, PlayerMoveEvent event) {
		UUID uuid = player.getUniqueId();
		float pitchMovement = event.getTo().getPitch() - event.getFrom().getPitch();
		if (!PITCH_MOVEMENTS_CACHE.containsKey(uuid)) {
			PITCH_MOVEMENTS_CACHE.put(uuid, new ArrayList<Float>());
			return PASS;
		}
		List<Float> pitchMovements = PITCH_MOVEMENTS_CACHE.get(uuid);
		if (pitchMovement != 0 && Math.abs(pitchMovement) <= 10 && Math.abs(event.getTo().getPitch()) != 90) {
			pitchMovements.add(Math.abs(pitchMovement));
		}
		
		if (pitchMovements.size() >= 20) {
			float greatestCommonDivisor = Utilities.gcdRational(pitchMovements);
			if (!GCD_CACHE.containsKey(uuid)) {
				GCD_CACHE.put(uuid, greatestCommonDivisor);
			}
			
			float divisorDifference = Math.abs(greatestCommonDivisor - GCD_CACHE.get(uuid));
			if (divisorDifference > 0.00425 || greatestCommonDivisor < 0.00001) {
				pitchMovements.clear();
				GCD_CACHE.put(uuid, greatestCommonDivisor);
				PITCH_MOVEMENTS_CACHE.put(uuid, pitchMovements);
				return new CheckResult(CheckResult.Result.FAILED, "performed aimbot-like movements (divisor-diff=" + divisorDifference + ")");
			}
			pitchMovements.clear();
			GCD_CACHE.put(uuid, greatestCommonDivisor);
			PITCH_MOVEMENTS_CACHE.put(uuid, pitchMovements);
		}
		PITCH_MOVEMENTS_CACHE.put(uuid, pitchMovements);
		return PASS;
	}
	
	
	public static double calculateYawDifference(Location from, Location to) {
		Location clonedFrom = from.clone();
		Vector startVector = clonedFrom.toVector();
		Vector targetVector = to.toVector();
		clonedFrom.setDirection(targetVector.subtract(startVector));
		return clonedFrom.getYaw();
	}
}
