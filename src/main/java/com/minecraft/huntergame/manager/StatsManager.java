package com.minecraft.huntergame.manager;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.database.PlayerRepository;
import com.minecraft.huntergame.models.PlayerData;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 统计管理器
 * 负责管理玩家统计数据的缓存和更新
 * 
 * @author YourName
 * @version 1.0.0
 */
public class StatsManager {
    
    private final HunterGame plugin;
    private final PlayerRepository playerRepository;
    
    // 数据缓存
    private final Map<UUID, PlayerData> dataCache;
    
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
                plugin.getLogger().info("为玩家 " + player.getName() + " 创建新数据");
            } else {
                // 更新玩家名称
                if (!data.getName().equals(player.getName())) {
                    data.setName(player.getName());
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
                plugin.getLogger().info("为玩家 " + uuid + " 创建新数据");
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
                    plugin.getLogger().warning("保存玩家数据失败: " + data.getName());
                }
            });
        }
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
    
    /**
     * 增加游戏场次
     */
    public void addGame(UUID uuid) {
        PlayerData data = dataCache.get(uuid);
        if (data != null) {
            data.addGame();
        }
    }
    
    /**
     * 增加胜利次数
     */
    public void addWin(UUID uuid) {
        PlayerData data = dataCache.get(uuid);
        if (data != null) {
            data.addWin();
        }
    }
    
    /**
     * 增加失败次数
     */
    public void addLoss(UUID uuid) {
        PlayerData data = dataCache.get(uuid);
        if (data != null) {
            data.addLoss();
        }
    }
    
    /**
     * 增加猎人击杀数
     */
    public void addHunterKill(UUID uuid) {
        PlayerData data = dataCache.get(uuid);
        if (data != null) {
            data.addHunterKill();
        }
    }
    
    /**
     * 增加猎人死亡数
     */
    public void addHunterDeath(UUID uuid) {
        PlayerData data = dataCache.get(uuid);
        if (data != null) {
            data.addHunterDeath();
        }
    }
    
    /**
     * 增加逃生者逃脱次数
     */
    public void addSurvivorEscape(UUID uuid) {
        PlayerData data = dataCache.get(uuid);
        if (data != null) {
            data.addSurvivorEscape();
        }
    }
    
    /**
     * 增加逃生者死亡数
     */
    public void addSurvivorDeath(UUID uuid) {
        PlayerData data = dataCache.get(uuid);
        if (data != null) {
            data.addSurvivorDeath();
        }
    }
    
    /**
     * 增加生存时间
     */
    public void addSurvivalTime(UUID uuid, int seconds) {
        PlayerData data = dataCache.get(uuid);
        if (data != null) {
            data.addSurvivalTime(seconds);
        }
    }
    
    /**
     * 获取胜利排行榜
     */
    public void getTopWins(int limit, java.util.function.Consumer<List<PlayerData>> callback) {
        org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                List<PlayerData> list = playerRepository.getTopWins(limit);
                org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> callback.accept(list));
            } catch (Exception ex) {
                plugin.getLogger().severe("获取胜利排行榜失败: " + ex.getMessage());
                ex.printStackTrace();
                org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> callback.accept(new java.util.ArrayList<>()));
            }
        });
    }
    
    /**
     * 获取击杀排行榜
     */
    public void getTopKills(int limit, java.util.function.Consumer<List<PlayerData>> callback) {
        org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                List<PlayerData> list = playerRepository.getTopKills(limit);
                org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> callback.accept(list));
            } catch (Exception ex) {
                plugin.getLogger().severe("获取击杀排行榜失败: " + ex.getMessage());
                ex.printStackTrace();
                org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> callback.accept(new java.util.ArrayList<>()));
            }
        });
    }
    
    /**
     * 获取逃脱排行榜
     */
    public void getTopEscapes(int limit, java.util.function.Consumer<List<PlayerData>> callback) {
        org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                List<PlayerData> list = playerRepository.getTopEscapes(limit);
                org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> callback.accept(list));
            } catch (Exception ex) {
                plugin.getLogger().severe("获取逃脱排行榜失败: " + ex.getMessage());
                ex.printStackTrace();
                org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> callback.accept(new java.util.ArrayList<>()));
            }
        });
    }
    
    /**
     * 保存所有数据
     */
    public void saveAll() {
        plugin.getLogger().info("正在保存所有玩家数据...");
        
        for (UUID uuid : dataCache.keySet()) {
            savePlayerData(uuid);
        }
        
        plugin.getLogger().info("已保存 " + dataCache.size() + " 个玩家数据");
    }
}
