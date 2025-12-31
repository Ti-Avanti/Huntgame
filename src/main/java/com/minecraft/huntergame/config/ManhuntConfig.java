package com.minecraft.huntergame.config;

import com.minecraft.huntergame.HunterGame;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Manhunt游戏配置类
 * 负责加载和管理Manhunt模式的专用配置
 * 
 * @author YourName
 * @version 1.0.0
 */
public class ManhuntConfig {
    
    private final HunterGame plugin;
    private FileConfiguration config;
    
    public ManhuntConfig(HunterGame plugin) {
        this.plugin = plugin;
        load();
    }
    
    /**
     * 加载配置
     */
    public void load() {
        config = plugin.getConfig();
        validate();
        plugin.getLogger().info("Manhunt配置已加载");
    }
    
    /**
     * 重载配置
     */
    public void reload() {
        load();
    }
    
    /**
     * 验证配置项
     */
    private void validate() {
        // 验证玩家数量配置
        int minPlayers = getMinPlayers();
        int maxPlayers = getMaxPlayers();
        int maxRunners = getMaxRunners();
        int maxHunters = getMaxHunters();
        
        if (minPlayers < 2) {
            plugin.getLogger().warning("最小玩家数不能小于2");
        }
        
        if (maxPlayers < minPlayers) {
            plugin.getLogger().warning("最大玩家数不能小于最小玩家数");
        }
        
        if (maxRunners < 1) {
            plugin.getLogger().warning("最大逃亡者数不能小于1");
        }
        
        if (maxHunters < 1) {
            plugin.getLogger().warning("最大猎人数不能小于1");
        }
        
        // 验证时间配置
        if (getPrepareTime() < 0) {
            plugin.getLogger().warning("准备时间不能为负数");
        }
        
        if (getMaxGameTime() < 0) {
            plugin.getLogger().warning("最大游戏时长不能为负数");
        }
    }
    
    // ==================== 玩家配置 ====================
    
    /**
     * 获取最小玩家数
     */
    public int getMinPlayers() {
        return Math.max(2, config.getInt("manhunt.players.min", 2));
    }
    
    /**
     * 获取最大玩家数
     */
    public int getMaxPlayers() {
        return Math.max(getMinPlayers(), config.getInt("manhunt.players.max", 10));
    }
    
    /**
     * 获取最大逃亡者数
     */
    public int getMaxRunners() {
        return Math.max(1, config.getInt("manhunt.players.max-runners", 3));
    }
    
    /**
     * 获取最大猎人数
     */
    public int getMaxHunters() {
        return Math.max(1, config.getInt("manhunt.players.max-hunters", 5));
    }
    
    // ==================== 游戏配置 ====================
    
    /**
     * 获取准备时间(秒)
     */
    public int getPrepareTime() {
        return Math.max(0, config.getInt("manhunt.game.prepare-time", 30));
    }
    
    /**
     * 获取最大游戏时长(秒，0=无限制)
     */
    public int getMaxGameTime() {
        return Math.max(0, config.getInt("manhunt.game.max-time", 3600));
    }
    
    /**
     * 获取逃亡者复活次数限制
     */
    public int getRespawnLimit() {
        return Math.max(0, config.getInt("manhunt.game.respawn-limit", 3));
    }
    
    /**
     * 获取复活延迟(秒)
     */
    public int getRespawnDelay() {
        return Math.max(0, config.getInt("manhunt.game.respawn-delay", 5));
    }
    
    /**
     * 是否允许击败凋灵获胜
     */
    public boolean isWitherVictoryAllowed() {
        return config.getBoolean("manhunt.game.allow-wither-victory", false);
    }
    
    // ==================== 追踪指南针配置 ====================
    
    /**
     * 获取指南针更新冷却时间(秒)
     */
    public int getCompassCooldown() {
        return Math.max(0, config.getInt("manhunt.tracker.compass-cooldown", 5));
    }
    
    /**
     * 是否启用跨维度追踪
     */
    public boolean isCrossDimensionTracking() {
        return config.getBoolean("manhunt.tracker.cross-dimension", true);
    }
    
    /**
     * 是否自动更新指南针
     */
    public boolean isAutoUpdateCompass() {
        return config.getBoolean("manhunt.tracker.auto-update", true);
    }
    
