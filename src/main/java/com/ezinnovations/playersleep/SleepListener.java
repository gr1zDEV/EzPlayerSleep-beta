package com.ezinnovations.playersleep;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;

public class SleepListener implements Listener {
    
    private final EzPlayerSleep plugin;
    
    public SleepListener(EzPlayerSleep plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerSleep(PlayerBedEnterEvent event) {
        if (event.getBedEnterResult() != PlayerBedEnterEvent.BedEnterResult.OK) {
            return;
        }
        
        Player player = event.getPlayer();
        World world = player.getWorld();
        
        // Check if storm sleep is prevented
        if (plugin.getConfigManager().isStormSleepPrevented() && world.hasStorm()) {
            return;
        }
        
        // Delay to ensure player is actually sleeping
        plugin.getSchedulerAdapter().runDelayed(player, () -> checkSleepStatus(world), 5L);
    }
    
    @EventHandler
    public void onPlayerWake(PlayerBedLeaveEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();

        // Delay to ensure sleep count is updated
        plugin.getSchedulerAdapter().runDelayed(player, () -> checkSleepStatus(world), 2L);
    }
    
    private void checkSleepStatus(World world) {
        int sleeping = plugin.getSleepingCount(world);
        int required = plugin.getRequiredSleepers(world);
        
        if (sleeping <= 0 || required <= 0) {
            return;
        }
        
        // Send notification if messages are enabled
        if (plugin.getConfigManager().isSleepMessagesEnabled()) {
            String message = plugin.getConfigManager().getSleepMessage();
            double percentage = plugin.getConfigManager().getSleepPercentage();
            
            message = message.replace("{sleeping}", String.valueOf(sleeping))
                           .replace("{required}", String.valueOf(required))
                           .replace("{percentage}", String.valueOf((int) percentage));
            
            message = ChatColor.translateAlternateColorCodes('&', message);
            
            for (Player player : world.getPlayers()) {
                player.sendMessage(message);
            }
        }
        
        // Check if enough players are sleeping
        if (sleeping >= required) {
            skipNight(world);
        }
    }
    
    private void skipNight(World world) {
        // Send skip message
        if (plugin.getConfigManager().isSleepMessagesEnabled()) {
            String message = ChatColor.translateAlternateColorCodes('&', 
                plugin.getConfigManager().getSkipMessage());
            
            for (Player player : world.getPlayers()) {
                player.sendMessage(message);
            }
        }
        
        // Reset phantom timers if configured
        if (plugin.getConfigManager().shouldResetPhantomTimers()) {
            for (Player player : world.getPlayers()) {
                player.setStatistic(org.bukkit.Statistic.TIME_SINCE_REST, 0);
            }
        }
        
        // Skip to day
        world.setTime(0);
        
        // Clear weather if storming
        if (world.hasStorm()) {
            world.setStorm(false);
            world.setThundering(false);
        }
    }
}
