/*
 * AntiCheat for Bukkit.
 * Copyright (C) 2012-2014 AntiCheat Team | http://gravitydevelopment.net
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

package com.rammelkast.anticheatreloaded.command.executors;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.rammelkast.anticheatreloaded.AntiCheat;
import com.rammelkast.anticheatreloaded.command.CommandBase;
import com.rammelkast.anticheatreloaded.util.Permission;
import com.rammelkast.anticheatreloaded.util.User;

public class CommandReset extends CommandBase {

	private static final String NAME = "AntiCheat Resetting";
	private static final String COMMAND = "reset";
	private static final String USAGE = "anticheat reset [user]";
	private static final Permission PERMISSION = Permission.SYSTEM_RESET;
	private static final String[] HELP = {
			GRAY + "Use: " + AQUA + "/anticheat reset [user]" + GRAY + " to reset this user's hack level",
	};

	public CommandReset() {
		super(NAME, COMMAND, USAGE, HELP, PERMISSION);
	}

	@Override
	protected void execute(CommandSender cs, String[] args) {
		User user;
		if (args.length == 1) {
			if (Bukkit.getPlayer(args[0]) != null) {
				user = USER_MANAGER.getUser(Bukkit.getPlayer(args[0]).getUniqueId());
			} else {
				user = USER_MANAGER.getUser(Bukkit.getOfflinePlayer(args[0]).getUniqueId());
			}
			if (user != null) {
				user.resetLevel();
				user.clearMessages();
				AntiCheat.getManager().getBackend().resetChatLevel(user);
				cs.sendMessage(args[0] + GREEN + " has been reset.");
			} else {
				cs.sendMessage(RED + "Player: " +args[0] + " not found.");
			}
		} else {
			sendHelp(cs);
		}
	}
}
