package com.minecraft.huntergame.game;

import com.minecraft.huntergame.HunterGame;
import org.bukkit.Location;

import java.util.*;

/**
 * Manhunt游戏会话类
 * 表示一局Manhunt游戏
 * 
 * @author YourName
 * @version 1.0.0
 */
public class ManhuntGame {
    
    private final HunterGame plugin;
    
    // 基础信息
    private final String gameId;
    private final String worldName;
    private GameState state;
    
    // 玩家管理
    private final List<UUID> runners;          // 逃亡者列表
    private final List<UUID> hunters;          // 猎人列表
    private final List<UUID> spectators;       // 观战者列表
    private final Map<UUID, Integer> respawnCounts;  // 逃亡者剩余复活次数
    private final Map<UUID, PlayerRole> playerRoles; // 玩家角色映射
    
    // 游戏配置
    private final int maxRunners;
    private final int maxHunters;
    private final int respawnLimit;      // 复活次数限制
    private final int prepareTime;       // 准备时间(秒)
    private final int maxGameTime;       // 最大游戏时长(秒，0=无限制)
    
    // 游戏状态
    private long startTime;              // 游戏开始时间
    private long prepareEndTime;         // 准备时间结束时间
    private boolean dragonDefeated;      // 末影龙是否被击败
    
    // 位置配置
    private Location spawnLocation;      // 出生点
    
    /**
     * 构造方法
     */
    public ManhuntGame(HunterGame plugin, String gameId, String worldName) {
        this.plugin = plugin;
        this.gameId = gameId;
        this.worldName = worldName;
        this.state = GameState.WAITING;
        
        // 初始化玩家列表
        this.runners = new ArrayList<>();
        this.hunters = new ArrayList<>();
        this.spectators = new ArrayList<>();
        this.respawnCounts = new HashMap<>();
        this.playerRoles = new HashMap<>();
        
        // 从配置加载游戏参数
        this.maxRunners = plugin.getManhuntConfig().getMaxRunners();
        this.maxHunters = plugin.getManhuntConfig().getMaxHunters();
        this.respawnLimit = plugin.getManhuntConfig().getRespawnLimit();
        this.prepareTime = plugin.getManhuntConfig().getPrepareTime();
        this.maxGameTime = plugin.getManhuntConfig().getMaxGameTime();
        
        // 初始化游戏状态
        this.startTime = 0;
        this.prepareEndTime = 0;
        this.dragonDefeated = false;
    }
    
    // ==================== 玩家管理 ====================
    
    /**
     * 添加玩家到游戏
     */
    public boolean addPlayer(UUID uuid) {
        if (state != GameState.WAITING) {
            return false;
        }
        
        if (isPlayerInGame(uuid)) {
            return false;
        }
        
        // 暂时不分配角色，等游戏开始时分配
        return true;
    }
    
    /**
     * 移除玩家
     */
    public void removePlayer(UUID uuid) {
        runners.remove(uuid);
        hunters.remove(uuid);
        spectators.remove(uuid);
        respawnCounts.remove(uuid);
        playerRoles.remove(uuid);
    }
    
    /**
     * 检查玩家是否在游戏中
     */
    public boolean isPlayerInGame(UUID uuid) {
        return runners.contains(uuid) || hunters.contains(uuid) || spectators.contains(uuid);
    }
    
    /**
     * 获取玩家角色
     */
    public PlayerRole getPlayerRole(UUID uuid) {
        return playerRoles.get(uuid);
    }
    
    /**
     * 设置玩家角色
     */
    public void setPlayerRole(UUID uuid, PlayerRole role) {
        playerRoles.put(uuid, role);
        
        // 更新角色列表
        runners.remove(uuid);
        hunters.remove(uuid);
        spectators.remove(uuid);
        
        switch (role) {
            case RUNNER:
                runners.add(uuid);
                respawnCounts.put(uuid, respawnLimit);
                break;
            case HUNTER:
                hunters.add(uuid);
                break;
            case SPECTATOR:
                spectators.add(uuid);
                break;
        }
    }
    
    /**
     * 获取所有玩家
     */
    public List<UUID> getAllPlayers() {
        List<UUID> all = new ArrayList<>();
        all.addAll(runners);
        all.addAll(hunters);
        all.addAll(spectators);
        return all;
    }
    
    /**
     * 获取玩家数量
     */
    public int getPlayerCount() {
        return runners.size() + hunters.size();
    }
    
