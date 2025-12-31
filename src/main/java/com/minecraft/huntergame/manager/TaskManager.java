package com.minecraft.huntergame.manager;

import com.minecraft.huntergame.HunterGame;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

/**
 * 任务管理器
 * 负责管理游戏中的定时任务
 * 
 * @author YourName
 * @version 1.0.0
 */
public class TaskManager {
    
    private final HunterGame plugin;
    private final String arenaName;
    
    // 任务映射
    private final Map<String, BukkitTask> tasks;
    
    public TaskManager(HunterGame plugin, String arenaName) {
        this.plugin = plugin;
        this.arenaName = arenaName;
        this.tasks = new HashMap<>();
    }
    
    /**
     * 启动倒计时任务
     * 
     * @param taskName 任务名称
     * @param seconds 倒计时秒数
     * @param onTick 每秒回调（参数为剩余秒数）
     * @param onComplete 完成回调
     */
    public void startCountdown(String taskName, int seconds, 
                              java.util.function.Consumer<Integer> onTick,
                              Runnable onComplete) {
        // 取消已存在的同名任务
        cancelTask(taskName);
        
        // 创建倒计时任务
        BukkitTask task = new CountdownTask(seconds, onTick, onComplete).runTaskTimer(plugin, 0L, 20L);
        tasks.put(taskName, task);
        
        plugin.getLogger().info("[" + arenaName + "] 启动倒计时任务: " + taskName + " (" + seconds + "秒)");
    }
    
    /**
     * 启动游戏计时任务
     * 
     * @param taskName 任务名称
     * @param seconds 游戏时长（秒）
     * @param onTick 每秒回调（参数为剩余秒数）
     * @param onComplete 完成回调
     */
    public void startGameTimer(String taskName, int seconds,
                              java.util.function.Consumer<Integer> onTick,
                              Runnable onComplete) {
        startCountdown(taskName, seconds, onTick, onComplete);
    }
    
    /**
     * 启动重复任务
     * 
     * @param taskName 任务名称
     * @param delay 延迟（tick）
     * @param period 周期（tick）
     * @param runnable 任务内容
     */
    public void startRepeatingTask(String taskName, long delay, long period, Runnable runnable) {
        // 取消已存在的同名任务
        cancelTask(taskName);
        
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, runnable, delay, period);
        tasks.put(taskName, task);
        
        plugin.getLogger().info("[" + arenaName + "] 启动重复任务: " + taskName);
    }
    
    /**
     * 启动延迟任务
     * 
     * @param taskName 任务名称
     * @param delay 延迟（tick）
     * @param runnable 任务内容
     */
    public void startDelayedTask(String taskName, long delay, Runnable runnable) {
        // 取消已存在的同名任务
        cancelTask(taskName);
        
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            runnable.run();
            tasks.remove(taskName);
        }, delay);
        tasks.put(taskName, task);
        
        plugin.getLogger().info("[" + arenaName + "] 启动延迟任务: " + taskName + " (延迟" + delay + "tick)");
    }
    
    /**
     * 取消任务
     * 
     * @param taskName 任务名称
     */
    public void cancelTask(String taskName) {
        BukkitTask task = tasks.remove(taskName);
        if (task != null) {
            task.cancel();
            plugin.getLogger().info("[" + arenaName + "] 取消任务: " + taskName);
        }
    }
    
    /**
     * 取消所有任务
     */
    public void cancelAll() {
        plugin.getLogger().info("[" + arenaName + "] 取消所有任务 (" + tasks.size() + "个)");
        
        for (BukkitTask task : tasks.values()) {
            task.cancel();
        }
        tasks.clear();
    }
    
    /**
     * 检查任务是否正在运行
     * 
     * @param taskName 任务名称
     * @return 是否正在运行
     */
    public boolean isRunning(String taskName) {
        return tasks.containsKey(taskName);
    }
    
    /**
     * 获取正在运行的任务数量
     */
    public int getRunningTaskCount() {
        return tasks.size();
    }
    
    /**
     * 倒计时任务内部类
     */
    private class CountdownTask implements Runnable {
        private int remaining;
        private final java.util.function.Consumer<Integer> onTick;
        private final Runnable onComplete;
        private BukkitTask task;
        
        public CountdownTask(int seconds, 
                           java.util.function.Consumer<Integer> onTick,
                           Runnable onComplete) {
            this.remaining = seconds;
            this.onTick = onTick;
            this.onComplete = onComplete;
        }
        
        public BukkitTask runTaskTimer(HunterGame plugin, long delay, long period) {
            this.task = Bukkit.getScheduler().runTaskTimer(plugin, this, delay, period);
            return task;
        }
        
        @Override
        public void run() {
            try {
                // 执行每秒回调
                if (onTick != null) {
                    onTick.accept(remaining);
                }
                
                // 倒计时结束
                if (remaining <= 0) {
                    task.cancel();
                    
                    // 执行完成回调
                    if (onComplete != null) {
                        onComplete.run();
                    }
                    return;
                }
                
                remaining--;
                
            } catch (Exception ex) {
                plugin.getLogger().severe("[" + arenaName + "] 倒计时任务执行失败: " + ex.getMessage());
                ex.printStackTrace();
                task.cancel();
            }
        }
    }
}
