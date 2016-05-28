package net.gravitydevelopment.anticheat.check.movement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import net.gravitydevelopment.anticheat.AntiCheat;
import net.gravitydevelopment.anticheat.check.CheckResult;
import net.gravitydevelopment.anticheat.util.Utilities;

/**
 * This check seems to be broken/outdated
 */
public class WaterWalkCheck {

	private static final CheckResult PASS = new CheckResult(CheckResult.Result.PASSED);
	
    public static List<String> isInWater = new ArrayList<String>();
    public static List<String> isInWaterCache = new ArrayList<String>();
    public static Map<String, Integer> waterSpeedViolation = new HashMap<String, Integer>();
    public static Map<String, Integer> waterAscensionViolation = new HashMap<String, Integer>();
	
	public static CheckResult runCheck(Player player, double x, double y, double z) {
        Block block = player.getLocation().getBlock();

        if (player.getVehicle() == null && !player.isFlying()) {
            if (block.isLiquid()) {
                if (isInWater.contains(player.getName())) {
                    if (isInWaterCache.contains(player.getName())) {
                        if (player.getNearbyEntities(1, 1, 1).isEmpty()) {
                            boolean b;
                            if (!Utilities.sprintFly(player)) {
                                b = x > AntiCheat.getManager().getBackend().getMagic().XZ_SPEED_MAX_WATER() || z > AntiCheat.getManager().getBackend().getMagic().XZ_SPEED_MAX_WATER();
                            } else {
                                b = x > AntiCheat.getManager().getBackend().getMagic().XZ_SPEED_MAX_WATER_SPRINT() || z > AntiCheat.getManager().getBackend().getMagic().XZ_SPEED_MAX_WATER_SPRINT();
                            }
                            if (!b && !Utilities.isFullyInWater(player.getLocation()) && Utilities.isHoveringOverWater(player.getLocation(), 1) && y == 0D && !block.getType().equals(Material.WATER_LILY)) {
                                b = true;
                            }
                            if (b) {
                                if (waterSpeedViolation.containsKey(player.getName())) {
                                    int v = waterSpeedViolation.get(player.getName());
                                    if (v >= AntiCheat.getManager().getBackend().getMagic().WATER_SPEED_VIOLATION_MAX()) {
                                        waterSpeedViolation.put(player.getName(), 0);
                                        return new CheckResult(CheckResult.Result.FAILED, player.getName() + " stood on water " + v + " times (can't stand on " + block.getType() + " or " + block.getRelative(BlockFace.DOWN).getType() + ")");
                                    } else {
                                        waterSpeedViolation.put(player.getName(), v + 1);
                                    }
                                } else {
                                    waterSpeedViolation.put(player.getName(), 1);
                                }
                            }
                        }
                    } else {
                        isInWaterCache.add(player.getName());
                        return PASS;
                    }
                } else {
                    isInWater.add(player.getName());
                    return PASS;
                }
            } else if (block.getRelative(BlockFace.DOWN).isLiquid() && !AntiCheat.getManager().getBackend().isAscending(player) && Utilities.cantStandAt(block) && Utilities.cantStandAt(block.getRelative(BlockFace.DOWN))) {
                if (waterAscensionViolation.containsKey(player.getName())) {
                    int v = waterAscensionViolation.get(player.getName());
                    if (v >= AntiCheat.getManager().getBackend().getMagic().WATER_ASCENSION_VIOLATION_MAX()) {
                        waterAscensionViolation.put(player.getName(), 0);
                        return new CheckResult(CheckResult.Result.FAILED, player.getName() + " stood on water " + v + " times (can't stand on " + block.getType() + " or " + block.getRelative(BlockFace.DOWN).getType() + ")");
                    } else {
                        waterAscensionViolation.put(player.getName(), v + 1);
                    }
                } else {
                    waterAscensionViolation.put(player.getName(), 1);
                }
            } else {
                isInWater.remove(player.getName());
                isInWaterCache.remove(player.getName());
            }
        }
        return PASS;
    }
	
}
