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

package com.rammelkast.anticheatreloaded.util;

import java.util.EnumSet;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

public class NMS_1_9_PLUS {

	static final EnumSet<Material> MOVE_UP_BLOCKS_1_9 = EnumSet.of(Material.ACACIA_STAIRS, Material.BIRCH_WOOD_STAIRS,
			Material.BRICK_STAIRS, Material.COBBLESTONE_STAIRS, Material.DARK_OAK_STAIRS, Material.JUNGLE_WOOD_STAIRS,
			Material.NETHER_BRICK_STAIRS, Material.QUARTZ_STAIRS, Material.RED_SANDSTONE_STAIRS,
			Material.SANDSTONE_STAIRS, Material.SMOOTH_STAIRS, Material.SPRUCE_WOOD_STAIRS, Material.WOOD_STAIRS,
			Material.PURPUR_STAIRS);

	static final PotionEffectType LEVITATION = PotionEffectType.LEVITATION;

	static final Enchantment FROST_WALKER = Enchantment.FROST_WALKER;

	public static ItemStack getItemInHand(Player player) {
		return player.getInventory().getItemInMainHand();
	}

}
