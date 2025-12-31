package com.minecraft.huntergame.event;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.game.ManhuntGame;

/**
 * 游戏事件抽象类
 * 所有游戏事件的基类
 * 
 * @author YourName
 * @version 1.0.0
 */
public abstract class GameEvent {
    
    protected final HunterGame plugin;
    protected final String eventName;
    protected final String description;
    
    public GameEvent(HunterGame plugin, String eventName, String description) {
        this.plugin = plugin;
        this.eventName = eventName;
        this.description = description;
    }
    
    /**
     * 触发事件
     */
    public abstract void trigger(ManhuntGame game);
    
    /**
     * 检查事件是否可以触发
     */
    public abstract boolean canTrigger(ManhuntGame game);
    
    /**
     * 获取事件名称
     */
    public String getEventName() {
        return eventName;
    }
    
    /**
     * 获取事件描述
     */
    public String getDescription() {
        return description;
    }
}
