package net.gravitydevelopment.anticheat.check.movement;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import net.gravitydevelopment.anticheat.AntiCheat;
import net.gravitydevelopment.anticheat.check.CheckResult;
import net.gravitydevelopment.anticheat.util.Distance;
import net.gravitydevelopment.anticheat.util.Utilities;

public class YAxisCheck {

	private static final CheckResult PASS = new CheckResult(CheckResult.Result.PASSED);
	
    public static Map<String, Double> lastYcoord = new HashMap<String, Double>();
    public static Map<String, Long> lastYtime = new HashMap<String, Long>();
    public static Map<String, Integer> yAxisViolations = new HashMap<String, Integer>();
    public static Map<String, Long> yAxisLastViolation = new HashMap<String, Long>();
	
    private static boolean hasJumpPotion(Player player) {
        return player.hasPotionEffect(PotionEffectType.JUMP);
    }
    
	public static CheckResult runCheck(Player player, Distance distance) {
        if (distance.getYDifference() > AntiCheat.getManager().getBackend().getMagic().TELEPORT_MIN() || distance.getYDifference() < 0) {
            return PASS;
        }
        if (!FlightCheck.isMovingExempt(player) && !Utilities.isClimbableBlock(player.getLocation().getBlock()) && !Utilities.isClimbableBlock(player.getLocation().add(0, -1, 0).getBlock()) && !player.isInsideVehicle() && !Utilities.isInWater(player) && !hasJumpPotion(player) && !isMoveUpBlock(player.getLocation().add(0, -1, 0).getBlock()) && !isMoveUpBlock(player.getLocation().add(0, -1.5, 0).getBlock())) {
            double y1 = player.getLocation().getY();
            String name = player.getName();
            // Fix Y axis spam.
            if (!lastYcoord.containsKey(name) || !lastYtime.containsKey(name) || !yAxisLastViolation.containsKey(name) || !yAxisLastViolation.containsKey(name)) {
                lastYcoord.put(name, y1);
                yAxisViolations.put(name, 0);
                yAxisLastViolation.put(name, 0L);
                lastYtime.put(name, System.currentTimeMillis());
            } else {
                if (y1 > lastYcoord.get(name) && yAxisViolations.get(name) > AntiCheat.getManager().getBackend().getMagic().Y_MAXVIOLATIONS() && (System.currentTimeMillis() - yAxisLastViolation.get(name)) < AntiCheat.getManager().getBackend().getMagic().Y_MAXVIOTIME()) {
                    Location g = player.getLocation();
                    yAxisViolations.put(name, yAxisViolations.get(name) + 1);
                    yAxisLastViolation.put(name, System.currentTimeMillis());
                    if (!AntiCheat.getManager().getBackend().silentMode()) {
                        g.setY(lastYcoord.get(name));
                        player.sendMessage(ChatColor.RED + "[AntiCheat] Fly hacking on the y-axis detected.  Please wait 5 seconds to prevent getting damage.");
                        if (g.getBlock().getType() == Material.AIR) {
                            player.teleport(g);
                        }
                    }
                    return new CheckResult(CheckResult.Result.FAILED, player.getName() + " tried to fly on y-axis " + yAxisViolations.get(name) + " times (max =" + AntiCheat.getManager().getBackend().getMagic().Y_MAXVIOLATIONS() + ")");
                } else {
                    if (yAxisViolations.get(name) > AntiCheat.getManager().getBackend().getMagic().Y_MAXVIOLATIONS() && (System.currentTimeMillis() - yAxisLastViolation.get(name)) > AntiCheat.getManager().getBackend().getMagic().Y_MAXVIOTIME()) {
                        yAxisViolations.put(name, 0);
                        yAxisLastViolation.put(name, 0L);
                    }
                }
                long i = System.currentTimeMillis() - lastYtime.get(name);
                double diff = AntiCheat.getManager().getBackend().getMagic().Y_MAXDIFF() + (Utilities.isStair(player.getLocation().add(0, -1, 0).getBlock()) ? 0.5 : 0.0);
                if ((y1 - lastYcoord.get(name)) > diff && i < AntiCheat.getManager().getBackend().getMagic().Y_TIME()) {
                    Location g = player.getLocation();
                    yAxisViolations.put(name, yAxisViolations.get(name) + 1);
                    yAxisLastViolation.put(name, System.currentTimeMillis());
                    if (!AntiCheat.getManager().getBackend().silentMode()) {
                        g.setY(lastYcoord.get(name));
                        if (g.getBlock().getType() == Material.AIR) {
                            player.teleport(g);
                        }
                    }
                    return new CheckResult(CheckResult.Result.FAILED, player.getName() + " tried to fly on y-axis in " + i + " ms (min =" + AntiCheat.getManager().getBackend().getMagic().Y_TIME() + ")");
                } else {
                    if ((y1 - lastYcoord.get(name)) > AntiCheat.getManager().getBackend().getMagic().Y_MAXDIFF() + 1 || (System.currentTimeMillis() - lastYtime.get(name)) > AntiCheat.getManager().getBackend().getMagic().Y_TIME()) {
                        lastYtime.put(name, System.currentTimeMillis());
                        lastYcoord.put(name, y1);
                    }
                }
            }
        }
        // Fix Y axis spam
        return PASS;
    }

	private static boolean isMoveUpBlock(Block block) {
		return MOVE_UP_BLOCKS.contains(block.getType());
	}
	
	private static final EnumSet<Material> MOVE_UP_BLOCKS = EnumSet.of(
		Material.ACACIA_STAIRS,
		Material.BIRCH_WOOD_STAIRS,
		Material.BRICK_STAIRS,
		Material.COBBLESTONE_STAIRS,
		Material.DARK_OAK_STAIRS,
		Material.JUNGLE_WOOD_STAIRS,
		Material.NETHER_BRICK_STAIRS,
		Material.QUARTZ_STAIRS,
		Material.RED_SANDSTONE_STAIRS,
		Material.SANDSTONE_STAIRS,
		Material.SMOOTH_STAIRS,
		Material.SPRUCE_WOOD_STAIRS,
		Material.WOOD_STAIRS,
		Material.PURPUR_STAIRS
	);
	
}
