package com.minecraft.huntergame.manager;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.database.PlayerRepository;
import com.minecraft.huntergame.models.PlayerData;
import com.minecraft.huntergame.rank.Rank;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 段位管理器
 * 负责管理玩家段位数据的缓存和更新
 * 
 * @author YourName
 * @version 2.0.0
 */
public class StatsManager {
    
    private final HunterGame plugin;
    private final PlayerRepository playerRepository;
    
    // 数据缓存
    private final Map<UUID, PlayerData> dataCache;
    
    // 当前赛季ID
    private int currentSeasonId = 1;
    
    public StatsManager(HunterGame plugin, PlayerRepository playerRepository) {
        this.plugin = plugin;
        this.playerRepository = playerRepository;
        this.dataCache = new HashMap<>();
    }
    
    /**
     * 加载玩家数据
     */
    public void loadPlayerData(Player player) {
        UUID uuid = player.getUniqueId();
        
        playerRepository.loadAsync(uuid, data -> {
            if (data == null) {
                // 创建新数据
                data = new PlayerData(uuid, player.getName());
                data.setSeasonId(currentSeasonId);
                plugin.getLogger().info("为玩家 " + player.getName() + " 创建新段位数据");
            } else {
                // 更新玩家名称
                if (!data.getName().equals(player.getName())) {
                    data.setName(player.getName());
                }
                
                // 检查赛季是否需要重置
                if (data.getSeasonId() != currentSeasonId) {
                    plugin.getLogger().info("玩家 " + player.getName() + " 赛季数据过期，重置赛季");
                    data.resetSeason(currentSeasonId);
                }
            }
            
            dataCache.put(uuid, data);
        });
    }
    
    /**
     * 加载玩家数据（别名方法）
     */
    public void loadPlayer(UUID uuid) {
        playerRepository.loadAsync(uuid, data -> {
            if (data == null) {
                // 创建新数据
                data = new PlayerData(uuid, "Unknown");
                data.setSeasonId(currentSeasonId);
                plugin.getLogger().info("为玩家 " + uuid + " 创建新段位数据");
            } else {
                // 检查赛季是否需要重置
                if (data.getSeasonId() != currentSeasonId) {
                    plugin.getLogger().info("玩家 " + uuid + " 赛季数据过期，重置赛季");
                    data.resetSeason(currentSeasonId);
                }
            }
            
            dataCache.put(uuid, data);
        });
    }
    
    /**
     * 保存玩家数据
     */
    public void savePlayerData(UUID uuid) {
        PlayerData data = dataCache.get(uuid);
        if (data != null) {
            playerRepository.saveAsync(data, success -> {
                if (!success) {
                    plugin.getLogger().warning("保存玩家段位数据失败: " + data.getName());
                }
            });
        }
    }
    
    /**
     * 保存玩家数据（通过Player对象）
     */
    public void savePlayerData(Player player) {
        savePlayerData(player.getUniqueId());
    }
    
    /**
     * 保存玩家数据（别名方法）
     */
    public void savePlayer(UUID uuid) {
        savePlayerData(uuid);
    }
    
    /**
     * 卸载玩家数据
     */
    public void unloadPlayerData(UUID uuid) {
        savePlayerData(uuid);
        dataCache.remove(uuid);
    }
    
    /**
     * 获取玩家数据
     */
    public PlayerData getPlayerData(UUID uuid) {
        return dataCache.get(uuid);
    }
    
    /**
     * 获取玩家数据（通过Player对象）
     */
    public PlayerData getPlayerData(Player player) {
        return getPlayerData(player.getUniqueId());
    }
    
    // ==================== 段位管理方法 ====================
    
    /**
     * 增加玩家分数
     */
    public void addScore(UUID uuid, int points) {
        PlayerData data = dataCache.get(uuid);
        if (data != null) {
            data.addScore(points);
        }
    }
    
    /**
     * 减少玩家分数
     */
    public void removeScore(UUID uuid, int points) {
        PlayerData data = dataCache.get(uuid);
        if (data != null) {
            data.removeScore(points);
        }
    }
    
