/*
 * AntiCheatReloaded for Bukkit and Spigot.
 * Copyright (c) 2012-2015 AntiCheat Team
 * Copyright (c) 2016-2021 Rammelkast
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
package com.rammelkast.anticheatreloaded.config.providers;

import com.rammelkast.anticheatreloaded.check.CheckType;

public interface Checks {

	public boolean getBoolean(CheckType checkType, String name);
	
	public boolean getBoolean(CheckType checkType, String subcheck, String name);
	
	public double getDouble(CheckType checkType, String name);
	
	public double getDouble(CheckType checkType, String subcheck, String name);

	public int getInteger(CheckType checkType, String name);
	
	public int getInteger(CheckType checkType, String subcheck, String name);
	
	public boolean isEnabled(CheckType checkType);
	
	public boolean isSubcheckEnabled(CheckType checkType, String subcheck);
	
}
