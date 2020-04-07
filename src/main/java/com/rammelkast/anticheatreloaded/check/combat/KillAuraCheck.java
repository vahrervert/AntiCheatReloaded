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

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.check.CheckResult;
import com.rammelkast.anticheatreloaded.config.providers.Magic;
import com.rammelkast.anticheatreloaded.util.Utilities;
import com.rammelkast.anticheatreloaded.util.checkassist.KillauraAssist;

public class KillAuraCheck {

	// Angle check
	public static final Map<String, Integer> ANGLE_FLAGS = new HashMap<String, Integer>();
	// Heuristic fight speed check
	public static final Map<String, Integer> DEVIATION_SCORES = new HashMap<String, Integer>();
	public static final Map<String, Integer> DIFF_MAP = new HashMap<String, Integer>();
	public static final Map<String, ClickSpeed> CLICKSPEED_MAP = new HashMap<String, ClickSpeed>();
	// Heuristic aimbot check
	public static final Map<String, List<Float>> PITCH_MOVEMENTS_CACHE = new HashMap<String, List<Float>>();
	public static final Map<String, Float> GCD_CACHE = new HashMap<String, Float>();
	// Not used as of now, code not committed yet
	private static final KillauraAssist KILLAURA_ASSIST;
	private static final CheckResult PASS = new CheckResult(CheckResult.Result.PASSED);
	
	public static CheckResult checkAngle(Player player, EntityDamageEvent event) {
		String uuid = player.getUniqueId().toString();
		Entity entity = event.getEntity();
		if (entity instanceof LivingEntity) {
			LivingEntity living = (LivingEntity) entity;
			Location eyeLocation = player.getEyeLocation();

			double yawDifference = KillauraAssist.calculateYawDifference(eyeLocation, living.getLocation());
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
	
	public static CheckResult checkFightSpeed(Player player, EntityDamageEvent event) {
		String uuid = player.getUniqueId().toString();
		if (!CLICKSPEED_MAP.containsKey(uuid)) {
			CLICKSPEED_MAP.put(uuid, new ClickSpeed());
			return PASS;
		}
		ClickSpeed clickSpeed = CLICKSPEED_MAP.get(uuid);
		clickSpeed.registerClick();
		int deviationScore = clickSpeed.getDeviationScore();
		// Prevents false positives since scores under 2000 aren't reliable
		if (deviationScore < 2000) {
			return PASS;
		}
		
		if (!DEVIATION_SCORES.containsKey(uuid)) {
			DEVIATION_SCORES.put(uuid, deviationScore);
			return PASS;
		}
		
		Magic magic = AntiCheatReloaded.getManager().getConfiguration().getMagic();
		int lastScore = DEVIATION_SCORES.get(uuid);
		DEVIATION_SCORES.put(uuid, deviationScore);
		int diff = Math.abs(deviationScore - lastScore);
		if (!DIFF_MAP.containsKey(uuid)) {
			DIFF_MAP.put(uuid, diff);
			return PASS;
		}
		int lastDiff = DIFF_MAP.get(uuid);
		DIFF_MAP.put(uuid, diff);
		if (Math.abs(diff - lastDiff) < magic.KILLAURA_FIGHTSPEED_MINDIFF()) {
			event.setCancelled(true);
			return new CheckResult(CheckResult.Result.FAILED, "had a suspiciously consistant fighting speed (absdiff=" + Math.abs(diff - lastDiff) + ")");
		}
		return PASS;
	}

	/**
	 * Check idea by Hawk AntiCheat (https://github.com/HawkAnticheat/Hawk)
	 */
	public static CheckResult checkAimbot(Player player, PlayerMoveEvent event) {
		String uuid = player.getUniqueId().toString();
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
	
	private static class ClickSpeed {
		private final List<Long> clicks;
		private long lastClick;
		
		public ClickSpeed() {
			this.clicks = new ArrayList<Long>();
			this.lastClick = System.currentTimeMillis();
		}
		
		public void registerClick() {
			this.lastClick = System.currentTimeMillis();
			this.clicks.add(this.lastClick);
		}
		
		public int getDeviationScore() {
			long last = 0;
			List<Integer> deviation = new ArrayList<Integer>();
			List<Long> toRemove = new ArrayList<Long>();
			for (long clickTime : this.clicks) {
				if (clickTime > (this.lastClick - 5000)) {
					if (last == 0) {
						last = clickTime;
						continue;
					}
					deviation.add((int) (clickTime - last));
				} else {
					toRemove.add(clickTime);
				}
			}
			this.clicks.removeAll(toRemove);
			if (deviation.isEmpty()) {
				return -1;
			}
			float averageDeviationScore = deviation.get(0);
			for (int i = 1; i < deviation.size(); i++) {
				averageDeviationScore += deviation.get(i);
			}
			return Math.round(averageDeviationScore / deviation.size());
		}
	}
	
	static {
		KILLAURA_ASSIST = new KillauraAssist();
	}

}
