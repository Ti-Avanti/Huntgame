package com.minecraft.huntergame.config;

import com.minecraft.huntergame.HunterGame;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 配置管理器
 * 负责加载和管理所有配置文件
 * 
 * @author YourName
 * @version 1.0.0
 */
public class ConfigManager {
    
    private final HunterGame plugin;
    
    // 配置文件
    private FileConfiguration manhuntConfig;
    private FileConfiguration scoreboardConfig;
    private FileConfiguration messagesConfig;
    private FileConfiguration rewardsConfig;
    
    // 配置文件对象
    private File manhuntFile;
    private File scoreboardFile;
    private File messagesFile;
    private File rewardsFile;
    
    public ConfigManager(HunterGame plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 加载所有配置文件
     */
    public void loadAll() {
        // 确保配置文件夹存在
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        
        // 加载主配置
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        
        // 加载其他配置文件
        loadManhuntConfig();
        loadScoreboardConfig();
        loadMessagesConfig();
        loadRewardsConfig();
        
        plugin.getLogger().info("所有配置文件已加载");
        
        // 注意：配置验证将在所有配置类初始化后进行
    }
    
    /**
     * 验证所有配置
     * 必须在所有配置类初始化后调用
     */
    public void validateConfigs() {
        ConfigValidator validator = new ConfigValidator(plugin);
        boolean valid = validator.validateAll();
        
        if (!valid) {
            plugin.getLogger().warning("配置验证失败！请检查配置文件并修复错误");
        }
    }
    
    /**
     * 重载所有配置文件
     * 完全重新从磁盘读取，清除所有缓存
     */
    public void reloadAll() {
        plugin.getLogger().info("开始重载所有配置文件...");
        
        // 强制重新加载主配置
        plugin.reloadConfig();
        plugin.getLogger().info("主配置文件已重载");
        
        // 清除旧的配置对象并重新加载
        try {
            manhuntConfig = null;
            manhuntConfig = YamlConfiguration.loadConfiguration(manhuntFile);
            plugin.getLogger().info("Manhunt配置文件已重载");
            
            scoreboardConfig = null;
            scoreboardConfig = YamlConfiguration.loadConfiguration(scoreboardFile);
            plugin.getLogger().info("计分板配置文件已重载");
            
            messagesConfig = null;
            messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
            plugin.getLogger().info("消息配置文件已重载");
            
            rewardsConfig = null;
            rewardsConfig = YamlConfiguration.loadConfiguration(rewardsFile);
            plugin.getLogger().info("奖励配置文件已重载");
            
            // 加载默认配置
            loadDefaults();
            
            plugin.getLogger().info("所有配置文件重载完成！");
        } catch (Exception e) {
            plugin.getLogger().severe("重载配置文件时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 加载 Manhunt 配置
     */
    private void loadManhuntConfig() {
        manhuntFile = new File(plugin.getDataFolder(), "manhunt.yml");
        
        if (!manhuntFile.exists()) {
            plugin.saveResource("manhunt.yml", false);
        }
        
        manhuntConfig = YamlConfiguration.loadConfiguration(manhuntFile);
        
        // 加载默认配置
        InputStream defConfigStream = plugin.getResource("manhunt.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(
                new InputStreamReader(defConfigStream, StandardCharsets.UTF_8));
            manhuntConfig.setDefaults(defConfig);
        }
    }
    
    /**
     * 加载计分板配置
     */
    private void loadScoreboardConfig() {
        scoreboardFile = new File(plugin.getDataFolder(), "scoreboard.yml");
        
        if (!scoreboardFile.exists()) {
            plugin.saveResource("scoreboard.yml", false);
        }
        
        scoreboardConfig = YamlConfiguration.loadConfiguration(scoreboardFile);
        
        // 加载默认配置
        InputStream defConfigStream = plugin.getResource("scoreboard.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(
                new InputStreamReader(defConfigStream, StandardCharsets.UTF_8));
            scoreboardConfig.setDefaults(defConfig);
        }
    }
    
    /**
     * 加载消息配置
     */
    private void loadMessagesConfig() {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        
        // 加载默认配置
        InputStream defConfigStream = plugin.getResource("messages.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(
                new InputStreamReader(defConfigStream, StandardCharsets.UTF_8));
            messagesConfig.setDefaults(defConfig);
        }
    }
    
    /**
     * 加载奖励配置
     */
    private void loadRewardsConfig() {
        rewardsFile = new File(plugin.getDataFolder(), "rewards.yml");
        
        if (!rewardsFile.exists()) {
            plugin.saveResource("rewards.yml", false);
        }
        
        rewardsConfig = YamlConfiguration.loadConfiguration(rewardsFile);
        
        // 加载默认配置
        InputStream defConfigStream = plugin.getResource("rewards.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(
                new InputStreamReader(defConfigStream, StandardCharsets.UTF_8));
            rewardsConfig.setDefaults(defConfig);
        }
    }
    
    /**
     * 加载所有默认配置
     */
    private void loadDefaults() {
        InputStream manhuntStream = plugin.getResource("manhunt.yml");
        if (manhuntStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(
                new InputStreamReader(manhuntStream, StandardCharsets.UTF_8));
            manhuntConfig.setDefaults(defConfig);
        }
        
        InputStream scoreboardStream = plugin.getResource("scoreboard.yml");
        if (scoreboardStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(
                new InputStreamReader(scoreboardStream, StandardCharsets.UTF_8));
            scoreboardConfig.setDefaults(defConfig);
        }
        
        InputStream messagesStream = plugin.getResource("messages.yml");
        if (messagesStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(
                new InputStreamReader(messagesStream, StandardCharsets.UTF_8));
            messagesConfig.setDefaults(defConfig);
        }
        
        InputStream rewardsStream = plugin.getResource("rewards.yml");
        if (rewardsStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(
                new InputStreamReader(rewardsStream, StandardCharsets.UTF_8));
            rewardsConfig.setDefaults(defConfig);
        }
    }
    
    /**
     * 保存配置文件
     */
    public void saveManhuntConfig() {
        try {
            manhuntConfig.save(manhuntFile);
        } catch (IOException e) {
            plugin.getLogger().severe("无法保存 manhunt.yml: " + e.getMessage());
        }
    }
    
    public void saveScoreboardConfig() {
        try {
            scoreboardConfig.save(scoreboardFile);
        } catch (IOException e) {
            plugin.getLogger().severe("无法保存 scoreboard.yml: " + e.getMessage());
        }
    }
    
    public void saveMessagesConfig() {
        try {
            messagesConfig.save(messagesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("无法保存 messages.yml: " + e.getMessage());
        }
    }
    
    public void saveRewardsConfig() {
        try {
            rewardsConfig.save(rewardsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("无法保存 rewards.yml: " + e.getMessage());
        }
    }
    
    // ==================== Getter方法 ====================
    
    public FileConfiguration getManhuntConfig() {
        return manhuntConfig;
    }
    
    public FileConfiguration getScoreboardConfig() {
        return scoreboardConfig;
    }
    
    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }
    
    public FileConfiguration getRewardsConfig() {
        return rewardsConfig;
    }
}
