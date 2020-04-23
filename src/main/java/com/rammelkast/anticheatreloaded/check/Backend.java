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

package com.rammelkast.anticheatreloaded.check;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.potion.PotionEffectType;

import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.check.combat.KillAuraCheck;
import com.rammelkast.anticheatreloaded.check.combat.VelocityCheck;
import com.rammelkast.anticheatreloaded.check.movement.ElytraCheck;
import com.rammelkast.anticheatreloaded.check.movement.FlightCheck;
import com.rammelkast.anticheatreloaded.check.movement.GlideCheck;
import com.rammelkast.anticheatreloaded.check.movement.SpeedCheck;
import com.rammelkast.anticheatreloaded.check.movement.WaterWalkCheck;
import com.rammelkast.anticheatreloaded.check.movement.YAxisCheck;
import com.rammelkast.anticheatreloaded.check.packet.MorePacketsCheck;
import com.rammelkast.anticheatreloaded.config.Configuration;
import com.rammelkast.anticheatreloaded.config.providers.Lang;
import com.rammelkast.anticheatreloaded.config.providers.Magic;
import com.rammelkast.anticheatreloaded.manage.AntiCheatManager;
import com.rammelkast.anticheatreloaded.util.Distance;
import com.rammelkast.anticheatreloaded.util.User;
import com.rammelkast.anticheatreloaded.util.Utilities;
import com.rammelkast.anticheatreloaded.util.VersionUtil;

public class Backend {
	public Map<UUID, Long> velocitized = new HashMap<UUID, Long>();
	private List<UUID> isAscending = new ArrayList<UUID>();
	private Map<UUID, Integer> ascensionCount = new HashMap<UUID, Integer>();
	private Map<UUID, Integer> chatLevel = new HashMap<UUID, Integer>();
	private Map<UUID, Integer> commandLevel = new HashMap<UUID, Integer>();
	private Map<UUID, Integer> nofallViolation = new HashMap<UUID, Integer>();
	private Map<UUID, Integer> fastBreakViolation = new HashMap<UUID, Integer>();
	private Map<UUID, Integer> fastBreaks = new HashMap<UUID, Integer>();
	private Map<UUID, Boolean> blockBreakHolder = new HashMap<UUID, Boolean>();
	private Map<UUID, Long> lastBlockBroken = new HashMap<UUID, Long>();
	private Map<UUID, Integer> fastPlaceViolation = new HashMap<UUID, Integer>();
	private Map<UUID, Long> lastBlockPlaced = new HashMap<UUID, Long>();
	private Map<UUID, Long> lastBlockPlaceTime = new HashMap<UUID, Long>();
	private Map<UUID, Integer> blockPunches = new HashMap<UUID, Integer>();
	private Map<UUID, Integer> projectilesShot = new HashMap<UUID, Integer>();
	private Map<UUID, Integer> velocitytrack = new HashMap<UUID, Integer>();
	private Map<UUID, Long> startEat = new HashMap<UUID, Long>();
	private Map<UUID, Long> lastHeal = new HashMap<UUID, Long>();
	private Map<UUID, Long> projectileTime = new HashMap<UUID, Long>();
	private Map<UUID, Long> bowWindUp = new HashMap<UUID, Long>();
	private Map<UUID, Long> instantBreakExempt = new HashMap<UUID, Long>();
	private Map<UUID, Long> sprinted = new HashMap<UUID, Long>();
	private Map<UUID, Long> brokenBlock = new HashMap<UUID, Long>();
	private Map<UUID, Long> placedBlock = new HashMap<UUID, Long>();
	private Map<UUID, Long> blockTime = new HashMap<UUID, Long>();
	private Map<UUID, Integer> blocksDropped = new HashMap<UUID, Integer>();
	private Map<UUID, Long> lastInventoryTime = new HashMap<UUID, Long>();
	private Map<UUID, Long> inventoryTime = new HashMap<UUID, Long>();
	private Map<UUID, Integer> inventoryClicks = new HashMap<UUID, Integer>();
	private Map<UUID, Material> itemInHand = new HashMap<UUID, Material>();
	private Map<UUID, Integer> steps = new HashMap<UUID, Integer>();
	private Map<UUID, Long> stepTime = new HashMap<UUID, Long>();
	private HashSet<Byte> transparent = new HashSet<Byte>();
	private Map<UUID, Long> lastFallPacket = new HashMap<UUID, Long>();
	private Map<String, Integer> fastSneakViolations = new HashMap<String, Integer>();

	private Magic magic;
	private AntiCheatManager manager = null;
	private Lang lang = null;
	private static final CheckResult PASS = new CheckResult(CheckResult.Result.PASSED);

	public Backend(AntiCheatManager instance) {
		magic = instance.getConfiguration().getMagic();
		manager = instance;
		lang = manager.getConfiguration().getLang();
		transparent.add((byte) -1);
	}

	public Magic getMagic() {
		return magic;
	}

	public void updateConfig(Configuration config) {
		magic = config.getMagic();
		lang = config.getLang();
	}

	public void resetChatLevel(User user) {
		chatLevel.put(user.getUUID(), 0);
	}

