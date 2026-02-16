package com.ezinnovations.playersleep;

import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    
    private final EzPlayerSleep plugin;
    private FileConfiguration config;
    
    public ConfigManager(EzPlayerSleep plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }
    
    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }
    
    /**
     * Get the percentage of players required to sleep
     * @return Percentage (1-100)
     */
    public int getSleepPercentage() {
        return config.getInt("sleep-percentage", 50);
    }
    
    /**
     * Set the percentage of players required to sleep
     * @param percentage Percentage (1-100)
     */
    public void setSleepPercentage(int percentage) {
        if (percentage < 1) percentage = 1;
        if (percentage > 100) percentage = 100;
        
        config.set("sleep-percentage", percentage);
        plugin.saveConfig();
    }
    
    /**
     * Check if sleep messages are enabled
     * @return true if enabled
     */
    public boolean isSleepMessagesEnabled() {
        return config.getBoolean("messages.enabled", true);
    }
    
    /**
     * Get the message format for sleep notifications
     * @return Message format
     */
    public String getSleepMessage() {
        return config.getString("messages.sleep-notification", 
            "&e{sleeping}/{required} players sleeping ({percentage}% required)");
    }
    
    /**
     * Get the message when night is skipped
     * @return Skip message
     */
    public String getSkipMessage() {
        return config.getString("messages.night-skipped", 
            "&aEnough players are sleeping. Skipping night...");
    }
    
    /**
     * Check if the plugin should reset phantom timers
     * @return true if should reset
     */
    public boolean shouldResetPhantomTimers() {
        return config.getBoolean("reset-phantom-timers", true);
    }
    
    /**
     * Check if sleep should be prevented during storms
     * @return true if prevented
     */
    public boolean isStormSleepPrevented() {
        return config.getBoolean("prevent-storm-sleep", false);
    }
}
