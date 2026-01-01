package com.minecraft.huntergame.game;

/**
 * 游戏状态枚举
 * 
 * @author YourName
 * @version 1.0.0
 */
public enum GameState {
    
    /**
     * 等待玩家加入
     */
    WAITING("等待中", "Waiting"),
    
    /**
     * 匹配中 - 等待更多玩家
     */
    MATCHING("匹配中", "Matching"),
    
    /**
     * 游戏开始倒计时
     */
    STARTING("开始倒计时", "Starting"),
    
    /**
     * 准备阶段 - 逃亡者准备，猎人冻结
     */
    PREPARING("准备中", "Preparing"),
    
    /**
     * 游戏进行中
     */
    PLAYING("游戏中", "Playing"),
    
    /**
     * 游戏结束
     */
    ENDING("结束中", "Ending"),
    
    /**
     * 竞技场重启中
     */
    RESTARTING("重启中", "Restarting"),
    
    /**
     * 竞技场已禁用
     */
    DISABLED("已禁用", "Disabled");
    
    private final String displayNameZh;
    private final String displayNameEn;
    
    GameState(String displayNameZh, String displayNameEn) {
        this.displayNameZh = displayNameZh;
        this.displayNameEn = displayNameEn;
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
     * 是否可以加入
     */
    public boolean isJoinable() {
        return this == WAITING || this == MATCHING || this == STARTING;
    }
    
    /**
     * 是否正在游戏中
     */
    public boolean isPlaying() {
        return this == PLAYING;
    }
    
    /**
     * 是否正在运行（包括开始倒计时、准备阶段和游戏中）
     */
    public boolean isRunning() {
        return this == STARTING || this == PREPARING || this == PLAYING;
    }
    
    /**
     * 是否可以重置
     */
    public boolean isResettable() {
        return this == ENDING || this == RESTARTING;
    }
    
    /**
     * 是否已结束
     */
    public boolean isEnded() {
        return this == ENDING || this == RESTARTING;
    }
    
    /**
     * 是否可用
     */
    public boolean isAvailable() {
        return this != DISABLED;
    }
    
    /**
     * 检查是否可以转换到目标状态
     * 
     * @param target 目标状态
     * @return 是否可以转换
     */
    public boolean canTransitionTo(GameState target) {
        // 禁用状态不能转换到其他状态
        if (this == DISABLED) {
            return false;
        }
        
        // 任何状态都可以转换到禁用状态
        if (target == DISABLED) {
            return true;
        }
        
        // 定义合法的状态转换
        switch (this) {
            case WAITING:
                return target == MATCHING || target == STARTING || target == RESTARTING;
            case MATCHING:
                return target == STARTING || target == PREPARING || target == WAITING || target == RESTARTING;
            case STARTING:
                return target == PREPARING || target == WAITING || target == RESTARTING;
            case PREPARING:
                return target == PLAYING || target == WAITING || target == RESTARTING;
            case PLAYING:
                return target == ENDING;
            case ENDING:
                return target == RESTARTING;
            case RESTARTING:
                return target == WAITING;
            default:
                return false;
        }
    }
    
    /**
     * 从字符串解析游戏状态
     * 
     * @param name 状态名称
     * @return 游戏状态，如果无效则返回null
     */
    public static GameState fromString(String name) {
        if (name == null) {
            return null;
        }
        
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
