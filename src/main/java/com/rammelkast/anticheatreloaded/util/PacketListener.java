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
package com.rammelkast.anticheatreloaded.util;

import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.check.packet.BadPacketsCheck;
import com.rammelkast.anticheatreloaded.check.packet.MorePacketsCheck;

public class PacketListener {

	public static void listenMovementPackets() {
		AntiCheatReloaded.getProtocolManager()
		.addPacketListener(new PacketAdapter(AntiCheatReloaded.getPlugin(), ListenerPriority.LOWEST,
				new PacketType[] { PacketType.Play.Client.POSITION, PacketType.Play.Client.POSITION_LOOK }) {
			@Override
			public void onPacketReceiving(PacketEvent event) {
				Player player = event.getPlayer();
				
				// Run MorePackets check
				MorePacketsCheck.runCheck(player, event);
				
				if (!event.isCancelled()) {
					// Run BadPackets check
					BadPacketsCheck.runCheck(player, event);
				}
			}
		});
	}
	
	public static void listenKeepAlivePackets() {
		AntiCheatReloaded.getProtocolManager()
		.addPacketListener(new PacketAdapter(AntiCheatReloaded.getPlugin(), ListenerPriority.LOWEST,
				new PacketType[] { PacketType.Play.Client.KEEP_ALIVE, PacketType.Play.Server.KEEP_ALIVE }) {
			@Override
			public void onPacketSending(PacketEvent event) {
				User user = AntiCheatReloaded.getManager().getUserManager().getUser(event.getPlayer().getUniqueId());
				user.onServerPing();
			}
			
			@Override
			public void onPacketReceiving(PacketEvent event) {
				User user = AntiCheatReloaded.getManager().getUserManager().getUser(event.getPlayer().getUniqueId());
				user.onClientPong();
			}
		});
	}
	
}
