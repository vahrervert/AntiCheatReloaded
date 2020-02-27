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

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.potion.PotionEffectType;

import com.rammelkast.anticheatreloaded.check.CheckResult;
import com.rammelkast.anticheatreloaded.check.CheckType;
import com.rammelkast.anticheatreloaded.event.EventListener;
import com.rammelkast.anticheatreloaded.util.Utilities;

public class CriticalsCheck {

	public static void doDamageEvent(EntityDamageByEntityEvent e, Player damager) {
		if (!(e.getDamager() instanceof Player) || e.getCause() != DamageCause.ENTITY_ATTACK)
			return;
		Player p = (Player) e.getDamager();
		if (isCritical(p)) {
			if ((p.getLocation().getY() % 1.0 == 0 || p.getLocation().getY() % 0.5 == 0)
					&& p.getLocation().clone().subtract(0, 1.0, 0).getBlock().getType().isSolid()) {
				e.setCancelled(true);
				EventListener.log(new CheckResult(CheckResult.Result.FAILED,
						p.getName() + " failed Criticals, tried to do a critical without needed conditions")
								.getMessage(),
						p, CheckType.CRITICALS);
			}
		}
	}

	private static boolean isCritical(Player player) {
		return player.getFallDistance() > 0.0f && !player.isOnGround() && !player.isInsideVehicle()
				&& !player.hasPotionEffect(PotionEffectType.BLINDNESS)
				&& !Utilities.isHoveringOverWater(player.getLocation())
				&& player.getEyeLocation().getBlock().getType() != Material.LADDER;
	}

}
