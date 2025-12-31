package com.minecraft.huntergame.event;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.game.ManhuntGame;
import com.minecraft.huntergame.game.PlayerRole;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;
import java.util.UUID;

/**
 * 随机效果事件
 * 给予玩家随机药水效果
 * 
 * @author YourName
 * @version 1.0.0
 */
public class RandomEffectEvent extends GameEvent {
    
    private final Random random;
    
    // 可能的效果列表
    private final PotionEffectType[] positiveEffects = {
        PotionEffectType.SPEED,
        PotionEffectType.JUMP_BOOST,
        PotionEffectType.REGENERATION,
        PotionEffectType.RESISTANCE,
        PotionEffectType.FIRE_RESISTANCE,
        PotionEffectType.WATER_BREATHING,
        PotionEffectType.INVISIBILITY,
        PotionEffectType.NIGHT_VISION
    };
    
    private final PotionEffectType[] negativeEffects = {
        PotionEffectType.SLOWNESS,
        PotionEffectType.WEAKNESS,
        PotionEffectType.POISON,
        PotionEffectType.BLINDNESS,
        PotionEffectType.HUNGER,
        PotionEffectType.MINING_FATIGUE
    };
    
    public RandomEffectEvent(HunterGame plugin) {
        super(plugin, "随机效果", "给予玩家随机药水效果");
        this.random = new Random();
    }
    
    @Override
    public void trigger(ManhuntGame game) {
        // 随机选择一个玩家
        UUID targetUuid = selectRandomPlayer(game);
        if (targetUuid == null) {
            return;
        }
        
        Player target = plugin.getServer().getPlayer(targetUuid);
        if (target == null || !target.isOnline()) {
            return;
        }
        
        // 随机决定是正面效果还是负面效果
        boolean isPositive = random.nextBoolean();
        PotionEffectType effectType;
        
        if (isPositive) {
            effectType = positiveEffects[random.nextInt(positiveEffects.length)];
        } else {
            effectType = negativeEffects[random.nextInt(negativeEffects.length)];
        }
        
        // 应用效果
        int duration = 20 * (30 + random.nextInt(60)); // 30-90秒
        int amplifier = random.nextInt(2); // 0-1级
        
        PotionEffect effect = new PotionEffect(effectType, duration, amplifier);
        target.addPotionEffect(effect);
        
        // 广播消息
        String effectName = getEffectName(effectType);
        String message = isPositive ? 
            "§a§l[事件] §e玩家 §b" + target.getName() + " §e获得了 §a" + effectName + " §e效果！" :
            "§c§l[事件] §e玩家 §b" + target.getName() + " §e受到了 §c" + effectName + " §e效果！";
        
        broadcastToGame(game, message);
        
        plugin.getLogger().info("触发随机效果事件: " + target.getName() + " - " + effectName);
    }
    
    @Override
    public boolean canTrigger(ManhuntGame game) {
        return game.getState() == com.minecraft.huntergame.game.GameState.PLAYING &&
               !game.getAllPlayers().isEmpty();
    }
    
    /**
     * 随机选择一个玩家
     */
    private UUID selectRandomPlayer(ManhuntGame game) {
        // 只选择逃亡者和猎人，不包括观战者
        java.util.List<UUID> players = new java.util.ArrayList<>();
        players.addAll(game.getRunners());
        players.addAll(game.getHunters());
        
        if (players.isEmpty()) {
            return null;
        }
        
        return players.get(random.nextInt(players.size()));
    }
    
    /**
     * 获取效果名称
     */
    private String getEffectName(PotionEffectType type) {
        if (type.equals(PotionEffectType.SPEED)) return "速度";
        if (type.equals(PotionEffectType.SLOWNESS)) return "缓慢";
        if (type.equals(PotionEffectType.JUMP_BOOST)) return "跳跃提升";
        if (type.equals(PotionEffectType.REGENERATION)) return "生命恢复";
        if (type.equals(PotionEffectType.RESISTANCE)) return "抗性提升";
        if (type.equals(PotionEffectType.FIRE_RESISTANCE)) return "防火";
        if (type.equals(PotionEffectType.WATER_BREATHING)) return "水下呼吸";
        if (type.equals(PotionEffectType.INVISIBILITY)) return "隐身";
        if (type.equals(PotionEffectType.NIGHT_VISION)) return "夜视";
        if (type.equals(PotionEffectType.WEAKNESS)) return "虚弱";
        if (type.equals(PotionEffectType.POISON)) return "中毒";
        if (type.equals(PotionEffectType.BLINDNESS)) return "失明";
        if (type.equals(PotionEffectType.HUNGER)) return "饥饿";
        if (type.equals(PotionEffectType.MINING_FATIGUE)) return "挖掘疲劳";
        return type.getName();
    }
    
    /**
     * 向游戏内所有玩家广播消息
     */
    private void broadcastToGame(ManhuntGame game, String message) {
        for (UUID uuid : game.getAllPlayers()) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.sendMessage(message);
            }
        }
    }
}