    // ==================== 复活管理 ====================
    
    /**
     * 获取逃亡者剩余复活次数
     */
    public int getRemainingRespawns(UUID uuid) {
        return respawnCounts.getOrDefault(uuid, 0);
    }
    
    /**
     * 减少复活次数
     */
    public void decreaseRespawns(UUID uuid) {
        int current = respawnCounts.getOrDefault(uuid, 0);
        if (current > 0) {
            respawnCounts.put(uuid, current - 1);
        }
    }
    
    /**
     * 检查是否还有复活次数
     */
    public boolean hasRespawns(UUID uuid) {
        return respawnCounts.getOrDefault(uuid, 0) > 0;
    }
    
    // ==================== 游戏状态管理 ====================
    
    /**
     * 开始游戏
     */
    public void start() {
        if (state != GameState.WAITING) {
            return;
        }
        
        state = GameState.PREPARING;
        startTime = System.currentTimeMillis();
        prepareEndTime = startTime + (prepareTime * 1000L);
        
        plugin.getLogger().info("游戏 " + gameId + " 进入准备阶段");
    }
    
    /**
     * 结束准备阶段，开始游戏
     */
    public void startPlaying() {
        if (state != GameState.PREPARING) {
            return;
        }
        
        state = GameState.PLAYING;
        plugin.getLogger().info("游戏 " + gameId + " 正式开始");
    }
    
    /**
     * 结束游戏
     */
    public void end() {
        if (state != GameState.PLAYING) {
            return;
        }
        
        state = GameState.ENDING;
        plugin.getLogger().info("游戏 " + gameId + " 结束");
    }
    
    /**
     * 检查游戏是否应该结束
     */
    public boolean shouldEnd() {
        if (state != GameState.PLAYING) {
            return false;
        }
        
        // 检查所有逃亡者是否被淘汰
        if (getAliveRunners().isEmpty()) {
            return true;
        }
        
        // 检查末影龙是否被击败
        if (dragonDefeated) {
            return true;
        }
        
        // 检查游戏时间是否结束
        if (maxGameTime > 0) {
            long elapsed = (System.currentTimeMillis() - startTime) / 1000;
            if (elapsed >= maxGameTime) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 获取存活的逃亡者
     */
    public List<UUID> getAliveRunners() {
        List<UUID> alive = new ArrayList<>();
        for (UUID uuid : runners) {
            if (!spectators.contains(uuid)) {
                alive.add(uuid);
            }
        }
        return alive;
    }
    
    /**
     * 获取游戏时长(秒)
     */
    public long getElapsedTime() {
        if (startTime == 0) {
            return 0;
        }
        return (System.currentTimeMillis() - startTime) / 1000;
    }
    
    /**
     * 获取剩余游戏时间(秒)
     */
    public long getRemainingTime() {
        if (maxGameTime == 0) {
            return -1; // 无限制
        }
        
        long elapsed = getElapsedTime();
        return Math.max(0, maxGameTime - elapsed);
    }
    
    /**
     * 检查是否在准备阶段
     */
    public boolean isPreparing() {
        return state == GameState.PREPARING;
    }
    
    /**
     * 检查准备时间是否结束
     */
    public boolean isPrepareTimeEnded() {
        if (!isPreparing()) {
            return false;
        }
        return System.currentTimeMillis() >= prepareEndTime;
    }
    
    /**
     * 获取准备时间结束时间戳
     */
    public long getPrepareEndTime() {
        return prepareEndTime;
    }
    
    // ==================== Getter 和 Setter ====================
    
    public String getGameId() {
        return gameId;
    }
    
    public String getWorldName() {
        return worldName;
    }
    
    public GameState getState() {
        return state;
    }
    
    public void setState(GameState state) {
        this.state = state;
    }
    
    public List<UUID> getRunners() {
        return new ArrayList<>(runners);
    }
    
    public List<UUID> getHunters() {
        return new ArrayList<>(hunters);
    }
    
    public List<UUID> getSpectators() {
        return new ArrayList<>(spectators);
    }
    
    public boolean isDragonDefeated() {
        return dragonDefeated;
    }
    
    public void setDragonDefeated(boolean dragonDefeated) {
        this.dragonDefeated = dragonDefeated;
    }
    
    public Location getSpawnLocation() {
        return spawnLocation;
    }
    
    public void setSpawnLocation(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }
    
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
}
