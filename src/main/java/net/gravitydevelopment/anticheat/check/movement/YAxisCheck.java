package net.gravitydevelopment.anticheat.check.movement;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
import net.gravitydevelopment.anticheat.util.VersionUtil;

public class YAxisCheck {

	private static final CheckResult PASS = new CheckResult(CheckResult.Result.PASSED);
	
    public static Map<UUID, Double> lastYcoord = new HashMap<UUID, Double>();
    public static Map<UUID, Long> lastYtime = new HashMap<UUID, Long>();
    public static Map<UUID, Integer> yAxisViolations = new HashMap<UUID, Integer>();
    public static Map<UUID, Long> yAxisLastViolation = new HashMap<UUID, Long>();
	
    private static boolean hasJumpPotion(Player player) {
        return player.hasPotionEffect(PotionEffectType.JUMP);
    }
    
	public static CheckResult runCheck(Player player, Distance distance) {
        if (distance.getYDifference() > AntiCheat.getManager().getBackend().getMagic().TELEPORT_MIN() || distance.getYDifference() < 0 || VersionUtil.isFlying(player)) {
            return PASS;
        }
        if (!FlightCheck.isMovingExempt(player) && !Utilities.isClimbableBlock(player.getLocation().getBlock()) && !Utilities.isClimbableBlock(player.getLocation().add(0, -1, 0).getBlock()) && !player.isInsideVehicle() && !Utilities.isInWater(player) && !hasJumpPotion(player) && !isMoveUpBlock(player.getLocation().add(0, -1, 0).getBlock()) && !isMoveUpBlock(player.getLocation().add(0, -1.5, 0).getBlock())) {
            double y1 = player.getLocation().getY();
            UUID uuid = player.getUniqueId();
            // Fix Y axis spam.
            if (!lastYcoord.containsKey(uuid) || !lastYtime.containsKey(uuid) || !yAxisLastViolation.containsKey(uuid) || !yAxisLastViolation.containsKey(uuid)) {
                lastYcoord.put(uuid, y1);
                yAxisViolations.put(uuid, 0);
                yAxisLastViolation.put(uuid, 0L);
                lastYtime.put(uuid, System.currentTimeMillis());
            } else {
                if (y1 > lastYcoord.get(uuid) && yAxisViolations.get(uuid) > AntiCheat.getManager().getBackend().getMagic().Y_MAXVIOLATIONS() && (System.currentTimeMillis() - yAxisLastViolation.get(uuid)) < AntiCheat.getManager().getBackend().getMagic().Y_MAXVIOTIME()) {
                    Location g = player.getLocation();
                    yAxisViolations.put(uuid, yAxisViolations.get(uuid) + 1);
                    yAxisLastViolation.put(uuid, System.currentTimeMillis());
                    if (!AntiCheat.getManager().getBackend().silentMode()) {
                        g.setY(lastYcoord.get(uuid));
                        player.sendMessage(ChatColor.RED + "[AntiCheat] Fly hacking on the y-axis detected.  Please wait 5 seconds to prevent getting damage.");
                        if (g.getBlock().getType() == Material.AIR) {
                            player.teleport(g);
                        }
                    }
                    return new CheckResult(CheckResult.Result.FAILED, player.getName() + " tried to fly on y-axis " + yAxisViolations.get(uuid) + " times (max =" + AntiCheat.getManager().getBackend().getMagic().Y_MAXVIOLATIONS() + ")");
                } else {
                    if (yAxisViolations.get(uuid) > AntiCheat.getManager().getBackend().getMagic().Y_MAXVIOLATIONS() && (System.currentTimeMillis() - yAxisLastViolation.get(uuid)) > AntiCheat.getManager().getBackend().getMagic().Y_MAXVIOTIME()) {
                        yAxisViolations.put(uuid, 0);
                        yAxisLastViolation.put(uuid, 0L);
                    }
                }
                long i = System.currentTimeMillis() - lastYtime.get(uuid);
                double diff = AntiCheat.getManager().getBackend().getMagic().Y_MAXDIFF() + (Utilities.isStair(player.getLocation().add(0, -1, 0).getBlock()) ? 0.5 : 0.0);
                if ((y1 - lastYcoord.get(uuid)) > diff && i < AntiCheat.getManager().getBackend().getMagic().Y_TIME()) {
                    Location g = player.getLocation();
                    yAxisViolations.put(uuid, yAxisViolations.get(uuid) + 1);
                    yAxisLastViolation.put(uuid, System.currentTimeMillis());
                    if (!AntiCheat.getManager().getBackend().silentMode()) {
                        g.setY(lastYcoord.get(uuid));
                        if (g.getBlock().getType() == Material.AIR) {
                            player.teleport(g);
                        }
                    }
                    return new CheckResult(CheckResult.Result.FAILED, player.getName() + " tried to fly on y-axis in " + i + " ms (min =" + AntiCheat.getManager().getBackend().getMagic().Y_TIME() + ")");
                } else {
                    if ((y1 - lastYcoord.get(uuid)) > AntiCheat.getManager().getBackend().getMagic().Y_MAXDIFF() + 1 || (System.currentTimeMillis() - lastYtime.get(uuid)) > AntiCheat.getManager().getBackend().getMagic().Y_TIME()) {
                        lastYtime.put(uuid, System.currentTimeMillis());
                        lastYcoord.put(uuid, y1);
                    }
                }
            }
        }
        // Fix Y axis spam
        return PASS;
    }

	public static boolean isMoveUpBlock(Block block) {
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
