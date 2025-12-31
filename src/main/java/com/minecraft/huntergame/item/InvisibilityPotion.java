package com.minecraft.huntergame.item;

import com.minecraft.huntergame.HunterGame;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * 隐身药水道具
 * 给予玩家隐身效果
 * 
 * @author YourName
 * @version 1.0.0
 */
public class InvisibilityPotion extends SurvivorItem {
    
    private final int duration; // 持续时间(秒)
    
    public InvisibilityPotion(HunterGame plugin) {
        super(plugin, "invisibility-potion");
        this.material = Material.POTION;
        this.duration = plugin.getMainConfig().getInvisibilityPotionDuration();
        this.maxUses = plugin.getMainConfig().getInvisibilityPotionCount();
        this.consumable = true;
    }
    
    @Override
    protected boolean onUse(Player player) {
        // 给予隐身效果
        int durationTicks = duration * 20; // 转换为tick
        PotionEffect invisibility = new PotionEffect(
            PotionEffectType.INVISIBILITY,
            durationTicks,
            0, // 等级0 (隐身I)
            false, // 不显示粒子效果
            false  // 不显示图标
        );
        
        player.addPotionEffect(invisibility);
        
        // 记录日志
        plugin.getLogger().info("[道具] 玩家 " + player.getName() + " 使用了隐身药水");
        
        return true;
    }
    
    /**
     * 获取持续时间
     */
    public int getDuration() {
        return duration;
    }
}