	public void garbageClean(Player player) {
		UUID pU = player.getUniqueId();

		VelocityCheck.cleanPlayer(player);
		MorePacketsCheck.MOVE_COUNT.remove(player.getUniqueId());
		blocksDropped.remove(pU);
		blockTime.remove(pU);
		FlightCheck.MOVING_EXEMPT.remove(pU);
		brokenBlock.remove(pU);
		placedBlock.remove(pU);
		bowWindUp.remove(pU);
		startEat.remove(pU);
		lastHeal.remove(pU);
		sprinted.remove(pU);
		WaterWalkCheck.IS_IN_WATER.remove(pU);
		WaterWalkCheck.IS_IN_WATER_CACHE.remove(pU);
		instantBreakExempt.remove(pU);
		isAscending.remove(player.getUniqueId());
		ascensionCount.remove(player.getUniqueId());
		FlightCheck.BLOCKS_OVER_FLIGHT.remove(pU);
		nofallViolation.remove(pU);
		fastBreakViolation.remove(pU);
		YAxisCheck.Y_AXIS_VIOLATIONS.remove(pU);
		YAxisCheck.LAST_Y_AXIS_VIOLATION.remove(pU);
		YAxisCheck.LAST_Y_COORD_CACHE.remove(pU);
		YAxisCheck.LAST_Y_TIME.remove(pU);
		WaterWalkCheck.WATER_ASCENSION_VIOLATIONS.remove(pU);
		WaterWalkCheck.WATER_SPEED_VIOLATIONS.remove(pU);
		fastBreaks.remove(pU);
		blockBreakHolder.remove(pU);
		lastBlockBroken.remove(pU);
		fastPlaceViolation.remove(pU);
		lastBlockPlaced.remove(pU);
		lastBlockPlaceTime.remove(pU);
		blockPunches.remove(pU);
		projectilesShot.remove(pU);
		velocitized.remove(pU);
		velocitytrack.remove(pU);
		startEat.remove(pU);
		lastHeal.remove(pU);
		projectileTime.remove(pU);
		bowWindUp.remove(pU);
		instantBreakExempt.remove(pU);
		sprinted.remove(pU);
		brokenBlock.remove(pU);
		placedBlock.remove(pU);
		FlightCheck.MOVING_EXEMPT.remove(pU);
		blockTime.remove(pU);
		blocksDropped.remove(pU);
		lastInventoryTime.remove(pU);
		inventoryTime.remove(pU);
		inventoryClicks.remove(pU);
		lastFallPacket.remove(pU);
		fastSneakViolations.remove(player.getUniqueId().toString());
		GlideCheck.LAST_MOTION_Y.remove(pU);
		GlideCheck.LAST_FALL_DISTANCE.remove(pU);
		GlideCheck.VIOLATIONS.remove(pU);
		SpeedCheck.SPEED_VIOLATIONS.remove(player.getUniqueId());
		ElytraCheck.JUMP_Y_VALUE.remove(player.getUniqueId().toString());
		KillAuraCheck.ANGLE_FLAGS.remove(pU);
		KillAuraCheck.PITCH_MOVEMENTS_CACHE.remove(pU);
		KillAuraCheck.GCD_CACHE.remove(pU);
	}

	public CheckResult checkFastBow(Player player, float force) {
		// Ignore magic numbers here, they are minecrafty vanilla stuff.
		if (!bowWindUp.containsKey(player.getUniqueId())) {
			return PASS;
		}
		int ticks = (int) ((((System.currentTimeMillis() - bowWindUp.get(player.getUniqueId())) * 20) / 1000) + 3);
		bowWindUp.remove(player.getUniqueId());
		float f = (float) ticks / 20.0F;
		f = (f * f + f * 2.0F) / 3.0F;
		f = f > 1.0F ? 1.0F : f;
		if (Math.abs(force - f) > magic.BOW_ERROR()) {
			return new CheckResult(CheckResult.Result.FAILED,
					"fired their bow too fast (actual force=" + force + ", calculated force=" + f + ")");
		} else {
			return PASS;
		}
	}

	public CheckResult checkProjectile(Player player) {
		incrementOld(player, projectilesShot, 10);
		if (!projectileTime.containsKey(player.getUniqueId())) {
			projectileTime.put(player.getUniqueId(), System.currentTimeMillis());
			return new CheckResult(CheckResult.Result.PASSED);
		} else if (projectilesShot.get(player.getUniqueId()) == magic.PROJECTILE_CHECK()) {
			long time = System.currentTimeMillis() - projectileTime.get(player.getUniqueId());
			projectileTime.remove(player.getUniqueId());
			projectilesShot.remove(player.getUniqueId());
			if (time < magic.PROJECTILE_TIME_MIN()) {
				return new CheckResult(CheckResult.Result.FAILED, "wound up a bow too fast (actual time=" + time
						+ ", min time=" + magic.PROJECTILE_TIME_MIN() + ")");
			}
		}
		return PASS;
	}

	public CheckResult checkFastDrop(Player player) {
		incrementOld(player, blocksDropped, 10);
		if (!blockTime.containsKey(player.getUniqueId())) {
			blockTime.put(player.getUniqueId(), System.currentTimeMillis());
			return new CheckResult(CheckResult.Result.PASSED);
		} else if (blocksDropped.get(player.getUniqueId()) == magic.DROP_CHECK()) {
			long time = System.currentTimeMillis() - blockTime.get(player.getUniqueId());
			blockTime.remove(player.getUniqueId());
			blocksDropped.remove(player.getUniqueId());
			if (time < magic.DROP_TIME_MIN()) {
				return new CheckResult(CheckResult.Result.FAILED,
						"dropped an item too fast (actual time=" + time + ", min time=" + magic.DROP_TIME_MIN() + ")");
			}
		}
		return PASS;
	}

