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

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.protocol.events.PacketEvent;
import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.check.CheckResult;
import com.rammelkast.anticheatreloaded.check.CheckType;
import com.rammelkast.anticheatreloaded.event.EventListener;

/**
 * @author Rammelkast
 */
public class MorePacketsCheck {

	public static final Map<UUID, Integer> MOVE_COUNT = new HashMap<UUID, Integer>();

	public static void startTimer() {
		new BukkitRunnable() {
			@Override
			public void run() {
				MOVE_COUNT.clear();
			}
		}.runTaskTimer(AntiCheatReloaded.getPlugin(), 20, 20);
	}

	public static void runCheck(Player player, PacketEvent event) {
		// Confirm if we should even check for MorePackets
		// If your TPS is lower than 15 you are running a shitshow
		double tps = AntiCheatReloaded.getPlugin().getTPS();
		if (!AntiCheatReloaded.getManager().getCheckManager().willCheck(player, CheckType.MOREPACKETS) || tps < 15) {
			return;
		}
		Location currentLocation = event.getPlayer().getLocation();
		if (!MOVE_COUNT.containsKey(player.getUniqueId())) {
			MOVE_COUNT.put(player.getUniqueId(), 1);
		} else {
			MOVE_COUNT.put(player.getUniqueId(), MOVE_COUNT.get(player.getUniqueId()) + 1);
			int ping = AntiCheatReloaded.getManager().getUserManager().getUser(player.getUniqueId()).getPing();
			int limit = AntiCheatReloaded.getManager().getBackend().getMagic().MOREPACKETS_LIMIT();
			int averagePing = AntiCheatReloaded.getManager().getBackend().getMagic().MOREPACKETS_AVERAGE_PING();
			float pingLeniency = (float) ((ping / averagePing));
			if (pingLeniency < 1)
				pingLeniency = 1;
			if (pingLeniency > 2)
				pingLeniency = 2;
			float tpsLeniency = (float) ((19 / tps));
			if (tps > 19) {
				tpsLeniency = 1;
			}
			float leniency = pingLeniency * tpsLeniency;
			limit = Math.round(leniency * limit);
			final float finalLeniency = leniency;
			try {
				if (MOVE_COUNT.get(player.getUniqueId()) > limit) {
					final int packets = MOVE_COUNT.get(player.getUniqueId());
					MOVE_COUNT.remove(player.getUniqueId());
					event.setCancelled(true);
					// We are currently not in the main server thread, so switch
					AntiCheatReloaded.sendToMainThread(new Runnable() {
						@Override
						public void run() {
							EventListener
									.log(new CheckResult(CheckResult.Result.FAILED,
											"sent " + packets + " packets in one second (max="
													+ AntiCheatReloaded.getManager().getBackend().getMagic()
															.MOREPACKETS_LIMIT()
													+ ", leniency=" + finalLeniency + ")").getMessage(),
											player, CheckType.MOREPACKETS);
							player.teleport(currentLocation);
						}
					});
				}
			} catch (NullPointerException nullPointer) {
				// TODO this is thrown sometimes, might have something to do with line 83
			}
		}
	}

}
