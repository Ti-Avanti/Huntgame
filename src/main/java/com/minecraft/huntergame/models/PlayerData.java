package com.minecraft.huntergame.models;

import com.minecraft.huntergame.rank.Rank;
import java.util.UUID;

/**
 * 玩家数据模型 - 段位系统
 * 存储玩家的段位和分数
 * 
 * @author YourName
 * @version 2.0.0
 */
public class PlayerData {
    
    private UUID uuid;
    private String name;
    
    // 段位数据
    private int score;              // 当前分数
    private Rank currentRank;       // 当前段位
    private Rank highestRank;       // 历史最高段位
    private int seasonId;           // 赛季ID
    
    // 时间戳
    private long createdAt;
    private long updatedAt;
    
    /**
     * 构造方法
     */
    public PlayerData(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.score = 0;
        this.currentRank = Rank.UNRANKED;
        this.highestRank = Rank.UNRANKED;
        this.seasonId = 1; // 默认第1赛季
        this.createdAt = System.currentTimeMillis() / 1000;
        this.updatedAt = this.createdAt;
    }
    
    // ==================== Getter 和 Setter ====================
    
    public UUID getUuid() {
        return uuid;
    }
    
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
        this.updatedAt = System.currentTimeMillis() / 1000;
    }
    
    public int getScore() {
        return score;
    }
    
    public void setScore(int score) {
        this.score = Math.max(0, score); // 分数不能为负
        updateRank();
        this.updatedAt = System.currentTimeMillis() / 1000;
    }
    
    public Rank getCurrentRank() {
        return currentRank;
    }
    
    public void setCurrentRank(Rank currentRank) {
        this.currentRank = currentRank;
        // 更新历史最高段位
        if (currentRank.ordinal() > highestRank.ordinal()) {
            this.highestRank = currentRank;
        }
        this.updatedAt = System.currentTimeMillis() / 1000;
    }
    
    public Rank getHighestRank() {
        return highestRank;
    }
    
    public void setHighestRank(Rank highestRank) {
        this.highestRank = highestRank;
        this.updatedAt = System.currentTimeMillis() / 1000;
    }
    
    public int getSeasonId() {
        return seasonId;
    }
    
    public void setSeasonId(int seasonId) {
        this.seasonId = seasonId;
        this.updatedAt = System.currentTimeMillis() / 1000;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
    
    public long getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // ==================== 业务方法 ====================
    
    /**
     * 增加分数
     */
    public void addScore(int points) {
        int oldScore = this.score;
        Rank oldRank = this.currentRank;
        
        this.score = Math.max(0, this.score + points);
        updateRank();
        
        // 检查是否晋级或掉段
        if (this.currentRank.ordinal() > oldRank.ordinal()) {
            // 晋级
            this.updatedAt = System.currentTimeMillis() / 1000;
        } else if (this.currentRank.ordinal() < oldRank.ordinal()) {
            // 掉段
            this.updatedAt = System.currentTimeMillis() / 1000;
        }
        
        this.updatedAt = System.currentTimeMillis() / 1000;
    }
    
    /**
     * 减少分数
     */
    public void removeScore(int points) {
        addScore(-points);
    }
    
    /**
     * 根据当前分数更新段位
     */
    private void updateRank() {
        Rank newRank = Rank.fromScore(this.score);
        if (newRank != this.currentRank) {
            setCurrentRank(newRank);
        }
    }
    
    /**
     * 重置赛季数据
     */
    public void resetSeason(int newSeasonId) {
        this.seasonId = newSeasonId;
        this.score = 0;
        this.currentRank = Rank.UNRANKED;
        // 保留历史最高段位
        this.updatedAt = System.currentTimeMillis() / 1000;
    }
    
    /**
     * 获取到下一段位所需分数
     */
    public int getScoreToNextRank() {
        return currentRank.getScoreToNext(score);
    }
    
    /**
     * 检查是否可以晋级
     */
    public boolean canPromote() {
        return currentRank.canPromote(score);
    }
    
    /**
     * 检查是否会掉段
     */
    public boolean canDemote() {
        return currentRank.canDemote(score);
    }
    
    // ==================== 兼容旧方法（避免编译错误） ====================
    
    @Deprecated
    public int getGamesPlayed() {
        return 0;
    }
    
    @Deprecated
    public void setGamesPlayed(int gamesPlayed) {
        // 不再使用
    }
    
    @Deprecated
    public int getGamesWon() {
        return 0;
    }
    
    @Deprecated
    public int getWins() {
        return 0;
    }
    
    @Deprecated
    public void setGamesWon(int gamesWon) {
        // 不再使用
    }
    
    @Deprecated
    public int getGamesLost() {
        return 0;
    }
    
    @Deprecated
    public int getLosses() {
        return 0;
    }
    
    @Deprecated
    public void setGamesLost(int gamesLost) {
        // 不再使用
    }
    
    @Deprecated
    public int getRunnerWins() {
        return 0;
    }
    
    @Deprecated
    public void setRunnerWins(int runnerWins) {
        // 不再使用
    }
    
    @Deprecated
    public int getHunterWins() {
        return 0;
    }
    
    @Deprecated
    public void setHunterWins(int hunterWins) {
        // 不再使用
    }
    
    @Deprecated
    public int getDragonKills() {
        return 0;
    }
    
    @Deprecated
    public void setDragonKills(int dragonKills) {
        // 不再使用
    }
    
    @Deprecated
    public int getHunterKills() {
        return 0;
    }
    
    @Deprecated
    public void setHunterKills(int hunterKills) {
        // 不再使用
    }
    
    @Deprecated
    public int getHunterDeaths() {
        return 0;
    }
    
    @Deprecated
    public void setHunterDeaths(int hunterDeaths) {
        // 不再使用
    }
    
    @Deprecated
    public int getSurvivorEscapes() {
        return 0;
    }
    
    @Deprecated
    public void setSurvivorEscapes(int survivorEscapes) {
        // 不再使用
    }
    
    @Deprecated
    public int getSurvivorDeaths() {
        return 0;
    }
    
    @Deprecated
    public void setSurvivorDeaths(int survivorDeaths) {
        // 不再使用
    }
    
    @Deprecated
    public int getTotalSurvivalTime() {
        return 0;
    }
    
    @Deprecated
    public void setTotalSurvivalTime(int totalSurvivalTime) {
        // 不再使用
    }
    
    @Deprecated
    public int getTotalKills() {
        return 0;
    }
    
    @Deprecated
    public int getTotalDeaths() {
        return 0;
    }
    
    @Deprecated
    public double getKDRatio() {
        return 0.0;
    }
    
    @Deprecated
    public double getWinRate() {
        return 0.0;
    }
    
    @Deprecated
    public double getAverageSurvivalTime() {
        return 0.0;
    }
    
    // 兼容 StatsManager 调用的方法
    @Deprecated
    public void addGame() {
        // 不再使用
    }
    
    @Deprecated
    public void addWin() {
        // 不再使用
    }
    
    @Deprecated
    public void addLoss() {
        // 不再使用
    }
    
    @Deprecated
    public void addHunterKill() {
        // 不再使用
    }
    
    @Deprecated
    public void addHunterDeath() {
        // 不再使用
    }
    
    @Deprecated
    public void addSurvivorEscape() {
        // 不再使用
    }
    
    @Deprecated
    public void addSurvivorDeath() {
        // 不再使用
    }
    
    @Deprecated
    public void addSurvivalTime(int seconds) {
        // 不再使用
    }
}