	public CheckResult checkLongReachBlock(Player player, double x, double y, double z) {
		if (isInstantBreakExempt(player)) {
			return new CheckResult(CheckResult.Result.PASSED);
		} else {
			String string = "reached too far for a block";
			double distance = player.getGameMode() == GameMode.CREATIVE ? magic.BLOCK_MAX_DISTANCE_CREATIVE()
					: player.getLocation().getDirection().getY() > 0.9 ? magic.BLOCK_MAX_DISTANCE_CREATIVE()
							: magic.BLOCK_MAX_DISTANCE();
			double i = x >= distance ? x : y > distance ? y : z > distance ? z : -1;
			if (i != -1) {
				return new CheckResult(CheckResult.Result.FAILED,
						string + " (distance=" + i + ", max=" + magic.BLOCK_MAX_DISTANCE() + ")");
			} else {
				return PASS;
			}
		}
	}

	public CheckResult checkLongReachDamage(Player player, double x, double y, double z) {
		String string = "reached too far for an entity";
		double i = x >= magic.ENTITY_MAX_DISTANCE() ? x
				: y > magic.ENTITY_MAX_DISTANCE() ? y : z > magic.ENTITY_MAX_DISTANCE() ? z : -1;
		if (i != -1) {
			return new CheckResult(CheckResult.Result.FAILED,
					string + " (distance=" + i + ", max=" + magic.ENTITY_MAX_DISTANCE() + ")");
		} else {
			return PASS;
		}
	}

	public CheckResult checkSpider(Player player, double y) {
		if (y <= magic.LADDER_Y_MAX() && y >= magic.LADDER_Y_MIN()
				&& !Utilities.isClimbableBlock(player.getLocation().getBlock())
				&& !Utilities.isClimbableBlock(player.getEyeLocation().getBlock())
				&& !Utilities.isClimbableBlock(player.getLocation().clone().add(0, -0.98, 0).getBlock())) {
			return new CheckResult(CheckResult.Result.FAILED,
					"tried to climb a non-climbable block (" + player.getLocation().getBlock().getType() + ")");
		} else {
			return PASS;
		}
	}

	public CheckResult checkYSpeed(Player player, double y) {
		if (!isMovingExempt(player) && !player.isInsideVehicle() && !player.isSleeping() && (y > magic.Y_SPEED_MAX())
				&& !isDoing(player, velocitized, magic.VELOCITY_TIME())
				&& !player.hasPotionEffect(PotionEffectType.JUMP) && !VersionUtil.isFlying(player)) {
			return new CheckResult(CheckResult.Result.FAILED,
					"y speed was too high (speed=" + y + ", max=" + magic.Y_SPEED_MAX() + ")");
		} else {
			return PASS;
		}
	}

	public CheckResult checkNoFall(Player player, double y) {
		UUID uuid = player.getUniqueId();
		if (player.getGameMode() != GameMode.CREATIVE && !player.isInsideVehicle() && !player.isSleeping()
				&& !isMovingExempt(player) && !justPlaced(player) && !Utilities.isInWater(player)
				&& !Utilities.isInWeb(player)
				&& !player.getLocation().getBlock().getType().name().endsWith("TRAPDOOR")) {
			if (player.getFallDistance() == 0) {
				if (nofallViolation.get(uuid) == null) {
					nofallViolation.put(uuid, 1);
				} else {
					nofallViolation.put(uuid, nofallViolation.get(player.getUniqueId()) + 1);
				}

				int i = nofallViolation.get(uuid);
				if (i >= magic.NOFALL_LIMIT()) {
					nofallViolation.put(player.getUniqueId(), 1);
					return new CheckResult(CheckResult.Result.FAILED, "tried to avoid fall damage (fall distance = 0 "
							+ i + " times in a row, max=" + magic.NOFALL_LIMIT() + ")");
				} else {
					return PASS;
				}
			} else {
				nofallViolation.put(uuid, 0);
				return PASS;
			}
		}
		return PASS;
	}

	public CheckResult checkSneak(Player player, Location location, double x, double z) {
		if (player.isSneaking() && !VersionUtil.isFlying(player) && !isMovingExempt(player) && !player.isInsideVehicle()
				&& !Utilities.cantStandAtExp(location)) {
			double i = x > magic.XZ_SPEED_MAX_SNEAK() ? x : z > magic.XZ_SPEED_MAX_SNEAK() ? z : -1;
			if (i != -1) {
				if (this.fastSneakViolations.containsKey(player.getUniqueId().toString())) {
					int flags = this.fastSneakViolations.get(player.getUniqueId().toString());
					if (flags >= 3) { // TODO possible config
						return new CheckResult(CheckResult.Result.FAILED,
								"was sneaking too fast (speed=" + i + ", max=" + magic.XZ_SPEED_MAX_SNEAK() + ")");
					}
					this.fastSneakViolations.put(player.getUniqueId().toString(), flags + 1);
					return PASS;
				} else {
					this.fastSneakViolations.put(player.getUniqueId().toString(), 1);
					return PASS;
				}
			} else {
				this.fastSneakViolations.remove(player.getUniqueId().toString());
				return PASS;
			}
		} else {
			this.fastSneakViolations.remove(player.getUniqueId().toString());
			return PASS;
		}
	}

