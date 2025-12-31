package com.minecraft.huntergame.models;

import java.util.UUID;

/**
 * 玩家数据模型
 * 存储玩家的游戏统计数据
 * 
 * @author YourName
 * @version 1.0.0
 */
public class PlayerData {
    
    private UUID uuid;
    private String name;
    
    // 统计数据
    private int gamesPlayed;      // 游戏场次
    private int gamesWon;          // 胜利次数
    private int gamesLost;         // 失败次数
    private int hunterKills;       // 作为猎人的击杀数
    private int hunterDeaths;      // 作为猎人的死亡数
    private int survivorEscapes;   // 作为逃生者的逃脱次数
    private int survivorDeaths;    // 作为逃生者的死亡数
    private int totalSurvivalTime; // 总生存时间(秒)
    
    // 时间戳
    private long createdAt;
    private long updatedAt;
    
    /**
     * 构造方法
     */
    public PlayerData(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.gamesPlayed = 0;
        this.gamesWon = 0;
        this.gamesLost = 0;
        this.hunterKills = 0;
        this.hunterDeaths = 0;
        this.survivorEscapes = 0;
        this.survivorDeaths = 0;
        this.totalSurvivalTime = 0;
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
    
    public int getGamesPlayed() {
        return gamesPlayed;
    }
    
    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
        this.updatedAt = System.currentTimeMillis() / 1000;
    }
    
    public int getGamesWon() {
        return gamesWon;
    }
    
    /**
     * 获取胜利次数（别名方法）
     */
    public int getWins() {
        return gamesWon;
    }
    
    public void setGamesWon(int gamesWon) {
        this.gamesWon = gamesWon;
        this.updatedAt = System.currentTimeMillis() / 1000;
    }
    
    public int getGamesLost() {
        return gamesLost;
    }
    
    /**
     * 获取失败次数（别名方法）
     */
    public int getLosses() {
        return gamesLost;
    }
    
    public void setGamesLost(int gamesLost) {
        this.gamesLost = gamesLost;
        this.updatedAt = System.currentTimeMillis() / 1000;
    }
    
    public int getHunterKills() {
        return hunterKills;
    }
    
    public void setHunterKills(int hunterKills) {
        this.hunterKills = hunterKills;
        this.updatedAt = System.currentTimeMillis() / 1000;
    }
    
    public int getHunterDeaths() {
        return hunterDeaths;
    }
    
    public void setHunterDeaths(int hunterDeaths) {
        this.hunterDeaths = hunterDeaths;
        this.updatedAt = System.currentTimeMillis() / 1000;
    }
    
    public int getSurvivorEscapes() {
        return survivorEscapes;
    }
    
    public void setSurvivorEscapes(int survivorEscapes) {
        this.survivorEscapes = survivorEscapes;
        this.updatedAt = System.currentTimeMillis() / 1000;
    }
    
    public int getSurvivorDeaths() {
        return survivorDeaths;
    }
    
    public void setSurvivorDeaths(int survivorDeaths) {
        this.survivorDeaths = survivorDeaths;
        this.updatedAt = System.currentTimeMillis() / 1000;
    }
    
    public int getTotalSurvivalTime() {
        return totalSurvivalTime;
    }
    
    public void setTotalSurvivalTime(int totalSurvivalTime) {
        this.totalSurvivalTime = totalSurvivalTime;
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
     * 增加游戏场次
     */
    public void addGame() {
        this.gamesPlayed++;
        this.updatedAt = System.currentTimeMillis() / 1000;
    }
    
    /**
     * 增加胜利次数
     */
    public void addWin() {
        this.gamesWon++;
        this.updatedAt = System.currentTimeMillis() / 1000;
    }
    
    /**
     * 增加失败次数
     */
    public void addLoss() {
        this.gamesLost++;
        this.updatedAt = System.currentTimeMillis() / 1000;
    }
    
    /**
     * 增加猎人击杀数
     */
    public void addHunterKill() {
        this.hunterKills++;
        this.updatedAt = System.currentTimeMillis() / 1000;
    }
    
    /**
     * 增加猎人死亡数
     */
    public void addHunterDeath() {
        this.hunterDeaths++;
        this.updatedAt = System.currentTimeMillis() / 1000;
    }
    
    /**
     * 增加逃生者逃脱次数
     */
    public void addSurvivorEscape() {
        this.survivorEscapes++;
        this.updatedAt = System.currentTimeMillis() / 1000;
    }
    
    /**
     * 增加逃生者死亡数
     */
    public void addSurvivorDeath() {
        this.survivorDeaths++;
        this.updatedAt = System.currentTimeMillis() / 1000;
    }
    
    /**
     * 增加生存时间
     */
    public void addSurvivalTime(int seconds) {
        this.totalSurvivalTime += seconds;
        this.updatedAt = System.currentTimeMillis() / 1000;
    }
    
    /**
     * 获取总击杀数
     */
    public int getTotalKills() {
        return hunterKills;
    }
    
    /**
     * 获取总死亡数
     */
    public int getTotalDeaths() {
        return hunterDeaths + survivorDeaths;
    }
    
    /**
     * 获取KD比率
     */
    public double getKDRatio() {
        int totalDeaths = getTotalDeaths();
        if (totalDeaths == 0) {
            return getTotalKills();
        }
        return (double) getTotalKills() / totalDeaths;
    }
    
    /**
     * 获取胜率
     */
    public double getWinRate() {
        if (gamesPlayed == 0) {
            return 0.0;
        }
        return (double) gamesWon / gamesPlayed * 100;
    }
    
    /**
     * 获取平均生存时间
     */
    public double getAverageSurvivalTime() {
        if (gamesPlayed == 0) {
            return 0.0;
        }
        return (double) totalSurvivalTime / gamesPlayed;
    }
}
