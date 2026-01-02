package com.minecraft.huntergame.config;

import com.minecraft.huntergame.HunterGame;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

/**
 * 计分板配置类
 * 
 * @author YourName
 * @version 1.0.0
 */
public class ScoreboardConfig {
    
    private final HunterGame plugin;
    private FileConfiguration config;
    
    public ScoreboardConfig(HunterGame plugin) {
        this.plugin = plugin;
        load();
    }
    
    /**
     * 加载配置
     */
    public void load() {
        config = plugin.getConfigManager().getScoreboardConfig();
        plugin.getLogger().info("计分板配置已加载");
    }
    
    /**
     * 重载配置
     */
    public void reload() {
        // 重新从ConfigManager获取最新的配置引用
        config = plugin.getConfigManager().getScoreboardConfig();
        plugin.getLogger().info("计分板配置已重载");
    }
    
    // ==================== 通用配置 ====================
    
    /**
     * 是否启用插件自带的计分板系统
     * 当设置为 false 时，插件不会创建任何计分板
     * 但仍然提供 PlaceholderAPI 变量供外部计分板使用
     */
    public boolean isEnabled() {
        return config.getBoolean("enabled", true);
    }
    
    /**
     * 是否启用计分板（别名方法，用于向后兼容）
     */
    public boolean isScoreboardEnabled() {
        return isEnabled();
    }
    
    /**
     * 获取更新间隔(tick)
     */
    public int getUpdateInterval() {
        return Math.max(1, config.getInt("update-interval", 20));
    }
    
    // ==================== 游戏中计分板 ====================
    
    /**
     * 获取游戏计分板标题
     */
    public String getGameTitle() {
        return ChatColor.translateAlternateColorCodes('&',
            config.getString("game.title", "&6&lMANHUNT"));
    }
    
    /**
     * 是否显示游戏状态
     */
    public boolean isShowState() {
        return config.getBoolean("game.display.show-state", true);
    }
    
    /**
     * 是否显示角色
     */
    public boolean isShowRole() {
        return config.getBoolean("game.display.show-role", true);
    }
    
    /**
     * 是否显示复活次数
     */
    public boolean isShowRespawns() {
        return config.getBoolean("game.display.show-respawns", true);
    }
    
    /**
     * 是否显示存活人数
     */
    public boolean isShowAliveCount() {
        return config.getBoolean("game.display.show-alive-count", true);
    }
    
    /**
     * 是否显示游戏时间
     */
    public boolean isShowTime() {
        return config.getBoolean("game.display.show-time", true);
    }
    
    /**
     * 获取游戏计分板行配置
     */
    public List<String> getGameLines() {
        return config.getStringList("game.lines");
    }
    
    // ==================== 大厅计分板 ====================
    
    /**
     * 是否启用大厅计分板
     */
    public boolean isLobbyEnabled() {
        return config.getBoolean("lobby.enabled", true);
    }
    
    /**
     * 获取大厅计分板标题
     */
    public String getLobbyTitle() {
        return ChatColor.translateAlternateColorCodes('&',
            config.getString("lobby.title", "&6&lMANHUNT &7- &e等待中"));
    }
    
    /**
     * 获取大厅计分板行配置
     */
    public List<String> getLobbyLines() {
        return config.getStringList("lobby.lines");
    }
    
    // ==================== 匹配计分板 ====================
    
    /**
     * 是否启用匹配计分板
     */
    public boolean isMatchingEnabled() {
        return config.getBoolean("matching.enabled", true);
    }
    
    /**
     * 获取匹配计分板标题
     */
    public String getMatchingTitle() {
        return ChatColor.translateAlternateColorCodes('&',
            config.getString("matching.title", "&6&lMANHUNT &7- &a匹配中"));
    }
    
    /**
     * 获取匹配计分板行配置
     */
    public List<String> getMatchingLines() {
        return config.getStringList("matching.lines");
    }
    
    // ==================== 状态文本 ====================
    
    /**
     * 获取状态文本
     */
    public String getStateText(String state) {
        return ChatColor.translateAlternateColorCodes('&',
            config.getString("states." + state, "&7" + state));
    }
    
    /**
     * 获取角色名称
     */
    public String getRoleName(String role) {
        return config.getString("roles." + role + ".name", role);
    }
    
    /**
     * 获取角色颜色
     */
    public String getRoleColor(String role) {
        return ChatColor.translateAlternateColorCodes('&',
            config.getString("roles." + role + ".color", "&7"));
    }
}
