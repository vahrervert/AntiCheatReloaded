/*
 * AntiCheatReloaded for Bukkit and Spigot.
 * Copyright (c) 2012-2015 AntiCheat Team | http://gravitydevelopment.net
 * Copyright (c) 2016-2018 Rammelkast | https://rammelkast.com
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

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.check.CheckResult;
import com.rammelkast.anticheatreloaded.util.Distance;
import com.rammelkast.anticheatreloaded.util.Utilities;

/**
 * TODO
 */
public class ElytraCheck {

	private static final CheckResult PASS = new CheckResult(CheckResult.Result.PASSED);

	public static CheckResult runCheck(Player player, Distance distance) {
		if (distance.getYDifference() > AntiCheatReloaded.getManager().getBackend().getMagic().TELEPORT_MIN()) {
			// This was a teleport, so skip check.
			return PASS;
		}
		/*if (!FlightCheck.isMovingExempt(player) && !Utilities.isHoveringOverWater(player.getLocation(), 1)
				&& Utilities.cantStandAtExp(player.getLocation())
				&& Utilities.blockIsnt(player.getLocation().getBlock().getRelative(BlockFace.DOWN),
						new Material[] { Material.FENCE, Material.FENCE_GATE, Material.COBBLE_WALL,
								Material.ACACIA_FENCE, Material.ACACIA_FENCE_GATE, Material.BIRCH_FENCE,
								Material.BIRCH_FENCE_GATE, Material.DARK_OAK_FENCE, Material.DARK_OAK_FENCE_GATE,
								Material.IRON_FENCE, Material.JUNGLE_FENCE, Material.JUNGLE_FENCE_GATE,
								Material.fence, Material.SPRUCE_FENCE, Material.SPRUCE_FENCE_GATE, Material.IRON_BARS })) {

		}*/
		//TODO 1.15 update

		return PASS;
	}

}
