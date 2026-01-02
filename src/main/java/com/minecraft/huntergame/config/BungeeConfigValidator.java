package com.minecraft.huntergame.config;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.ServerMode;

/**
 * Bungee 配置验证器
 * 负责验证 Bungee 模式配置的完整性和有效性
 * 
 * @author YourName
 * @version 1.0.0
 */
public class BungeeConfigValidator {
    
    private final HunterGame plugin;
    private boolean hasErrors = false;
    private boolean hasWarnings = false;
    
    public BungeeConfigValidator(HunterGame plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 验证 Bungee 配置
     * 
     * @return 是否通过验证（有错误返回 false）
     */
    public boolean validate() {
        plugin.getLogger().info("开始验证 Bungee 配置...");
        
        // 只在 Bungee 模式下验证
        if (plugin.getServerMode() != ServerMode.BUNGEE) {
            plugin.getLogger().info("当前为单服务器模式，跳过 Bungee 配置验证");
            return true;
        }
        
        // 验证 Bungee 配置
        validateBungeeConfig();
        
        // 验证 Redis 配置
        validateRedisConfig();
        
        // 验证服务器名称格式
        validateServerName();
        
        // 输出验证结果
        if (hasErrors) {
            plugin.getLogger().severe("Bungee 配置验证失败！请检查配置文件");
            return false;
        }
        
        if (hasWarnings) {
            plugin.getLogger().warning("Bungee 配置验证完成，但存在警告");
        } else {
            plugin.getLogger().info("Bungee 配置验证通过");
        }
        
        return true;
    }
    
    /**
     * 验证 Bungee 基础配置
     */
    private void validateBungeeConfig() {
        // 检查主大厅配置
        String mainLobby = plugin.getManhuntConfig().getMainLobby();
        if (mainLobby == null || mainLobby.trim().isEmpty()) {
            logError("主大厅服务器名称未配置 (bungee.main-lobby)");
        }
        
        // 检查子大厅前缀
        String subLobbyPrefix = plugin.getManhuntConfig().getSubLobbyPrefix();
        if (subLobbyPrefix == null || subLobbyPrefix.trim().isEmpty()) {
            logWarning("子大厅服务器前缀未配置 (bungee.sub-lobby-prefix)，使用默认值: game-");
        }
        
        // 检查服务器类型
        ServerType serverType = plugin.getManhuntConfig().getServerType();
        if (serverType == null) {
            logError("服务器类型未配置或无效 (bungee.server-type)");
        } else {
            plugin.getLogger().info("服务器类型: " + serverType);
        }
        
        // 检查返回延迟
        int returnDelay = plugin.getManhuntConfig().getReturnDelay();
        if (returnDelay < 0) {
            logWarning("返回延迟时间无效 (bungee.return-delay)，使用默认值: 5");
        }
    }
    
    /**
     * 验证 Redis 配置
     */
    private void validateRedisConfig() {
        // 检查 Redis 是否启用
        boolean redisEnabled = plugin.getMainConfig().isRedisEnabled();
        if (!redisEnabled) {
            logWarning("Bungee 模式已启用但 Redis 未启用，跨服务器功能将不可用");
            return;
        }
        
        // 检查 Redis 主机
        String redisHost = plugin.getMainConfig().getRedisHost();
        if (redisHost == null || redisHost.trim().isEmpty()) {
            logError("Redis 主机地址未配置 (redis.host)");
        }
        
        // 检查 Redis 端口
        int redisPort = plugin.getMainConfig().getRedisPort();
        if (redisPort <= 0 || redisPort > 65535) {
            logError("Redis 端口无效 (redis.port): " + redisPort);
        }
        
        // 检查服务器名称
        String serverName = plugin.getMainConfig().getRedisServerName();
        if (serverName == null || serverName.trim().isEmpty()) {
            logError("Redis 服务器名称未配置 (redis.server-name)");
        }
        
        // 检查更新间隔
        int updateInterval = plugin.getMainConfig().getRedisUpdateInterval();
        if (updateInterval < 5) {
            logWarning("Redis 更新间隔过短 (redis.update-interval): " + updateInterval + "秒，建议至少 5 秒");
        }
    }
    
    /**
     * 验证服务器名称格式
     */
    private void validateServerName() {
        String serverName = plugin.getMainConfig().getRedisServerName();
        if (serverName == null || serverName.trim().isEmpty()) {
            return; // 已在 Redis 配置验证中报错
        }
        
        // 检查服务器名称格式（只允许字母、数字、连字符、下划线）
        if (!serverName.matches("^[a-zA-Z0-9_-]+$")) {
            logWarning("服务器名称包含特殊字符 (redis.server-name): " + serverName + "，建议只使用字母、数字、连字符和下划线");
        }
        
        // 检查服务器名称长度
        if (serverName.length() > 32) {
            logWarning("服务器名称过长 (redis.server-name): " + serverName.length() + " 字符，建议不超过 32 字符");
        }
    }
    
    /**
     * 记录错误
     */
    private void logError(String message) {
        plugin.getLogger().severe("[配置验证] " + message);
        hasErrors = true;
    }
    
    /**
     * 记录警告
     */
    private void logWarning(String message) {
        plugin.getLogger().warning("[配置验证] " + message);
        hasWarnings = true;
    }
    
    /**
     * 是否有错误
     */
    public boolean hasErrors() {
        return hasErrors;
    }
    
    /**
     * 是否有警告
     */
    public boolean hasWarnings() {
        return hasWarnings;
    }
}
