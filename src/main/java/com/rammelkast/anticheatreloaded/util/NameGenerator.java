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

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class NameGenerator {

	private static final List<String> BEGIN = Arrays.asList(new String[] { "Pixel", "Pro", "NoHax", "Just", "That",
			"New", "Ultra", "Killer", "Swift", "Anarchy", "Yolo", "Rammel" });

	private static final List<String> END = Arrays.asList(new String[] { "Gamer", "PvP", "JustYolo", "Gaming",
			"Minecraft", "Games", "Bawz", "Tech", "Noob", "Extra", "kast", "" + new Random().nextInt(100) });

	public static String generateName() {
		Random r = new Random();
		String begin = BEGIN.get(r.nextInt(BEGIN.size() - 1));
		String end = END.get(r.nextInt(END.size() - 1));
		if (r.nextBoolean())
			return begin + end + r.nextInt(9);
		else
			return begin + end;
	}

}
