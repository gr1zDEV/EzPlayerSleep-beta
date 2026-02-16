package com.ezinnovations.playersleep;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SleepCommand implements CommandExecutor, TabCompleter {
    
    private final EzPlayerSleep plugin;
    
    public SleepCommand(EzPlayerSleep plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "set":
                return handleSet(sender, args);
            case "info":
            case "status":
                return handleInfo(sender);
            case "reload":
                return handleReload(sender);
            default:
                sendHelp(sender);
                return true;
        }
    }
    
    private boolean handleSet(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ezplayersleep.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /playersleep set <percentage>");
            return true;
        }
        
        try {
            int percentage = Integer.parseInt(args[1]);
            
            if (percentage < 1 || percentage > 100) {
                sender.sendMessage(ChatColor.RED + "Percentage must be between 1 and 100.");
                return true;
            }
            
            plugin.getConfigManager().setSleepPercentage(percentage);
            sender.sendMessage(ChatColor.GREEN + "Sleep percentage set to " + percentage + "%");
            
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid number. Please enter a value between 1 and 100.");
        }
        
        return true;
    }
    
    private boolean handleInfo(CommandSender sender) {
        int percentage = plugin.getConfigManager().getSleepPercentage();
        
        sender.sendMessage(ChatColor.GOLD + "=== EzPlayerSleep Info ===");
        sender.sendMessage(ChatColor.YELLOW + "Sleep Percentage: " + ChatColor.WHITE + percentage + "%");
        
        if (sender instanceof Player) {
            Player player = (Player) sender;
            int required = plugin.getRequiredSleepers(player.getWorld());
            int sleeping = plugin.getSleepingCount(player.getWorld());
            
            sender.sendMessage(ChatColor.YELLOW + "Currently Sleeping: " + ChatColor.WHITE + sleeping);
            sender.sendMessage(ChatColor.YELLOW + "Required to Skip: " + ChatColor.WHITE + required);
        }
        
        sender.sendMessage(ChatColor.YELLOW + "Messages Enabled: " + ChatColor.WHITE + 
            plugin.getConfigManager().isSleepMessagesEnabled());
        sender.sendMessage(ChatColor.YELLOW + "Reset Phantom Timers: " + ChatColor.WHITE + 
            plugin.getConfigManager().shouldResetPhantomTimers());
        
        return true;
    }
    
    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("ezplayersleep.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }
        
        plugin.getConfigManager().reload();
        sender.sendMessage(ChatColor.GREEN + "Configuration reloaded!");
        
        return true;
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== EzPlayerSleep Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/playersleep info " + ChatColor.WHITE + "- View current settings");
        
        if (sender.hasPermission("ezplayersleep.admin")) {
            sender.sendMessage(ChatColor.YELLOW + "/playersleep set <percentage> " + ChatColor.WHITE + "- Set sleep percentage");
            sender.sendMessage(ChatColor.YELLOW + "/playersleep reload " + ChatColor.WHITE + "- Reload configuration");
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.add("info");
            if (sender.hasPermission("ezplayersleep.admin")) {
                completions.addAll(Arrays.asList("set", "reload"));
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            completions.addAll(Arrays.asList("25", "50", "75", "100"));
        }
        
        return completions;
    }
}
