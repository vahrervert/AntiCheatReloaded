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
package com.rammelkast.anticheatreloaded.util;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class MovementManager {

	// Ticks in air
	public int airTicks = 0;
	// Ticks on ground
	public int groundTicks = 0;
	// Ticks on ice
	public int iceTicks = 0;
	// Ticks in air before last grounded moment
	public int airTicksBeforeGrounded = 0;
	// Ticks influenced by ice
	public int iceInfluenceTicks = 0;
	// If the player touched the ground again this tick
	public boolean touchedGroundThisTick = false;
	// Last recorded distance
	public Distance lastDistance = new Distance();
	// Movement acceleration
	public double acceleration;

	public void handle(Player player, Location from, Location to, Distance distance) {
		boolean serverOnGround = !Utilities.cantStandAtExp(to) || Utilities.couldBeOnBoat(player);
		this.touchedGroundThisTick = false;
		if (!serverOnGround) {
			this.groundTicks = 0;
			this.airTicks++;
		} else {
			if (this.airTicks > 0)
				this.touchedGroundThisTick = true;
			this.airTicksBeforeGrounded = this.airTicks;
			this.airTicks = 0;
			this.groundTicks++;
		}

		if (Utilities.couldBeOnIce(to)) {
			this.iceTicks++;
			this.iceInfluenceTicks = 14;
		} else {
			this.iceTicks = 0;
			if (this.iceInfluenceTicks > 0)
				this.iceInfluenceTicks--;
		}
		
		double lastDistanceSq = Math.sqrt(this.lastDistance.getXDifference() * this.lastDistance.getXDifference()
				+ this.lastDistance.getZDifference() * this.lastDistance.getZDifference());
		double currentDistanceSq = Math.sqrt(distance.getXDifference() * distance.getXDifference()
				+ distance.getZDifference() * distance.getZDifference());
		this.acceleration = currentDistanceSq - lastDistanceSq;
	}

}