	public CheckResult checkSprintHungry(PlayerToggleSprintEvent event) {
		Player player = event.getPlayer();
		if (event.isSprinting() && player.getGameMode() != GameMode.CREATIVE
				&& player.getFoodLevel() <= magic.SPRINT_FOOD_MIN()) {
			return new CheckResult(CheckResult.Result.FAILED,
					"sprinted while hungry (food=" + player.getFoodLevel() + ", min=" + magic.SPRINT_FOOD_MIN() + ")");
		} else {
			return PASS;
		}
	}

	public CheckResult checkVClip(Player player, Distance distance) {
		double from = Math.round(distance.fromY());
		double to = Math.round(distance.toY());

		if (player.isInsideVehicle() || (from == to || from < to) || Math.round(distance.getYDifference()) < 2) {
			return PASS;
		}

		for (int i = 0; i < (Math.round(distance.getYDifference())) + 1; i++) {
			Block block = new Location(player.getWorld(), player.getLocation().getX(), to + i,
					player.getLocation().getZ()).getBlock();
			if (block.getType() != Material.AIR && block.getType().isSolid()) {
				return new CheckResult(CheckResult.Result.FAILED, "tried to move through a solid block",
						(int) from + 3);
			}
		}

		return PASS;
	}

	public void logAscension(Player player, double y1, double y2) {
		UUID name = player.getUniqueId();
		if (y1 < y2 && !isAscending.contains(name)) {
			isAscending.add(name);
		} else {
			isAscending.remove(name);
		}
	}

	public CheckResult checkAscension(Player player, double y1, double y2) {
		int max = magic.ASCENSION_COUNT_MAX();
		String string = "";
		if (player.hasPotionEffect(PotionEffectType.JUMP)) {
			max += 12;
			string = " with jump potion";
		}
		Block block = player.getLocation().getBlock();
		if (!isMovingExempt(player) && !Utilities.isInWater(player) && !VersionUtil.isFlying(player)
				&& !justBroke(player) && !Utilities.isClimbableBlock(player.getLocation().getBlock())
				&& !Utilities.isClimbableBlock(player.getEyeLocation().getBlock())
				&& !Utilities.isClimbableBlock(player.getLocation().clone().add(0, -0.98, 0).getBlock())
				&& !player.isInsideVehicle() && !YAxisCheck.isMoveUpBlock(player.getLocation().add(0, -1, 0).getBlock())
				&& !YAxisCheck.isMoveUpBlock(player.getLocation().add(0, -0.5, 0).getBlock())) {
			// TODO isMoveUpBlock does not seem to be a success with stairs
			if (y1 < y2) {
				if (!block.getRelative(BlockFace.NORTH).isLiquid() && !block.getRelative(BlockFace.SOUTH).isLiquid()
						&& !block.getRelative(BlockFace.EAST).isLiquid()
						&& !block.getRelative(BlockFace.WEST).isLiquid()) {
					increment(player, ascensionCount, max);
					if (ascensionCount.get(player.getUniqueId()) >= max) {
						return new CheckResult(CheckResult.Result.FAILED,
								"ascended " + ascensionCount.get(player.getUniqueId()) + " times in a row (max = " + max
										+ string + ")");
					}
				}
			} else {
				ascensionCount.put(player.getUniqueId(), 0);
			}
		}
		return PASS;
	}

	public CheckResult checkSwing(Player player, Block block) {
		UUID uuid = player.getUniqueId();
		if (!isInstantBreakExempt(player)) {
			if (!VersionUtil.getItemInHand(player).containsEnchantment(Enchantment.DIG_SPEED)
					&& !(VersionUtil.getItemInHand(player).getType() == Material.SHEARS
							&& block.getType().name().endsWith("LEAVES"))) {
				if (blockPunches.get(uuid) != null && player.getGameMode() != GameMode.CREATIVE) {
					int i = blockPunches.get(uuid);
					if (i < magic.BLOCK_PUNCH_MIN()) {
						return new CheckResult(CheckResult.Result.FAILED, "tried to break a block of " + block.getType()
								+ " after only " + i + " punches (min=" + magic.BLOCK_PUNCH_MIN() + ")");
					} else {
						blockPunches.put(uuid, 0); // it should reset after EACH block break.
					}
				}
			}
		}
		return PASS;
	}

