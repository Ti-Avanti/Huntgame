package com.minecraft.huntergame.event;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.arena.Arena;

import java.util.Random;

/**
 * 游戏事件抽象类
 * 定义游戏中的随机事件
 * 
 * @author YourName
 * @version 1.0.0
 */
public abstract class GameEvent {
    
    protected final HunterGame plugin;
    protected final Arena arena;
    protected final String eventName;
    protected final double triggerChance;
    protected final Random random;
    
    public GameEvent(HunterGame plugin, Arena arena, String eventName, double triggerChance) {
        this.plugin = plugin;
        this.arena = arena;
        this.eventName = eventName;
        this.triggerChance = triggerChance;
        this.random = new Random();
    }
    
    /**
     * 获取事件名称
     */
    public String getEventName() {
        return eventName;
    }
    
    /**
     * 获取触发概率
     */
    public double getTriggerChance() {
        return triggerChance;
    }
    
    /**
     * 检查是否应该触发事件
     */
    public boolean shouldTrigger() {
        return random.nextDouble() < triggerChance;
    }
    
    /**
     * 检查事件是否可以触发
     * 子类可以重写此方法添加额外条件
     */
    public boolean canTrigger() {
        return true;
    }
    
    /**
     * 触发事件
     */
    public void trigger() {
        if (!canTrigger()) {
            return;
        }
        
        if (!shouldTrigger()) {
            return;
        }
        
        execute();
        
        plugin.getLogger().info("[" + arena.getArenaName() + "] 触发事件: " + eventName);
    }
    
    /**
     * 执行事件逻辑
     * 子类必须实现此方法
     */
    protected abstract void execute();
    
    /**
     * 广播事件消息
     */
    protected void broadcastMessage(String message) {
        arena.broadcastMessage(message);
    }
}
