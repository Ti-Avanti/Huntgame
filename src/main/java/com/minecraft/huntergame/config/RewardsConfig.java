package com.minecraft.huntergame.config;

import com.minecraft.huntergame.HunterGame;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Map;

/**
 * 奖励配置类
 * 
 * @author YourName
 * @version 1.0.0
 */
public class RewardsConfig {
    
    private final HunterGame plugin;
    private FileConfiguration config;
    
    public RewardsConfig(HunterGame plugin) {
        this.plugin = plugin;
        load();
    }
    
    /**
     * 加载配置
     */
    public void load() {
        config = plugin.getConfigManager().getRewardsConfig();
        plugin.getLogger().info("奖励配置已加载");
    }
    
    /**
     * 重载配置
     */
    public void reload() {
        // 重新从ConfigManager获取最新的配置引用
        config = plugin.getConfigManager().getRewardsConfig();
        plugin.getLogger().info("奖励配置已重载");
    }
    
    // ==================== 通用配置 ====================
    
    /**
     * 是否启用奖励系统
     */
    public boolean isEnabled() {
        return config.getBoolean("enabled", true);
    }
    
    // ==================== 金币奖励 ====================
    
    /**
     * 获取逃亡者胜利奖励
     */
    public double getRunnerWinMoney() {
        return config.getDouble("money.runner-win", 100.0);
    }
    
    /**
     * 获取猎人胜利奖励
     */
    public double getHunterWinMoney() {
        return config.getDouble("money.hunter-win", 100.0);
    }
    
    /**
     * 获取参与奖励
     */
    public double getParticipationMoney() {
        return config.getDouble("money.participation", 20.0);
    }
    
    /**
     * 获取击杀奖励
     */
    public double getKillMoney() {
        return config.getDouble("money.kill", 10.0);
    }
    
    /**
     * 获取击败末影龙奖励
     */
    public double getDragonKillMoney() {
        return config.getDouble("money.dragon-kill", 50.0);
    }
    
    /**
     * 获取每分钟游戏时间奖励
     */
    public double getPerMinuteMoney() {
        return config.getDouble("money.per-minute", 1.0);
    }
    
    // ==================== 经验奖励 ====================
    
    /**
     * 是否启用经验奖励
     */
    public boolean isExperienceEnabled() {
        return config.getBoolean("experience.enabled", true);
    }
    
    /**
     * 获取逃亡者胜利经验
     */
    public int getRunnerWinExp() {
        return config.getInt("experience.runner-win", 100);
    }
    
    /**
     * 获取猎人胜利经验
     */
    public int getHunterWinExp() {
        return config.getInt("experience.hunter-win", 100);
    }
    
    /**
     * 获取参与经验
     */
    public int getParticipationExp() {
        return config.getInt("experience.participation", 20);
    }
    
    /**
     * 获取击杀经验
     */
    public int getKillExp() {
        return config.getInt("experience.kill", 10);
    }
    
    /**
     * 获取击败末影龙经验
     */
    public int getDragonKillExp() {
        return config.getInt("experience.dragon-kill", 200);
    }
    
    // ==================== 物品奖励 ====================
    
    /**
     * 是否启用物品奖励
     */
    public boolean isItemsEnabled() {
        return config.getBoolean("items.enabled", false);
    }
    
    /**
     * 获取胜利奖励物品
     */
    public List<String> getWinItems() {
        return config.getStringList("items.win");
    }
    
    /**
     * 获取击败末影龙奖励物品
     */
    public List<String> getDragonKillItems() {
        return config.getStringList("items.dragon-kill");
    }
    
    // ==================== 称号奖励 ====================
    
    /**
     * 是否启用称号奖励
     */
    public boolean isTitlesEnabled() {
        return config.getBoolean("titles.enabled", false);
    }
    
    /**
     * 获取称号
     */
    public String getTitle(String key) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&',
            config.getString("titles." + key, ""));
    }
    
    // ==================== 连胜奖励 ====================
    
    /**
     * 是否启用连胜奖励
     */
    public boolean isStreakEnabled() {
        return config.getBoolean("streak.enabled", true);
    }
    
    /**
     * 获取连胜倍率
     */
    public double getStreakMultiplier(int streak) {
        return config.getDouble("streak.multipliers." + streak, 1.0);
    }
    
    /**
     * 是否启用连胜广播
     */
    public boolean isStreakBroadcastEnabled() {
        return config.getBoolean("streak.broadcast.enabled", true);
    }
    
    /**
     * 获取连胜广播消息
     */
    public String getStreakBroadcastMessage(int streak) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&',
            config.getString("streak.broadcast.messages." + streak, ""));
    }
    
    // ==================== 成就奖励 ====================
    
    /**
     * 是否启用成就奖励
     */
    public boolean isAchievementsEnabled() {
        return config.getBoolean("achievements.enabled", false);
    }
    
    // ==================== 排行榜奖励 ====================
    
    /**
     * 是否启用排行榜奖励
     */
    public boolean isLeaderboardEnabled() {
        return config.getBoolean("leaderboard.enabled", false);
    }
    
    /**
     * 是否启用每日排行榜
     */
    public boolean isDailyLeaderboardEnabled() {
        return config.getBoolean("leaderboard.daily.enabled", true);
    }
    
    /**
     * 获取每日排行榜奖励
     */
    public double getDailyLeaderboardReward(int rank) {
        String key = rank == 1 ? "first" : rank == 2 ? "second" : "third";
        return config.getDouble("leaderboard.daily." + key, 0.0);
    }
}
