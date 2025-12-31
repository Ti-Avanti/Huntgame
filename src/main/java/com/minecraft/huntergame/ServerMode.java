package com.minecraft.huntergame;

/**
 * 服务器运行模式枚举
 * 
 * @author YourName
 * @version 1.0.0
 */
public enum ServerMode {
    
    /**
     * 单服务器模式
     * - 单服务器运行
     * - 支持Manhunt游戏模式
     * - 玩家通过命令加入游戏
     */
    STANDALONE("单服务器模式"),
    
    /**
     * Bungee分布式模式
     * - 多服务器分布式架构
     * - 每个游戏服务器运行独立竞技场
     * - 通过BungeeCord代理连接
     * - 使用Redis同步服务器状态
     */
    BUNGEE("Bungee分布式模式");
    
    private final String displayName;
    
    ServerMode(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * 获取显示名称
     * 
     * @return 显示名称
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * 是否为Bungee模式
     * 
     * @return 是否为Bungee模式
     */
    public boolean isBungeeMode() {
        return this == BUNGEE;
    }
    
    /**
     * 是否为单服务器模式
     * 
     * @return 是否为单服务器模式
     */
    public boolean isStandaloneMode() {
        return this == STANDALONE;
    }
}