    /**
     * 获取玩家当前段位
     */
    public Rank getCurrentRank(UUID uuid) {
        PlayerData data = dataCache.get(uuid);
        return data != null ? data.getCurrentRank() : Rank.UNRANKED;
    }
    
    /**
     * 获取玩家历史最高段位
     */
    public Rank getHighestRank(UUID uuid) {
        PlayerData data = dataCache.get(uuid);
        return data != null ? data.getHighestRank() : Rank.UNRANKED;
    }
    
    /**
     * 获取玩家分数
     */
    public int getScore(UUID uuid) {
        PlayerData data = dataCache.get(uuid);
        return data != null ? data.getScore() : 0;
    }
    
    /**
     * 获取当前赛季ID
     */
    public int getCurrentSeasonId() {
        return currentSeasonId;
    }
    
    /**
     * 设置当前赛季ID
     */
    public void setCurrentSeasonId(int seasonId) {
        this.currentSeasonId = seasonId;
    }
    
    /**
     * 重置所有玩家的赛季数据
     */
    public void resetAllSeasons(int newSeasonId) {
        this.currentSeasonId = newSeasonId;
        
        for (PlayerData data : dataCache.values()) {
            data.resetSeason(newSeasonId);
        }
        
        plugin.getLogger().info("已重置所有玩家的赛季数据到赛季 " + newSeasonId);
    }
    
    // ==================== 排行榜系统 ====================
    
    // 排行榜缓存
    private List<PlayerData> topRanksCache = null;
    private long lastCacheUpdate = 0;
    private final long CACHE_DURATION = 5 * 60 * 1000; // 5分钟
    
    /**
     * 获取段位排行榜
     */
    public List<PlayerData> getTopRanks(int limit) {
        updateCacheIfNeeded();
        if (topRanksCache == null || topRanksCache.isEmpty()) {
            try {
                return playerRepository.getTopRanks(limit);
            } catch (Exception ex) {
                plugin.getLogger().warning("获取段位排行榜失败: " + ex.getMessage());
                return new java.util.ArrayList<>();
            }
        }
        return topRanksCache.subList(0, Math.min(limit, topRanksCache.size()));
    }
    
    /**
     * 获取当前赛季段位排行榜
     */
    public List<PlayerData> getTopRanksBySeason(int limit) {
        try {
            return playerRepository.getTopRanksBySeason(currentSeasonId, limit);
        } catch (Exception ex) {
            plugin.getLogger().warning("获取赛季段位排行榜失败: " + ex.getMessage());
            return new java.util.ArrayList<>();
        }
    }
    
    /**
     * 获取玩家排名
     */
    public int getPlayerRanking(UUID uuid) {
        try {
            return playerRepository.getPlayerRanking(uuid);
        } catch (Exception ex) {
            plugin.getLogger().warning("获取玩家排名失败: " + ex.getMessage());
            return 0;
        }
    }
    
