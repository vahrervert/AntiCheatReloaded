package net.gravitydevelopment.anticheat.check.movement;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import net.gravitydevelopment.anticheat.AntiCheat;
import net.gravitydevelopment.anticheat.check.CheckResult;
import net.gravitydevelopment.anticheat.util.Distance;
import net.gravitydevelopment.anticheat.util.Utilities;

public class FlightCheck {

	private static final CheckResult PASS = new CheckResult(CheckResult.Result.PASSED);
	
	public static Map<String, Double> blocksOverFlight = new HashMap<String, Double>();
	public static Map<String, Long> movingExempt = new HashMap<String, Long>();
	
    public static CheckResult runCheck(Player player, Distance distance) {
        if (distance.getYDifference() > AntiCheat.getManager().getBackend().getMagic().TELEPORT_MIN()) {
            // This was a teleport, so we don't care about it.
            return PASS;
        }
        final String name = player.getName();
        final double y1 = distance.fromY();
        final double y2 = distance.toY();
        if (!isMovingExempt(player) && !Utilities.isHoveringOverWater(player.getLocation(), 1) && Utilities.cantStandAtExp(player.getLocation()) && Utilities.blockIsnt(player.getLocation().getBlock().getRelative(BlockFace.DOWN), new Material[]{Material.FENCE, Material.FENCE_GATE, Material.COBBLE_WALL})) {

            if (!blocksOverFlight.containsKey(name)) {
                blocksOverFlight.put(name, 0D);
            }

            blocksOverFlight.put(name, (blocksOverFlight.get(name) + distance.getXDifference() + distance.getYDifference() + distance.getZDifference()));

            if (y1 > y2) {
                blocksOverFlight.put(name, (blocksOverFlight.get(name) - distance.getYDifference()));
            }

            if (blocksOverFlight.get(name) > AntiCheat.getManager().getBackend().getMagic().FLIGHT_BLOCK_LIMIT() && (y1 <= y2)) {
                return new CheckResult(CheckResult.Result.FAILED, player.getName() + " flew over " + blocksOverFlight.get(name) + " blocks (max=" + AntiCheat.getManager().getBackend().getMagic().FLIGHT_BLOCK_LIMIT() + ")");
            }
        } else {
            blocksOverFlight.put(name, 0D);
        }

        return PASS;
    }
    
    private static boolean isMovingExempt(Player player) {
        return isDoing(player, movingExempt, -1);
    }
    
    private static boolean isDoing(Player player, Map<String, Long> map, double max) {
        if (map.containsKey(player.getName())) {
            if (max != -1) {
                if (((System.currentTimeMillis() - map.get(player.getName())) / 1000) > max) {
                    map.remove(player.getName());
                    return false;
                } else {
                    return true;
                }
            } else {
                // Termination time has already been calculated
                if (map.get(player.getName()) < System.currentTimeMillis()) {
                    map.remove(player.getName());
                    return false;
                } else {
                    return true;
                }
            }
        } else {
            return false;
        }
    }
	
}
