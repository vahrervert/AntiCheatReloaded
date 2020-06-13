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
	// Ticks on slime
	public int slimeTicks = 0;
	// Ticks in air before last grounded moment
	public int airTicksBeforeGrounded = 0;
	// Ticks influenced by ice
	public int iceInfluenceTicks = 0;
	// Ticks influenced by slime
	public int slimeInfluenceTicks = 0;
	// Ticks influenced by sign
	public int signInfluenceTicks = 0;
	// Last Y motion
	public double motionY;
	// If the player touched the ground again this tick
	public boolean touchedGroundThisTick = false;
	// Last recorded distance
	public Distance lastDistance = new Distance();
	// Movement acceleration
	public double acceleration;
	// Is the block above solid
	public boolean topSolid;
	// Is the block below solid
	public boolean bottomSolid;
	// If the current movement is up a slab or stair
	public boolean halfMovement;
	// Ticks counter for last halfMovement
	public int halfMovementHistoryCounter = 0;

	public void handle(Player player, Location from, Location to, Distance distance) {
		boolean serverOnGround = !Utilities.cantStandAtExp(to) || Utilities.couldBeOnBoat(player);
		this.touchedGroundThisTick = false;
		this.halfMovement = false;
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
		
		if (to.getBlock().getType().name().endsWith("SIGN") || to.getBlock().getType().name().endsWith("POST")
				|| from.getBlock().getType().name().endsWith("SIGN")
				|| from.getBlock().getType().name().endsWith("POST"))
			this.signInfluenceTicks = 3;
		else
			if (this.signInfluenceTicks > 0)
				this.signInfluenceTicks--;

		if (Utilities.couldBeOnIce(to)) {
			this.iceTicks++;
			this.iceInfluenceTicks = 60;
		} else {
			this.iceTicks = 0;
			if (this.iceInfluenceTicks > 0)
				this.iceInfluenceTicks--;
		}
		
		if (Utilities.couldBeOnSlime(to)) {
			this.slimeTicks++;
			this.slimeInfluenceTicks = 40;
		} else {
			this.slimeTicks = 0;
			if (this.slimeInfluenceTicks > 0)
				this.slimeInfluenceTicks--;
		}
		
		double lastDistanceSq = Math.sqrt(this.lastDistance.getXDifference() * this.lastDistance.getXDifference()
				+ this.lastDistance.getZDifference() * this.lastDistance.getZDifference());
		double currentDistanceSq = Math.sqrt(distance.getXDifference() * distance.getXDifference()
				+ distance.getZDifference() * distance.getZDifference());
		this.acceleration = currentDistanceSq - lastDistanceSq;
		
		this.motionY = to.getY() - from.getY();
		
		Location top = to.clone().add(0, 2, 0);
		this.topSolid = top.getBlock().getType().isSolid();
		Location bottom = to.clone().add(0, -1, 0);
		this.bottomSolid = bottom.getBlock().getType().isSolid();
		
		if ((this.motionY > 0.464D && this.motionY <= 0.5625D) && (Utilities.couldBeOnHalfblock(to) || Utilities.isNearBed(to))) {
			this.halfMovement = true;
			this.halfMovementHistoryCounter = 30;
		} else {
			if (this.halfMovementHistoryCounter > 0)
				this.halfMovementHistoryCounter--;
		}
	}

}