	public CheckResult checkFastBreak(Player player, Block block) {
		int violations = magic.FASTBREAK_MAXVIOLATIONS();
		long timemax = isInstantBreakExempt(player) ? 0
				: Utilities.calcSurvivalFastBreak(VersionUtil.getItemInHand(player), block.getType());
		if (player.getGameMode() == GameMode.CREATIVE) {
			violations = magic.FASTBREAK_MAXVIOLATIONS_CREATIVE();
			timemax = magic.FASTBREAK_TIMEMAX_CREATIVE();
		}
		UUID uuid = player.getUniqueId();
		if (!fastBreakViolation.containsKey(uuid)) {
			fastBreakViolation.put(uuid, 0);
		} else {
			Long math = System.currentTimeMillis() - lastBlockBroken.get(uuid);
			int i = fastBreakViolation.get(uuid);
			if (i > violations && math < magic.FASTBREAK_MAXVIOLATIONTIME()) {
				lastBlockBroken.put(uuid, System.currentTimeMillis());
				return new CheckResult(CheckResult.Result.FAILED,
						"broke blocks too fast " + i + " times in a row (max=" + violations + ")");
			} else if (fastBreakViolation.get(uuid) > 0 && math > magic.FASTBREAK_MAXVIOLATIONTIME()) {
				fastBreakViolation.put(uuid, 0);
			}
		}
		if (!fastBreaks.containsKey(uuid) || !lastBlockBroken.containsKey(uuid)) {
			if (!lastBlockBroken.containsKey(uuid)) {
				lastBlockBroken.put(uuid, System.currentTimeMillis());
			}
			if (!fastBreaks.containsKey(uuid)) {
				fastBreaks.put(uuid, 0);
			}
		} else {
			Long math = System.currentTimeMillis() - lastBlockBroken.get(uuid);
			if ((math != 0L && timemax != 0L)) {
				if (math < timemax) {
					if (fastBreakViolation.containsKey(uuid) && fastBreakViolation.get(uuid) > 0) {
						fastBreakViolation.put(uuid, fastBreakViolation.get(uuid) + 1);
					} else {
						fastBreaks.put(uuid, fastBreaks.get(uuid) + 1);
					}
					blockBreakHolder.put(uuid, false);
				}
				if (fastBreaks.get(uuid) >= magic.FASTBREAK_LIMIT() && math < timemax) {
					int i = fastBreaks.get(uuid);
					fastBreaks.put(uuid, 0);
					fastBreakViolation.put(uuid, fastBreakViolation.get(uuid) + 1);
					return new CheckResult(CheckResult.Result.FAILED, "tried to break " + i + " blocks in " + math
							+ " ms (max=" + magic.FASTBREAK_LIMIT() + " in " + timemax + " ms)");
				} else if (fastBreaks.get(uuid) >= magic.FASTBREAK_LIMIT() || fastBreakViolation.get(uuid) > 0) {
					if (!blockBreakHolder.containsKey(uuid) || !blockBreakHolder.get(uuid)) {
						blockBreakHolder.put(uuid, true);
					} else {
						fastBreaks.put(uuid, fastBreaks.get(uuid) - 1);
						if (fastBreakViolation.get(uuid) > 0) {
							fastBreakViolation.put(uuid, fastBreakViolation.get(uuid) - 1);
						}
						blockBreakHolder.put(uuid, false);
					}
				}
			}
		}

		lastBlockBroken.put(uuid, System.currentTimeMillis()); // always keep a log going.
		return PASS;
	}

	public CheckResult checkFastPlace(Player player) {
		int violations = player.getGameMode() == GameMode.CREATIVE ? magic.FASTPLACE_MAXVIOLATIONS_CREATIVE()
				: magic.FASTPLACE_MAXVIOLATIONS();
		long time = System.currentTimeMillis();
		UUID uuid = player.getUniqueId();
		if (!lastBlockPlaceTime.containsKey(uuid) || !fastPlaceViolation.containsKey(uuid)) {
			lastBlockPlaceTime.put(uuid, 0L);
			if (!fastPlaceViolation.containsKey(uuid)) {
				fastPlaceViolation.put(uuid, 0);
			}
		} else if (fastPlaceViolation.containsKey(uuid) && fastPlaceViolation.get(uuid) > violations) {
			Long math = System.currentTimeMillis() - lastBlockPlaced.get(uuid);
			if (lastBlockPlaced.get(uuid) > 0 && math < magic.FASTPLACE_MAXVIOLATIONTIME()) {
				lastBlockPlaced.put(uuid, time);
				return new CheckResult(CheckResult.Result.FAILED, "placed blocks too fast "
						+ fastBreakViolation.get(uuid) + " times in a row (max=" + violations + ")");
			} else if (lastBlockPlaced.get(uuid) > 0 && math > magic.FASTPLACE_MAXVIOLATIONTIME()) {
				fastPlaceViolation.put(uuid, 0);
			}
		} else if (lastBlockPlaced.containsKey(uuid)) {
			long last = lastBlockPlaced.get(uuid);
			long lastTime = lastBlockPlaceTime.get(uuid);
			long thisTime = time - last;

			if (lastTime != 0 && thisTime < magic.FASTPLACE_TIMEMIN()) {
				lastBlockPlaceTime.put(uuid, (time - last));
				lastBlockPlaced.put(uuid, time);
				fastPlaceViolation.put(uuid, fastPlaceViolation.get(uuid) + 1);
				return new CheckResult(CheckResult.Result.FAILED, "tried to place a block " + thisTime
						+ " ms after the last one (min=" + magic.FASTPLACE_TIMEMIN() + " ms)");
			}
			lastBlockPlaceTime.put(uuid, (time - last));
		}
		lastBlockPlaced.put(uuid, time);
		return PASS;
	}

