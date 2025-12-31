package com.minecraft.huntergame.event;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.game.ManhuntGame;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 游戏事件管理器
 * 负责管理和触发游戏事件
 * 
 * @author YourName
 * @version 1.0.0
 */
public class GameEventManager {
    
    private final HunterGame plugin;
    private final List<GameEvent> events;
    private final Random random;
    
    public GameEventManager(HunterGame plugin) {
        this.plugin = plugin;
        this.events = new ArrayList<>();
        this.random = new Random();
        
        // 注册所有事件
        registerEvents();
        
        plugin.getLogger().info("游戏事件管理器已初始化");
    }
    
    /**
     * 注册所有事件
     */
    private void registerEvents() {
        events.add(new RandomEffectEvent(plugin));
        events.add(new TeleportEvent(plugin));
        events.add(new WeatherChangeEvent(plugin));
        
        plugin.getLogger().info("已注册 " + events.size() + " 个游戏事件");
    }
    
    /**
     * 检查并触发事件
     */
    public void checkAndTrigger(ManhuntGame game) {
        // 检查是否启用随机事件
        if (!plugin.getManhuntConfig().isRandomEventsEnabled()) {
            return;
        }
        
        // 检查触发概率
        double triggerChance = plugin.getManhuntConfig().getEventTriggerChance();
        if (random.nextDouble() > triggerChance) {
            return;
        }
        
        // 获取所有可以触发的事件
        List<GameEvent> availableEvents = new ArrayList<>();
        for (GameEvent event : events) {
            if (event.canTrigger(game)) {
                availableEvents.add(event);
            }
        }
        
        // 如果没有可用事件，返回
        if (availableEvents.isEmpty()) {
            return;
        }
        
        // 随机选择一个事件触发
        GameEvent selectedEvent = availableEvents.get(random.nextInt(availableEvents.size()));
        selectedEvent.trigger(game);
    }
    
    /**
     * 启动事件检测任务
     */
    public void startEventCheckTask() {
        // 检查是否启用随机事件
        if (!plugin.getManhuntConfig().isRandomEventsEnabled()) {
            plugin.getLogger().info("随机事件系统已禁用");
            return;
        }
        
        int interval = plugin.getManhuntConfig().getEventTriggerInterval();
        
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            // 性能优化：只在有游戏时检查事件
            if (plugin.getManhuntManager().getGameCount() == 0) {
                return;
            }
            
            // 检查所有游戏
            for (ManhuntGame game : plugin.getManhuntManager().getAllGames()) {
                // 只在游戏进行中触发事件
                if (game.getState() == com.minecraft.huntergame.game.GameState.PLAYING) {
                    checkAndTrigger(game);
                }
            }
        }, 20L * interval, 20L * interval);
        
        plugin.getLogger().info("游戏事件检测任务已启动，间隔: " + interval + "秒");
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
