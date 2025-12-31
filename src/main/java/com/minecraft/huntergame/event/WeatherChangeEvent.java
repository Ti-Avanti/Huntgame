package com.minecraft.huntergame.event;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.game.ManhuntGame;
import org.bukkit.World;

import java.util.Random;
import java.util.UUID;

/**
 * 天气变化事件
 * 改变游戏世界的天气和时间
 * 
 * @author YourName
 * @version 1.0.0
 */
public class WeatherChangeEvent extends GameEvent {
    
    private final Random random;
    
    public WeatherChangeEvent(HunterGame plugin) {
        super(plugin, "天气变化", "改变游戏世界的天气和时间");
        this.random = new Random();
    }
    
    @Override
    public void trigger(ManhuntGame game) {
        // 获取游戏世界
        World world = plugin.getServer().getWorld(game.getWorldName());
        if (world == null) {
            return;
        }
        
        // 随机选择天气类型
        int weatherType = random.nextInt(3);
        String weatherName;
        
        switch (weatherType) {
            case 0: // 晴天
                world.setStorm(false);
                world.setThundering(false);
                weatherName = "晴天";
                break;
            case 1: // 雨天
                world.setStorm(true);
                world.setThundering(false);
                world.setWeatherDuration(20 * 60 * (5 + random.nextInt(10))); // 5-15分钟
                weatherName = "雨天";
                break;
            case 2: // 雷暴
                world.setStorm(true);
                world.setThundering(true);
                world.setWeatherDuration(20 * 60 * (3 + random.nextInt(7))); // 3-10分钟
                weatherName = "雷暴";
                break;
            default:
                weatherName = "未知";
        }
        
        // 随机决定是否改变时间
        if (random.nextBoolean()) {
            int timeType = random.nextInt(3);
            String timeName;
            
            switch (timeType) {
                case 0: // 白天
                    world.setTime(1000);
                    timeName = "白天";
                    break;
                case 1: // 黄昏
                    world.setTime(12000);
                    timeName = "黄昏";
                    break;
                case 2: // 夜晚
                    world.setTime(18000);
                    timeName = "夜晚";
                    break;
                default:
                    timeName = "未知";
            }
            
            // 广播消息
            broadcastToGame(game, "§b§l[事件] §e天气变为 §a" + weatherName + "§e，时间变为 §a" + timeName + "§e！");
        } else {
            // 只改变天气
            broadcastToGame(game, "§b§l[事件] §e天气变为 §a" + weatherName + "§e！");
        }
        
        plugin.getLogger().info("触发天气变化事件: " + weatherName);
    }
    
    @Override
    public boolean canTrigger(ManhuntGame game) {
        return game.getState() == com.minecraft.huntergame.game.GameState.PLAYING;
    }
    
    /**
     * 向游戏内所有玩家广播消息
     */
    private void broadcastToGame(ManhuntGame game, String message) {
        for (UUID uuid : game.getAllPlayers()) {
            org.bukkit.entity.Player player = plugin.getServer().getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.sendMessage(message);
            }
        }
    }
}
