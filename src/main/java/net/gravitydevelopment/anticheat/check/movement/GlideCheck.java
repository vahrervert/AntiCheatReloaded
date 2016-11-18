package net.gravitydevelopment.anticheat.check.movement;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import net.gravitydevelopment.anticheat.AntiCheat;
import net.gravitydevelopment.anticheat.check.CheckResult;
import net.gravitydevelopment.anticheat.util.Distance;
import net.gravitydevelopment.anticheat.util.Utilities;

/**
 * This is broken as f*ck
 */
public class GlideCheck {

	private static final CheckResult PASS = new CheckResult(CheckResult.Result.PASSED);
	
	public static Map<UUID, Double> lastYDelta = new HashMap<UUID, Double>();
	public static Map<UUID, Integer> glideBuffer = new HashMap<UUID, Integer>();
	
	public static CheckResult runCheck(Player player, Distance distance) {
    	/*UUID uuid = player.getUniqueId();
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
    	return PASS;
	}

	/*private static double distanceToFall(Location location) {
		double firstY = location.getY();
		while (location.clone().add(0, -0.1, 0).getBlock().getType() == Material.AIR)
			location.add(0, -0.1, 0);
		return firstY - location.getY();
	}*/

}
