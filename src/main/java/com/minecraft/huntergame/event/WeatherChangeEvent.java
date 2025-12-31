package com.minecraft.huntergame.event;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.arena.Arena;
import org.bukkit.World;

/**
 * 天气变化事件
 * 改变游戏世界的天气和时间
 * 
 * @author YourName
 * @version 1.0.0
 */
public class WeatherChangeEvent extends GameEvent {
    
    public WeatherChangeEvent(HunterGame plugin, Arena arena) {
        super(plugin, arena, "天气变化", 0.15); // 15%触发概率
    }
    
    @Override
    protected void execute() {
        World world = arena.getWorld();
        if (world == null) {
            return;
        }
        
        // 随机选择天气类型
        int weatherType = random.nextInt(3);
        
        switch (weatherType) {
            case 0:
                // 晴天
                world.setStorm(false);
                world.setThundering(false);
                broadcastMessage(plugin.getLanguageManager().getMessage("event.weather-clear"));
                break;
                
            case 1:
                // 下雨
                world.setStorm(true);
                world.setThundering(false);
                broadcastMessage(plugin.getLanguageManager().getMessage("event.weather-rain"));
                break;
                
            case 2:
                // 雷暴
                world.setStorm(true);
                world.setThundering(true);
                broadcastMessage(plugin.getLanguageManager().getMessage("event.weather-thunder"));
                break;
        }
        
        // 随机改变时间
        if (random.nextDouble() < 0.5) { // 50%概率改变时间
            changeTime(world);
        }
    }
    
    /**
     * 改变时间
     */
    private void changeTime(World world) {
        int timeType = random.nextInt(3);
        
        switch (timeType) {
            case 0:
                // 白天
                world.setTime(1000);
                broadcastMessage(plugin.getLanguageManager().getMessage("event.time-day"));
                break;
                
            case 1:
                // 黄昏
                world.setTime(12000);
                broadcastMessage(plugin.getLanguageManager().getMessage("event.time-sunset"));
                break;
                
            case 2:
                // 夜晚
                world.setTime(18000);
                broadcastMessage(plugin.getLanguageManager().getMessage("event.time-night"));
                break;
        }
    }
}
