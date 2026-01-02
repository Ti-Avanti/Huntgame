package com.minecraft.huntergame.config;

/**
 * 服务器类型枚举
 * 用于标识当前服务器在 Bungee 网络中的角色
 * 
 * @author YourName
 * @version 1.0.0
 */
public enum ServerType {
    
    /**
     * 主大厅服务器
     * 玩家初始进入的服务器，用于匹配和分配游戏服务器
     */
    MAIN_LOBBY,
    
    /**
     * 子大厅服务器（游戏服务器）
     * 实际运行游戏的服务器
     */
    SUB_LOBBY;
    
    /**
     * 从字符串解析服务器类型
     * 
     * @param str 字符串值
     * @return 服务器类型，如果无法解析则返回 SUB_LOBBY
     */
    public static ServerType fromString(String str) {
        if (str == null || str.isEmpty()) {
            return SUB_LOBBY;
        }
        
        try {
            return valueOf(str.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return SUB_LOBBY;
        }
    }
    
    /**
     * 是否是主大厅
     */
    public boolean isMainLobby() {
        return this == MAIN_LOBBY;
    }
    
    /**
     * 是否是子大厅
     */
    public boolean isSubLobby() {
        return this == SUB_LOBBY;
    }
}
