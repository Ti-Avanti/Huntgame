package com.minecraft.huntergame.manager;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.ServerMode;
import com.minecraft.huntergame.game.ManhuntGame;
import com.minecraft.huntergame.game.PlayerRole;
import com.minecraft.huntergame.models.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manhunt游戏管理器
 * 负责管理所有Manhunt游戏会话
 * 
 * @author YourName
 * @version 1.0.0
 */
public class ManhuntManager {
    
    private final HunterGame plugin;
    
    // 游戏会话管理
    private final Map<String, ManhuntGame> games;           // gameId -> ManhuntGame
    private final Map<UUID, String> playerGameMap;          // playerUUID -> gameId
    
    // 游戏ID计数器
    private int gameIdCounter = 0;
    
    public ManhuntManager(HunterGame plugin) {
        this.plugin = plugin;
        this.games = new ConcurrentHashMap<>();
        this.playerGameMap = new ConcurrentHashMap<>();
        
        plugin.getLogger().info("Manhunt管理器已初始化");
    }
    
    // ==================== 游戏会话管理 ====================
    
    /**
     * 创建新游戏
     */
    public ManhuntGame createGame(String worldName) {
        String gameId = generateGameId();
        ManhuntGame game = new ManhuntGame(plugin, gameId, worldName);
        games.put(gameId, game);
        
        // 同步游戏状态到Redis（如果启用Bungee模式）
        syncGameStateToRedis(game);
        
        plugin.getLogger().info("创建新游戏: " + gameId + " (世界: " + worldName + ")");
        return game;
    }
    
    /**
     * 获取游戏
     */
    public ManhuntGame getGame(String gameId) {
        return games.get(gameId);
    }
    
    /**
     * 移除游戏
     */
    public void removeGame(String gameId) {
        ManhuntGame game = games.remove(gameId);
        if (game != null) {
            // 清理玩家映射
            for (UUID uuid : game.getAllPlayers()) {
                playerGameMap.remove(uuid);
            }
            
            // 从Redis移除游戏状态（如果启用Bungee模式）
            removeGameStateFromRedis(gameId);
            
            plugin.getLogger().info("移除游戏: " + gameId);
        }
    }
    
    /**
     * 获取所有游戏
     */
    public Collection<ManhuntGame> getAllGames() {
        return games.values();
    }
    
    /**
     * 获取游戏数量
     */
    public int getGameCount() {
        return games.size();
    }
    
    /**
     * 生成游戏ID
     */
    private String generateGameId() {
        return "manhunt-" + (++gameIdCounter);
    }
    
    // ==================== 玩家-游戏映射 ====================
    
    /**
     * 玩家加入游戏
     */
    public boolean joinGame(Player player, String gameId) {
        ManhuntGame game = games.get(gameId);
        if (game == null) {
            return false;
        }
        
        // 检查玩家是否已在其他游戏中
        if (isInGame(player)) {
            return false;
        }
        
        // 添加玩家到游戏
        if (game.addPlayer(player.getUniqueId())) {
            playerGameMap.put(player.getUniqueId(), gameId);
            plugin.getLogger().info("玩家 " + player.getName() + " 加入游戏 " + gameId);
            return true;
        }
        
        return false;
    }
    
    /**
     * 玩家离开游戏
     */
    public void leaveGame(Player player) {
        String gameId = playerGameMap.remove(player.getUniqueId());
        if (gameId != null) {
            ManhuntGame game = games.get(gameId);
            if (game != null) {
                game.removePlayer(player.getUniqueId());
                plugin.getLogger().info("玩家 " + player.getName() + " 离开游戏 " + gameId);
                
                // 检查游戏是否应该结束
                if (game.shouldEnd()) {
                    endGame(gameId);
                }
            }
        }
    }
    
    /**
     * 获取玩家所在的游戏
     */
    public ManhuntGame getPlayerGame(Player player) {
        String gameId = playerGameMap.get(player.getUniqueId());
        return gameId != null ? games.get(gameId) : null;
    }
    
    /**
     * 获取玩家所在的游戏ID
     */
    public String getPlayerGameId(Player player) {
        return playerGameMap.get(player.getUniqueId());
    }
    