	public void logBowWindUp(Player player) {
		bowWindUp.put(player.getUniqueId(), System.currentTimeMillis());
	}

	public void logEatingStart(Player player) {
		startEat.put(player.getUniqueId(), System.currentTimeMillis());
	}

	public boolean isEating(Player player) {
		return startEat.containsKey(player.getUniqueId()) && startEat.get(player.getUniqueId()) < magic.EAT_TIME_MIN();
	}

	public void logHeal(Player player) {
		lastHeal.put(player.getUniqueId(), System.currentTimeMillis());
	}

	public CheckResult checkChatSpam(Player player, String msg) {
		UUID uuid = player.getUniqueId();
		User user = manager.getUserManager().getUser(uuid);
		if (user.getLastMessageTime() != -1) {
			for (int i = 0; i < 2; i++) {
				String m = user.getMessage(i);
				if (m == null) {
					break;
				}
				Long l = user.getMessageTime(i);

				if (System.currentTimeMillis() - l > magic.CHAT_REPEAT_MIN()) {
					user.clearMessages();
					break;
				} else {
					if (manager.getConfiguration().getConfig().blockChatSpamRepetition.getValue()
							&& m.equalsIgnoreCase(msg) && i == 1) {
						manager.getLoggingManager().logFineInfo(player.getName() + " spam-repeated \"" + msg + "\"");
						return new CheckResult(CheckResult.Result.FAILED, lang.SPAM_WARNING());
					} else if (manager.getConfiguration().getConfig().blockChatSpamSpeed.getValue()
							&& System.currentTimeMillis() - user.getLastCommandTime() < magic.CHAT_MIN()) {
						manager.getLoggingManager().logFineInfo(player.getName() + " spammed quickly \"" + msg + "\"");
						return new CheckResult(CheckResult.Result.FAILED, lang.SPAM_WARNING());
					}
				}
			}
		}
		user.addMessage(msg);
		return PASS;
	}

	public CheckResult checkChatUnicode(Player player, String msg) {
		UUID uuid = player.getUniqueId();
		User user = manager.getUserManager().getUser(uuid);
		for (char ch : msg.toCharArray()) {
			if (Character.UnicodeBlock.of(ch) != Character.UnicodeBlock.BASIC_LATIN
					&& Character.UnicodeBlock.of(ch) != Character.UnicodeBlock.LATIN_1_SUPPLEMENT
					&& Character.UnicodeBlock.of(ch) != Character.UnicodeBlock.LATIN_EXTENDED_A
					&& Character.UnicodeBlock.of(ch) != Character.UnicodeBlock.GENERAL_PUNCTUATION) {
				return new CheckResult(CheckResult.Result.FAILED, "Unicode chat is not allowed.");
			}
		}
		user.addMessage(msg);
		return PASS;
	}

	public CheckResult checkCommandSpam(Player player, String cmd) {
		UUID uuid = player.getUniqueId();
		User user = manager.getUserManager().getUser(uuid);
		if (user.getLastCommandTime() != -1) {
			for (int i = 0; i < 2; i++) {
				String m = user.getCommand(i);
				if (m == null) {
					break;
				}
				Long l = user.getCommandTime(i);

				if (System.currentTimeMillis() - l > magic.COMMAND_REPEAT_MIN()) {
					user.clearCommands();
					break;
				} else {
					if (manager.getConfiguration().getConfig().blockCommandSpamRepetition.getValue()
							&& m.equalsIgnoreCase(cmd) && i == 1) {
						return new CheckResult(CheckResult.Result.FAILED, lang.SPAM_WARNING());
					} else if (manager.getConfiguration().getConfig().blockCommandSpamSpeed.getValue()
							&& System.currentTimeMillis() - user.getLastCommandTime() < magic.COMMAND_MIN()) {
						return new CheckResult(CheckResult.Result.FAILED, lang.SPAM_WARNING());
					}
				}
			}
		}
		user.addCommand(cmd);
		return PASS;
	}

	public CheckResult checkInventoryClicks(Player player) {
		if (player.getGameMode() == GameMode.CREATIVE) {
			return PASS;
		}
		UUID uuid = player.getUniqueId();
		int clicks = 1;
		if (inventoryClicks.containsKey(uuid)) {
			clicks = inventoryClicks.get(uuid) + 1;
		}
		inventoryClicks.put(uuid, clicks);
		if (clicks == 1) {
			inventoryTime.put(uuid, System.currentTimeMillis());
		} else if (clicks == magic.INVENTORY_CHECK()) {
			long time = System.currentTimeMillis() - inventoryTime.get(uuid);
			inventoryClicks.put(uuid, 0);
			if (time < magic.INVENTORY_TIMEMIN()) {
				return new CheckResult(CheckResult.Result.FAILED, "clicked inventory slots " + clicks + " times in "
						+ time + " ms (max=" + magic.INVENTORY_CHECK() + " in " + magic.INVENTORY_TIMEMIN() + " ms)");
			}
		}
		return PASS;
	}

	public CheckResult checkAutoTool(Player player) {
		if (itemInHand.containsKey(player.getUniqueId())
				&& itemInHand.get(player.getUniqueId()) != VersionUtil.getItemInHand(player).getType()) {
			return new CheckResult(CheckResult.Result.FAILED,
					"switched tools too fast (had " + itemInHand.get(player.getUniqueId()) + ", has "
							+ VersionUtil.getItemInHand(player).getType() + ")");
		} else {
			return PASS;
		}
	}

