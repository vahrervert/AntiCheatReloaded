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

package com.rammelkast.anticheatreloaded.util;

import java.util.Random;

public class NameUtil {

	private static String[] firstParts = new String[] {
		"Gamer",
		"Pixel",
		"Derp",
		"Random",
		"That",
		"NoHaxJust",
		"Derp",
		"Fun",
		"Luigi"
	};

	private static String[] secondParts = new String[] {
		"01",
		"Boy",
		"Guy",
		"Mario",
		"Plain",
		"Simple",
		"NotHacking",
		"Random",
		"11",
		"06"
	};
	
	private static Random theRandom = new Random();
	
	public static String getRandomName() {
		return firstParts[theRandom.nextInt(firstParts.length - 1)] + secondParts[theRandom.nextInt(secondParts.length - 1)];
	}
}
