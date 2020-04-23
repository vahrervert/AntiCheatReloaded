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

package com.rammelkast.anticheatreloaded.check.player;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.check.CheckResult;
import com.rammelkast.anticheatreloaded.check.combat.KillAuraCheck;
import com.rammelkast.anticheatreloaded.config.providers.Magic;
import com.rammelkast.anticheatreloaded.util.Utilities;
import com.rammelkast.anticheatreloaded.util.VersionUtil;

public class IllegalInteract {

	private static final CheckResult PASS = new CheckResult(CheckResult.Result.PASSED);

	public static CheckResult performCheck(Player player, Event event) {
		if (event instanceof BlockPlaceEvent) {
			return checkBlockPlace(player, (BlockPlaceEvent) event);
		} else if (event instanceof BlockBreakEvent) {
			return checkBlockBreak(player, (BlockBreakEvent) event);
		}
		return PASS;
	}

	private static CheckResult checkBlockBreak(Player player, BlockBreakEvent event) {
		if (!isValidTarget(player, event.getBlock())) {
			return new CheckResult(CheckResult.Result.FAILED, "tried to break a block which was out of view");
		}
		return PASS;
	}

	private static CheckResult checkBlockPlace(Player player, BlockPlaceEvent event) {
		if (!isValidTarget(player, event.getBlock())) {
			return new CheckResult(CheckResult.Result.FAILED, "tried to place a block out of their view");
		}
		return PASS;
	}

	private static boolean isValidTarget(Player player, Block block) {
		Magic magic = AntiCheatReloaded.getManager().getConfiguration().getMagic();
		double distance = player.getGameMode() == GameMode.CREATIVE ? magic.BLOCK_MAX_DISTANCE_CREATIVE()
				: player.getLocation().getDirection().getY() > 0.9 ? magic.BLOCK_MAX_DISTANCE_CREATIVE()
						: magic.BLOCK_MAX_DISTANCE();
		Block targetBlock = VersionUtil.getTargetBlock(player, ((int) Math.ceil(distance)));
		if (targetBlock == null) {
			// TODO better check here
			return true;
		}

		if (Utilities.isClimbableBlock(targetBlock)) {
			if (targetBlock.getLocation().distance(player.getLocation()) <= distance) {
				return true;
			}
		}

		if (targetBlock.equals(block)) {
			return true;
		}

		Location eyeLocation = player.getEyeLocation();
		double yawDifference = KillAuraCheck.calculateYawDifference(eyeLocation, block.getLocation());
		double playerYaw = player.getEyeLocation().getYaw();
		double angleDifference = Math.abs(180 - Math.abs(Math.abs(yawDifference - playerYaw) - 180));
		if (Math.round(angleDifference) > magic.ILLEGALINTERACT_MAX_ANGLE_DIFFERENCE()) {
			return false;
		}
		return true;
	}

}
