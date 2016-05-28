package net.gravitydevelopment.anticheat.check.movement;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
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
    	if(!glideBuffer.containsKey(name))
    	{
    		glideBuffer.put(name, 0);
    	}
    	if(!lastYDelta.containsKey(name))
    	{
    		lastYDelta.put(name, 0.0);
    	}
    	double currentY = player.getLocation().getY();
    	double math = currentY - YAxisCheck.lastYcoord.get(name);
    	if(math < 0 && !AntiCheat.getManager().getBackend().isMovingExempt(player))
    	{
    		if(math <= lastYDelta.get(name) && !(player.getEyeLocation().getBlock().getType() == Material.LADDER)
    				&& !Utilities.isInWater(player) && !Utilities.isInWeb(player)
    				&& Utilities.cantStandAt(player.getLocation().getBlock()))
    		{
    			int currentBuffer = glideBuffer.get(name);
    			glideBuffer.put(name, currentBuffer + 1);
    			if((currentBuffer + 1) >= AntiCheat.getManager().getBackend().getMagic().FLIGHT_LIMIT())
    			{
    				if(!AntiCheat.getManager().getBackend().silentMode())
    				{
    					player.sendMessage(ChatColor.RED + "[AntiCheat] Glide/Fly hacking detected.");
    				}
    				lastYDelta.put(name, math);
    				return new CheckResult(CheckResult.Result.FAILED, name + " attempted to fall too slowly!");
    			}
    		}
    	}
    	lastYDelta.put(name, math);
		return PASS;
	}

}
