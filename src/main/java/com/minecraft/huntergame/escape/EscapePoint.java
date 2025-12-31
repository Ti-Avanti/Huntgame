package com.minecraft.huntergame.escape;

import com.minecraft.huntergame.HunterGame;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 逃生点类
 * 代表一个逃生点位置和逃脱进度管理
 * 
 * @author YourName
 * @version 1.0.0
 */
public class EscapePoint {
    
    private final HunterGame plugin;
    private final String pointName;
    private final Location location;
    private final int radius; // 逃生点范围(方块)
    private final int escapeTime; // 逃脱所需时间(秒)
    private boolean enabled;
    
    // 逃脱进度追踪 (玩家UUID -> 进度秒数)
    private final Map<UUID, Integer> escapeProgress;
    
    public EscapePoint(HunterGame plugin, String pointName, Location location, int radius, int escapeTime) {
        this.plugin = plugin;
        this.pointName = pointName;
        this.location = location;
        this.radius = radius;
        this.escapeTime = escapeTime;
        this.enabled = true;
        this.escapeProgress = new HashMap<>();
    }
    
    /**
     * 检查玩家是否在逃生点范围内
     * 
     * @param player 玩家
     * @return 是否在范围内
     */
    public boolean isInRange(Player player) {
        if (!enabled) {
            return false;
        }
        
        if (player.getWorld() != location.getWorld()) {
            return false;
        }
        
        double distance = player.getLocation().distance(location);
        return distance <= radius;
    }
    
    /**
     * 开始逃脱
     * 
     * @param player 玩家
     */
    public void startEscape(Player player) {
        UUID uuid = player.getUniqueId();
        
        if (!escapeProgress.containsKey(uuid)) {
            escapeProgress.put(uuid, 0);
            
            // 发送消息
            plugin.getLanguageManager().sendMessage(player, "escape.started");
            
            plugin.getLogger().info("[逃脱] 玩家 " + player.getName() + " 开始逃脱");
        }
    }
    
    /**
     * 更新逃脱进度
     * 
     * @param player 玩家
     */
    public void updateProgress(Player player) {
        UUID uuid = player.getUniqueId();
        
        if (!escapeProgress.containsKey(uuid)) {
            return;
        }
        
        // 增加进度
        int progress = escapeProgress.get(uuid) + 1;
        escapeProgress.put(uuid, progress);
        
        // 发送进度消息
        int percentage = (progress * 100) / escapeTime;
        plugin.getLanguageManager().sendMessage(player, "escape.progress", 
            String.valueOf(percentage));
        
        // 检查是否完成逃脱
        if (progress >= escapeTime) {
            completeEscape(player);
        }
    }
    
    /**
     * 取消逃脱
     * 
     * @param player 玩家
     */
    public void cancelEscape(Player player) {
        UUID uuid = player.getUniqueId();
        
        if (escapeProgress.containsKey(uuid)) {
            escapeProgress.remove(uuid);
            
            // 发送消息
            plugin.getLanguageManager().sendMessage(player, "escape.cancelled");
            
            plugin.getLogger().info("[逃脱] 玩家 " + player.getName() + " 逃脱被取消");
        }
    }
    
    /**
     * 完成逃脱
     * 
     * @param player 玩家
     */
    private void completeEscape(Player player) {
        UUID uuid = player.getUniqueId();
        
        // 移除进度
        escapeProgress.remove(uuid);
        
        // 发送消息
        plugin.getLanguageManager().sendMessage(player, "escape.completed");
        
        // 触发逃脱完成事件
        // TODO: 在Arena中处理逃脱完成逻辑
        
        plugin.getLogger().info("[逃脱] 玩家 " + player.getName() + " 成功逃脱!");
    }
    
    /**
     * 获取玩家逃脱进度
     * 
     * @param uuid 玩家UUID
     * @return 进度秒数
     */
    public int getProgress(UUID uuid) {
        return escapeProgress.getOrDefault(uuid, 0);
    }
    
    /**
     * 获取玩家逃脱进度百分比
     * 
     * @param uuid 玩家UUID
     * @return 进度百分比(0-100)
     */
    public int getProgressPercentage(UUID uuid) {
        int progress = getProgress(uuid);
        return (progress * 100) / escapeTime;
    }
    
    /**
     * 检查玩家是否正在逃脱
     * 
     * @param uuid 玩家UUID
     * @return 是否正在逃脱
     */
    public boolean isEscaping(UUID uuid) {
        return escapeProgress.containsKey(uuid);
    }
    
    /**
     * 清除所有逃脱进度
     */
    public void clearAll() {
        escapeProgress.clear();
    }
    
    /**
     * 清除玩家逃脱进度
     * 
     * @param uuid 玩家UUID
     */
    public void clearPlayer(UUID uuid) {
        escapeProgress.remove(uuid);
    }
    
    // ==================== Getter 和 Setter ====================
    
    public String getPointName() {
        return pointName;
    }
    
    public Location getLocation() {
        return location.clone();
    }
    
    public int getRadius() {
        return radius;
    }
    
    public int getEscapeTime() {
        return escapeTime;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
