package com.ezinnovations.playersleep;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class EzPlayerSleep extends JavaPlugin {
    
    private static EzPlayerSleep instance;
    private ConfigManager configManager;
    private SchedulerAdapter schedulerAdapter;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize config
        saveDefaultConfig();
        configManager = new ConfigManager(this);
        schedulerAdapter = new SchedulerAdapter(this);
        
        // Register events
        getServer().getPluginManager().registerEvents(new SleepListener(this), this);
        
        // Register command
        getCommand("playersleep").setExecutor(new SleepCommand(this));
        
        getLogger().info("EzPlayerSleep has been enabled!");
        getLogger().info("Sleep percentage: " + configManager.getSleepPercentage() + "%");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("EzPlayerSleep has been disabled!");
    }
    
    public static EzPlayerSleep getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }

    public SchedulerAdapter getSchedulerAdapter() {
        return schedulerAdapter;
    }
    
    /**
     * Get the number of players required to be sleeping to skip night
     * @param world The world to check
     * @return Number of players required
     */
    public int getRequiredSleepers(World world) {
        int onlinePlayers = 0;
        
        for (Player player : world.getPlayers()) {
            if (!player.isSleepingIgnored()) {
                onlinePlayers++;
            }
        }
        
        double percentage = configManager.getSleepPercentage() / 100.0;
        return (int) Math.ceil(onlinePlayers * percentage);
    }
    
    /**
     * Get the current number of sleeping players in a world
     * @param world The world to check
     * @return Number of sleeping players
     */
    public int getSleepingCount(World world) {
        int sleeping = 0;
        
        for (Player player : world.getPlayers()) {
            if (player.isSleeping() && !player.isSleepingIgnored()) {
                sleeping++;
            }
        }
        
        return sleeping;
    }
}
