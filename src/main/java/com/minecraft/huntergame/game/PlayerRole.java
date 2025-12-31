package com.minecraft.huntergame.game;

import org.bukkit.ChatColor;

/**
 * 玩家角色枚举
 * 
 * @author YourName
 * @version 1.0.0
 */
public enum PlayerRole {
    
    /**
     * 猎人 - 追捕逃生者
     */
    HUNTER("猎人", "Hunter", ChatColor.RED),
    
    /**
     * 逃生者 - 躲避猎人并逃脱
     */
    SURVIVOR("逃生者", "Survivor", ChatColor.GREEN),
    
    /**
     * 观战者 - 观看游戏
     */
    SPECTATOR("观战者", "Spectator", ChatColor.GRAY);
    
    private final String displayNameZh;
    private final String displayNameEn;
    private final ChatColor color;
    
    PlayerRole(String displayNameZh, String displayNameEn, ChatColor color) {
        this.displayNameZh = displayNameZh;
        this.displayNameEn = displayNameEn;
        this.color = color;
    }
    
    /**
     * 获取中文显示名称
     */
    public String getDisplayNameZh() {
        return displayNameZh;
    }
    
    /**
     * 获取英文显示名称
     */
    public String getDisplayNameEn() {
        return displayNameEn;
    }
    
    /**
     * 获取角色颜色
     */
    public ChatColor getColor() {
        return color;
    }
    
    /**
     * 获取带颜色的中文显示名称
     */
    public String getColoredNameZh() {
        return color + displayNameZh;
    }
    
    /**
     * 获取带颜色的英文显示名称
     */
    public String getColoredNameEn() {
        return color + displayNameEn;
    }
    
    /**
     * 是否是猎人
     */
    public boolean isHunter() {
        return this == HUNTER;
    }
    
    /**
     * 是否是逃生者
     */
    public boolean isSurvivor() {
        return this == SURVIVOR;
    }
    
    /**
     * 是否是观战者
     */
    public boolean isSpectator() {
        return this == SPECTATOR;
    }
    
    /**
     * 是否是游戏中的角色（猎人或逃生者）
     */
    public boolean isPlaying() {
        return this == HUNTER || this == SURVIVOR;
    }
}
