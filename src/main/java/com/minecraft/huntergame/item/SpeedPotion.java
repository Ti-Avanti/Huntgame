package com.minecraft.huntergame.item;

import com.minecraft.huntergame.HunterGame;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * 速度药水道具
 * 给予玩家速度提升效果
 * 
 * @author YourName
 * @version 1.0.0
 */
public class SpeedPotion extends SurvivorItem {
    
    private final int duration; // 持续时间(秒)
    private final int speedLevel; // 速度等级
    
    public SpeedPotion(HunterGame plugin) {
        super(plugin, "speed-potion");
        this.material = Material.SUGAR;
        this.duration = plugin.getMainConfig().getSpeedPotionDuration();
        this.speedLevel = 1; // 速度II (等级从0开始，1表示II)
        this.maxUses = plugin.getMainConfig().getSpeedPotionCount();
        this.consumable = true;
    }
    
    @Override
    protected boolean onUse(Player player) {
        // 给予速度效果
        int durationTicks = duration * 20; // 转换为tick
        PotionEffect speed = new PotionEffect(
            PotionEffectType.SPEED,
            durationTicks,
            speedLevel, // 速度II
            false, // 不显示粒子效果
            true   // 显示图标
        );
        
        player.addPotionEffect(speed);
        
        // 记录日志
        plugin.getLogger().info("[道具] 玩家 " + player.getName() + " 使用了速度药水");
        
        return true;
    }
    
    /**
     * 获取持续时间
     */
    public int getDuration() {
        return duration;
    }
    
    /**
     * 获取速度等级
     */
    public int getSpeedLevel() {
        return speedLevel;
    }
}
