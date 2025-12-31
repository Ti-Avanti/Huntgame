package com.minecraft.huntergame.ability;

import com.minecraft.huntergame.HunterGame;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * 夜视能力
 * 给予猎人夜视效果
 * 
 * @author YourName
 * @version 1.0.0
 */
public class NightVisionAbility extends HunterAbility {
    
    public NightVisionAbility(HunterGame plugin) {
        super(plugin, "night-vision");
        
        // 从配置加载参数
        this.duration = plugin.getMainConfig().getNightVisionDuration();
        this.cooldown = plugin.getMainConfig().getNightVisionCooldown();
        this.enabled = plugin.getMainConfig().isNightVisionEnabled();
    }
    
    @Override
    protected boolean onActivate(Player player) {
        // 给予夜视效果
        player.addPotionEffect(new PotionEffect(
            PotionEffectType.NIGHT_VISION,
            duration * 20,
            0,  // 夜视I
            false,
            true
        ));
        
        // 发送消息
        plugin.getLanguageManager().sendMessage(player, "ability.night-vision");
        
        return true;
    }
    
    @Override
    protected void onDeactivate(Player player) {
        // 移除夜视效果
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
    }
}