    /**
     * 更新缓存（如果需要）
     */
    private void updateCacheIfNeeded() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCacheUpdate > CACHE_DURATION) {
            updateLeaderboardCache();
        }
    }
    
    /**
     * 更新排行榜缓存
     */
    private void updateLeaderboardCache() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                topRanksCache = playerRepository.getTopRanks(100);
                lastCacheUpdate = System.currentTimeMillis();
                
                plugin.getLogger().info("段位排行榜缓存已更新");
            } catch (Exception ex) {
                plugin.getLogger().warning("更新段位排行榜缓存失败: " + ex.getMessage());
            }
        });
    }
    
    /**
     * 强制刷新缓存
     */
    public void refreshLeaderboardCache() {
        lastCacheUpdate = 0;
        updateLeaderboardCache();
    }
    
    /**
     * 启动定时缓存更新任务
     */
    public void startCacheUpdateTask() {
        // 立即更新一次
        updateLeaderboardCache();
        
        // 每5分钟更新一次
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            updateLeaderboardCache();
        }, 20L * 60 * 5, 20L * 60 * 5);
        
        plugin.getLogger().info("段位排行榜缓存更新任务已启动");
    }
    
    // ==================== 兼容旧方法（避免编译错误） ====================
    
    @Deprecated
    public void addGame(UUID uuid) {
        // 不再使用
    }
    
    @Deprecated
    public void addWin(UUID uuid) {
        // 不再使用
    }
    
    @Deprecated
    public void addLoss(UUID uuid) {
        // 不再使用
    }
    
    @Deprecated
    public void addHunterKill(UUID uuid) {
        // 不再使用
    }
    
    @Deprecated
    public void addHunterDeath(UUID uuid) {
        // 不再使用
    }
    
    @Deprecated
    public void addSurvivorEscape(UUID uuid) {
        // 不再使用
    }
    
    @Deprecated
    public void addSurvivorDeath(UUID uuid) {
        // 不再使用
    }
    
    @Deprecated
    public void addSurvivalTime(UUID uuid, int seconds) {
        // 不再使用
    }
    
    @Deprecated
    public void getTopWins(int limit, java.util.function.Consumer<List<PlayerData>> callback) {
        // 使用新的段位排行榜
        callback.accept(getTopRanks(limit));
    }
    
    @Deprecated
    public void getTopKills(int limit, java.util.function.Consumer<List<PlayerData>> callback) {
        // 使用新的段位排行榜
        callback.accept(getTopRanks(limit));
    }
    
    @Deprecated
    public void getTopEscapes(int limit, java.util.function.Consumer<List<PlayerData>> callback) {
        // 使用新的段位排行榜
        callback.accept(getTopRanks(limit));
    }
    
    @Deprecated
    public void getTopRunnerWins(int limit, java.util.function.Consumer<List<PlayerData>> callback) {
        // 使用新的段位排行榜
        callback.accept(getTopRanks(limit));
    }
    
    @Deprecated
    public void getTopHunterWins(int limit, java.util.function.Consumer<List<PlayerData>> callback) {
        // 使用新的段位排行榜
        callback.accept(getTopRanks(limit));
    }
    
    @Deprecated
    public void getTopDragonKills(int limit, java.util.function.Consumer<List<PlayerData>> callback) {
        // 使用新的段位排行榜
        callback.accept(getTopRanks(limit));
    }
    
    @Deprecated
    public List<PlayerData> getTopWins(int limit) {
        return getTopRanks(limit);
    }
    
    @Deprecated
    public List<PlayerData> getTopKills(int limit) {
        return getTopRanks(limit);
    }
    
    @Deprecated
    public List<PlayerData> getTopRunnerWins(int limit) {
        return getTopRanks(limit);
    }
    
    @Deprecated
    public List<PlayerData> getTopHunterWins(int limit) {
        return getTopRanks(limit);
    }
    
    @Deprecated
    public List<PlayerData> getTopDragonKills(int limit) {
        return getTopRanks(limit);
    }
    
    /**
     * 保存所有数据
     */
    public void saveAll() {
        plugin.getLogger().info("正在保存所有玩家段位数据...");
        
        for (UUID uuid : dataCache.keySet()) {
            savePlayerData(uuid);
        }
        
        plugin.getLogger().info("已保存 " + dataCache.size() + " 个玩家段位数据");
    }
    
    /**
     * 同步保存所有数据(用于插件关闭时)
     */
    public void saveAllSync() {
        plugin.getLogger().info("正在同步保存所有玩家段位数据...");
        
        int count = 0;
        for (UUID uuid : dataCache.keySet()) {
            PlayerData data = dataCache.get(uuid);
            if (data != null) {
                try {
                    playerRepository.save(data);
                    count++;
                } catch (Exception ex) {
                    plugin.getLogger().warning("保存玩家段位数据失败: " + data.getName() + " - " + ex.getMessage());
                }
            }
        }
        
        plugin.getLogger().info("已同步保存 " + count + " 个玩家段位数据");
    }
}
