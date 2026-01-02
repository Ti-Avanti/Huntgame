package com.minecraft.huntergame.listener;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.game.ManhuntGame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * 玩家加入/离开监听器
 * 处理玩家加入和离开服务器的事件
 * 
 * @author YourName
 * @version 1.0.0
 */
public class PlayerJoinLeaveListener implements Listener {
    
    private final HunterGame plugin;
    
    public PlayerJoinLeaveListener(HunterGame plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 监听玩家加入服务器
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // 加载玩家数据
        plugin.getStatsManager().loadPlayerData(player);
        
        // 延迟创建大厅计分板（确保玩家完全加载）
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            // Bungee 模式：检查玩家是否有待处理的动作
            if (plugin.getServerMode() == com.minecraft.huntergame.ServerMode.BUNGEE &&
                plugin.getRedisManager() != null && plugin.getRedisManager().isConnected()) {
                
                handlePlayerPendingAction(player);
            }
            
            // 检查玩家是否在游戏中
            ManhuntGame game = plugin.getManhuntManager().getPlayerGame(player);
            
            if (game == null) {
                // Bungee 模式 + 子大厅服务器：自动加入游戏
                if (plugin.getServerMode() == com.minecraft.huntergame.ServerMode.BUNGEE &&
                    plugin.getManhuntConfig().getServerType() == com.minecraft.huntergame.config.ServerType.SUB_LOBBY) {
                    
                    plugin.getLogger().info("[AUTO-JOIN] 玩家 " + player.getName() + " 进入子大厅，自动加入游戏");
                    autoJoinGame(player);
                    return;
                }
                
                // 玩家不在游戏中，显示大厅计分板
                plugin.getSidebarManager().createLobbySidebar(player);
                
                // 给予大厅快捷道具
                plugin.getHotbarManager().giveLobbyItems(player);
            } else {
                // 玩家已在游戏中，根据游戏状态给予对应道具
                com.minecraft.huntergame.game.GameState state = game.getState();
                if (state == com.minecraft.huntergame.game.GameState.WAITING || 
                    state == com.minecraft.huntergame.game.GameState.MATCHING) {
                    // 等待/匹配状态 - 给予匹配道具
                    plugin.getHotbarManager().giveMatchingItems(player, game);
                }
                // 游戏进行中的道具由 RoleManager 处理，这里不需要处理
            }
        }, 20L); // 1秒后创建
        
        plugin.getLogger().info("玩家 " + player.getName() + " 加入服务器");
    }
    
    /**
     * 监听玩家离开服务器
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // 检查玩家是否在游戏中
        ManhuntGame game = plugin.getManhuntManager().getPlayerGame(player);
        if (game != null) {
            // 玩家在游戏中离开
            handlePlayerLeaveGame(player, game);
        }
        
        // 保存并卸载玩家数据
        plugin.getStatsManager().unloadPlayerData(player.getUniqueId());
        
        plugin.getLogger().info("玩家 " + player.getName() + " 离开服务器");
    }
    
    /**
     * 处理玩家在游戏中离开
     */
    private void handlePlayerLeaveGame(Player player, ManhuntGame game) {
        // 移除侧边栏
        plugin.getSidebarManager().removeSidebar(player);
        
        // 从游戏中移除玩家
        plugin.getManhuntManager().leaveGame(player);
        
        // 广播离开消息
        for (java.util.UUID uuid : game.getAllPlayers()) {
            Player p = plugin.getServer().getPlayer(uuid);
            if (p != null && p.isOnline()) {
                p.sendMessage("§c玩家 " + player.getName() + " 离开了游戏");
            }
        }
        
        plugin.getLogger().info("玩家 " + player.getName() + " 在游戏中离开");
    }
    
    /**
     * 处理玩家的待处理动作（Bungee 模式）
     */
    private void handlePlayerPendingAction(Player player) {
        try {
            java.util.Map<String, String> actionData = plugin.getRedisManager()
                .getAndClearPlayerPendingAction(player.getUniqueId().toString());
            
            if (actionData == null || actionData.isEmpty()) {
                return; // 没有待处理的动作
            }
            
            String action = actionData.get("action");
            String data = actionData.get("data");
            
            plugin.getLogger().info("处理玩家待处理动作: " + player.getName() + " -> " + action);
            
            if ("CREATE_ROOM".equals(action)) {
                // 延迟执行创建房间，确保玩家完全加载
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    handleCreateRoomAction(player, data);
                }, 40L); // 2秒后执行
            }
            // 可以在这里添加其他动作类型的处理
            
        } catch (Exception ex) {
            plugin.getLogger().warning("处理玩家待处理动作失败: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    /**
     * 处理创建房间动作
     */
    private void handleCreateRoomAction(Player player, String data) {
        try {
            // 检查是否可以创建游戏
            if (!plugin.getManhuntManager().canCreateGame()) {
                player.sendMessage("§c当前无法创建游戏！");
                
                if (plugin.getManhuntConfig().isSingleGameMode() && 
                    !plugin.getManhuntManager().getAllGames().isEmpty()) {
                    player.sendMessage("§e原因: 单场比赛模式下已有游戏正在进行");
                }
                
                return;
            }
            
            // 获取世界名称
            String worldName = plugin.getManhuntConfig().getWorldName();
            if (worldName == null || worldName.isEmpty()) {
                worldName = "manhunt_world";
            }
            
            // 加载或创建游戏世界
            org.bukkit.World gameWorld = plugin.getWorldManager().loadOrCreateWorld(worldName);
            if (gameWorld == null) {
                player.sendMessage("§c无法创建游戏世界！");
                return;
            }
            
            // 创建游戏
            ManhuntGame game = plugin.getManhuntManager().createGame(worldName);
            
            if (game == null) {
                player.sendMessage("§c创建游戏失败！");
                return;
            }
            
            // 玩家加入游戏
            boolean joined = plugin.getManhuntManager().joinGame(player, game.getGameId());
            
            if (joined) {
                player.sendMessage("§a§l成功创建游戏房间！");
                player.sendMessage("§e游戏ID: §a" + game.getGameId());
                player.sendMessage("§e等待其他玩家加入...");
                player.sendMessage("§7提示: 达到 §a" + plugin.getManhuntConfig().getMinPlayersToStart() + " §7人后将自动开始匹配");
            } else {
                player.sendMessage("§c加入游戏失败！");
                plugin.getManhuntManager().removeGame(game.getGameId());
            }
            
        } catch (Exception ex) {
            plugin.getLogger().severe("处理创建房间动作失败: " + ex.getMessage());
            ex.printStackTrace();
            player.sendMessage("§c创建房间时发生错误，请联系管理员");
        }
    }
    
    /**
     * 自动加入游戏（子大厅服务器）
     */
    private void autoJoinGame(Player player) {
        try {
            // Bungee模式：先传送玩家到大厅出生点
            if (plugin.getManhuntConfig().isLobbyEnabled()) {
                org.bukkit.Location lobbyLocation = plugin.getManhuntConfig().getLobbyLocation();
                if (lobbyLocation != null) {
                    player.teleport(lobbyLocation);
                    plugin.getLogger().info("[AUTO-JOIN] 玩家 " + player.getName() + " 已传送到大厅出生点");
                }
            }
            
            // 查找可加入的游戏
            ManhuntGame availableGame = null;
            
            for (ManhuntGame game : plugin.getManhuntManager().getAllGames()) {
                // 只加入等待或匹配状态的游戏
                if (game.getState() == com.minecraft.huntergame.game.GameState.WAITING ||
                    game.getState() == com.minecraft.huntergame.game.GameState.MATCHING) {
                    
                    // 检查游戏是否未满
                    if (!game.isFull()) {
                        availableGame = game;
                        plugin.getLogger().info("[AUTO-JOIN] 找到可加入的游戏: " + game.getGameId());
                        break;
                    }
                }
            }
            
            // 如果没有可用游戏，创建新游戏
            if (availableGame == null) {
                if (!plugin.getManhuntManager().canCreateGame()) {
                    player.sendMessage("§c当前无法创建游戏！");
                    plugin.getLogger().warning("[AUTO-JOIN] 无法创建游戏");
                    
                    // 给予大厅道具
                    plugin.getHotbarManager().giveLobbyItems(player);
                    return;
                }
                
                // 获取默认世界名称
                String worldName = plugin.getManhuntConfig().getWorldName();
                if (worldName == null || worldName.isEmpty()) {
                    worldName = "manhunt_world";
                }
                
                // 创建新游戏
                availableGame = plugin.getManhuntManager().createGame(worldName);
                
                if (availableGame == null) {
                    player.sendMessage("§c创建游戏失败！");
                    plugin.getLogger().severe("[AUTO-JOIN] 创建游戏失败");
                    
                    // 给予大厅道具
                    plugin.getHotbarManager().giveLobbyItems(player);
                    return;
                }
                
                plugin.getLogger().info("[AUTO-JOIN] 创建了新游戏: " + availableGame.getGameId());
                player.sendMessage("§a创建了新的游戏房间");
            }
            
            // 加入游戏
            boolean joined = plugin.getManhuntManager().joinGame(player, availableGame.getGameId());
            
            if (joined) {
                player.sendMessage("§a欢迎来到游戏大厅！");
                player.sendMessage("§e等待其他玩家加入...");
                player.sendMessage("§7达到 §a" + plugin.getManhuntConfig().getMinPlayersToStart() + " §7人后将自动开始匹配");
                plugin.getLogger().info("[AUTO-JOIN] 玩家 " + player.getName() + " 成功加入游戏 " + availableGame.getGameId());
            } else {
                player.sendMessage("§c加入游戏失败！");
                plugin.getLogger().warning("[AUTO-JOIN] 玩家 " + player.getName() + " 加入游戏失败");
                
                // 给予大厅道具
                plugin.getHotbarManager().giveLobbyItems(player);
            }
            
        } catch (Exception ex) {
            plugin.getLogger().severe("[AUTO-JOIN] 自动加入游戏失败: " + ex.getMessage());
            ex.printStackTrace();
            player.sendMessage("§c加入游戏时发生错误");
            
            // 给予大厅道具
            plugin.getHotbarManager().giveLobbyItems(player);
        }
    }
}
