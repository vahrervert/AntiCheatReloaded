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

package com.rammelkast.anticheatreloaded;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.rammelkast.anticheatreloaded.check.packet.MorePacketsCheck;
import com.rammelkast.anticheatreloaded.command.CommandHandler;
import com.rammelkast.anticheatreloaded.config.Configuration;
import com.rammelkast.anticheatreloaded.event.BlockListener;
import com.rammelkast.anticheatreloaded.event.EntityListener;
import com.rammelkast.anticheatreloaded.event.InventoryListener;
import com.rammelkast.anticheatreloaded.event.PlayerListener;
import com.rammelkast.anticheatreloaded.event.VehicleListener;
import com.rammelkast.anticheatreloaded.manage.AntiCheatManager;
import com.rammelkast.anticheatreloaded.metrics.Metrics;
import com.rammelkast.anticheatreloaded.util.PacketListener;
import com.rammelkast.anticheatreloaded.util.UpdateManager;
import com.rammelkast.anticheatreloaded.util.User;
import com.rammelkast.anticheatreloaded.util.VersionUtil;

public class AntiCheatReloaded extends JavaPlugin {

	public static final List<UUID> MUTE_ENABLED_MODS = new ArrayList<UUID>();
	
	private static AntiCheatManager manager;
	private static AntiCheatReloaded plugin;
	private static List<Listener> eventList = new ArrayList<Listener>();
	private static Configuration config;
	private static boolean verbose;
	private static ProtocolManager protocolManager;
	private static SecureRandom random;
	private static Long loadTime;
	private static UpdateManager updateManager;
	
	private double tps = -1;
	private int currentTick;

	@Override
	public void onEnable() {
		plugin = this;
		random = new SecureRandom();
		loadTime = System.currentTimeMillis();
		manager = new AntiCheatManager(this, getLogger());
		eventList.add(new PlayerListener());
		eventList.add(new BlockListener());
		eventList.add(new EntityListener());
		eventList.add(new VehicleListener());
		eventList.add(new InventoryListener());
		// Order is important in some cases, don't screw with these unless
		// needed, especially config
		this.setupConfig();
		this.setupEvents();
		this.setupCommands();
		// Enterprise must come before levels
		this.setupEnterprise();
		this.restoreLevels();
		
		if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
			this.setupProtocol();
		} else {
			Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "ACR " + ChatColor.RED + "Shutting down, ProtocolLib not found!");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		
		getLogger().info("NMS version is " + VersionUtil.getVersion());
		if (!VersionUtil.isSupported()) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "ACR " + ChatColor.RED
					+ "The version of this server is NOT supported by ACR! The plugin will not work as expected!");
		}
		
		getLogger().info("Enabling packet listeners");
		PacketListener.listenKeepAlivePackets();
		PacketListener.listenMovementPackets();
		
		updateManager = new UpdateManager();
		
		// End tests
		verboseLog("Finished loading.");

		// Metrics
		try {
			Metrics metrics = new Metrics(this, 202);
			metrics.addCustomChart(new Metrics.SingleLineChart("cheaters_kicked", new Callable<Integer>() {
				@Override
				public Integer call() throws Exception {
					int kicked = playersKicked;
					// Reset so we don't keep sending the same value
					playersKicked = 0;
					return kicked;
				}
			}));
			metrics.addCustomChart(new Metrics.SimplePie("protocollib_version", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return Bukkit.getPluginManager().getPlugin("ProtocolLib").getDescription().getVersion();
				}
			}));
			metrics.addCustomChart(new Metrics.SimplePie("nms_version", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return VersionUtil.getVersion();
				}
			}));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Launch TPS check
		new BukkitRunnable() {
            long sec;
            long currentSec;
            int ticks;

            public void run() {
                sec = (System.currentTimeMillis() / 1000L);
                if (currentSec == sec) {
                    ticks += 1;
                } else {
                    currentSec = sec;
                    tps = (tps == 0.0D ? ticks : (tps + ticks) / 2.0D);
                    ticks = 1;
                }
                currentTick++;
            }
        }.runTaskTimer(this, 40L, 1L);
        // Launch MorePackets timer
        MorePacketsCheck.startTimer();
	}

	@Override
	public void onDisable() {
		verboseLog("Saving user levels...");
		config.getLevels().saveLevelsFromUsers(getManager().getUserManager().getUsers());

		AntiCheatManager.close();
		getServer().getScheduler().cancelTasks(this);
		cleanup();
	}

	private void setupProtocol() {
		protocolManager = ProtocolLibrary.getProtocolManager();
		verboseLog("Hooked into ProtocolLib");
	}

	private void setupEvents() {
		for (Listener listener : eventList) {
			getServer().getPluginManager().registerEvents(listener, this);
			verboseLog("Registered events for ".concat(listener.toString().split("@")[0].split(".anticheat.")[1]));
		}
	}

	private void setupCommands() {
		getCommand("anticheat").setExecutor(new CommandHandler());
		verboseLog("Registered commands.");
	}

	private void setupConfig() {
		config = manager.getConfiguration();
		verboseLog("Setup the config.");
	}

	private void setupEnterprise() {
		if (config.getConfig().enterprise.getValue()) {
			if (config.getEnterprise().loggingEnabled.getValue()) {
				config.getEnterprise().database.cleanEvents();
			}
		}
	}

	private void restoreLevels() {
		for (Player player : getServer().getOnlinePlayers()) {
			UUID uuid = player.getUniqueId();

			User user = new User(uuid);
			user.setIsWaitingOnLevelSync(true);
			config.getLevels().loadLevelToUser(user);

			manager.getUserManager().addUser(user);
			verboseLog("Data for " + uuid + " loaded");
		}
	}

	public static AntiCheatReloaded getPlugin() {
		return plugin;
	}

	public static AntiCheatManager getManager() {
		return manager;
	}

	public static SecureRandom getRandom() {
		return random;
	}
	
	public static String getVersion() {
		return manager.getPlugin().getDescription().getVersion();
	}

	private void cleanup() {
		eventList = null;
		manager = null;
		config = null;
	}
	
	public static void debugLog(final String string) {
		Bukkit.getScheduler().runTask(getPlugin(), new Runnable() {
			public void run() {
				if (getManager().getConfiguration().getConfig().debugMode.getValue()) {
					manager.debugLog("[DEBUG] " + string);
				}
			}
		});
	}

	public void verboseLog(final String string) {
		if (verbose) {
			getLogger().info(string);
		}
	}

	public void setVerbose(boolean b) {
		verbose = b;
	}

	public Long getLoadTime() {
		return loadTime;
	}

	public static ProtocolManager getProtocolManager() {
		return protocolManager;
	}

	/**
	 * Amount of players kicked since start
	 */
	private int playersKicked = 0;

	public void onPlayerKicked() {
		this.playersKicked++;
	}

	public static void sendToMainThread(Runnable runnable) {
		Bukkit.getScheduler().runTask(AntiCheatReloaded.getPlugin(), runnable);
	}
	
	public void sendToStaff(String message) {
		Bukkit.getOnlinePlayers().forEach(player -> {
			if (player.hasPermission("anticheat.system.alert")) {
				if (!MUTE_ENABLED_MODS.contains(player.getUniqueId())) {
					player.sendMessage(message);
				}
			}
		});
		Bukkit.getConsoleSender().sendMessage(message);
	}
	
	public static UpdateManager getUpdateManager() {
		return updateManager;
	}
	
	public double getTPS() {
		if (this.tps < 0 || this.tps > 20) {
			return 20;
		}
		return this.tps;
	}
	
}
