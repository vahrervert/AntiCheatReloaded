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

package com.rammelkast.anticheatreloaded.check;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.rammelkast.anticheatreloaded.api.CheckFailEvent;
import com.rammelkast.anticheatreloaded.util.Permission;
import com.rammelkast.anticheatreloaded.util.User;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * <p/>
 * All the types of checks and their corresponding permission nodes.
 */

public enum CheckType {
    ZOMBE_FLY(Permission.CHECK_ZOMBE_FLY),
    ZOMBE_NOCLIP(Permission.CHECK_ZOMBE_NOCLIP),
    ZOMBE_CHEAT(Permission.CHECK_ZOMBE_CHEAT),
    FLY(Permission.CHECK_FLY),
    WATER_WALK(Permission.CHECK_WATERWALK),
    NO_SWING(Permission.CHECK_NOSWING),
    FAST_BREAK(Permission.CHECK_FASTBREAK),
    FAST_PLACE(Permission.CHECK_FASTPLACE),
    CHAT_SPAM(Permission.CHECK_CHATSPAM),
    COMMAND_SPAM(Permission.CHECK_COMMANDSPAM),
    SPRINT(Permission.CHECK_SPRINT),
    SNEAK(Permission.CHECK_SNEAK),
    SPEED(Permission.CHECK_SPEED),
    VCLIP(Permission.CHECK_VCLIP),
    SPIDER(Permission.CHECK_SPIDER),
    NOFALL(Permission.CHECK_NOFALL),
    FAST_BOW(Permission.CHECK_FASTBOW),
    FAST_EAT(Permission.CHECK_FASTEAT),
    FAST_HEAL(Permission.CHECK_FASTHEAL),
    FORCEFIELD(Permission.CHECK_FORCEFIELD),
    LONG_REACH(Permission.CHECK_LONGREACH),
    FAST_PROJECTILE(Permission.CHECK_FASTPROJECTILE),
    ITEM_SPAM(Permission.CHECK_ITEMSPAM),
    FAST_INVENTORY(Permission.CHECK_FASTINVENTORY),
    AUTOTOOL(Permission.CHECK_AUTOTOOL),
    KILLAURA(Permission.CHECK_KILLAURA), 
    BLINK(Permission.CHECK_BLINK),
	VELOCITY(Permission.VELOCITY),
	CRITICALS(Permission.CRITICALS);

    private final Permission permission;
    private final Map<UUID, Integer> level = new HashMap<UUID, Integer>();

    /**
     * Initialize a CheckType
     *
     * @param perm Permission that applies to this check
     */
    private CheckType(Permission perm) {
        this.permission = perm;
    }

    /**
     * Determine whether a player has permission to bypass this check
     *
     * @param player Player to check
     * @return true if the player can bypass
     */
    public boolean checkPermission(Player player) {
        return permission.get(player);
    }

    /**
     * Log the failure of this check
     *
     * @param user User who failed the check
     */
    public void logUse(User user) {
        int amount = level.get(user.getUUID()) == null ? 1 : level.get(user.getUUID()) + 1;
        level.put(user.getUUID(), amount);
        Bukkit.getServer().getPluginManager().callEvent(new CheckFailEvent(user, this));
    }

    /**
     * Clear failure history of this check for a user
     *
     * @param UUID User's UUID to clear
     */
    public void clearUse(UUID uuid) {
        level.put(uuid, 0);
    }

    /**
     * Get how many times a user has failed this check
     *
     * @param UUID User's UUID
     * @return number of times failed
     */
    public int getUses(UUID uuid) {
        return level.get(uuid) != null ? level.get(uuid) : 0;
    }

    /**
     * Get the reference name of a check
     *
     * @param type Type of check
     * @return reference name
     */
    public static String getName(CheckType type) {
        char[] chars = type.toString().replaceAll("_", " ").toLowerCase().toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }
}
