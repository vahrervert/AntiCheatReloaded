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

package com.rammelkast.anticheatreloaded.check.movement;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.check.CheckResult;
import com.rammelkast.anticheatreloaded.check.CheckType;
import com.rammelkast.anticheatreloaded.event.EventListener;
import com.rammelkast.anticheatreloaded.util.VersionUtil;

/**
 * @author Rammelkast
 */
public class BlinkCheck {

	public static final Map<UUID, Integer> MOVE_COUNT = new HashMap<UUID, Integer>();

	public static void startTimer() {
		new BukkitRunnable() {
			@Override
			public void run() {
				MOVE_COUNT.clear();
			}
		}.runTaskTimer(AntiCheatReloaded.getPlugin(), 20, 20);
	}

	public static void listenPackets() {
		AntiCheatReloaded.getProtocolManager()
				.addPacketListener(new PacketAdapter(AntiCheatReloaded.getPlugin(), ListenerPriority.LOWEST,
						new PacketType[] { PacketType.Play.Client.POSITION, PacketType.Play.Client.POSITION_LOOK }) {
					@Override
					public void onPacketReceiving(PacketEvent e) {
						Player p = e.getPlayer();
						Location cur = e.getPlayer().getLocation();
						if (!MOVE_COUNT.containsKey(p.getUniqueId())) {
							MOVE_COUNT.put(p.getUniqueId(), 1);
						} else {
							MOVE_COUNT.put(p.getUniqueId(), MOVE_COUNT.get(p.getUniqueId()) + 1);
							if (AntiCheatReloaded.getManager().getCheckManager().checkInWorld(p)
									&& !AntiCheatReloaded.getManager().getCheckManager().isOpExempt(p)
									&& !AntiCheatReloaded.getManager().getCheckManager().isExempt(p, CheckType.BLINK)) {
								int ping = VersionUtil.getPlayerPing(p);
								int blinkMagic = AntiCheatReloaded.getManager().getBackend()
										.getMagic().BLINK_PACKET();
								int averagePing =  AntiCheatReloaded.getManager().getBackend()
										.getMagic().BLINK_AVERAGE_PING();
								float pingLeniency = (float) ((ping / averagePing));
								if (pingLeniency < 1) pingLeniency = 1;
								if (pingLeniency > 3) pingLeniency = 3;
								blinkMagic = Math.round(pingLeniency * blinkMagic);
								final float finalPingLeniency = pingLeniency;
								try {
									if (MOVE_COUNT.get(p.getUniqueId()) > blinkMagic) {
										final int packets = MOVE_COUNT.get(p.getUniqueId());
										MOVE_COUNT.remove(p.getUniqueId());
										AntiCheatReloaded.sendToMainThread(new Runnable() {
											@Override
											public void run() {
												EventListener.log(
														new CheckResult(CheckResult.Result.FAILED, p.getName()
																+ " failed Blink, sent " + packets
																+ " packets in one second (max=" + AntiCheatReloaded.getManager()
																		.getBackend().getMagic().BLINK_PACKET()
																+ ", leniency=" + finalPingLeniency + ", ping=" + ping + ")").getMessage(),
														p, CheckType.BLINK);
												e.setCancelled(true);
												e.getPlayer().teleport(cur);
											}
										});
									}
								} catch (NullPointerException nullPointer) {
									// TODO this is thrown sometimes, might have something to do with line 83
								}
							}
						}
					}
				});
	}

}
