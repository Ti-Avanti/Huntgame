package com.minecraft.huntergame.escape;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.arena.Arena;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 逃脱管理器
 * 负责管理竞技场的所有逃生点和逃脱检测
 * 
 * @author YourName
 * @version 1.0.0
 */
public class EscapeManager {
    
    private final HunterGame plugin;
    private final Arena arena;
    
    // 逃生点列表
    private final List<EscapePoint> escapePoints;
    
    // 逃脱检测任务
    private BukkitTask escapeCheckTask;
    
    public EscapeManager(HunterGame plugin, Arena arena) {
        this.plugin = plugin;
        this.arena = arena;
        this.escapePoints = new ArrayList<>();
    }
    
    /**
     * 添加逃生点
     * 
     * @param escapePoint 逃生点
     */
    public void addEscapePoint(EscapePoint escapePoint) {
        escapePoints.add(escapePoint);
    }
    
    /**
     * 从位置创建并添加逃生点
     * 
     * @param location 位置
     * @param radius 范围
     * @param escapeTime 逃脱时间
     */
    public void addEscapePoint(Location location, int radius, int escapeTime) {
        String pointName = "escape-" + (escapePoints.size() + 1);
        EscapePoint point = new EscapePoint(plugin, pointName, location, radius, escapeTime);
        addEscapePoint(point);
    }
    
    /**
     * 移除逃生点
     * 
     * @param escapePoint 逃生点
     */
    public void removeEscapePoint(EscapePoint escapePoint) {
        escapePoints.remove(escapePoint);
    }
    
    /**
     * 清除所有逃生点
     */
    public void clearEscapePoints() {
        escapePoints.clear();
    }
    
    /**
     * 启动逃脱检测任务
     */
    public void startEscapeCheck() {
        if (escapeCheckTask != null) {
            escapeCheckTask.cancel();
        }
        
        escapeCheckTask = new BukkitRunnable() {
            @Override
            public void run() {
                checkEscapeProgress();
            }
        }.runTaskTimer(plugin, 0L, 20L); // 每秒检测一次
        
        plugin.getLogger().info("[" + arena.getArenaName() + "] 逃脱检测任务已启动");
    }
    
    /**
     * 启动逃脱检测（别名方法）
     */
    public void startEscapeDetection() {
        startEscapeCheck();
    }
    
    /**
     * 停止逃脱检测任务
     */
    public void stopEscapeCheck() {
        if (escapeCheckTask != null) {
            escapeCheckTask.cancel();
            escapeCheckTask = null;
        }
        
        plugin.getLogger().info("[" + arena.getArenaName() + "] 逃脱检测任务已停止");
    }
    
    /**
     * 停止逃脱检测（别名方法）
     */
    public void stopEscapeDetection() {
        stopEscapeCheck();
    }
    
    /**
     * 检测逃脱进度
     */
    private void checkEscapeProgress() {
        // 获取所有存活的逃生者
        List<UUID> survivors = arena.getAliveSurvivors();
        
        for (UUID uuid : survivors) {
            Player player = plugin.getServer().getPlayer(uuid);
            
            if (player == null || !player.isOnline()) {
                continue;
            }
            
            // 检查玩家是否在任何逃生点范围内
            boolean inRange = false;
            EscapePoint currentPoint = null;
            
            for (EscapePoint point : escapePoints) {
                if (point.isInRange(player)) {
                    inRange = true;
                    currentPoint = point;
                    break;
                }
            }
            
            if (inRange && currentPoint != null) {
                // 玩家在逃生点范围内
                if (!currentPoint.isEscaping(uuid)) {
                    // 开始逃脱
                    currentPoint.startEscape(player);
                } else {
                    // 更新逃脱进度
                    currentPoint.updateProgress(player);
                }
            } else {
                // 玩家不在任何逃生点范围内，取消所有逃脱进度
                for (EscapePoint point : escapePoints) {
                    if (point.isEscaping(uuid)) {
                        point.cancelEscape(player);
                    }
                }
            }
        }
    }
    
    /**
     * 处理玩家逃脱完成
     * 
     * @param player 玩家
     */
    public void handleEscapeComplete(Player player) {
        UUID uuid = player.getUniqueId();
        
        // 标记玩家已逃脱
        arena.markEscaped(uuid);
        
        // 传送到安全区域（观战位置）
        Location spectatorLocation = arena.getSpectatorLocation();
        if (spectatorLocation != null) {
            player.teleport(spectatorLocation);
        }
        
        // 设置为观战模式
        player.setGameMode(org.bukkit.GameMode.SPECTATOR);
        
        // 广播逃脱消息
        arena.broadcastMessage(plugin.getLanguageManager().getMessage("escape.player-escaped", 
            player.getName()));
        
        // 清除所有逃生点的该玩家进度
        for (EscapePoint point : escapePoints) {
            point.clearPlayer(uuid);
        }
        
        plugin.getLogger().info("[" + arena.getArenaName() + "] 玩家 " + player.getName() + " 成功逃脱");
    }
    
    /**
     * 获取玩家当前所在的逃生点
     * 
     * @param player 玩家
     * @return 逃生点，如果不在任何逃生点则返回null
     */
    public EscapePoint getCurrentEscapePoint(Player player) {
        for (EscapePoint point : escapePoints) {
            if (point.isInRange(player)) {
                return point;
            }
        }
        return null;
    }
    
    /**
     * 获取玩家逃脱进度
     * 
     * @param uuid 玩家UUID
     * @return 进度百分比(0-100)，如果不在逃脱则返回0
     */
    public int getEscapeProgress(UUID uuid) {
        for (EscapePoint point : escapePoints) {
            if (point.isEscaping(uuid)) {
                return point.getProgressPercentage(uuid);
            }
        }
        return 0;
    }
    
    /**
     * 检查玩家是否正在逃脱
     * 
     * @param uuid 玩家UUID
     * @return 是否正在逃脱
     */
    public boolean isEscaping(UUID uuid) {
        for (EscapePoint point : escapePoints) {
            if (point.isEscaping(uuid)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 清除所有玩家的逃脱进度
     */
    public void clearAll() {
        for (EscapePoint point : escapePoints) {
            point.clearAll();
        }
    }
    
    /**
     * 清除玩家的逃脱进度
     * 
     * @param uuid 玩家UUID
     */
    public void clearPlayer(UUID uuid) {
        for (EscapePoint point : escapePoints) {
            point.clearPlayer(uuid);
        }
    }
    
    // ==================== Getter ====================
    
    public List<EscapePoint> getEscapePoints() {
        return new ArrayList<>(escapePoints);
    }
    
    public int getEscapePointCount() {
        return escapePoints.size();
    }
}
