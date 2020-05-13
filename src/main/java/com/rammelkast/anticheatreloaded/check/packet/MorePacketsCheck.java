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

package com.rammelkast.anticheatreloaded.check.packet;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

import com.comphenix.protocol.events.PacketEvent;
import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.check.CheckResult;
import com.rammelkast.anticheatreloaded.check.CheckType;
import com.rammelkast.anticheatreloaded.config.providers.Magic;
import com.rammelkast.anticheatreloaded.event.EventListener;

/**
 * @author Rammelkast
 */
public class MorePacketsCheck {

	public static final Map<UUID, Long> LAST_PACKET_TIME = new HashMap<UUID, Long>();
	public static final Map<UUID, Double> PACKET_BALANCE = new HashMap<UUID, Double>();

	public static void runCheck(Player player, PacketEvent event) {
		// Confirm if we should even check for MorePackets
		// If your TPS is lower than 15 you are running a shitshow
		double tps = AntiCheatReloaded.getPlugin().getTPS();
		if (!AntiCheatReloaded.getManager().getCheckManager().willCheck(player, CheckType.MOREPACKETS) || tps < 15) {
			return;
		}

		UUID uuid = player.getUniqueId();
		Magic magic = AntiCheatReloaded.getManager().getConfiguration().getMagic();
		long packetTimeNow = System.currentTimeMillis();
		long lastPacketTime = LAST_PACKET_TIME.getOrDefault(uuid, packetTimeNow - 50L);
		double packetBalance = PACKET_BALANCE.getOrDefault(uuid, 0D);

		long rate = packetTimeNow - lastPacketTime;
		packetBalance += 50;
		packetBalance -= rate;
		if (packetBalance >= magic.MOREPACKETS_TRIGGER_BALANCE()) {
			int ticks = (int) Math.round(packetBalance / 50);
			packetBalance = -1 * (magic.MOREPACKETS_TRIGGER_BALANCE() / 2);
			flag(player, event, "overshot timer by " + ticks + " tick(s)");
		} else if (packetBalance < -1 * (magic.MOREPACKETS_MINIMUM_CLAMP())) {
			// Clamp minimum, 50ms=1tick of lag leniency
			packetBalance = -1 * (magic.MOREPACKETS_MINIMUM_CLAMP());
		}

		LAST_PACKET_TIME.put(uuid, packetTimeNow);
		PACKET_BALANCE.put(uuid, packetBalance);
	}

	private static void flag(Player player, PacketEvent event, String message) {
		event.setCancelled(true);
		// We are currently not in the main server thread, so switch
		AntiCheatReloaded.sendToMainThread(new Runnable() {
			@Override
			public void run() {
				EventListener.log(new CheckResult(CheckResult.Result.FAILED, message).getMessage(), player,
						CheckType.MOREPACKETS);
				player.teleport(player.getLocation());
			}
		});
	}

	public static void compensate(Player player) {
		UUID uuid = player.getUniqueId();
		Magic magic = AntiCheatReloaded.getManager().getConfiguration().getMagic();
		double packetBalance = PACKET_BALANCE.getOrDefault(uuid, 0D);
		PACKET_BALANCE.put(uuid, packetBalance - magic.MOREPACKETS_TELEPORT_COMPENSATION());
	}

}
