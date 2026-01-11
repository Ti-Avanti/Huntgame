package com.minecraft.huntergame.rank;

import com.minecraft.huntergame.HunterGame;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * 赛季管理器
 * 负责管理赛季系统
 * 
 * @author YourName
 * @version 2.0.0
 */
public class SeasonManager {
    
    private final HunterGame plugin;
    private File seasonFile;
    private FileConfiguration seasonConfig;
    
    private int currentSeasonId;
    private long seasonStartTime;
    private long seasonEndTime;
    
    public SeasonManager(HunterGame plugin) {
        this.plugin = plugin;
        loadSeasonConfig();
    }
    
    /**
     * 加载赛季配置
     */
    private void loadSeasonConfig() {
        seasonFile = new File(plugin.getDataFolder(), "season.yml");
        
        // 如果文件不存在，从resources复制默认配置
        if (!seasonFile.exists()) {
            plugin.saveResource("season.yml", false);
            plugin.getLogger().info("已创建默认赛季配置文件");
        }
        
        seasonConfig = YamlConfiguration.loadConfiguration(seasonFile);
        
        // 读取赛季信息
        currentSeasonId = seasonConfig.getInt("current-season", 1);
        seasonStartTime = seasonConfig.getLong("season-start-time", 0);
        seasonEndTime = seasonConfig.getLong("season-end-time", 0);
        
        // 如果是首次运行（season-start-time为0），设置为当前时间
        if (seasonStartTime == 0) {
            seasonStartTime = System.currentTimeMillis();
            seasonConfig.set("season-start-time", seasonStartTime);
            
            // 计算赛季结束时间
            int durationDays = seasonConfig.getInt("season-duration-days", 30);
            if (durationDays > 0) {
                seasonEndTime = seasonStartTime + (durationDays * 24L * 60 * 60 * 1000);
                seasonConfig.set("season-end-time", seasonEndTime);
            }
            
            saveSeasonConfig();
        }
        
        // 同步到StatsManager
        plugin.getStatsManager().setCurrentSeasonId(currentSeasonId);
        
        plugin.getLogger().info("当前赛季: S" + currentSeasonId);
    }
    
    /**
     * 创建默认配置（已废弃，改用saveResource）
     */
    @Deprecated
    private void createDefaultConfig() {
        try {
            seasonFile.getParentFile().mkdirs();
            seasonFile.createNewFile();
            
            FileConfiguration config = YamlConfiguration.loadConfiguration(seasonFile);
            config.set("current-season", 1);
            config.set("season-start-time", System.currentTimeMillis());
            config.set("season-end-time", 0);
            config.set("season-duration-days", 30);
            
            config.save(seasonFile);
            plugin.getLogger().info("已创建默认赛季配置");
        } catch (IOException e) {
            plugin.getLogger().severe("创建赛季配置文件失败: " + e.getMessage());
        }
    }
    
    /**
     * 保存赛季配置
     */
    private void saveSeasonConfig() {
        try {
            seasonConfig.set("current-season", currentSeasonId);
            seasonConfig.set("season-start-time", seasonStartTime);
            seasonConfig.set("season-end-time", seasonEndTime);
            seasonConfig.save(seasonFile);
        } catch (IOException e) {
            plugin.getLogger().severe("保存赛季配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取当前赛季ID
     */
    public int getCurrentSeasonId() {
        return currentSeasonId;
    }
    
    /**
     * 获取赛季开始时间
     */
    public long getSeasonStartTime() {
        return seasonStartTime;
    }
    
    /**
     * 获取赛季结束时间
     */
    public long getSeasonEndTime() {
        return seasonEndTime;
    }
    
    /**
     * 检查赛季是否结束
     */
    public boolean isSeasonEnded() {
        if (seasonEndTime == 0) {
            return false; // 无限期赛季
        }
        return System.currentTimeMillis() >= seasonEndTime;
    }
    
    /**
     * 获取赛季剩余时间（毫秒）
     */
    public long getSeasonRemainingTime() {
        if (seasonEndTime == 0) {
            return -1; // 无限期
        }
        long remaining = seasonEndTime - System.currentTimeMillis();
        return Math.max(0, remaining);
    }
    
    /**
     * 开始新赛季
     */
    public void startNewSeason() {
        currentSeasonId++;
        seasonStartTime = System.currentTimeMillis();
        
        // 计算赛季结束时间（默认30天）
        int durationDays = seasonConfig.getInt("season-duration-days", 30);
        if (durationDays > 0) {
            seasonEndTime = seasonStartTime + (durationDays * 24L * 60 * 60 * 1000);
        } else {
            seasonEndTime = 0; // 无限期
        }
        
        // 保存配置
        saveSeasonConfig();
        
        // 同步到StatsManager
        plugin.getStatsManager().setCurrentSeasonId(currentSeasonId);
        
        plugin.getLogger().info("新赛季已开始: S" + currentSeasonId);
    }
    
    /**
     * 重置赛季
     * 重置所有玩家的段位数据
     */
    public void resetSeason() {
        plugin.getLogger().info("正在重置赛季...");
        
        // 重置所有在线玩家的赛季数据
        plugin.getStatsManager().resetAllSeasons(currentSeasonId);
        
        // 广播消息
        plugin.getServer().broadcastMessage("§6========================================");
        plugin.getServer().broadcastMessage("§e§l赛季重置！");
        plugin.getServer().broadcastMessage("§7新赛季: §6S" + currentSeasonId);
        plugin.getServer().broadcastMessage("§7所有玩家的段位已重置");
        plugin.getServer().broadcastMessage("§6========================================");
        
        plugin.getLogger().info("赛季重置完成");
    }
    
    /**
     * 手动开始新赛季并重置
     */
    public void startNewSeasonAndReset() {
        startNewSeason();
        resetSeason();
    }
    
    /**
     * 设置赛季持续时间（天数）
     */
    public void setSeasonDuration(int days) {
        seasonConfig.set("season-duration-days", days);
        saveSeasonConfig();
        
        // 重新计算结束时间
        if (days > 0) {
            seasonEndTime = seasonStartTime + (days * 24L * 60 * 60 * 1000);
        } else {
            seasonEndTime = 0;
        }
        
        saveSeasonConfig();
        plugin.getLogger().info("赛季持续时间已设置为: " + days + " 天");
    }
    
    /**
     * 检查并自动重置赛季
     */
    public void checkAutoReset() {
        if (isSeasonEnded()) {
            plugin.getLogger().info("赛季已结束，自动开始新赛季");
            startNewSeasonAndReset();
        }
    }
    
    /**
     * 启动赛季检查任务
     */
    public void startSeasonCheckTask() {
        // 每小时检查一次赛季是否结束
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            checkAutoReset();
        }, 20L * 60 * 60, 20L * 60 * 60); // 1小时
        
        plugin.getLogger().info("赛季检查任务已启动");
    }
}
