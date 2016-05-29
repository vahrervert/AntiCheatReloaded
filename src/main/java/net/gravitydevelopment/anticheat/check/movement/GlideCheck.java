package net.gravitydevelopment.anticheat.check.movement;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import net.gravitydevelopment.anticheat.AntiCheat;
import net.gravitydevelopment.anticheat.check.CheckResult;
import net.gravitydevelopment.anticheat.util.Distance;
import net.gravitydevelopment.anticheat.util.Utilities;

/**
 * 
 * @author Marco STILL IN ALPHA TESTING!
 */
public class GlideCheck {

	private static final CheckResult PASS = new CheckResult(CheckResult.Result.PASSED);
	
	public static Map<String, Double> lastYDelta = new HashMap<String, Double>();
	public static Map<String, Integer> glideBuffer = new HashMap<String, Integer>();
	
	public static CheckResult runCheck(Player player, Distance distance) {
    	String name = player.getName();
    	if(!lastYDelta.containsKey(name))
    		lastYDelta.put(name, 0.0);
    	if(!YAxisCheck.lastYcoord.containsKey(name))
    		YAxisCheck.lastYcoord.put(name, player.getLocation().getY());
    	double currentY = player.getLocation().getY();
    	double math = currentY - YAxisCheck.lastYcoord.get(name);
    	if((math < 0 && math > AntiCheat.getManager().getBackend().getMagic().GLIDE_MAX()) && !AntiCheat.getManager().getBackend().isMovingExempt(player))
    	{
    		if(math <= lastYDelta.get(name) && !(player.getEyeLocation().getBlock().getType() == Material.LADDER)
    				&& !Utilities.isInWater(player) && !Utilities.isInWeb(player)
    				&& Utilities.cantStandAtSingle(player.getLocation().getBlock()))
    		{
    			if(!glideBuffer.containsKey(name))
    	    		glideBuffer.put(name, 0);
    			int currentBuffer = glideBuffer.get(name);
    			glideBuffer.put(name, currentBuffer + 1);
    			if(currentBuffer >= AntiCheat.getManager().getBackend().getMagic().GLIDE_LIMIT())
    			{
        			double fallDist = distanceToFall(player.getLocation());
        			player.teleport(player.getLocation().add(0, -fallDist, 0));
        			player.setFallDistance((float) fallDist);
    				if(!AntiCheat.getManager().getBackend().silentMode())
    				{
    					player.sendMessage(ChatColor.RED + "[AntiCheat] Glide/Fly hacking detected.");
    				}
    				lastYDelta.put(name, math);
    				return new CheckResult(CheckResult.Result.FAILED, name + " attempted to fall too slowly!");
    			}
    		}
    	}else {
	    	glideBuffer.remove(name);
    	}
    	lastYDelta.put(name, math);
    	return PASS;
	}

	private static double distanceToFall(Location location) {
		double firstY = location.getY();
		while (location.clone().add(0, -0.1, 0).getBlock().getType() == Material.AIR)
			location.add(0, -0.1, 0);
		return firstY - location.getY();
	}

}
