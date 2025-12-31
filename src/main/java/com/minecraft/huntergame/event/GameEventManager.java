package com.minecraft.huntergame.event;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.arena.Arena;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

/**
 * 游戏事件管理器
 * 负责管理和触发游戏事件
 * 
 * @author YourName
 * @version 1.0.0
 */
public class GameEventManager {
    
    private final HunterGame plugin;
    private final Arena arena;
    private final List<GameEvent> events;
    private BukkitTask eventTask;
    private final int checkInterval;
    
    public GameEventManager(HunterGame plugin, Arena arena) {
        this.plugin = plugin;
        this.arena = arena;
        this.events = new ArrayList<>();
        this.checkInterval = plugin.getMainConfig().getEventCheckInterval();
        
        // 注册事件
        registerEvents();
    }
    
    /**
     * 注册所有事件
     */
    private void registerEvents() {
        // 检查配置是否启用各个事件
        if (plugin.getMainConfig().isChestRefillEnabled()) {
            events.add(new ChestRefillEvent(plugin, arena));
        }
        
        if (plugin.getMainConfig().isItemDropEnabled()) {
            events.add(new ItemDropEvent(plugin, arena));
        }
        
        if (plugin.getMainConfig().isWeatherChangeEnabled()) {
            events.add(new WeatherChangeEvent(plugin, arena));
        }
        
        plugin.getLogger().info("[" + arena.getArenaName() + "] 已注册 " + events.size() + " 个游戏事件");
    }
    
    /**
     * 启动事件检测
     */
    public void startEventDetection() {
        if (eventTask != null) {
            return;
        }
        
        eventTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            checkAndTriggerEvents();
        }, checkInterval * 20L, checkInterval * 20L);
        
        plugin.getLogger().info("[" + arena.getArenaName() + "] 游戏事件检测已启动");
    }
    
    /**
     * 停止事件检测
     */
    public void stopEventDetection() {
        if (eventTask != null) {
            eventTask.cancel();
            eventTask = null;
        }
        
        plugin.getLogger().info("[" + arena.getArenaName() + "] 游戏事件检测已停止");
    }
    
    /**
     * 检查并触发事件
     */
    private void checkAndTriggerEvents() {
        // 检查游戏是否正在进行
        if (!arena.getStateManager().isPlaying()) {
            return;
        }
        
        // 遍历所有事件，尝试触发
        for (GameEvent event : events) {
            event.trigger();
        }
    }
    
    /**
     * 手动触发指定事件
     */
    public void triggerEvent(String eventName) {
        for (GameEvent event : events) {
            if (event.getEventName().equalsIgnoreCase(eventName)) {
                event.execute();
                return;
            }
        }
    }
    
    /**
     * 获取所有事件
     */
    public List<GameEvent> getEvents() {
        return new ArrayList<>(events);
    }
    
    /**
     * 获取事件数量
     */
    public int getEventCount() {
        return events.size();
    }
}
