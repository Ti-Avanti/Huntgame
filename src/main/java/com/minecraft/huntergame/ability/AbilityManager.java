package com.minecraft.huntergame.ability;

import com.minecraft.huntergame.HunterGame;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 能力管理器
 * 负责管理所有猎人能力
 * 
 * @author YourName
 * @version 1.0.0
 */
public class AbilityManager {
    
    private final HunterGame plugin;
    private final Map<String, HunterAbility> abilities;
    
    public AbilityManager(HunterGame plugin) {
        this.plugin = plugin;
        this.abilities = new HashMap<>();
        
        // 注册所有能力
        registerAbilities();
    }
    
    /**
     * 注册所有能力
     */
    private void registerAbilities() {
        registerAbility(new SpeedBoostAbility(plugin));
        registerAbility(new TrackerAbility(plugin));
        registerAbility(new NightVisionAbility(plugin));
        
        plugin.getLogger().info("已注册 " + abilities.size() + " 个猎人能力");
    }
    
    /**
     * 注册单个能力
     */
    private void registerAbility(HunterAbility ability) {
        abilities.put(ability.getAbilityName(), ability);
        
        if (ability.isEnabled()) {
            plugin.getLogger().info("  - " + ability.getAbilityName() + 
                " (冷却: " + ability.getCooldown() + "s, 持续: " + ability.getDuration() + "s)");
        } else {
            plugin.getLogger().info("  - " + ability.getAbilityName() + " (已禁用)");
        }
    }
    
    /**
     * 获取能力
     */
    public HunterAbility getAbility(String name) {
        return abilities.get(name);
    }
    
    /**
     * 激活能力
     */
    public boolean activateAbility(Player player, String abilityName) {
        HunterAbility ability = abilities.get(abilityName);
        
        if (ability == null) {
            plugin.getLogger().warning("未知的能力: " + abilityName);
            return false;
        }
        
        return ability.activate(player);
    }
    
    /**
     * 停用能力
     */
    public void deactivateAbility(Player player, String abilityName) {
        HunterAbility ability = abilities.get(abilityName);
        
        if (ability != null) {
            ability.deactivate(player);
        }
    }
    
    /**
     * 清除玩家的所有能力状态
     */
    public void clearPlayer(UUID uuid) {
        for (HunterAbility ability : abilities.values()) {
            ability.clearPlayer(uuid);
        }
    }
    
    /**
     * 清除所有玩家的能力状态
     */
    public void clearAll() {
        for (HunterAbility ability : abilities.values()) {
            ability.clearAll();
        }
    }
    
    /**
     * 获取所有能力
     */
    public Map<String, HunterAbility> getAbilities() {
        return new HashMap<>(abilities);
    }
    
    /**
     * 检查能力是否在冷却中
     */
    public boolean isOnCooldown(UUID uuid, String abilityName) {
        HunterAbility ability = abilities.get(abilityName);
        return ability != null && ability.isOnCooldown(uuid);
    }
    
    /**
     * 获取剩余冷却时间
     */
    public int getRemainingCooldown(UUID uuid, String abilityName) {
        HunterAbility ability = abilities.get(abilityName);
        return ability != null ? ability.getRemainingCooldown(uuid) : 0;
    }
}
