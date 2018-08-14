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

package com.rammelkast.anticheatreloaded.manage;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.rammelkast.anticheatreloaded.AntiCheatReloaded;

public class PacketManager {

    private ProtocolManager protocolManager;
    private AntiCheatReloaded plugin;
    private AntiCheatManager manager;

    public PacketManager(ProtocolManager protocolManager, AntiCheatReloaded plugin, AntiCheatManager manager) {
        this.protocolManager = protocolManager;
        this.plugin = plugin;
        this.manager = manager;
        addListeners();
    }

    private void addListeners() {
        // Arm Animation
        protocolManager.addPacketListener(new PacketAdapter(plugin,
                ListenerPriority.NORMAL,
                PacketType.Play.Client.ARM_ANIMATION) {

            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (event.getPacketType() == PacketType.Play.Client.ARM_ANIMATION) {
                    PacketContainer packet = event.getPacket();
                    //System.out.println("Arm animation from "+packet.getIntegers().read(0));
                }
            }
        });
    }
}
