package com.ezinnovations.playersleep;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.logging.Level;

/**
 * Schedules delayed tasks in a way that works on both Bukkit/Paper and Folia.
 */
public class SchedulerAdapter {

    private static final String FOLIA_TASK_CLASS = "io.papermc.paper.threadedregions.scheduler.ScheduledTask";

    private final Plugin plugin;
    private final boolean folia;
    private final Method getSchedulerMethod;
    private final Method runDelayedMethod;
    private final Method getGlobalRegionSchedulerMethod;
    private final Method executeGlobalMethod;

    public SchedulerAdapter(Plugin plugin) {
        this.plugin = plugin;
        boolean foliaDetected = false;
        Method schedulerMethod = null;
        Method delayedMethod = null;
        Method globalSchedulerMethod = null;
        Method globalExecuteMethod = null;

        try {
            Class<?> scheduledTaskClass = Class.forName(FOLIA_TASK_CLASS);
            schedulerMethod = Player.class.getMethod("getScheduler");
            Class<?> entitySchedulerClass = schedulerMethod.getReturnType();
            delayedMethod = entitySchedulerClass.getMethod(
                "runDelayed",
                Plugin.class,
                java.util.function.Consumer.class,
                Runnable.class,
                long.class
            );

            globalSchedulerMethod = Bukkit.class.getMethod("getGlobalRegionScheduler");
            Class<?> globalSchedulerClass = globalSchedulerMethod.getReturnType();
            globalExecuteMethod = globalSchedulerClass.getMethod("execute", Plugin.class, Runnable.class);

            if (scheduledTaskClass != null) {
                foliaDetected = true;
            }
        } catch (ReflectiveOperationException ignored) {
            foliaDetected = false;
        }

        this.folia = foliaDetected;
        this.getSchedulerMethod = schedulerMethod;
        this.runDelayedMethod = delayedMethod;
        this.getGlobalRegionSchedulerMethod = globalSchedulerMethod;
        this.executeGlobalMethod = globalExecuteMethod;

        if (folia) {
            plugin.getLogger().info("Folia environment detected. Using entity scheduler for delayed sleep checks.");
        }
    }

    public void runDelayed(Player player, Runnable task, long ticks) {
        if (!folia) {
            Bukkit.getScheduler().runTaskLater(plugin, task, ticks);
            return;
        }

        try {
            Object entityScheduler = getSchedulerMethod.invoke(player);
            runDelayedMethod.invoke(entityScheduler, plugin, (java.util.function.Consumer<Object>) ignored -> task.run(), null, ticks);
        } catch (ReflectiveOperationException ex) {
            plugin.getLogger().log(Level.WARNING, "Failed to schedule task on Folia scheduler, falling back to Bukkit scheduler.", ex);
            Bukkit.getScheduler().runTaskLater(plugin, task, ticks);
        }
    }

    public void runGlobal(Runnable task) {
        if (!folia) {
            Bukkit.getScheduler().runTask(plugin, task);
            return;
        }

        try {
            Object globalRegionScheduler = getGlobalRegionSchedulerMethod.invoke(null);
            executeGlobalMethod.invoke(globalRegionScheduler, plugin, task);
        } catch (ReflectiveOperationException ex) {
            plugin.getLogger().log(Level.WARNING, "Failed to schedule task on Folia global region scheduler, falling back to Bukkit scheduler.", ex);
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }
}