    /**
     * 检查玩家是否在游戏中
     */
    public boolean isInGame(Player player) {
        return playerGameMap.containsKey(player.getUniqueId());
    }
    
    // ==================== 游戏流程控制 ====================
    
    /**
     * 开始游戏
     */
    public void startGame(String gameId) {
        ManhuntGame game = games.get(gameId);
        if (game == null) {
            return;
        }
        
        // 获取所有玩家
        List<UUID> players = game.getAllPlayers();
        if (players.isEmpty()) {
            plugin.getLogger().warning("无法开始游戏 " + gameId + ": 没有玩家");
            return;
        }
        
        // 分配角色
        plugin.getRoleManager().assignRoles(game, players);
        
        // 通知角色
        plugin.getRoleManager().notifyRoles(game);
        
        // 传送玩家到出生点
        if (game.getSpawnLocation() != null) {
            for (UUID uuid : players) {
                Player player = plugin.getServer().getPlayer(uuid);
                if (player != null && player.isOnline()) {
                    player.teleport(game.getSpawnLocation());
                }
            }
        }
        
        // 给予初始装备
        plugin.getRoleManager().giveStartingItems(game);
        
        // 为所有玩家创建侧边栏
        for (UUID uuid : players) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null && player.isOnline()) {
                plugin.getSidebarManager().createSidebar(player, game);
            }
        }
        
        // 开始游戏（进入准备阶段）
        game.start();
        
        // 广播准备阶段消息
        broadcastToGame(game, com.minecraft.huntergame.util.Constants.SEPARATOR);
        broadcastToGame(game, "§e游戏即将开始！");
        broadcastToGame(game, "§e逃亡者有 §a" + game.getPrepareTime() + " §e秒准备时间");
        broadcastToGame(game, "§e猎人将被冻结，无法移动");
        broadcastToGame(game, com.minecraft.huntergame.util.Constants.SEPARATOR);
        
        plugin.getLogger().info("游戏 " + gameId + " 已开始");
    }
    
    /**
     * 结束游戏
     */
    public void endGame(String gameId) {
        ManhuntGame game = games.get(gameId);
        if (game == null) {
            return;
        }
        
        game.end();
        
        // 判定胜利方
        boolean runnersWin = game.isDragonDefeated();
        
        // 显示游戏结果
        broadcastToGame(game, com.minecraft.huntergame.util.Constants.SEPARATOR);
        broadcastToGame(game, "§e§l游戏结束！");
        broadcastToGame(game, "");
        
        if (runnersWin) {
            broadcastToGame(game, "§a§l逃亡者获胜！");
            broadcastToGame(game, "§7末影龙已被击败");
        } else {
            broadcastToGame(game, "§c§l猎人获胜！");
            broadcastToGame(game, "§7所有逃亡者已被淘汰");
        }
        
        broadcastToGame(game, "");
        broadcastToGame(game, "§e游戏时长: §a" + com.minecraft.huntergame.util.TimeUtil.formatTimeChinese(game.getElapsedTime()));
        broadcastToGame(game, com.minecraft.huntergame.util.Constants.SEPARATOR);
        
        // 更新统计数据
        updatePlayerStats(game, runnersWin);
        
        // 发放奖励
        giveRewards(game, runnersWin);
        
        // 传送玩家回出生点
        teleportPlayersToSpawn(game);
        
        // 移除所有玩家的侧边栏
        for (UUID uuid : game.getAllPlayers()) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null && player.isOnline()) {
                plugin.getSidebarManager().removeSidebar(player);
            }
        }
        
        // TODO: 重置游戏世界
        
        plugin.getLogger().info("游戏 " + gameId + " 已结束");
        
        // 延迟移除游戏
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            removeGame(gameId);
        }, 200L); // 10秒后移除
    }
    
    /**
     * 强制停止游戏
     */
    public void forceStopGame(String gameId) {
        ManhuntGame game = games.get(gameId);
        if (game == null) {
            return;
        }
        
        // 直接移除游戏
        removeGame(gameId);
        plugin.getLogger().info("游戏 " + gameId + " 已强制停止");
    }
    
    // ==================== 游戏检测 ====================
    
    /**
     * 检查所有游戏状态
     */
    public void checkAllGames() {
        for (ManhuntGame game : games.values()) {
            // 检查准备时间倒计时
            if (game.isPreparing()) {
                long remainingSeconds = (game.getPrepareEndTime() - System.currentTimeMillis()) / 1000;
                
                // 在特定时间点广播倒计时
                if (remainingSeconds == 10 || remainingSeconds == 5 || 
                    remainingSeconds == 3 || remainingSeconds == 2 || remainingSeconds == 1) {
                    broadcastToGame(game, "§e准备时间剩余: §c" + remainingSeconds + " §e秒");
                }
                
                // 检查准备时间是否结束
                if (game.isPrepareTimeEnded()) {
                    game.startPlaying();
                    
                    // 广播准备时间结束
                    broadcastToGame(game, com.minecraft.huntergame.util.Constants.SEPARATOR);
                    broadcastToGame(game, "§a§l准备时间结束！");
                    broadcastToGame(game, "§e猎人已解除冻结，开始追捕！");
                    broadcastToGame(game, com.minecraft.huntergame.util.Constants.SEPARATOR);
                    
                    // 同步状态到Redis
                    syncGameStateToRedis(game);
                    
                    plugin.getLogger().info("游戏 " + game.getGameId() + " 准备时间结束，正式开始");
                }
            }
            
            // 游戏进行中的时间提醒
            if (game.getState() == com.minecraft.huntergame.game.GameState.PLAYING) {
                checkGameTimeReminders(game);
            }
            
            // 更新所有玩家的侧边栏
            updateGameSidebars(game);
            
            // 检查游戏是否应该结束
            if (game.shouldEnd()) {
                endGame(game.getGameId());
            }
        }
    }
    
    /**
     * 检查游戏时间提醒
     */
    private void checkGameTimeReminders(ManhuntGame game) {
        // 如果没有时间限制，跳过
        if (game.getMaxGameTime() == 0) {
            return;
        }
        
        long remainingTime = game.getRemainingTime();
        
        // 在特定时间点广播提醒
        if (remainingTime == 3600) { // 1小时
            broadcastToGame(game, "§e游戏剩余时间: §c1小时");
        } else if (remainingTime == 1800) { // 30分钟
            broadcastToGame(game, "§e游戏剩余时间: §c30分钟");
        } else if (remainingTime == 600) { // 10分钟
            broadcastToGame(game, "§e游戏剩余时间: §c10分钟");
        } else if (remainingTime == 300) { // 5分钟
            broadcastToGame(game, "§e游戏剩余时间: §c5分钟");
        } else if (remainingTime == 60) { // 1分钟
            broadcastToGame(game, "§e游戏剩余时间: §c1分钟");
        } else if (remainingTime == 30 || remainingTime == 10 || 
                   remainingTime == 5 || remainingTime == 3 || 
                   remainingTime == 2 || remainingTime == 1) {
            broadcastToGame(game, "§e游戏剩余时间: §c" + remainingTime + " §e秒");
        }
    }
    
    /**
     * 更新游戏内所有玩家的侧边栏
     */
    private void updateGameSidebars(ManhuntGame game) {
        for (UUID uuid : game.getAllPlayers()) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null && player.isOnline()) {
                plugin.getSidebarManager().updateSidebar(player);
            }
        }
    }
    
    /**
     * 启动游戏检测任务
     */
    public void startGameCheckTask() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            // 性能优化：如果没有游戏，跳过检查
            if (games.isEmpty()) {
                return;
            }
            checkAllGames();
        }, 20L, 20L); // 每秒检测一次
    }
    
    // ==================== 辅助方法 ====================
    
    /**
     * 更新玩家统计数据
     */
    private void updatePlayerStats(ManhuntGame game, boolean runnersWin) {
        for (UUID uuid : game.getAllPlayers()) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player == null) continue;
            
            PlayerData data = plugin.getStatsManager().getPlayerData(player);
            if (data == null) continue;
            
            PlayerRole role = game.getPlayerRole(uuid);
            if (role == null) continue;
            
            // 更新胜利统计
            if (role == PlayerRole.RUNNER && runnersWin) {
                data.setRunnerWins(data.getRunnerWins() + 1);
            } else if (role == PlayerRole.HUNTER && !runnersWin) {
                data.setHunterWins(data.getHunterWins() + 1);
            }
            
            // 如果末影龙被击败，增加击杀龙统计
            if (game.isDragonDefeated() && role == PlayerRole.RUNNER) {
                data.setDragonKills(data.getDragonKills() + 1);
            }
            
            // 保存数据
            plugin.getStatsManager().savePlayerData(player);
        }
    }
    
    /**
     * 发放奖励
     */
    private void giveRewards(ManhuntGame game, boolean runnersWin) {
        // 检查是否启用Vault集成
        if (!plugin.getIntegrationManager().isVaultEnabled()) {
            return;
        }
        
        for (UUID uuid : game.getAllPlayers()) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player == null || !player.isOnline()) continue;
            
            PlayerRole role = game.getPlayerRole(uuid);
            if (role == null || role == PlayerRole.SPECTATOR) continue;
            
            double reward = 0;
            
            // 根据角色和胜负发放奖励
            if (role == PlayerRole.RUNNER && runnersWin) {
                reward = plugin.getConfig().getDouble("manhunt.rewards.runner-win", 100.0);
            } else if (role == PlayerRole.HUNTER && !runnersWin) {
                reward = plugin.getConfig().getDouble("manhunt.rewards.hunter-win", 100.0);
            } else {
                // 失败方也给予参与奖励
                reward = plugin.getConfig().getDouble("manhunt.rewards.participation", 20.0);
            }
            
            // 发放奖励
            if (reward > 0) {
                plugin.getIntegrationManager().getVaultIntegration().giveMoney(player, reward);
                player.sendMessage(ChatColor.GOLD + "你获得了 " + ChatColor.GREEN + reward + ChatColor.GOLD + " 金币奖励！");
            }
        }
    }
    
    /**
     * 传送玩家回出生点
     */
    private void teleportPlayersToSpawn(ManhuntGame game) {
        Location spawnLocation = game.getSpawnLocation();
        if (spawnLocation == null) {
            // 如果没有设置出生点，使用世界默认出生点
            spawnLocation = plugin.getServer().getWorlds().get(0).getSpawnLocation();
        }
        
        for (UUID uuid : game.getAllPlayers()) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.teleport(spawnLocation);
                
                // 恢复玩家状态
                player.setHealth(player.getMaxHealth());
                player.setFoodLevel(20);
                player.getInventory().clear();
                player.setGameMode(org.bukkit.GameMode.SURVIVAL);
            }
        }
    }
    
    /**
     * 向游戏内所有玩家广播消息
     */
    private void broadcastToGame(ManhuntGame game, String message) {
        for (UUID uuid : game.getAllPlayers()) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.sendMessage(message);
            }
        }
    }
    

    
    /**
     * 同步游戏状态到Redis
     */
    private void syncGameStateToRedis(ManhuntGame game) {
        if (plugin.getServerMode() != ServerMode.BUNGEE) {
            return;
        }
        
        if (plugin.getRedisManager() == null || !plugin.getRedisManager().isConnected()) {
            return;
        }
        
        plugin.getRedisManager().syncManhuntGameState(
            game.getGameId(),
            game.getState().name(),
            game.getPlayerCount()
        );
    }
    
    /**
     * 从Redis移除游戏状态
     */
    private void removeGameStateFromRedis(String gameId) {
        if (plugin.getServerMode() != ServerMode.BUNGEE) {
            return;
        }
        
        if (plugin.getRedisManager() == null || !plugin.getRedisManager().isConnected()) {
            return;
        }
        
        plugin.getRedisManager().removeManhuntGameState(gameId);
    }
    
    // ==================== 清理 ====================
    
    /**
     * 关闭管理器
     */
    public void shutdown() {
        // 结束所有游戏
        for (String gameId : new ArrayList<>(games.keySet())) {
            forceStopGame(gameId);
        }
        
        games.clear();
        playerGameMap.clear();
        
        plugin.getLogger().info("Manhunt管理器已关闭");
    }
}
