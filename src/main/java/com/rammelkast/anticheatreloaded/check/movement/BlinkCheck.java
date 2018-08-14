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
				.addPacketListener(new PacketAdapter(AntiCheatReloaded.getPlugin(), ListenerPriority.NORMAL,
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
								if (MOVE_COUNT.get(p.getUniqueId()) > AntiCheatReloaded.getManager().getBackend()
										.getMagic().BLINK_PACKET()) {
									EventListener.log(
											new CheckResult(CheckResult.Result.FAILED, p.getName()
													+ " failed Blink, sent " + MOVE_COUNT.get(p.getUniqueId())
													+ " packets in one second (max=" + AntiCheatReloaded.getManager()
															.getBackend().getMagic().BLINK_PACKET()
													+ ")").getMessage(),
											p, CheckType.BLINK);
									MOVE_COUNT.remove(p.getUniqueId());
									e.setCancelled(true);
									e.getPlayer().teleport(cur);
								}
							}
						}
					}
				});
	}

}
