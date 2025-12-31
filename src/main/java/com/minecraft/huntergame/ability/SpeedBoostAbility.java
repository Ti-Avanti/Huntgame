package com.minecraft.huntergame.ability;

import com.minecraft.huntergame.HunterGame;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * 速度提升能力
 * 给予猎人速度II效果
 * 
 * @author YourName
 * @version 1.0.0
 */
public class SpeedBoostAbility extends HunterAbility {
    
    public SpeedBoostAbility(HunterGame plugin) {
        super(plugin, "speed-boost");
        
        // 从配置加载参数
        this.duration = plugin.getMainConfig().getSpeedBoostDuration();
        this.cooldown = plugin.getMainConfig().getSpeedBoostCooldown();
        this.enabled = plugin.getMainConfig().isSpeedBoostEnabled();
    }
    
    @Override
    protected boolean onActivate(Player player) {
        // 给予速度II效果
        player.addPotionEffect(new PotionEffect(
            PotionEffectType.SPEED,
            duration * 20,
            1,  // 速度II (等级从0开始，1表示II)
            false,
            true
        ));
        
        // 发送消息
        plugin.getLanguageManager().sendMessage(player, "ability.speed-boost");
        
        return true;
    }
    
    @Override
    protected void onDeactivate(Player player) {
        // 移除速度效果
        player.removePotionEffect(PotionEffectType.SPEED);
    }
}
