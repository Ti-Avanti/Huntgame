package com.minecraft.huntergame.config;

import com.minecraft.huntergame.HunterGame;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * 主配置管理类
 * 负责加载和管理config.yml中的配置项
 * 
 * @author YourName
 * @version 1.0.0
 */
public class MainConfig {
    
    private final HunterGame plugin;
    private FileConfiguration config;
    
    public MainConfig(HunterGame plugin) {
        this.plugin = plugin;
        load();
    }
    
    /**
     * 加载配置文件
     */
    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
        
        // 验证配置
        validate();
        
        plugin.getLogger().info("主配置已加载");
    }
    
    /**
     * 重载配置文件
     */
    public void reload() {
        // 重新加载配置文件
        plugin.reloadConfig();
        // 重新获取配置引用（这是关键！）
        config = plugin.getConfig();
        
        // 重新验证配置
        validate();
        
        plugin.getLogger().info("主配置已重载");
    }
    
    /**
     * 验证配置项
     */
    private void validate() {
        // 验证必要的配置项
        if (!config.contains("mode")) {
            plugin.getLogger().warning("配置项 'mode' 未找到，使用默认值: STANDALONE");
        }
        
        if (!config.contains("database.type")) {
            plugin.getLogger().warning("配置项 'database.type' 未找到，使用默认值: SQLITE");
        }
        
        if (!config.contains("language.default")) {
            plugin.getLogger().warning("配置项 'language.default' 未找到，使用默认值: zh_CN");
        }
    }
    
    // ==================== 游戏配置 ====================
    
    /**
     * 获取游戏时长(秒)
     */
    public int getGameDuration() {
        return config.getInt("game.duration", 600);
    }
    
    /**
     * 获取等待时间(秒)
     */
    public int getWaitingTime() {
        return config.getInt("game.waiting-time", 30);
    }
    
    /**
     * 获取准备时间(秒)
     */
    public int getPreparationTime() {
        return config.getInt("game.preparation-time", 10);
    }
    
    /**
     * 获取重启延迟(秒)
     */
    public int getRestartDelay() {
        return config.getInt("game.restart-delay", 10);
    }
    
    /**
     * 获取默认游戏模式
     */
    public String getDefaultGameMode() {
        return config.getString("game.default-mode", "CLASSIC");
    }
    
    // ==================== 猎人配置 ====================
    
    /**
     * 获取经典模式猎人数量
     */
    public int getClassicHunterCount() {
        return config.getInt("hunter.classic-mode-count", 1);
    }
    
    /**
     * 获取团队模式猎人比例
     */
    public double getTeamHunterRatio() {
        return config.getDouble("hunter.team-mode-ratio", 0.3);
    }
    
    /**
     * 获取猎人准备时间(秒)
     */
    public int getHunterPreparationTime() {
        return config.getInt("hunter.preparation-time", 30);
    }
    
    // ==================== 能力配置 ====================
    
    /**
     * 速度提升能力是否启用
     */
    public boolean isSpeedBoostEnabled() {
        return config.getBoolean("abilities.speed-boost.enabled", true);
    }
    
    /**
     * 获取速度提升持续时间(秒)
     */
    public int getSpeedBoostDuration() {
        return config.getInt("abilities.speed-boost.duration", 10);
    }
    
    /**
     * 获取速度提升冷却时间(秒)
     */
    public int getSpeedBoostCooldown() {
        return config.getInt("abilities.speed-boost.cooldown", 60);
    }
    
    /**
     * 追踪能力是否启用
     */
    public boolean isTrackerEnabled() {
        return config.getBoolean("abilities.tracker.enabled", true);
    }
    
    /**
     * 获取追踪冷却时间(秒)
     */
    public int getTrackerCooldown() {
        return config.getInt("abilities.tracker.cooldown", 30);
    }
    
    /**
     * 夜视能力是否启用
     */
    public boolean isNightVisionEnabled() {
        return config.getBoolean("abilities.night-vision.enabled", true);
    }
    
    /**
     * 获取夜视持续时间(秒)
     */
    public int getNightVisionDuration() {
        return config.getInt("abilities.night-vision.duration", 30);
    }
    
    /**
     * 获取夜视冷却时间(秒)
     */
    public int getNightVisionCooldown() {
        return config.getInt("abilities.night-vision.cooldown", 90);
    }
    
    // ==================== 道具配置 ====================
    
    /**
     * 获取隐身药水数量
     */
    public int getInvisibilityPotionCount() {
        return config.getInt("items.invisibility-potion.count", 2);
    }
    
    /**
     * 获取隐身药水持续时间(秒)
     */
    public int getInvisibilityPotionDuration() {
        return config.getInt("items.invisibility-potion.duration", 10);
    }
    
    /**
     * 获取速度药水数量
     */
    public int getSpeedPotionCount() {
        return config.getInt("items.speed-potion.count", 2);
    }
    
    /**
     * 获取速度药水持续时间(秒)
     */
    public int getSpeedPotionDuration() {
        return config.getInt("items.speed-potion.duration", 10);
    }
    
    /**
     * 获取烟雾弹数量
     */
    public int getSmokeBombCount() {
        return config.getInt("items.smoke-bomb.count", 1);
    }
    
    /**
     * 获取烟雾弹持续时间(秒)
     */
    public int getSmokeBombDuration() {
        return config.getInt("items.smoke-bomb.duration", 15);
    }
    
    /**
     * 获取烟雾弹范围
     */
    public int getSmokeBombRadius() {
        return config.getInt("items.smoke-bomb.radius", 5);
    }
    
    /**
     * 获取诱饵数量
     */
    public int getDecoyCount() {
        return config.getInt("items.decoy.count", 1);
    }
    
    /**
     * 获取诱饵持续时间(秒)
     */
    public int getDecoyDuration() {
        return config.getInt("items.decoy.duration", 30);
    }
    
    // ==================== 逃脱配置 ====================
    
    /**
     * 获取逃脱所需时间(秒)
     */
    public int getEscapeTime() {
        return config.getInt("escape.time-required", 10);
    }
    
    /**
     * 获取逃脱点范围
     */
    public double getEscapeRadius() {
        return config.getDouble("escape.radius", 3.0);
    }
    
    // ==================== 奖励配置 ====================
    
    /**
     * 获取逃生者胜利奖励
     */
    public double getSurvivorWinReward() {
        return config.getDouble("rewards.survivor-win", 100.0);
    }
    
    /**
     * 获取猎人胜利奖励
     */
    public double getHunterWinReward() {
        return config.getDouble("rewards.hunter-win", 150.0);
    }
    
    /**
     * 获取击杀奖励
     */
    public double getKillReward() {
        return config.getDouble("rewards.kill", 50.0);
    }
    
    /**
     * 获取逃脱奖励
     */
    public double getEscapeReward() {
        return config.getDouble("rewards.escape", 200.0);
    }
    
    /**
     * 获取胜利奖励（通用）
     */
    public double getWinReward() {
        return config.getDouble("rewards.win", 100.0);
    }
    
    /**
     * 获取失败奖励（通用）
     */
    public double getLoseReward() {
        return config.getDouble("rewards.lose", 20.0);
    }
    
    // ==================== 数据库配置 ====================
    
    /**
     * 获取数据库类型
     */
    public String getDatabaseType() {
        return config.getString("database.type", "sqlite");
    }
    
    /**
     * 获取数据库主机
     */
    public String getDatabaseHost() {
        return config.getString("database.mysql.host", "localhost");
    }
    
    /**
     * 获取数据库端口
     */
    public int getDatabasePort() {
        return config.getInt("database.mysql.port", 3306);
    }
    
    /**
     * 获取数据库名称
     */
    public String getDatabaseName() {
        return config.getString("database.mysql.database", "huntergame");
    }
    
    /**
     * 获取数据库用户名
     */
    public String getDatabaseUsername() {
        return config.getString("database.mysql.username", "root");
    }
    
    /**
     * 获取数据库密码
     */
    public String getDatabasePassword() {
        return config.getString("database.mysql.password", "");
    }
    
    /**
     * 获取连接池最大连接数
     */
    public int getDatabaseMaxPoolSize() {
        return config.getInt("database.pool.maximum-pool-size", 10);
    }
    
    /**
     * 获取连接池最小空闲连接数
     */
    public int getDatabaseMinIdle() {
        return config.getInt("database.pool.minimum-idle", 2);
    }
    
    /**
     * 获取连接超时时间(毫秒)
     */
    public long getDatabaseConnectionTimeout() {
        return config.getLong("database.pool.connection-timeout", 30000);
    }
    
    // ==================== Bungee配置 ====================
    
    /**
     * Bungee模式是否启用
     */
    public boolean isBungeeModeEnabled() {
        return config.getBoolean("bungee.enabled", false);
    }
    
    /**
     * 获取服务器名称
     */
    public String getServerName() {
        return config.getString("bungee.server-name", "hunter-1");
    }
    
    /**
     * 获取服务器组
     */
    public String getServerGroup() {
        return config.getString("bungee.server-group", "hunter");
    }
    
    /**
     * 获取大厅服务器名称
     */
    public String getLobbyServer() {
        return config.getString("bungee.lobby-server", "lobby");
    }
    
    /**
     * 游戏结束后是否自动传送到大厅
     */
    public boolean isAutoSendToLobby() {
        return config.getBoolean("bungee.auto-send-to-lobby", true);
    }
    
    /**
     * 是否自动传送（别名方法）
     */
    public boolean isAutoSend() {
        return isAutoSendToLobby();
    }
    
    /**
     * 获取传送延迟(秒)
     */
    public int getSendDelay() {
        return config.getInt("bungee.send-delay", 5);
    }
    
    // ==================== Redis配置 ====================
    
    /**
     * Redis是否启用
     */
    public boolean isRedisEnabled() {
        return config.getBoolean("redis.enabled", false);
    }
    
    /**
     * 获取Redis主机
     */
    public String getRedisHost() {
        return config.getString("redis.host", "localhost");
    }
    
    /**
     * 获取Redis端口
     */
    public int getRedisPort() {
        return config.getInt("redis.port", 6379);
    }
    
    /**
     * 获取Redis密码
     */
    public String getRedisPassword() {
        String password = config.getString("redis.password", "");
        return password.isEmpty() ? null : password;
    }
    
    /**
     * 获取Redis数据库索引
     */
    public int getRedisDatabase() {
        return config.getInt("redis.database", 0);
    }
    
    /**
     * 获取Redis超时时间(毫秒)
     */
    public int getRedisTimeout() {
        return config.getInt("redis.timeout", 2000);
    }
    
    /**
     * 获取Redis连接池最大连接数
     */
    public int getRedisMaxTotal() {
        return config.getInt("redis.pool.max-total", 8);
    }
    
    /**
     * 获取Redis连接池最大空闲连接数
     */
    public int getRedisMaxIdle() {
        return config.getInt("redis.pool.max-idle", 8);
    }
    
    /**
     * 获取Redis连接池最小空闲连接数
     */
    public int getRedisMinIdle() {
        return config.getInt("redis.pool.min-idle", 0);
    }
    
    /**
     * 获取Redis服务器名称
     */
    public String getRedisServerName() {
        return config.getString("redis.server-name", "game-1");
    }
    
    /**
     * 获取Redis状态更新间隔(秒)
     */
    public int getRedisUpdateInterval() {
        return Math.max(5, config.getInt("redis.update-interval", 10));
    }
    
    /**
     * 获取状态同步间隔(秒)
     */
    public int getStatusSyncInterval() {
        return config.getInt("redis.status-sync-interval", 5);
    }
    
    // ==================== 队伍配置 ====================
    
    /**
     * 获取队伍最大成员数
     */
    public int getMaxPartySize() {
        return config.getInt("party.max-size", 4);
    }
    
    /**
     * 是否启用队伍系统
     */
    public boolean isPartyEnabled() {
        return config.getBoolean("party.enabled", true);
    }
    
    /**
     * 队伍成员是否分配到同一阵营
     */
    public boolean isPartySameFaction() {
        return config.getBoolean("party.same-faction", true);
    }
    
    // ==================== 游戏事件配置 ====================
    
    /**
     * 是否启用游戏事件系统
     */
    public boolean isGameEventsEnabled() {
        return config.getBoolean("events.enabled", true);
    }
    
    /**
     * 事件检测间隔(秒)
     */
    public int getEventCheckInterval() {
        return config.getInt("events.check-interval", 60);
    }
    
    /**
     * 是否启用箱子刷新事件
     */
    public boolean isChestRefillEnabled() {
        return config.getBoolean("events.chest-refill.enabled", true);
    }
    
    /**
     * 是否启用道具掉落事件
     */
    public boolean isItemDropEnabled() {
        return config.getBoolean("events.item-drop.enabled", true);
    }
    
    /**
     * 是否启用天气变化事件
     */
    public boolean isWeatherChangeEnabled() {
        return config.getBoolean("events.weather-change.enabled", true);
    }
}
