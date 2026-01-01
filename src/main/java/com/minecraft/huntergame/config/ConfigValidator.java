package com.minecraft.huntergame.config;

import com.minecraft.huntergame.HunterGame;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * 配置验证器
 * 验证配置文件的有效性并提供修复建议
 * 
 * @author YourName
 * @version 1.0.0
 */
public class ConfigValidator {
    
    private final HunterGame plugin;
    private final List<String> warnings;
    private final List<String> errors;
    
    public ConfigValidator(HunterGame plugin) {
        this.plugin = plugin;
        this.warnings = new ArrayList<>();
        this.errors = new ArrayList<>();
    }
    
    /**
     * 验证所有配置
     */
    public boolean validateAll() {
        warnings.clear();
        errors.clear();
        
        boolean valid = true;
        
        // 验证主配置
        if (!validateMainConfig()) {
            valid = false;
        }
        
        // 验证Manhunt配置
        if (!validateManhuntConfig()) {
            valid = false;
        }
        
        // 验证计分板配置
        if (!validateScoreboardConfig()) {
            valid = false;
        }
        
        // 输出验证结果
        if (!warnings.isEmpty()) {
            plugin.getLogger().warning("配置验证发现 " + warnings.size() + " 个警告:");
            for (String warning : warnings) {
                plugin.getLogger().warning("  - " + warning);
            }
        }
        
        if (!errors.isEmpty()) {
            plugin.getLogger().severe("配置验证发现 " + errors.size() + " 个错误:");
            for (String error : errors) {
                plugin.getLogger().severe("  - " + error);
            }
        }
        
        if (valid && warnings.isEmpty()) {
            plugin.getLogger().info("配置验证通过！");
        }
        
        return valid;
    }
    
    /**
     * 验证主配置
     */
    private boolean validateMainConfig() {
        FileConfiguration config = plugin.getConfig();
        boolean valid = true;
        
        // 验证调试模式
        if (!config.isBoolean("debug")) {
            warnings.add("config.yml: 'debug' 应该是布尔值");
        }
        
        // 验证服务器模式
        String mode = config.getString("mode", "STANDALONE");
        if (!mode.equals("STANDALONE") && !mode.equals("BUNGEE")) {
            errors.add("config.yml: 'mode' 必须是 STANDALONE 或 BUNGEE");
            valid = false;
        }
        
        // 验证语言设置
        String language = config.getString("language.default", "zh_CN");
        if (!language.equals("zh_CN") && !language.equals("en_US")) {
            warnings.add("config.yml: 'language.default' 值 '" + language + "' 可能不被支持");
        }
        
        return valid;
    }
    
    /**
     * 验证Manhunt配置
     */
    private boolean validateManhuntConfig() {
        ManhuntConfig config = plugin.getManhuntConfig();
        
        // 如果配置未加载，跳过验证
        if (config == null) {
            warnings.add("manhunt.yml: 配置未加载，跳过验证");
            return true;
        }
        
        boolean valid = true;
        
        // 验证玩家数量
        if (config.getMaxRunners() < 1) {
            errors.add("manhunt.yml: 'max-runners' 必须至少为 1");
            valid = false;
        }
        
        if (config.getMaxHunters() < 1) {
            errors.add("manhunt.yml: 'max-hunters' 必须至少为 1");
            valid = false;
        }
        
        if (config.getMinPlayersToStart() < 2) {
            errors.add("manhunt.yml: 'min-players-to-start' 必须至少为 2");
            valid = false;
        }
        
        if (config.getMinPlayersToStart() > (config.getMaxRunners() + config.getMaxHunters())) {
            errors.add("manhunt.yml: 'min-players-to-start' 不能大于最大玩家数");
            valid = false;
        }
        
        // 验证时间设置
        if (config.getPrepareTime() < 5) {
            warnings.add("manhunt.yml: 'prepare-time' 小于 5 秒可能太短");
        }
        
        if (config.getPrepareTime() > 300) {
            warnings.add("manhunt.yml: 'prepare-time' 大于 300 秒可能太长");
        }
        
        if (config.getMatchingTimeout() < 30) {
            warnings.add("manhunt.yml: 'matching-timeout' 小于 30 秒可能太短");
        }
        
        // 验证复活设置
        if (config.getRespawnLimit() < 0) {
            errors.add("manhunt.yml: 'respawn-limit' 不能为负数");
            valid = false;
        }
        
        if (config.getRespawnDelay() < 0) {
            errors.add("manhunt.yml: 'respawn-delay' 不能为负数");
            valid = false;
        }
        
        return valid;
    }
    
    /**
     * 验证计分板配置
     */
    private boolean validateScoreboardConfig() {
        ScoreboardConfig config = plugin.getScoreboardConfig();
        
        // 如果配置未加载，跳过验证
        if (config == null) {
            warnings.add("scoreboard.yml: 配置未加载，跳过验证");
            return true;
        }
        
        boolean valid = true;
        
        // 验证更新间隔
        if (config.getUpdateInterval() < 1) {
            errors.add("scoreboard.yml: 'update-interval' 必须至少为 1");
            valid = false;
        }
        
        if (config.getUpdateInterval() < 10) {
            warnings.add("scoreboard.yml: 'update-interval' 小于 10 tick 可能影响性能");
        }
        
        // 验证标题长度
        if (config.getGameTitle().length() > 32) {
            warnings.add("scoreboard.yml: 'game-title' 长度超过 32 字符可能显示不全");
        }
        
        if (config.getLobbyTitle().length() > 32) {
            warnings.add("scoreboard.yml: 'lobby-title' 长度超过 32 字符可能显示不全");
        }
        
        if (config.getMatchingTitle().length() > 32) {
            warnings.add("scoreboard.yml: 'matching-title' 长度超过 32 字符可能显示不全");
        }
        
        return valid;
    }
    
    /**
     * 获取警告列表
     */
    public List<String> getWarnings() {
        return new ArrayList<>(warnings);
    }
    
    /**
     * 获取错误列表
     */
    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }
    
    /**
     * 是否有警告
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }
    
    /**
     * 是否有错误
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
