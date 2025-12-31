package com.minecraft.huntergame.listener;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.game.GameState;
import com.minecraft.huntergame.game.ManhuntGame;
import com.minecraft.huntergame.game.PlayerRole;
import org.bukkit.ChatColor;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Wither;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;

/**
 * Manhunt核心事件监听器
 * 处理游戏中的关键事件
 * 
 * @author YourName
 * @version 1.0.0
 */
public class ManhuntListener implements Listener {
    
    private final HunterGame plugin;
    
    public ManhuntListener(HunterGame plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 监听玩家死亡事件
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        
        // 检查玩家是否在游戏中
        ManhuntGame game = plugin.getManhuntManager().getPlayerGame(player);
        if (game == null) {
            return;
        }
        
        // 检查游戏状态
        if (game.getState() != GameState.PLAYING) {
            return;
        }
        
        PlayerRole role = game.getPlayerRole(player.getUniqueId());
        if (role == null) {
            return;
        }
        
        // 根据角色处理死亡
        switch (role) {
            case RUNNER:
                handleRunnerDeath(game, player, event);
                break;
                
            case HUNTER:
                handleHunterDeath(game, player, event);
                break;
                
            default:
                break;
        }
    }
    
    /**
     * 处理逃亡者死亡
     */
    private void handleRunnerDeath(ManhuntGame game, Player player, PlayerDeathEvent event) {
        // 检查是否还有复活次数
        if (game.hasRespawns(player.getUniqueId())) {
            // 还有复活次数，延迟复活
            int remaining = game.getRemainingRespawns(player.getUniqueId());
            game.decreaseRespawns(player.getUniqueId());
            
            // 广播消息
            broadcastToGame(game, ChatColor.YELLOW + "逃亡者 " + player.getName() + 
                " 死亡！剩余复活次数: " + (remaining - 1));
            
            // 延迟复活
            int respawnDelay = plugin.getManhuntConfig().getRespawnDelay();
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    player.spigot().respawn();
                    player.sendMessage(ChatColor.GREEN + "你已复活！剩余复活次数: " + 
                        game.getRemainingRespawns(player.getUniqueId()));
                }
            }, respawnDelay * 20L);
            
        } else {
            // 没有复活次数，淘汰
            game.setPlayerRole(player.getUniqueId(), PlayerRole.SPECTATOR);
            
            // 广播消息
            broadcastToGame(game, ChatColor.RED + "逃亡者 " + player.getName() + 
                " 已被淘汰！");
            
            player.sendMessage(ChatColor.RED + "你已被淘汰，进入观战模式");
            
            // 检查游戏是否结束
            if (game.shouldEnd()) {
                endGame(game, false); // 猎人获胜
            }
        }
    }
    
    /**
     * 处理猎人死亡
     */
    private void handleHunterDeath(ManhuntGame game, Player player, PlayerDeathEvent event) {
        // 猎人死亡后在出生点复活
        broadcastToGame(game, ChatColor.YELLOW + "猎人 " + player.getName() + " 死亡");
        
        // 延迟复活
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline() && game.getSpawnLocation() != null) {
                player.spigot().respawn();
                player.teleport(game.getSpawnLocation());
                player.sendMessage(ChatColor.GREEN + "你已在出生点复活");
                
                // 重新给予追踪指南针
                plugin.getTrackerManager().giveTrackerCompass(player, game);
            }
        }, 20L); // 1秒后复活
    }
    
    /**
     * 监听末影龙死亡事件
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onEnderDragonDeath(EntityDeathEvent event) {
        // 检查是否是末影龙
        if (event.getEntityType() != EntityType.ENDER_DRAGON) {
            return;
        }
        
        EnderDragon dragon = (EnderDragon) event.getEntity();
        Player killer = dragon.getKiller();
        
        if (killer == null) {
            return;
        }
        
        // 检查玩家是否在游戏中
        ManhuntGame game = plugin.getManhuntManager().getPlayerGame(killer);
        if (game == null) {
            return;
        }
        
        // 检查游戏状态
        if (game.getState() != GameState.PLAYING) {
            return;
        }
        
        // 检查击杀者是否是逃亡者
        PlayerRole role = game.getPlayerRole(killer.getUniqueId());
        if (role != PlayerRole.RUNNER) {
            return;
        }
        
        // 逃亡者击败末影龙，游戏结束
        game.setDragonDefeated(true);
        
        // 广播消息
        broadcastToGame(game, ChatColor.GOLD + "========================================");
        broadcastToGame(game, ChatColor.GREEN + "逃亡者 " + killer.getName() + 
            " 击败了末影龙！");
        broadcastToGame(game, ChatColor.GREEN + "逃亡者获胜！");
        broadcastToGame(game, ChatColor.GOLD + "========================================");
        
        // 结束游戏
        endGame(game, true); // 逃亡者获胜
    }
    
    /**
     * 监听凋灵死亡事件（可选）
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onWitherDeath(EntityDeathEvent event) {
        // 检查是否是凋灵
        if (event.getEntityType() != EntityType.WITHER) {
            return;
        }
        
        // 检查是否启用凋灵胜利
        if (!plugin.getManhuntConfig().isWitherVictoryAllowed()) {
            return;
        }
        
        org.bukkit.entity.Wither wither = (org.bukkit.entity.Wither) event.getEntity();
        Player killer = wither.getKiller();
        
        if (killer == null) {
            return;
        }
        
        // 检查玩家是否在游戏中
        ManhuntGame game = plugin.getManhuntManager().getPlayerGame(killer);
        if (game == null) {
            return;
        }
        
        // 检查游戏状态
        if (game.getState() != GameState.PLAYING) {
            return;
        }
        
        // 检查击杀者是否是逃亡者
        PlayerRole role = game.getPlayerRole(killer.getUniqueId());
        if (role != PlayerRole.RUNNER) {
            return;
        }
        
        // 逃亡者击败凋灵，游戏结束
        game.setDragonDefeated(true); // 使用相同标记
        
        // 广播消息
        broadcastToGame(game, ChatColor.GOLD + "========================================");
        broadcastToGame(game, ChatColor.GREEN + "逃亡者 " + killer.getName() + 
            " 击败了凋灵！");
        broadcastToGame(game, ChatColor.GREEN + "逃亡者获胜！");
        broadcastToGame(game, ChatColor.GOLD + "========================================");
        
        // 结束游戏
        endGame(game, true); // 逃亡者获胜
    }
    
    /**
     * 监听玩家移动事件（准备阶段冻结猎人）
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        // 检查玩家是否在游戏中
        ManhuntGame game = plugin.getManhuntManager().getPlayerGame(player);
        if (game == null) {
            return;
        }
        
        // 检查是否在准备阶段
        if (!game.isPreparing()) {
            return;
        }
        
        // 检查玩家是否是猎人
        PlayerRole role = game.getPlayerRole(player.getUniqueId());
        if (role != PlayerRole.HUNTER) {
            return;
        }
        
        // 检查是否有实际移动（不是视角转动）
        if (event.getFrom().getX() == event.getTo().getX() &&
            event.getFrom().getY() == event.getTo().getY() &&
            event.getFrom().getZ() == event.getTo().getZ()) {
            return;
        }
        
        // 取消移动
        event.setCancelled(true);
        
        // 提示玩家
        if (System.currentTimeMillis() % 3000 < 50) { // 每3秒提示一次
            player.sendMessage(ChatColor.RED + "准备阶段，猎人无法移动！");
        }
    }
    
    /**
     * 监听传送门使用事件
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerPortal(PlayerPortalEvent event) {
        Player player = event.getPlayer();
        
        // 检查玩家是否在游戏中
        ManhuntGame game = plugin.getManhuntManager().getPlayerGame(player);
        if (game == null) {
            return;
        }
        
        // 检查游戏状态
        if (game.getState() != GameState.PLAYING) {
            return;
        }
        
        // 获取目标世界环境
        String fromDimension = getDimensionName(event.getFrom().getWorld().getEnvironment());
        
        // 延迟广播维度切换消息（等传送完成）
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                String toDimension = getDimensionName(player.getWorld().getEnvironment());
                broadcastToGame(game, ChatColor.AQUA + player.getName() + 
                    " 从 " + fromDimension + " 进入了 " + toDimension);
            }
        }, 20L);
    }
    
    /**
     * 获取维度名称
     */
    private String getDimensionName(org.bukkit.World.Environment environment) {
        switch (environment) {
            case NORMAL:
                return "主世界";
            case NETHER:
                return "下界";
            case THE_END:
                return "末地";
            default:
                return "未知维度";
        }
    }
    
    /**
     * 向游戏中所有玩家广播消息
     */
    private void broadcastToGame(ManhuntGame game, String message) {
        for (java.util.UUID uuid : game.getAllPlayers()) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.sendMessage(message);
            }
        }
    }
    
    /**
     * 结束游戏
     */
    private void endGame(ManhuntGame game, boolean runnersWin) {
        // 延迟结束，让玩家看到消息
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.getManhuntManager().endGame(game.getGameId());
        }, 100L); // 5秒后结束
    }
}
