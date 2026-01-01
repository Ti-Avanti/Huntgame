package com.minecraft.huntergame.game;

import com.minecraft.huntergame.HunterGame;
import org.bukkit.Location;
import org.bukkit.entity.Player;

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
    private final int matchingTimeout;   // 匹配超时时间(秒)
    private final int minPlayersToStart; // 最小开始人数
    
    // 游戏状态
    private long startTime;              // 游戏开始时间
    private long prepareEndTime;         // 准备时间结束时间
    private long matchingStartTime;      // 匹配开始时间
    private long matchingEndTime;        // 匹配结束时间
    private boolean dragonDefeated;      // 末影龙是否被击败
    
    // 位置配置
    private Location spawnLocation;      // 出生点
    
    /**
     * 构造方法（使用默认配置）
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
        this.matchingTimeout = plugin.getManhuntConfig().getMatchingTimeout();
        this.minPlayersToStart = plugin.getManhuntConfig().getMinPlayersToStart();
        
        // 初始化游戏状态
        this.startTime = 0;
        this.prepareEndTime = 0;
        this.matchingStartTime = 0;
        this.matchingEndTime = 0;
        this.dragonDefeated = false;
    }
    
    /**
     * 构造方法（使用自定义配置）
     * 由GameBuilder调用
     */
    public ManhuntGame(HunterGame plugin, String gameId, String worldName,
                      int maxRunners, int maxHunters, int respawnLimit,
                      int prepareTime, int maxGameTime, int matchingTimeout,
                      int minPlayersToStart) {
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
        
        // 使用传入的自定义参数
        this.maxRunners = maxRunners;
        this.maxHunters = maxHunters;
        this.respawnLimit = respawnLimit;
        this.prepareTime = prepareTime;
        this.maxGameTime = maxGameTime;
        this.matchingTimeout = matchingTimeout;
        this.minPlayersToStart = minPlayersToStart;
        
        // 初始化游戏状态
        this.startTime = 0;
        this.prepareEndTime = 0;
        this.matchingStartTime = 0;
        this.matchingEndTime = 0;
        this.dragonDefeated = false;
    }
    
    // ==================== 玩家管理 ====================
    
    /**
     * 添加玩家到游戏
     */
    public boolean addPlayer(UUID uuid) {
        plugin.debug("ManhuntGame.addPlayer: uuid=" + uuid + ", state=" + state);
        
        if (state != GameState.WAITING && state != GameState.MATCHING) {
            plugin.debug("Cannot add player: game not in WAITING or MATCHING state");
            return false;
        }
        
        if (isPlayerInGame(uuid)) {
            plugin.debug("Cannot add player: already in game");
            return false;
        }
        
        // 检查人数限制
        if (getPlayerCount() >= maxRunners + maxHunters) {
            plugin.debug("Cannot add player: game is full");
            return false;
        }
        
        // 暂时添加到观战者列表，等游戏开始时分配角色
        spectators.add(uuid);
        playerRoles.put(uuid, PlayerRole.SPECTATOR);
        
        plugin.debug("Player added successfully (role will be assigned on start)");
        
        // 检查是否应该开始匹配
        if (state == GameState.WAITING && getPlayerCount() >= minPlayersToStart) {
            startMatching();
        }
        
        // 检查是否达到最大人数，自动开始游戏
        if (state == GameState.MATCHING && getPlayerCount() >= maxRunners + maxHunters) {
            plugin.debug("Max players reached, auto-starting game");
            // 由ManhuntManager处理自动开始
        }
        
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
     * 在WAITING/MATCHING状态下包含所有玩家(包括spectators)
     * 在游戏进行中只计算runners和hunters
     */
    public int getPlayerCount() {
        if (state == GameState.WAITING || state == GameState.MATCHING) {
            // 等待/匹配状态下,所有玩家都在spectators列表中
            return spectators.size();
        }
        // 游戏进行中,只计算实际参与的玩家
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
     * 开始匹配
     */
    public void startMatching() {
        plugin.debug("ManhuntGame.startMatching: gameId=" + gameId + ", currentState=" + state);
        
        if (state != GameState.WAITING) {
            plugin.debug("Cannot start matching: game not in WAITING state");
            return;
        }
        
        state = GameState.MATCHING;
        matchingStartTime = System.currentTimeMillis();
        matchingEndTime = matchingStartTime + (matchingTimeout * 1000L);
        
        plugin.debug("Matching started: timeout=" + matchingTimeout + "s");
        plugin.getLogger().info("游戏 " + gameId + " 进入匹配阶段");
        
        // 为所有玩家更新计分板和道具为匹配状态
        for (UUID uuid : spectators) {
            org.bukkit.entity.Player player = plugin.getServer().getPlayer(uuid);
            if (player != null && player.isOnline()) {
                // 更新计分板
                plugin.getSidebarManager().removeLobbySidebar(player);
                plugin.getSidebarManager().createMatchingSidebar(player, this);
                plugin.debug("Updated sidebar to matching for: " + player.getName());
                
                // 更新 hotbar 道具
                plugin.getHotbarManager().giveMatchingItems(player, this);
                plugin.debug("Updated hotbar to matching for: " + player.getName());
            }
        }
    }
    
    /**
     * 开始游戏
     */
    public void start() {
        plugin.debug("ManhuntGame.start: gameId=" + gameId + ", currentState=" + state);
        
        if (state != GameState.WAITING && state != GameState.MATCHING) {
            plugin.debug("Cannot start: game not in WAITING or MATCHING state");
            return;
        }
        
        state = GameState.PREPARING;
        startTime = System.currentTimeMillis();
        prepareEndTime = startTime + (prepareTime * 1000L);
        
        plugin.debug("Game started: state=" + state + ", prepareTime=" + prepareTime + "s");
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
    
    /**
     * 检查是否在匹配阶段
     */
    public boolean isMatching() {
        return state == GameState.MATCHING;
    }
    
    /**
     * 检查匹配时间是否结束
     */
    public boolean isMatchingTimeout() {
        if (!isMatching()) {
            return false;
        }
        return System.currentTimeMillis() >= matchingEndTime;
    }
    
    /**
     * 获取匹配剩余时间(秒)
     */
    public long getMatchingRemainingTime() {
        if (!isMatching()) {
            return 0;
        }
        long remaining = (matchingEndTime - System.currentTimeMillis()) / 1000;
        return Math.max(0, remaining);
    }
    
    /**
     * 检查是否达到最小开始人数
     */
    public boolean hasMinPlayers() {
        return getPlayerCount() >= minPlayersToStart;
    }
    
    /**
     * 检查是否达到最大人数
     */
    public boolean isFull() {
        return getPlayerCount() >= maxRunners + maxHunters;
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
    
    public int getMatchingTimeout() {
        return matchingTimeout;
    }
    
    public int getMinPlayersToStart() {
        return minPlayersToStart;
    }
    
    public long getMatchingStartTime() {
        return matchingStartTime;
    }
    
    public long getMatchingEndTime() {
        return matchingEndTime;
    }
    
    /**
     * 向游戏中的所有玩家广播消息
     */
    public void broadcast(String message) {
        for (UUID uuid : getAllPlayers()) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.sendMessage(message);
            }
        }
    }
}
