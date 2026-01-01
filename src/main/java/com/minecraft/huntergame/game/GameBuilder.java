package com.minecraft.huntergame.game;

import com.minecraft.huntergame.HunterGame;

/**
 * 游戏构建器
 * 使用Builder模式创建ManhuntGame实例
 * 
 * @author YourName
 * @version 1.0.0
 */
public class GameBuilder {
    
    private final HunterGame plugin;
    private final String gameId;
    private final String worldName;
    
    // 可配置参数
    private int maxRunners;
    private int maxHunters;
    private int respawnLimit;
    private int prepareTime;
    private int maxGameTime;
    private int matchingTimeout;
    private int minPlayersToStart;
    
    /**
     * 构造函数
     */
    public GameBuilder(HunterGame plugin, String gameId, String worldName) {
        this.plugin = plugin;
        this.gameId = gameId;
        this.worldName = worldName;
        
        // 从配置加载默认值
        loadDefaults();
    }
    
    /**
     * 从配置加载默认值
     */
    private void loadDefaults() {
        this.maxRunners = plugin.getManhuntConfig().getMaxRunners();
        this.maxHunters = plugin.getManhuntConfig().getMaxHunters();
        this.respawnLimit = plugin.getManhuntConfig().getRespawnLimit();
        this.prepareTime = plugin.getManhuntConfig().getPrepareTime();
        this.maxGameTime = plugin.getManhuntConfig().getMaxGameTime();
        this.matchingTimeout = plugin.getManhuntConfig().getMatchingTimeout();
        this.minPlayersToStart = plugin.getManhuntConfig().getMinPlayersToStart();
    }
    
    /**
     * 设置最大逃亡者数量
     */
    public GameBuilder maxRunners(int maxRunners) {
        this.maxRunners = Math.max(1, Math.min(50, maxRunners));
        return this;
    }
    
    /**
     * 设置最大猎人数量
     */
    public GameBuilder maxHunters(int maxHunters) {
        this.maxHunters = Math.max(1, Math.min(50, maxHunters));
        return this;
    }
    
    /**
     * 设置复活次数限制
     */
    public GameBuilder respawnLimit(int respawnLimit) {
        this.respawnLimit = Math.max(0, Math.min(10, respawnLimit));
        return this;
    }
    
    /**
     * 设置准备时间
     */
    public GameBuilder prepareTime(int prepareTime) {
        this.prepareTime = Math.max(0, Math.min(600, prepareTime));
        return this;
    }
    
    /**
     * 设置最大游戏时长
     */
    public GameBuilder maxGameTime(int maxGameTime) {
        this.maxGameTime = Math.max(0, Math.min(7200, maxGameTime));
        return this;
    }
    
    /**
     * 设置匹配超时时间
     */
    public GameBuilder matchingTimeout(int matchingTimeout) {
        this.matchingTimeout = Math.max(10, Math.min(300, matchingTimeout));
        return this;
    }
    
    /**
     * 设置最小开始人数
     */
    public GameBuilder minPlayersToStart(int minPlayersToStart) {
        this.minPlayersToStart = Math.max(2, Math.min(maxRunners + maxHunters, minPlayersToStart));
        return this;
    }
    
    /**
     * 验证配置
     */
    private void validate() {
        if (maxRunners < 1) {
            throw new IllegalStateException("逃亡者数量必须至少为1");
        }
        
        if (maxHunters < 1) {
            throw new IllegalStateException("猎人数量必须至少为1");
        }
        
        if (minPlayersToStart < 2) {
            throw new IllegalStateException("最小开始人数必须至少为2");
        }
        
        if (minPlayersToStart > maxRunners + maxHunters) {
            throw new IllegalStateException("最小开始人数不能超过最大玩家数");
        }
    }
    
    /**
     * 构建游戏实例
     */
    public ManhuntGame build() {
        validate();
        return new ManhuntGame(
            plugin,
            gameId,
            worldName,
            maxRunners,
            maxHunters,
            respawnLimit,
            prepareTime,
            maxGameTime,
            matchingTimeout,
            minPlayersToStart
        );
    }
    
    // ==================== Getter方法 ====================
    
    public int getMaxRunners() {
        return maxRunners;
    }
    
    public int getMaxHunters() {
        return maxHunters;
    }
    
    public int getRespawnLimit() {
        return respawnLimit;
    }
    
    public int getPrepareTime() {
        return prepareTime;
    }
    
    public int getMaxGameTime() {
        return maxGameTime;
    }
    
    public int getMatchingTimeout() {
        return matchingTimeout;
    }
    
    public int getMinPlayersToStart() {
        return minPlayersToStart;
    }
}