    /**
     * 获取自动更新间隔(秒)
     */
    public int getAutoUpdateInterval() {
        return Math.max(1, config.getInt("manhunt.tracker.auto-update-interval", 10));
    }
    
    // ==================== 世界管理配置 ====================
    
    /**
     * 是否使用独立游戏世界
     */
    public boolean useCustomWorld() {
        return config.getBoolean("manhunt.world.use-custom", false);
    }
    
    /**
     * 获取游戏世界名称
     */
    public String getWorldName() {
        return config.getString("manhunt.world.name", "manhunt_world");
    }
    
    /**
     * 获取世界边界大小(半径)
     */
    public int getWorldBorder() {
        return Math.max(0, config.getInt("manhunt.world.border", 5000));
    }
    
    /**
     * 获取世界类型
     */
    public String getWorldType() {
        return config.getString("manhunt.world.type", "DEFAULT");
    }
    
    /**
     * 游戏结束后是否重置世界
     */
    public boolean isResetWorldOnEnd() {
        return config.getBoolean("manhunt.world.reset-on-end", true);
    }
    
    // ==================== 猎人能力配置 ====================
    
    /**
     * 是否启用猎人速度提升
     */
    public boolean isHunterSpeedBoost() {
        return config.getBoolean("manhunt.hunter.abilities.speed-boost", false);
    }
    
    /**
     * 获取猎人速度等级
     */
    public int getHunterSpeedLevel() {
        return Math.max(0, config.getInt("manhunt.hunter.abilities.speed-level", 1));
    }
    
    /**
     * 是否启用猎人力量提升
     */
    public boolean isHunterStrengthBoost() {
        return config.getBoolean("manhunt.hunter.abilities.strength-boost", false);
    }
    
    /**
     * 获取猎人力量等级
     */
    public int getHunterStrengthLevel() {
        return Math.max(0, config.getInt("manhunt.hunter.abilities.strength-level", 1));
    }
    
    /**
     * 是否启用猎人生命值提升
     */
    public boolean isHunterHealthBoost() {
        return config.getBoolean("manhunt.hunter.abilities.health-boost", false);
    }
    
    /**
     * 获取猎人额外生命值
     */
    public int getHunterExtraHealth() {
        return Math.max(0, config.getInt("manhunt.hunter.abilities.extra-health", 4));
    }
    
    /**
     * 是否启用猎人夜视
     */
    public boolean isHunterNightVision() {
        return config.getBoolean("manhunt.hunter.abilities.night-vision", false);
    }
    
    // ==================== 逃亡者道具配置 ====================
    
    /**
     * 获取逃亡者初始道具配置
     */
    public int getRunnerStartItem(String itemType) {
        return Math.max(0, config.getInt("manhunt.runner.start-items." + itemType, 0));
    }
    
    /**
     * 是否启用逃亡者初始道具
     */
    public boolean isRunnerStartItemsEnabled() {
        return config.getBoolean("manhunt.runner.start-items.enabled", false);
    }
    
    // ==================== 游戏事件配置 ====================
    
    /**
     * 是否启用随机游戏事件
     */
    public boolean isRandomEventsEnabled() {
        return config.getBoolean("manhunt.events.enabled", false);
    }
    
    /**
     * 获取事件触发间隔(秒)
     */
    public int getEventTriggerInterval() {
        return Math.max(60, config.getInt("manhunt.events.trigger-interval", 300));
    }
    
    /**
     * 获取事件触发概率
     */
    public double getEventTriggerChance() {
        return Math.max(0.0, Math.min(1.0, config.getDouble("manhunt.events.trigger-chance", 0.3)));
    }
    
    // ==================== 防作弊配置 ====================
    
    /**
     * 是否禁用传送命令
     */
    public boolean isDisableTeleportCommands() {
        return config.getBoolean("manhunt.anti-cheat.disable-tp", true);
    }
    
    /**
     * 是否禁用游戏模式命令
     */
    public boolean isDisableGamemodeCommands() {
        return config.getBoolean("manhunt.anti-cheat.disable-gamemode", true);
    }
    
    /**
     * 是否禁用give命令
     */
    public boolean isDisableGiveCommands() {
        return config.getBoolean("manhunt.anti-cheat.disable-give", true);
    }
    
    /**
     * 获取禁用命令列表
     */
    public java.util.List<String> getDisabledCommands() {
        return config.getStringList("manhunt.anti-cheat.disabled-commands");
    }
}
