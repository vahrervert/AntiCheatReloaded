package net.gravitydevelopment.anticheat.check.movement;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import net.gravitydevelopment.anticheat.AntiCheat;
import net.gravitydevelopment.anticheat.check.Backend;
import net.gravitydevelopment.anticheat.check.CheckResult;
import net.gravitydevelopment.anticheat.util.Distance;
import net.gravitydevelopment.anticheat.util.Utilities;
import net.gravitydevelopment.anticheat.util.VersionUtil;

/**
 * This is broken as f*ck
 */
public class GlideCheck {

	private static final CheckResult PASS = new CheckResult(CheckResult.Result.PASSED);
	
	public static Map<UUID, Double> lastDiff = new HashMap<UUID, Double>();
	public static Map<UUID, Float> lastFallDistance = new HashMap<UUID, Float>();
	public static Map<UUID, Integer> violations = new HashMap<UUID, Integer>();
	
	/*
	 * SOME OLD CODE
	 * 
	 * UUID uuid = player.getUniqueId();
	if(!lastYDelta.containsKey(uuid))
		lastYDelta.put(uuid, 0.0);
	if(!YAxisCheck.lastYcoord.containsKey(uuid))
		YAxisCheck.lastYcoord.put(uuid, player.getLocation().getY());
	double currentY = player.getLocation().getY();
	double math = currentY - YAxisCheck.lastYcoord.get(uuid);
	if((math < 0 && math > AntiCheat.getManager().getBackend().getMagic().GLIDE_MAX()) && !AntiCheat.getManager().getBackend().isMovingExempt(player))
	{
		if(math <= lastYDelta.get(uuid) && !(player.getEyeLocation().getBlock().getType() == Material.LADDER)
				&& !Utilities.isInWater(player) && !Utilities.isInWeb(player)
				&& Utilities.cantStandAt(player.getLocation().getBlock()) && !Utilities.cantStandAtSingle(player.getLocation().getBlock()) && !!Utilities.cantStandAtSingle(player.getLocation().getBlock().getRelative(BlockFace.DOWN)))
		{
			if(!glideBuffer.containsKey(uuid))
	    		glideBuffer.put(uuid, 0);
			int currentBuffer = glideBuffer.get(uuid);
			glideBuffer.put(uuid, currentBuffer + 1);
			if(currentBuffer >= AntiCheat.getManager().getBackend().getMagic().GLIDE_LIMIT())
			{
    			double fallDist = distanceToFall(player.getLocation());
    			player.teleport(player.getLocation().add(0, -fallDist, 0));
    			player.setFallDistance((float) fallDist);
				if(!AntiCheat.getManager().getBackend().silentMode())
				{
					player.sendMessage(ChatColor.RED + "[AntiCheat] Glide/Fly hacking detected.");
				}
				lastYDelta.put(uuid, math);
				return new CheckResult(CheckResult.Result.FAILED, uuid + " attempted to fall too slowly!");
			}
		}
	}else {
    	glideBuffer.remove(uuid);
	}
	lastYDelta.put(uuid, math);*/
	
	public static CheckResult runCheck(Player player, Distance distance) {
		if (VersionUtil.isFlying(player)) {
			return PASS;
		}
		if (!lastDiff.containsKey(player.getUniqueId())) {
			lastDiff.put(player.getUniqueId(), distance.getYDifference());
			return PASS;
		}
		if (!lastFallDistance.containsKey(player.getUniqueId())) {
			lastFallDistance.put(player.getUniqueId(), player.getFallDistance());
			return PASS;
		}
		double yDiff = distance.getYDifference();
		float fallDistance = player.getFallDistance();
		if (yDiff == lastDiff.get(player.getUniqueId()) && fallDistance > lastFallDistance.get(player.getUniqueId())) {
			if (!violations.containsKey(player.getUniqueId())) {
				violations.put(player.getUniqueId(), 1);
			} else {
				if (violations.get(player.getUniqueId()) + 1 >= AntiCheat.getManager().getBackend().getMagic().GLIDE_LIMIT()) {
					violations.remove(player.getUniqueId());
					Location to = player.getLocation();
					to.setY(to.getY() - distanceToFall(to));
					player.teleport(to);
					return new CheckResult(CheckResult.Result.FAILED,
							player.getName() + " was set back for gliding (yDiff=" + new BigDecimal(yDiff).setScale(2, BigDecimal.ROUND_UP) + ")");
				} else {
					violations.put(player.getUniqueId(), violations.get(player.getUniqueId()) + 1);
				}
			}
		}
		lastDiff.put(player.getUniqueId(), distance.getYDifference());
		lastFallDistance.put(player.getUniqueId(), player.getFallDistance());
    	return PASS;
	}

	private static double distanceToFall(Location location) {
		location = location.clone();
		double firstY = location.getY();
		while (location.clone().add(0, -0.1, 0).getBlock().getType() == Material.AIR)
			location.add(0, -0.1, 0);
		return firstY - location.getY();
	}

}
