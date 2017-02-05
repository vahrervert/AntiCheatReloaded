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

package com.rammelkast.anticheatreloaded.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.rammelkast.anticheatreloaded.AntiCheat;
import com.rammelkast.anticheatreloaded.check.Backend;
import com.rammelkast.anticheatreloaded.check.CheckType;
import com.rammelkast.anticheatreloaded.config.Configuration;
import com.rammelkast.anticheatreloaded.manage.*;
import com.rammelkast.anticheatreloaded.util.User;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EventListener implements Listener {
    private static final Map<CheckType, Integer> USAGE_LIST = new EnumMap<CheckType, Integer>(CheckType.class);
    private static final Map<UUID, Integer> DECREASE_LIST = new HashMap<UUID, Integer>();
    private static final CheckManager CHECK_MANAGER = AntiCheat.getManager().getCheckManager();
    private static final Backend BACKEND = AntiCheat.getManager().getBackend();
    private static final AntiCheat PLUGIN = AntiCheat.getManager().getPlugin();
    private static final UserManager USER_MANAGER = AntiCheat.getManager().getUserManager();
    private static final Configuration CONFIG = AntiCheat.getManager().getConfiguration();

    public static void log(String message, Player player, CheckType type) {
        User user = getUserManager().getUser(player.getUniqueId());
        if (user != null) { // npc
            logCheat(type, user);
            if (user.increaseLevel(type) && message != null) {
                AntiCheat.getManager().log(message);
            }
            removeDecrease(user);
        }
    }

    private static void logCheat(CheckType type, User user) {
        USAGE_LIST.put(type, getCheats(type) + 1);
        // Ignore plugins that are creating NPCs with no names (why the hell)
        if (user != null && user.getUUID() != null) {
            type.logUse(user);
            if (CONFIG.getConfig().enterprise.getValue() && CONFIG.getEnterprise().loggingEnabled.getValue()) {
                CONFIG.getEnterprise().database.logEvent(user, type);
            }
        }
    }

    public void resetCheck(CheckType type) {
        USAGE_LIST.put(type, 0);
    }

    public static int getCheats(CheckType type) {
        int x = 0;
        if (USAGE_LIST.get(type) != null) {
            x = USAGE_LIST.get(type);
        }
        return x;
    }

    private static void removeDecrease(User user) {
        int x = 0;
        // Ignore plugins that are creating NPCs with no names
        if (user.getUUID() != null) {
            if (DECREASE_LIST.get(user.getUUID()) != null) {
                x = DECREASE_LIST.get(user.getUUID());
                x -= 2;
                if (x < 0) {
                    x = 0;
                }
            }
            DECREASE_LIST.put(user.getUUID(), x);
        }
    }

    public static void decrease(Player player) {
        User user = getUserManager().getUser(player.getUniqueId());
        // Ignore plugins that are creating NPCs with no names
        if (user.getUUID() != null) {
            int x = 0;

            if (DECREASE_LIST.get(user.getUUID()) != null) {
                x = DECREASE_LIST.get(user.getUUID());
            }

            x += 1;
            DECREASE_LIST.put(user.getUUID(), x);

            if (x >= 10) {
                user.decreaseLevel();
                DECREASE_LIST.put(user.getUUID(), 0);
            }
        }
    }

    public static CheckManager getCheckManager() {
        return CHECK_MANAGER;
    }

    public static AntiCheatManager getManager() {
        return AntiCheat.getManager();
    }

    public static Backend getBackend() {
        return BACKEND;
    }

    public static UserManager getUserManager() {
        return USER_MANAGER;
    }

    public static AntiCheat getPlugin() {
        return PLUGIN;
    }

    public static Configuration getConfig() {
        return CONFIG;
    }

    public static boolean silentMode() {
        return CONFIG.getConfig().silentMode.getValue();
    }
}
