package com.minecraft.huntergame.manager;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.ServerMode;
import com.minecraft.huntergame.game.ManhuntGame;
import com.minecraft.huntergame.game.PlayerRole;
import com.minecraft.huntergame.models.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
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
     * 检查是否可以创建游戏
     * 
     * @return 是否可以创建
     */
    public boolean canCreateGame() {
        // 检查服务器类型
        if (plugin.getManhuntConfig().getServerType() == com.minecraft.huntergame.config.ServerType.MAIN_LOBBY) {
            plugin.getLogger().warning("主大厅服务器不能创建游戏");
            return false;
        }
        
        // 检查单场比赛模式
        if (plugin.getManhuntConfig().isSingleGameMode() && !games.isEmpty()) {
            plugin.getLogger().warning("单场比赛模式下不能创建多个游戏实例");
            return false;
        }
        
        return true;
    }
    
    /**
     * 创建新游戏
     */
    public ManhuntGame createGame(String worldName) {
        // 检查是否可以创建游戏
        if (!canCreateGame()) {
            return null;
        }
        
        String gameId = generateGameId();
        ManhuntGame game = new ManhuntGame(plugin, gameId, worldName);
        registerGame(game);
        
        plugin.getLogger().info("创建新游戏: " + gameId + " (世界: " + worldName + ")");
        return game;
    }
    
    /**
     * 注册游戏到管理器
     * 用于Builder模式创建的游戏
     */
    public void registerGame(ManhuntGame game) {
        games.put(game.getGameId(), game);
        
        // 同步游戏状态到Redis（如果启用Bungee模式）
        syncGameStateToRedis(game);
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
            // 移除所有玩家的计分板
            for (UUID uuid : game.getAllPlayers()) {
                Player player = plugin.getServer().getPlayer(uuid);
                if (player != null && player.isOnline()) {
                    plugin.getSidebarManager().removeMatchingSidebar(player);
                    plugin.getSidebarManager().removeLobbySidebar(player);
                    plugin.getSidebarManager().removeSidebar(player);
                }
                
                // 清理玩家映射
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
        plugin.debug("joinGame called: player=" + player.getName() + ", gameId=" + gameId);
        
        ManhuntGame game = games.get(gameId);
        if (game == null) {
            plugin.debug("Game not found: " + gameId);
            player.sendMessage("§c游戏不存在！");
            return false;
        }
        
        // 检查玩家是否已在其他游戏中
        if (isInGame(player)) {
            plugin.debug("Player already in game: " + player.getName());
            player.sendMessage("§c你已经在游戏中了！");
            return false;
        }
        
        // 额外的状态检查：明确拒绝准备阶段、游戏中、结束阶段的加入请求
        com.minecraft.huntergame.game.GameState state = game.getState();
        if (state == com.minecraft.huntergame.game.GameState.PREPARING) {
            plugin.debug("Cannot join: game is in PREPARING state");
            player.sendMessage("§c游戏已经开始准备，无法加入！");
            player.sendMessage("§e请等待当前游戏结束后再试");
            return false;
        }
        
        if (state == com.minecraft.huntergame.game.GameState.PLAYING) {
            plugin.debug("Cannot join: game is in PLAYING state");
            player.sendMessage("§c游戏正在进行中，无法加入！");
            player.sendMessage("§e你可以使用 §a/manhunt spectate §e观战游戏");
            return false;
        }
        
        if (state == com.minecraft.huntergame.game.GameState.ENDING || 
            state == com.minecraft.huntergame.game.GameState.RESTARTING) {
            plugin.debug("Cannot join: game is ending or restarting");
            player.sendMessage("§c游戏正在结束，无法加入！");
            player.sendMessage("§e请稍后再试");
            return false;
        }
        
        // 只允许在 WAITING 和 MATCHING 状态加入
        if (state != com.minecraft.huntergame.game.GameState.WAITING && 
            state != com.minecraft.huntergame.game.GameState.MATCHING) {
            plugin.debug("Cannot join: invalid game state: " + state);
            player.sendMessage("§c当前游戏状态不允许加入！");
            return false;
        }
        
        // 添加玩家到游戏
        boolean added = game.addPlayer(player.getUniqueId());
        plugin.debug("addPlayer result: " + added);
        
        if (added) {
            playerGameMap.put(player.getUniqueId(), gameId);
            plugin.getLogger().info("玩家 " + player.getName() + " 加入游戏 " + gameId);
            plugin.debug("Player successfully joined game");
            
            // 根据游戏状态创建对应的计分板
            // state 变量已在上面定义，这里直接使用
            plugin.debug("Game state: " + state + ", creating appropriate sidebar");
            
            if (state == com.minecraft.huntergame.game.GameState.WAITING) {
                // 等待状态 - 显示大厅计分板
                plugin.getSidebarManager().createLobbySidebar(player);
                plugin.debug("Created lobby sidebar for: " + player.getName());
                
                // 给予匹配道具（等待状态下玩家已在房间中）
                plugin.getHotbarManager().giveMatchingItems(player, game);
            } else if (state == com.minecraft.huntergame.game.GameState.MATCHING) {
                // 匹配状态 - 显示匹配计分板
                plugin.getSidebarManager().createMatchingSidebar(player, game);
                plugin.debug("Created matching sidebar for: " + player.getName());
                
                // 给予匹配道具
                plugin.getHotbarManager().giveMatchingItems(player, game);
            } else if (state.isPlaying()) {
                // 游戏进行中 - 显示游戏计分板
                plugin.getSidebarManager().createSidebar(player, game);
                plugin.debug("Created game sidebar for: " + player.getName());
            }
            
            // Bungee模式：立即更新Redis服务器状态
            if (plugin.getServerMode() == ServerMode.BUNGEE && 
                plugin.getRedisManager() != null && 
                plugin.getRedisManager().isConnected()) {
                
                int playerCount = plugin.getServer().getOnlinePlayers().size();
                String status = determineServerStatus();
                plugin.getRedisManager().syncServerStatus(
                    plugin.getRedisManager().getServerName(), 
                    status, 
                    playerCount
                );
                plugin.debug("玩家加入后立即更新Redis状态: " + status + ", 玩家数: " + playerCount);
            }
            
            return true;
        }
        
        plugin.debug("Failed to add player to game");
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
                
                // 传送玩家到大厅
                teleportPlayerToLobby(player);
                
                // 移除玩家的所有侧边栏
                plugin.getSidebarManager().removeMatchingSidebar(player);
                plugin.getSidebarManager().removeLobbySidebar(player);
                plugin.getSidebarManager().removeSidebar(player);
                
                // 恢复玩家状态
                player.setHealth(player.getMaxHealth());
                player.setFoodLevel(20);
                player.getInventory().clear();
                player.setGameMode(org.bukkit.GameMode.SURVIVAL);
                
                // 重置玩家数据（经验、配方、成就）
                resetPlayerData(player);
                
                // 给予大厅道具
                plugin.getHotbarManager().giveLobbyItems(player);
                
                // 创建大厅计分板
                plugin.getSidebarManager().createLobbySidebar(player);
                
                // Bungee模式：立即更新Redis服务器状态
                if (plugin.getServerMode() == ServerMode.BUNGEE && 
                    plugin.getRedisManager() != null && 
                    plugin.getRedisManager().isConnected()) {
                    
                    int playerCount = plugin.getServer().getOnlinePlayers().size();
                    String status = determineServerStatus();
                    plugin.getRedisManager().syncServerStatus(
                        plugin.getRedisManager().getServerName(), 
                        status, 
                        playerCount
                    );
                    plugin.debug("玩家离开后立即更新Redis状态: " + status + ", 玩家数: " + playerCount);
                }
                
                // 检查匹配状态：如果在匹配中且人数不足，取消匹配
                if (game.isMatching() && !game.hasMinPlayers()) {
                    plugin.getLogger().info("匹配中玩家离开导致人数不足，取消匹配: " + gameId);
                    broadcastToGame(game, "§c人数不足，匹配已取消");
                    cancelGame(game);
                    return;
                }
                
                // 检查游戏是否应该结束
                if (game.shouldEnd()) {
                    // 判断结束原因
                    com.minecraft.huntergame.game.GameEndReason reason = determineEndReason(game);
                    endGame(gameId, reason);
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
    
    /**
     * 从游戏中移除玩家
     */
    public void removePlayer(Player player, ManhuntGame game) {
        if (player == null || game == null) {
            return;
        }
        
        UUID uuid = player.getUniqueId();
        
        // 从游戏中移除玩家
        game.removePlayer(uuid);
        
        // 从玩家-游戏映射中移除
        playerGameMap.remove(uuid);
        
        plugin.getLogger().info("玩家 " + player.getName() + " 已离开游戏 " + game.getGameId());
    }
    
    // ==================== 游戏流程控制 ====================
    
    /**
     * 开始游戏
     */
    public void startGame(String gameId) {
        plugin.debug("startGame called: gameId=" + gameId);
        
        ManhuntGame game = games.get(gameId);
        if (game == null) {
            plugin.debug("Game not found: " + gameId);
            return;
        }
        
        plugin.debug("Game state: " + game.getState());
        
        // 获取所有玩家
        List<UUID> players = game.getAllPlayers();
        plugin.debug("Player count: " + players.size());
        
        if (players.isEmpty()) {
            plugin.getLogger().warning("无法开始游戏 " + gameId + ": 没有玩家");
            plugin.debug("No players in game");
            return;
        }
        
        // 分配角色
        plugin.debug("Assigning roles...");
        plugin.getRoleManager().assignRoles(game, players);
        
        // 通知角色
        plugin.debug("Notifying roles...");
        plugin.getRoleManager().notifyRoles(game);
        
        // 随机选择安全的出生点
        plugin.debug("Finding random safe spawn location...");
        World gameWorld = plugin.getServer().getWorld(game.getWorldName());
        
        // 如果世界不存在，尝试加载或创建世界
        if (gameWorld == null) {
            plugin.getLogger().warning("游戏世界未加载: " + game.getWorldName() + "，正在尝试加载...");
            gameWorld = plugin.getWorldManager().loadOrCreateWorld(game.getWorldName());
            
            if (gameWorld == null) {
                plugin.getLogger().severe("无法加载游戏世界: " + game.getWorldName());
                plugin.getLogger().severe("游戏启动失败！");
                return;
            }
            
            plugin.getLogger().info("游戏世界加载成功: " + game.getWorldName());
        }
        
        if (gameWorld != null) {
            Location spawnLocation = com.minecraft.huntergame.util.LocationUtil.findRandomSafeSpawn(gameWorld, 50);
            game.setSpawnLocation(spawnLocation);
            plugin.getLogger().info("随机出生点: " + com.minecraft.huntergame.util.LocationUtil.formatLocation(spawnLocation));
        }
        
        // 传送玩家到出生点并设置初始状态
        if (game.getSpawnLocation() != null) {
            plugin.debug("Teleporting players to spawn...");
            for (UUID uuid : players) {
                Player player = plugin.getServer().getPlayer(uuid);
                if (player != null && player.isOnline()) {
                    player.teleport(game.getSpawnLocation());
                    
                    // 设置满的生命值、饱食度和饱和度
                    player.setHealth(player.getMaxHealth());
                    player.setFoodLevel(20); // 满饱食度
                    player.setSaturation(20.0f); // 满饱和度
                    player.setExhaustion(0.0f); // 清除疲劳值
                    
                    plugin.debug("Teleported and set full stats for: " + player.getName());
                }
            }
        } else {
            plugin.debug("WARNING: No spawn location set!");
        }
        
        // 给予初始装备
        plugin.debug("Giving starting items...");
        plugin.getRoleManager().giveStartingItems(game);
        
        // 为所有玩家创建侧边栏
        plugin.debug("Creating sidebars...");
        for (UUID uuid : players) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null && player.isOnline()) {
                plugin.getSidebarManager().createSidebar(player, game);
                plugin.debug("Created sidebar for: " + player.getName());
            }
        }
        
        // 开始游戏（进入准备阶段）
        plugin.debug("Starting game (entering PREPARING state)...");
        game.start();
        plugin.debug("Game state after start: " + game.getState());
        
        // 广播准备阶段消息
        broadcastToGame(game, com.minecraft.huntergame.util.Constants.SEPARATOR);
        broadcastToGame(game, "§e游戏即将开始！");
        broadcastToGame(game, "§e逃亡者有 §a" + game.getPrepareTime() + " §e秒准备时间");
        broadcastToGame(game, "§e猎人将被冻结，无法移动");
        broadcastToGame(game, com.minecraft.huntergame.util.Constants.SEPARATOR);
        
        plugin.getLogger().info("游戏 " + gameId + " 已开始");
        plugin.debug("startGame completed successfully");
    }
    
    /**
     * 结束游戏
     */
    public void endGame(String gameId) {
        endGame(gameId, com.minecraft.huntergame.game.GameEndReason.UNKNOWN);
    }
    
    /**
     * 结束游戏（带结束原因）
     */
    public void endGame(ManhuntGame game, com.minecraft.huntergame.game.GameEndReason reason) {
        if (game == null) {
            return;
        }
        endGame(game.getGameId(), reason);
    }
    
    /**
     * 结束游戏（带结束原因）
     */
    public void endGame(String gameId, com.minecraft.huntergame.game.GameEndReason reason) {
        ManhuntGame game = games.get(gameId);
        if (game == null) {
            return;
        }
        
        game.end();
        
        // 判定胜利方
        boolean runnersWin = reason.isRunnersWin();
        
        // 显示游戏结果
        broadcastToGame(game, com.minecraft.huntergame.util.Constants.SEPARATOR);
        broadcastToGame(game, "§e§l游戏结束！");
        broadcastToGame(game, "");
        
        // 显示结束原因和胜利方
        if (reason == com.minecraft.huntergame.game.GameEndReason.RUNNERS_WIN_DRAGON) {
            broadcastToGame(game, "§a§l逃亡者获胜！");
            broadcastToGame(game, "§7末影龙已被击败");
        } else if (reason == com.minecraft.huntergame.game.GameEndReason.HUNTERS_WIN_KILL) {
            broadcastToGame(game, "§c§l猎人获胜！");
            broadcastToGame(game, "§7所有逃亡者已被淘汰");
        } else if (reason == com.minecraft.huntergame.game.GameEndReason.HUNTERS_LEFT) {
            broadcastToGame(game, "§a§l逃亡者获胜！");
            broadcastToGame(game, "§7所有猎人已离开游戏");
        } else if (reason == com.minecraft.huntergame.game.GameEndReason.RUNNERS_LEFT) {
            broadcastToGame(game, "§c§l猎人获胜！");
            broadcastToGame(game, "§7所有逃亡者已离开游戏");
        } else if (reason == com.minecraft.huntergame.game.GameEndReason.TIMEOUT) {
            broadcastToGame(game, "§e§l游戏超时！");
            if (runnersWin) {
                broadcastToGame(game, "§7逃亡者存活，逃亡者获胜");
            } else {
                broadcastToGame(game, "§7猎人获胜");
            }
        } else {
            // 其他原因（取消、管理员停止等）
            broadcastToGame(game, reason.getDisplayName());
        }
        
        broadcastToGame(game, "");
        broadcastToGame(game, "§e游戏时长: §a" + com.minecraft.huntergame.util.TimeUtil.formatTimeChinese(game.getElapsedTime()));
        broadcastToGame(game, com.minecraft.huntergame.util.Constants.SEPARATOR);
        
        // 只有正常结束才更新统计和发放奖励
        if (reason.isNormalEnd()) {
            // 更新统计数据
            updatePlayerStats(game, runnersWin);
            
            // 发放奖励
            giveRewards(game, runnersWin);
        }
        
        // 获取返回延迟时间（秒）
        int returnDelay = plugin.getManhuntConfig().getReturnDelay();
        long delayTicks = returnDelay * 20L; // 转换为tick
        
        plugin.getLogger().info("游戏结束，" + returnDelay + "秒后传送玩家到大厅");
        
        // 延迟传送玩家到大厅
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            // 传送玩家到大厅
            teleportPlayersToLobby(game);
            
            // Bungee 模式：传送玩家回主大厅
            if (plugin.getServerMode() == ServerMode.BUNGEE) {
                java.util.List<Player> onlinePlayers = new java.util.ArrayList<>();
                for (UUID uuid : game.getAllPlayers()) {
                    Player player = plugin.getServer().getPlayer(uuid);
                    if (player != null && player.isOnline()) {
                        onlinePlayers.add(player);
                    }
                }
                
                if (!onlinePlayers.isEmpty()) {
                    plugin.getBungeeManager().sendPlayersToMainLobby(onlinePlayers);
                }
            }
            
            // 移除所有玩家的侧边栏
            for (UUID uuid : game.getAllPlayers()) {
                Player player = plugin.getServer().getPlayer(uuid);
                if (player != null && player.isOnline()) {
                    plugin.getSidebarManager().removeSidebar(player);
                }
            }
        }, delayTicks);
        
        // 延迟重置世界，确保玩家已传送
        if (plugin.getManhuntConfig().isResetWorldOnEnd()) {
            String worldName = game.getWorldName();
            plugin.getLogger().info("准备重置游戏世界: " + worldName);
            
            // 在玩家传送后再重置世界
            long resetDelay = delayTicks + 60L; // 传送延迟 + 3秒
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                boolean success = plugin.getWorldManager().resetWorld(worldName);
                if (success) {
                    plugin.getLogger().info("游戏世界已重置: " + worldName);
                } else {
                    plugin.getLogger().warning("游戏世界重置失败: " + worldName);
                }
            }, resetDelay);
        }
        
        plugin.getLogger().info("游戏 " + gameId + " 已结束 (原因: " + reason.name() + ")");
        
        // 延迟移除游戏
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            removeGame(gameId);
        }, 200L); // 10秒后移除
    }
    
    /**
     * 取消游戏（人数不足等原因）
     */
    public void cancelGame(ManhuntGame game) {
        if (game == null) {
            return;
        }
        
        String gameId = game.getGameId();
        
        // 通知所有玩家
        broadcastToGame(game, "§c游戏已取消！");
        
        // 处理所有玩家
        for (UUID uuid : game.getAllPlayers()) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null && player.isOnline()) {
                // 移除所有计分板
                plugin.getSidebarManager().removeMatchingSidebar(player);
                plugin.getSidebarManager().removeLobbySidebar(player);
                plugin.getSidebarManager().removeSidebar(player);
                
                // 恢复玩家状态
                player.setHealth(player.getMaxHealth());
                player.setFoodLevel(20);
                player.getInventory().clear();
                player.setGameMode(org.bukkit.GameMode.SURVIVAL);
                
                // 重置玩家数据（经验、配方、成就）
                resetPlayerData(player);
                
                // 给予大厅道具
                plugin.getHotbarManager().giveLobbyItems(player);
                
                // 创建大厅计分板
                plugin.getSidebarManager().createLobbySidebar(player);
            }
        }
        
        // 传送玩家到大厅
        teleportPlayersToLobby(game);
        
        // 移除游戏
        removeGame(gameId);
        
        plugin.getLogger().info("游戏 " + gameId + " 已取消");
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
            // 检查匹配超时
            if (game.isMatching()) {
                long remainingSeconds = game.getMatchingRemainingTime();
                
                // 显示匹配倒计时（使用Title + ActionBar）
                if (remainingSeconds == 60 || remainingSeconds == 30 || remainingSeconds == 20 ||
                    remainingSeconds == 10 || remainingSeconds == 5 || 
                    remainingSeconds == 3 || remainingSeconds == 2 || remainingSeconds == 1) {
                    
                    for (UUID uuid : game.getAllPlayers()) {
                        Player player = plugin.getServer().getPlayer(uuid);
                        if (player != null && player.isOnline()) {
                            player.sendTitle(
                                "§e§l匹配中",
                                "§7" + remainingSeconds + "秒后自动开始 §8| §a" + game.getPlayerCount() + "§7/§a" + (game.getMaxRunners() + game.getMaxHunters()) + " §7玩家",
                                10, 40, 10
                            );
                        }
                    }
                }
                
                // 每秒显示ActionBar倒计时（更明显的提示）
                String actionBarMessage = "§e§l匹配中... §a" + remainingSeconds + "秒 §7| §a" + 
                                         game.getPlayerCount() + "§7/§a" + (game.getMaxRunners() + game.getMaxHunters()) + " §7玩家";
                
                for (UUID uuid : game.getAllPlayers()) {
                    Player player = plugin.getServer().getPlayer(uuid);
                    if (player != null && player.isOnline()) {
                        // 使用 spigot().sendMessage() 发送ActionBar
                        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, 
                            net.md_5.bungee.api.chat.TextComponent.fromLegacyText(actionBarMessage));
                    }
                }
                
                // 检查人数是否不足最低要求
                if (!game.hasMinPlayers()) {
                    broadcastToGame(game, "§c人数不足，匹配已取消");
                    cancelGame(game);
                    continue;
                }
                
                // 检查是否达到最大人数，自动开始
                if (game.isFull()) {
                    broadcastToGame(game, "§a人数已满，游戏即将开始！");
                    startGame(game.getGameId());
                    continue;
                }
                
                // 检查匹配是否超时
                if (game.isMatchingTimeout()) {
                    if (plugin.getManhuntConfig().isMatchingAutoStart() && game.hasMinPlayers()) {
                        broadcastToGame(game, "§e匹配时间结束，游戏即将开始！");
                        startGame(game.getGameId());
                    } else {
                        broadcastToGame(game, "§c匹配超时，人数不足，游戏取消");
                        cancelGame(game);
                    }
                    continue;
                }
            }
            
            // 检查准备时间倒计时
            if (game.isPreparing()) {
                long remainingSeconds = (game.getPrepareEndTime() - System.currentTimeMillis()) / 1000;
                
                // 在特定时间点广播倒计时
                if (remainingSeconds == 10 || remainingSeconds == 5 || 
                    remainingSeconds == 3 || remainingSeconds == 2 || remainingSeconds == 1) {
                    broadcastToGame(game, "§e准备时间剩余: §c" + remainingSeconds + " §e秒");
                    
                    // 为猎人显示 Title 倒计时提示
                    for (UUID uuid : game.getHunters()) {
                        Player hunter = plugin.getServer().getPlayer(uuid);
                        if (hunter != null && hunter.isOnline()) {
                            String titleColor;
                            if (remainingSeconds <= 3) {
                                titleColor = "§c§l"; // 红色加粗
                            } else if (remainingSeconds <= 5) {
                                titleColor = "§e§l"; // 黄色加粗
                            } else {
                                titleColor = "§a§l"; // 绿色加粗
                            }
                            
                            hunter.sendTitle(
                                titleColor + remainingSeconds,
                                "§7定身时间剩余",
                                5, 15, 5
                            );
                        }
                    }
                }
                
                // 检查准备时间是否结束
                if (game.isPrepareTimeEnded()) {
                    game.startPlaying();
                    
                    // 为猎人显示解除冻结的 Title
                    for (UUID uuid : game.getHunters()) {
                        Player hunter = plugin.getServer().getPlayer(uuid);
                        if (hunter != null && hunter.isOnline()) {
                            hunter.sendTitle(
                                "§a§l解除冻结！",
                                "§7开始追捕逃亡者",
                                10, 40, 10
                            );
                        }
                    }
                    
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
                // 判断结束原因
                com.minecraft.huntergame.game.GameEndReason reason = determineEndReason(game);
                endGame(game.getGameId(), reason);
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
     * 判断游戏结束原因（公开方法供外部调用）
     */
    public com.minecraft.huntergame.game.GameEndReason determineEndReason(ManhuntGame game) {
        // 检查末影龙是否被击败
        if (game.isDragonDefeated()) {
            return com.minecraft.huntergame.game.GameEndReason.RUNNERS_WIN_DRAGON;
        }
        
        // 获取存活的逃亡者和猎人
        java.util.List<UUID> aliveRunners = game.getAliveRunners();
        java.util.List<UUID> aliveHunters = game.getAliveHunters();
        
        // 检查所有逃亡者是否不在游戏中
        boolean allRunnersLeft = true;
        for (UUID uuid : game.getRunners()) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null && player.isOnline()) {
                allRunnersLeft = false;
                break;
            }
        }
        
        // 检查所有猎人是否不在游戏中
        boolean allHuntersLeft = true;
        for (UUID uuid : game.getHunters()) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null && player.isOnline()) {
                allHuntersLeft = false;
                break;
            }
        }
        
        // 如果所有逃亡者都离开了服务器（不在线）
        if (allRunnersLeft) {
            return com.minecraft.huntergame.game.GameEndReason.RUNNERS_LEFT;
        }
        
        // 如果所有猎人都离开了服务器（不在线）
        if (allHuntersLeft) {
            return com.minecraft.huntergame.game.GameEndReason.HUNTERS_LEFT;
        }
        
        // 如果所有逃亡者被淘汰（在线但已死亡/观战）
        if (aliveRunners.isEmpty()) {
            return com.minecraft.huntergame.game.GameEndReason.HUNTERS_WIN_KILL;
        }
        
        // 检查游戏时间是否结束
        if (game.getMaxGameTime() > 0) {
            long elapsed = game.getElapsedTime();
            if (elapsed >= game.getMaxGameTime()) {
                return com.minecraft.huntergame.game.GameEndReason.TIMEOUT;
            }
        }
        
        // 默认未知原因
        return com.minecraft.huntergame.game.GameEndReason.UNKNOWN;
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
     * 更新玩家段位分数
     * 逃亡者获胜：+20分，失败：-10分
     * 猎人获胜：+10分，失败：-20分
     */
    private void updatePlayerStats(ManhuntGame game, boolean runnersWin) {
        for (UUID uuid : game.getAllPlayers()) {
            // 获取玩家角色
            PlayerRole role = game.getPlayerRole(uuid);
            if (role == null || role == PlayerRole.SPECTATOR) continue;
            
            // 获取玩家数据（即使玩家离线也要更新）
            PlayerData data = plugin.getStatsManager().getPlayerData(uuid);
            
            // 如果数据不在缓存中，尝试加载
            if (data == null) {
                plugin.debug("玩家数据不在缓存中，尝试加载: " + uuid);
                // 同步加载玩家数据
                try {
                    data = plugin.getPlayerRepository().load(uuid);
                    if (data == null) {
                        plugin.getLogger().warning("无法加载玩家数据: " + uuid);
                        continue;
                    }
                    plugin.debug("成功加载玩家数据: " + uuid);
                } catch (Exception ex) {
                    plugin.getLogger().warning("加载玩家数据失败: " + uuid + " - " + ex.getMessage());
                    continue;
                }
            }
            
            plugin.debug("更新玩家段位: " + uuid + ", 角色: " + role);
            
            int oldScore = data.getScore();
            com.minecraft.huntergame.rank.Rank oldRank = data.getCurrentRank();
            
            // 判断是否胜利
            boolean isWinner = (role == PlayerRole.RUNNER && runnersWin) || 
                              (role == PlayerRole.HUNTER && !runnersWin);
            
            // 根据角色和胜负加减分
            int scoreChange = 0;
            if (role == PlayerRole.RUNNER) {
                if (isWinner) {
                    scoreChange = 20; // 逃亡者获胜 +20分
                    plugin.debug("逃亡者获胜: +" + scoreChange + "分");
                } else {
                    scoreChange = -10; // 逃亡者失败 -10分
                    plugin.debug("逃亡者失败: " + scoreChange + "分");
                }
            } else if (role == PlayerRole.HUNTER) {
                if (isWinner) {
                    scoreChange = 10; // 猎人获胜 +10分
                    plugin.debug("猎人获胜: +" + scoreChange + "分");
                } else {
                    scoreChange = -20; // 猎人失败 -20分
                    plugin.debug("猎人失败: " + scoreChange + "分");
                }
            }
            
            // 更新分数
            data.addScore(scoreChange);
            
            int newScore = data.getScore();
            com.minecraft.huntergame.rank.Rank newRank = data.getCurrentRank();
            
            plugin.debug("分数变化: " + oldScore + " -> " + newScore + " (" + (scoreChange > 0 ? "+" : "") + scoreChange + ")");
            
            // 检查段位变化
            if (newRank.ordinal() > oldRank.ordinal()) {
                // 晋级
                plugin.debug("玩家晋级: " + oldRank.getDisplayName() + " -> " + newRank.getDisplayName());
                
                // 通知玩家晋级
                Player player = plugin.getServer().getPlayer(uuid);
                if (player != null && player.isOnline()) {
                    player.sendMessage("§6§l恭喜！你已晋级到 " + newRank.getColoredName() + "§6§l！");
                    player.sendMessage("§e当前分数: §a" + newScore + " §7(" + (scoreChange > 0 ? "+" : "") + scoreChange + ")");
                }
            } else if (newRank.ordinal() < oldRank.ordinal()) {
                // 掉段
                plugin.debug("玩家掉段: " + oldRank.getDisplayName() + " -> " + newRank.getDisplayName());
                
                // 通知玩家掉段
                Player player = plugin.getServer().getPlayer(uuid);
                if (player != null && player.isOnline()) {
                    player.sendMessage("§c你已掉段到 " + newRank.getColoredName() + "§c！");
                    player.sendMessage("§e当前分数: §c" + newScore + " §7(" + scoreChange + ")");
                }
            } else {
                // 段位未变化，只通知分数变化
                Player player = plugin.getServer().getPlayer(uuid);
                if (player != null && player.isOnline()) {
                    String scoreColor = scoreChange > 0 ? "§a" : "§c";
                    player.sendMessage("§e段位分数: " + scoreColor + (scoreChange > 0 ? "+" : "") + scoreChange + " §7(当前: " + newScore + ")");
                }
            }
            
            // 直接保存数据到数据库（异步保存）
            final PlayerData finalData = data;
            plugin.getPlayerRepository().saveAsync(data, success -> {
                if (success) {
                    plugin.debug("成功保存玩家段位数据: " + uuid);
                } else {
                    plugin.getLogger().warning("保存玩家段位数据失败: " + uuid);
                }
            });
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
     * 传送所有玩家到大厅
     */
    private void teleportPlayersToLobby(ManhuntGame game) {
        for (UUID uuid : game.getAllPlayers()) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null && player.isOnline()) {
                teleportPlayerToLobby(player);
                
                // 恢复玩家状态
                player.setHealth(player.getMaxHealth());
                player.setFoodLevel(20);
                player.getInventory().clear();
                player.setGameMode(org.bukkit.GameMode.SURVIVAL);
                
                // 重置玩家数据
                resetPlayerData(player);
            }
        }
    }
    
    /**
     * 重置玩家数据（经验、配方、成就）
     */
    private void resetPlayerData(Player player) {
        // 重置经验
        player.setLevel(0);
        player.setExp(0);
        player.setTotalExperience(0);
        
        // 重置配方（清除所有已解锁的配方）
        // 注意：这会清除玩家所有已解锁的配方，包括默认配方
        // 如果需要保留默认配方，可以选择性清除
        for (org.bukkit.NamespacedKey recipe : player.getDiscoveredRecipes()) {
            player.undiscoverRecipe(recipe);
        }
        
        // 重置成就（进度）
        // 遍历所有进度并重置
        java.util.Iterator<org.bukkit.advancement.Advancement> iterator = plugin.getServer().advancementIterator();
        while (iterator.hasNext()) {
            org.bukkit.advancement.Advancement advancement = iterator.next();
            org.bukkit.advancement.AdvancementProgress progress = player.getAdvancementProgress(advancement);
            for (String criteria : progress.getAwardedCriteria()) {
                progress.revokeCriteria(criteria);
            }
        }
        
        plugin.debug("已重置玩家数据: " + player.getName() + " (经验、配方、成就)");
    }
    
    /**
     * 传送单个玩家到大厅
     */
    private void teleportPlayerToLobby(Player player) {
        // Bungee 模式：传送玩家回主大厅服务器
        if (plugin.getServerMode() == ServerMode.BUNGEE) {
            com.minecraft.huntergame.config.ServerType serverType = plugin.getManhuntConfig().getServerType();
            
            // 如果当前是子大厅服务器，传送玩家回主大厅
            if (serverType == com.minecraft.huntergame.config.ServerType.SUB_LOBBY) {
                String mainLobby = plugin.getManhuntConfig().getMainLobby();
                plugin.getLogger().info("传送玩家 " + player.getName() + " 回主大厅服务器: " + mainLobby);
                plugin.getBungeeManager().sendPlayerToServer(player, mainLobby);
                return;
            }
        }
        
        // 单服务器模式：传送到本地大厅位置
        if (!plugin.getManhuntConfig().isLobbyEnabled()) {
            // 如果未启用大厅，传送到主世界出生点
            if (!plugin.getServer().getWorlds().isEmpty()) {
                player.teleport(plugin.getServer().getWorlds().get(0).getSpawnLocation());
            }
            return;
        }
        
        Location lobbyLocation = plugin.getManhuntConfig().getLobbyLocation();
        if (lobbyLocation != null) {
            player.teleport(lobbyLocation);
            plugin.getLogger().info("玩家 " + player.getName() + " 已传送到大厅");
        } else {
            plugin.getLogger().warning("无法传送玩家到大厅：大厅位置未设置");
            // 传送到主世界出生点作为备选
            if (!plugin.getServer().getWorlds().isEmpty()) {
                player.teleport(plugin.getServer().getWorlds().get(0).getSpawnLocation());
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
        
        // 同时更新服务器状态
        int playerCount = plugin.getServer().getOnlinePlayers().size();
        String status = determineServerStatus();
        plugin.getRedisManager().syncServerStatus(
            plugin.getRedisManager().getServerName(), 
            status, 
            playerCount
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
        
        // 同时更新服务器状态
        int playerCount = plugin.getServer().getOnlinePlayers().size();
        String status = determineServerStatus();
        plugin.getRedisManager().syncServerStatus(
            plugin.getRedisManager().getServerName(), 
            status, 
            playerCount
        );
    }
    
    /**
     * 确定服务器状态（公开方法供外部调用）
     */
    public String determineServerStatus() {
        if (getAllGames().isEmpty()) {
            return "ONLINE";
        }
        
        // 检查是否有游戏正在进行
        boolean hasPlayingGame = false;
        boolean hasWaitingGame = false;
        
        for (ManhuntGame game : getAllGames()) {
            // 检查游戏是否有玩家
            if (game.getPlayerCount() == 0) {
                continue; // 跳过空游戏
            }
            
            if (game.getState() == com.minecraft.huntergame.game.GameState.PLAYING) {
                hasPlayingGame = true;
                break; // 有游戏在进行，直接返回
            } else if (game.getState() == com.minecraft.huntergame.game.GameState.WAITING ||
                       game.getState() == com.minecraft.huntergame.game.GameState.MATCHING) {
                hasWaitingGame = true;
            }
        }
        
        if (hasPlayingGame) {
            return "PLAYING";
        } else if (hasWaitingGame) {
            return "WAITING";
        } else {
            // 所有游戏都是空的
            return "ONLINE";
        }
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