	public CheckResult checkSprintDamage(Player player) {
		if (isDoing(player, sprinted, magic.SPRINT_MAX())) {
			return new CheckResult(CheckResult.Result.FAILED,
					"sprinted and damaged an entity too fast (min sprint=" + magic.SPRINT_MAX() + " ms)");
		} else {
			return PASS;
		}
	}

	public CheckResult checkFastHeal(Player player) {
		if (lastHeal.containsKey(player.getUniqueId())) // Otherwise it was modified by a plugin, don't worry about it.
		{
			long healTime = magic.MIN_HEAL_TIME();
			long l = lastHeal.get(player.getUniqueId());
			lastHeal.remove(player.getUniqueId());
			if ((System.currentTimeMillis() - l) < healTime) {
				return new CheckResult(CheckResult.Result.FAILED, "healed too quickly (time="
						+ (System.currentTimeMillis() - l) + " ms, min=" + healTime + " ms)");
			}
		}
		return PASS;
	}

	public CheckResult checkFastEat(Player player) {
		if (startEat.containsKey(player.getUniqueId())) // Otherwise it was modified by a plugin, don't worry about it.
		{
			long l = startEat.get(player.getUniqueId());
			startEat.remove(player.getUniqueId());
			if ((System.currentTimeMillis() - l) < magic.EAT_TIME_MIN()) {
				return new CheckResult(CheckResult.Result.FAILED, "ate too quickly (time="
						+ (System.currentTimeMillis() - l) + " ms, min=" + magic.EAT_TIME_MIN() + " ms)");
			}
		}
		return PASS;
	}

	public void logInstantBreak(final Player player) {
		instantBreakExempt.put(player.getUniqueId(), System.currentTimeMillis());
	}

	public boolean isInstantBreakExempt(Player player) {
		return isDoing(player, instantBreakExempt, magic.INSTANT_BREAK_TIME());
	}

	public void logSprint(final Player player) {
		sprinted.put(player.getUniqueId(), System.currentTimeMillis());
	}

	public boolean isHoveringOverWaterAfterViolation(Player player) {
		if (WaterWalkCheck.WATER_SPEED_VIOLATIONS.containsKey(player.getUniqueId())) {
			if (WaterWalkCheck.WATER_SPEED_VIOLATIONS.get(player.getUniqueId()) >= magic.WATER_SPEED_VIOLATION_MAX()
					&& Utilities.isHoveringOverWater(player.getLocation())) {
				return true;
			}
		}
		return false;
	}

	public void logBlockBreak(final Player player) {
		brokenBlock.put(player.getUniqueId(), System.currentTimeMillis());
	}

	public boolean justBroke(Player player) {
		return isDoing(player, brokenBlock, magic.BLOCK_BREAK_MIN());
	}

	public void logVelocity(final Player player) {
		velocitized.put(player.getUniqueId(), System.currentTimeMillis());
	}

	public boolean justVelocity(Player player) {
		return (velocitized.containsKey(player.getUniqueId())
				? (System.currentTimeMillis() - velocitized.get(player.getUniqueId())) < magic.VELOCITY_CHECKTIME()
				: false);
	}

	public boolean extendVelocityTime(final Player player) {
		if (velocitytrack.containsKey(player.getUniqueId())) {
			velocitytrack.put(player.getUniqueId(), velocitytrack.get(player.getUniqueId()) + 1);
			if (velocitytrack.get(player.getUniqueId()) > magic.VELOCITY_MAXTIMES()) {
				velocitized.put(player.getUniqueId(), System.currentTimeMillis() + magic.VELOCITY_PREVENT());
				manager.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(manager.getPlugin(),
						new Runnable() {
							@Override
							public void run() {
								velocitytrack.put(player.getUniqueId(), 0);
							}
						}, magic.VELOCITY_SCHETIME() * 20L);
				return true;
			}
		} else {
			velocitytrack.put(player.getUniqueId(), 0);
		}

		return false;
	}

	public void logBlockPlace(final Player player) {
		placedBlock.put(player.getUniqueId(), System.currentTimeMillis());
	}

	public boolean justPlaced(Player player) {
		return isDoing(player, placedBlock, magic.BLOCK_PLACE_MIN());
	}

	public void logDamage(final Player player, int type) {
		long time;
		switch (type) {
		case 1:
			time = magic.DAMAGE_TIME();
			break;
		case 2:
			time = magic.KNOCKBACK_DAMAGE_TIME();
			break;
		case 3:
			time = magic.EXPLOSION_DAMAGE_TIME();
			break;
		default:
			time = magic.DAMAGE_TIME();
			break;

		}
		FlightCheck.MOVING_EXEMPT.put(player.getUniqueId(), System.currentTimeMillis() + time);
		// Only map in which termination time is calculated beforehand.
	}

	public void logEnterExit(final Player player) {
		FlightCheck.MOVING_EXEMPT.put(player.getUniqueId(), System.currentTimeMillis() + magic.ENTERED_EXITED_TIME());
	}

