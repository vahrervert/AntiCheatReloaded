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

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * @author Rammelkast
 */
@Deprecated
public class KillAuraCheck {

	//private static final Map<UUID, NPC_1_12> NPC_LIST = new HashMap<UUID, NPC_1_12>();
	//private static final Map<UUID, Integer> VL_COUNT = new HashMap<UUID, Integer>();

	public static void cleanPlayer(Player p) {
		/*if (!VersionUtil.getVersion().equals("v1_12_R1")) {
			return;
		}
		NPC_LIST.remove(p.getUniqueId());*/
	}

	public static void listenPackets() {
		/*if (!VersionUtil.getVersion().equals("v1_12_R1")) {
			return;
		}
		AntiCheatReloaded.getProtocolManager().addPacketListener(new PacketAdapter(AntiCheatReloaded.getPlugin(),
				ListenerPriority.NORMAL, new PacketType[] { PacketType.Play.Client.USE_ENTITY }) {
			@Override
			public void onPacketReceiving(PacketEvent e) {
				Player p = e.getPlayer();
				if (!NPC_LIST.containsKey(p.getUniqueId()))
					return;
				int id = e.getPacket().getIntegers().read(0);
				if (NPC_LIST.containsKey(p.getUniqueId())) {
					if (NPC_LIST.get(p.getUniqueId()).getID() != id) {
						return;
					}
				}
				if (!VL_COUNT.containsKey(p.getUniqueId())) {
					VL_COUNT.put(p.getUniqueId(), 1);
					NPC_LIST.get(p.getUniqueId()).damage();
				} else {
					VL_COUNT.put(p.getUniqueId(), VL_COUNT.get(p.getUniqueId()) + 1);
					if (VL_COUNT.get(p.getUniqueId()) >= 5) {
						EventListener.log(
								new CheckResult(CheckResult.Result.FAILED,
										p.getName() + " failed KillAura (botcheck), hit the bot "
												+ VL_COUNT.get(p.getUniqueId()) + " times (max=5)").getMessage(),
								p, CheckType.KILLAURA);
						AntiCheatReloaded.getPlugin().onKillAuraViolation();
						VL_COUNT.remove(p.getUniqueId());
					}
				}
			}
		});*/
	}

	public static void doDamageEvent(EntityDamageByEntityEvent e, Player p) {
		/*if (!VersionUtil.getVersion().equals("v1_12_R1")) {
			return;
		}
		if (NPC_LIST.containsKey(p.getUniqueId()))
			NPC_LIST.get(p.getUniqueId()).damage(e, p);
		else {
			NPC_1_12 npc = new NPC_1_12(p);
			NPC_LIST.put(p.getUniqueId(), npc);
			npc.spawn();
		}*/
	}

	public static void doMove(PlayerMoveEvent e) {
		/*if (!VersionUtil.getVersion().equals("v1_12_R1")) {
			return;
		}
		if (NPC_LIST.containsKey(e.getPlayer().getUniqueId())) {
			NPC_LIST.get(e.getPlayer().getUniqueId()).move(e);
		}*/
	}

}
