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

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.check.CheckResult;
import com.rammelkast.anticheatreloaded.util.checkassist.KillauraAssist;

public class KillAuraCheck {

	public static final Map<String, Integer> ANGLE_FLAGS = new HashMap<String, Integer>();
	private static final KillauraAssist KILLAURA_ASSIST;
	private static final CheckResult PASS = new CheckResult(CheckResult.Result.PASSED);

	public static void listenPackets() {
		/*AntiCheatReloaded.getProtocolManager().addPacketListener(new PacketAdapter(AntiCheatReloaded.getPlugin(),
				ListenerPriority.LOWEST, new PacketType[] { PacketType.Play.Client.POSITION_LOOK }) {
			@Override
			public void onPacketReceiving(PacketEvent e) {
				Player player = e.getPlayer();
				PacketContainer packet = e.getPacket();
			}
		});*/
	}
	
	public static CheckResult checkAngle(Player player, EntityDamageEvent event) {
		Entity entity = event.getEntity();
		if (entity instanceof LivingEntity) {
			LivingEntity living = (LivingEntity) entity;
			Location eyeLocation = player.getEyeLocation();

			double yawDifference = calculateYawDifference(eyeLocation, living.getLocation());
			double playerYaw = player.getEyeLocation().getYaw();

			double angleDifference = Math.abs(180 - Math.abs(Math.abs(yawDifference - playerYaw) - 180));
			if (Math.round(angleDifference) > AntiCheatReloaded.getManager().getConfiguration().getMagic().KILLAURA_MAX_ANGLE_DIFFERENCE()) {
				if (!ANGLE_FLAGS.containsKey(player.getUniqueId().toString())) {
					ANGLE_FLAGS.put(player.getUniqueId().toString(), 1);
					return PASS;
				}
				
				int flags = ANGLE_FLAGS.get(player.getUniqueId().toString());
				if (flags >= AntiCheatReloaded.getManager().getConfiguration().getMagic().KILLAURA_MAX_ANGLE_VIOLATIONS()) {
					ANGLE_FLAGS.remove(player.getUniqueId().toString());
					return new CheckResult(CheckResult.Result.FAILED, player.getName() + " failed KillAura, tried to attack from an illegal angle (angle=" + Math.round(angleDifference) + ")");
				}
				
				ANGLE_FLAGS.put(player.getUniqueId().toString(), flags + 1);
			}
		}
		return PASS;
	}
	
	public static double calculateYawDifference(Location from, Location to) {
		Location clonedFrom = from.clone();
		Vector startVector = clonedFrom.toVector();
		Vector targetVector = to.toVector();
		clonedFrom.setDirection(targetVector.subtract(startVector));
		return clonedFrom.getYaw();
	}

	static {
		KILLAURA_ASSIST = new KillauraAssist();
	}

}
