package com.minecraft.huntergame.rank;

import org.bukkit.ChatColor;

/**
 * 段位枚举
 * 每100分一个段位
 * 
 * @author YourName
 * @version 2.0.0
 */
public enum Rank {
    UNRANKED(0, 99, "未定级", ChatColor.GRAY, "<gray>"),
    BRONZE_I(100, 199, "青铜I", ChatColor.GOLD, "<#CD7F32>"),
    BRONZE_II(200, 299, "青铜II", ChatColor.GOLD, "<#CD7F32>"),
    BRONZE_III(300, 399, "青铜III", ChatColor.GOLD, "<#CD7F32>"),
    SILVER_I(400, 499, "白银I", ChatColor.WHITE, "<white>"),
    SILVER_II(500, 599, "白银II", ChatColor.WHITE, "<white>"),
    SILVER_III(600, 699, "白银III", ChatColor.WHITE, "<white>"),
    GOLD_I(700, 799, "黄金I", ChatColor.YELLOW, "<yellow>"),
    GOLD_II(800, 899, "黄金II", ChatColor.YELLOW, "<yellow>"),
    GOLD_III(900, 999, "黄金III", ChatColor.YELLOW, "<yellow>"),
    PLATINUM_I(1000, 1099, "铂金I", ChatColor.AQUA, "<aqua>"),
    PLATINUM_II(1100, 1199, "铂金II", ChatColor.AQUA, "<aqua>"),
    PLATINUM_III(1200, 1299, "铂金III", ChatColor.AQUA, "<aqua>"),
    DIAMOND_I(1300, 1399, "钻石I", ChatColor.BLUE, "<blue>"),
    DIAMOND_II(1400, 1499, "钻石II", ChatColor.BLUE, "<blue>"),
    DIAMOND_III(1500, 1599, "钻石III", ChatColor.BLUE, "<blue>"),
    MASTER(1600, 1799, "大师", ChatColor.DARK_PURPLE, "<dark_purple>"),
    GRANDMASTER(1800, 1999, "宗师", ChatColor.LIGHT_PURPLE, "<light_purple>"),
    CHALLENGER(2000, Integer.MAX_VALUE, "王者", ChatColor.RED, "<red>");
    
    private final int minScore;
    private final int maxScore;
    private final String displayName;
    private final ChatColor color;
    private final String miniMessageColor;
    
    Rank(int minScore, int maxScore, String displayName, ChatColor color, String miniMessageColor) {
        this.minScore = minScore;
        this.maxScore = maxScore;
        this.displayName = displayName;
        this.color = color;
        this.miniMessageColor = miniMessageColor;
    }
    
    public int getMinScore() {
        return minScore;
    }
    
    public int getMaxScore() {
        return maxScore;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public ChatColor getColor() {
        return color;
    }
    
    public String getMiniMessageColor() {
        return miniMessageColor;
    }
    
    /**
     * 获取带颜色的显示名称
     */
    public String getColoredName() {
        return color + displayName;
    }
    
    /**
     * 根据分数获取段位
     */
    public static Rank fromScore(int score) {
        for (Rank rank : values()) {
            if (score >= rank.minScore && score <= rank.maxScore) {
                return rank;
            }
        }
        return UNRANKED;
    }
    
    /**
     * 获取下一个段位
     */
    public Rank getNext() {
        int nextOrdinal = this.ordinal() + 1;
        if (nextOrdinal < values().length) {
            return values()[nextOrdinal];
        }
        return this;
    }
    
    /**
     * 获取上一个段位
     */
    public Rank getPrevious() {
        int prevOrdinal = this.ordinal() - 1;
        if (prevOrdinal >= 0) {
            return values()[prevOrdinal];
        }
        return this;
    }
    
    /**
     * 检查是否可以晋级
     */
    public boolean canPromote(int score) {
        return score > this.maxScore;
    }
    
    /**
     * 检查是否会掉段
     */
    public boolean canDemote(int score) {
        return score < this.minScore;
    }
    
    /**
     * 获取到下一段位所需分数
     */
    public int getScoreToNext(int currentScore) {
        if (this == CHALLENGER) {
            return 0; // 已经是最高段位
        }
        return getNext().minScore - currentScore;
    }
}
