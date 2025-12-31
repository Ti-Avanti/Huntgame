package com.minecraft.huntergame.game;

/**
 * 游戏模式枚举
 * 
 * @author YourName
 * @version 1.0.0
 */
public enum GameMode {
    
    /**
     * 经典模式: 1名猎人 vs 多名逃生者
     */
    CLASSIC("经典模式", "Classic Mode"),
    
    /**
     * 团队模式: 多名猎人 vs 多名逃生者
     */
    TEAM("团队模式", "Team Mode");
    
    private final String displayNameZh;
    private final String displayNameEn;
    
    GameMode(String displayNameZh, String displayNameEn) {
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
     * 根据玩家总数计算猎人数量
     * 
     * @param totalPlayers 玩家总数
     * @return 猎人数量
     */
    public int getHunterCount(int totalPlayers) {
        switch (this) {
            case CLASSIC:
                // 经典模式固定1名猎人
                return 1;
            case TEAM:
                // 团队模式: 1:3比例，至少1名猎人
                return Math.max(1, totalPlayers / 4);
            default:
                return 1;
        }
    }
    
    /**
     * 根据玩家总数计算猎人数量（使用配置的比例）
     * 
     * @param totalPlayers 玩家总数
     * @param classicCount 经典模式猎人数量
     * @param teamRatio 团队模式猎人比例
     * @return 猎人数量
     */
    public int getHunterCount(int totalPlayers, int classicCount, double teamRatio) {
        switch (this) {
            case CLASSIC:
                return classicCount;
            case TEAM:
                return Math.max(1, (int) (totalPlayers * teamRatio));
            default:
                return 1;
        }
    }
    
    /**
     * 从字符串解析游戏模式
     * 
     * @param name 模式名称
     * @return 游戏模式，如果无效则返回CLASSIC
     */
    public static GameMode fromString(String name) {
        if (name == null) {
            return CLASSIC;
        }
        
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return CLASSIC;
        }
    }
}