	public void logToggleSneak(final Player player) {
		FlightCheck.MOVING_EXEMPT.put(player.getUniqueId(), System.currentTimeMillis() + magic.SNEAK_TIME());
	}

	public void logTeleport(final Player player) {
		FlightCheck.MOVING_EXEMPT.put(player.getUniqueId(), System.currentTimeMillis() + magic.TELEPORT_TIME());

		/* Data for fly/speed should be reset */
		nofallViolation.remove(player.getUniqueId());
		FlightCheck.BLOCKS_OVER_FLIGHT.remove(player.getUniqueId());
		YAxisCheck.Y_AXIS_VIOLATIONS.remove(player.getUniqueId());
		YAxisCheck.LAST_Y_AXIS_VIOLATION.remove(player.getUniqueId());
		YAxisCheck.LAST_Y_COORD_CACHE.remove(player.getUniqueId());
		YAxisCheck.LAST_Y_TIME.remove(player.getUniqueId());
		GlideCheck.LAST_FALL_DISTANCE.remove(player.getUniqueId());
		GlideCheck.LAST_MOTION_Y.remove(player.getUniqueId());
		GlideCheck.VIOLATIONS.remove(player.getUniqueId());
		ElytraCheck.JUMP_Y_VALUE.remove(player.getUniqueId().toString());
	}

	public void logExitFly(final Player player) {
		FlightCheck.MOVING_EXEMPT.put(player.getUniqueId(), System.currentTimeMillis() + magic.EXIT_FLY_TIME());
	}

	public void logJoin(final Player player) {
		FlightCheck.MOVING_EXEMPT.put(player.getUniqueId(), System.currentTimeMillis() + magic.JOIN_TIME());
	}

	public boolean isMovingExempt(Player player) {
		return isDoing(player, FlightCheck.MOVING_EXEMPT, -1);
	}

	public boolean isAscending(Player player) {
		return isAscending.contains(player.getUniqueId());
	}

	public boolean isDoing(Player player, Map<UUID, Long> map, double max) {
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

	public boolean hasJumpPotion(Player player) {
		return player.hasPotionEffect(PotionEffectType.JUMP);
	}

	public boolean hasSpeedPotion(Player player) {
		return player.hasPotionEffect(PotionEffectType.SPEED);
	}

	public void processChatSpammer(Player player) {
		User user = manager.getUserManager().getUser(player.getUniqueId());
		int level = chatLevel.containsKey(user.getUUID()) ? chatLevel.get(user.getUUID()) : 0;
		if (player != null && player.isOnline() && level >= magic.CHAT_ACTION_ONE_LEVEL()) {
			String event = level >= magic.CHAT_ACTION_TWO_LEVEL()
					? manager.getConfiguration().getConfig().chatSpamActionTwo.getValue()
					: manager.getConfiguration().getConfig().chatSpamActionOne.getValue();
			manager.getUserManager().execute(manager.getUserManager().getUser(player.getUniqueId()),
					Utilities.stringToList(event), CheckType.CHAT_SPAM, lang.SPAM_KICK_REASON(),
					Utilities.stringToList(lang.SPAM_WARNING()), lang.SPAM_BAN_REASON());
		}
		chatLevel.put(user.getUUID(), level + 1);
	}

	public void processCommandSpammer(Player player) {
		User user = manager.getUserManager().getUser(player.getUniqueId());
		int level = commandLevel.containsKey(user.getUUID()) ? commandLevel.get(user.getUUID()) : 0;
		if (player != null && player.isOnline() && level >= magic.COMMAND_ACTION_ONE_LEVEL()) {
			String event = level >= magic.COMMAND_ACTION_TWO_LEVEL()
					? manager.getConfiguration().getConfig().commandSpamActionTwo.getValue()
					: manager.getConfiguration().getConfig().commandSpamActionOne.getValue();
			manager.getUserManager().execute(manager.getUserManager().getUser(player.getUniqueId()),
					Utilities.stringToList(event), CheckType.COMMAND_SPAM, lang.SPAM_KICK_REASON(),
					Utilities.stringToList(lang.SPAM_WARNING()), lang.SPAM_BAN_REASON());
		}
		commandLevel.put(user.getUUID(), level + 1);
	}

	public int increment(Player player, Map<UUID, Integer> ascensionCount2, int num) {
		UUID name = player.getUniqueId();
		if (ascensionCount2.get(name) == null) {
			ascensionCount2.put(name, 1);
			return 1;
		} else {
			int amount = ascensionCount2.get(name) + 1;
			if (amount < num + 1) {
				ascensionCount2.put(name, amount);
				return amount;
			} else {
				ascensionCount2.put(name, num);
				return num;
			}
		}
	}

	public int incrementOld(Player player, Map<UUID, Integer> ascensionCount2, int num) {
		UUID uuid = player.getUniqueId();
		if (ascensionCount2.get(uuid) == null) {
			ascensionCount2.put(uuid, 1);
			return 1;
		} else {
			int amount = ascensionCount2.get(uuid) + 1;
			if (amount < num + 1) {
				ascensionCount2.put(uuid, amount);
				return amount;
			} else {
				ascensionCount2.put(uuid, num);
				return num;
			}
		}
	}

	public boolean silentMode() {
		return manager.getConfiguration().getConfig().silentMode.getValue();
	}
}
