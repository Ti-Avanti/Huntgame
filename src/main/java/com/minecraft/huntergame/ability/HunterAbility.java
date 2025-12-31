package com.minecraft.huntergame.ability;

import com.minecraft.huntergame.HunterGame;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 猎人能力抽象基类
 * 
 * @author YourName
 * @version 1.0.0
 */
public abstract class HunterAbility {
    
    protected final HunterGame plugin;
    protected final String abilityName;
    protected int cooldown;      // 冷却时间(秒)
    protected int duration;      // 持续时间(秒)
    protected boolean enabled;
    
    // 冷却时间映射 (玩家UUID -> 冷却结束时间戳)
    protected final Map<UUID, Long> cooldowns;
    
    // 激活状态映射 (玩家UUID -> 激活结束时间戳)
    protected final Map<UUID, Long> activeUntil;
    
    public HunterAbility(HunterGame plugin, String abilityName) {
        this.plugin = plugin;
        this.abilityName = abilityName;
        this.cooldowns = new HashMap<>();
        this.activeUntil = new HashMap<>();
        this.enabled = true;
    }
    
    /**
     * 激活能力
     * 
     * @param player 玩家
     * @return 是否成功激活
     */
    public boolean activate(Player player) {
        UUID uuid = player.getUniqueId();
        
        // 检查能力是否启用
        if (!enabled) {
            return false;
        }
        
        // 检查是否在冷却中
        if (isOnCooldown(uuid)) {
            int remaining = getRemainingCooldown(uuid);
            plugin.getLanguageManager().sendMessage(player, "ability.cooldown", remaining);
            return false;
        }
        
        // 执行能力效果
        if (onActivate(player)) {
            // 设置冷却时间
            long now = System.currentTimeMillis();
            cooldowns.put(uuid, now + (cooldown * 1000L));
            
            // 如果有持续时间，设置激活状态
            if (duration > 0) {
                activeUntil.put(uuid, now + (duration * 1000L));
                
                // 安排停用任务
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    deactivate(player);
                }, duration * 20L);
            }
            
            return true;
        }
        
        return false;
    }
    
    /**
     * 停用能力
     * 
     * @param player 玩家
     */
    public void deactivate(Player player) {
        UUID uuid = player.getUniqueId();
        activeUntil.remove(uuid);
        onDeactivate(player);
    }
    
    /**
     * 能力激活时的具体实现
     * 
     * @param player 玩家
     * @return 是否成功
     */
    protected abstract boolean onActivate(Player player);
    
    /**
     * 能力停用时的具体实现
     * 
     * @param player 玩家
     */
    protected abstract void onDeactivate(Player player);
    
    /**
     * 检查是否在冷却中
     */
    public boolean isOnCooldown(UUID uuid) {
        Long cooldownEnd = cooldowns.get(uuid);
        if (cooldownEnd == null) {
            return false;
        }
        
        long now = System.currentTimeMillis();
        if (now >= cooldownEnd) {
            cooldowns.remove(uuid);
            return false;
        }
        
        return true;
    }
    
    /**
     * 获取剩余冷却时间(秒)
     */
    public int getRemainingCooldown(UUID uuid) {
        Long cooldownEnd = cooldowns.get(uuid);
        if (cooldownEnd == null) {
            return 0;
        }
        
        long now = System.currentTimeMillis();
        long remaining = cooldownEnd - now;
        
        return Math.max(0, (int) (remaining / 1000));
    }
    
    /**
     * 检查能力是否激活中
     */
    public boolean isActive(UUID uuid) {
        Long activeEnd = activeUntil.get(uuid);
        if (activeEnd == null) {
            return false;
        }
        
        long now = System.currentTimeMillis();
        return now < activeEnd;
    }
    
    /**
     * 清除玩家的冷却和激活状态
     */
    public void clearPlayer(UUID uuid) {
        cooldowns.remove(uuid);
        activeUntil.remove(uuid);
    }
    
    /**
     * 清除所有玩家状态
     */
    public void clearAll() {
        cooldowns.clear();
        activeUntil.clear();
    }
    
    // ==================== Getter 和 Setter ====================
    
    public String getAbilityName() {
        return abilityName;
    }
    
    public int getCooldown() {
        return cooldown;
    }
    
    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }
    
    public int getDuration() {
        return duration;
    }
    
    public void setDuration(int duration) {
        this.duration = duration;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
