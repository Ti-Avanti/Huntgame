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
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.GameMode;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manhunt核心事件监听器
 * 处理游戏中的关键事件
 * 
 * @author YourName
 * @version 1.0.0
 */
public class ManhuntListener implements Listener {
    
    private final HunterGame plugin;
    
    // 跟踪等待复活的玩家 <玩家UUID, 死亡位置>
    private final Map<UUID, Location> waitingRespawn = new HashMap<>();
    
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
        // 逃亡者死亡正常掉落物品
        event.setKeepInventory(false);
        event.setKeepLevel(false);
        // 不清空掉落物，让物品正常掉落
        // event.getDrops().clear();
        
        // 更新统计数据
        Player killer = player.getKiller();
        if (killer != null && game.isPlayerInGame(killer.getUniqueId())) {
            // 更新猎人击杀数
            plugin.getStatsManager().addHunterKill(killer.getUniqueId());
        }
        // 更新逃亡者死亡数
        plugin.getStatsManager().addSurvivorDeath(player.getUniqueId());
        
        // 记录死亡位置
        Location deathLocation = player.getLocation().clone();
        
        // 获取当前复活次数
        int remaining = game.getRemainingRespawns(player.getUniqueId());
        
        // 检查是否还能复活(复活次数>0时可以复活)
        if (remaining > 0) {
            // 减少复活次数
            game.decreaseRespawns(player.getUniqueId());
            int newRemaining = game.getRemainingRespawns(player.getUniqueId());
            
            // 获取重生延迟配置（秒转tick）
            int respawnDelay = plugin.getManhuntConfig().getRespawnDelay();
            long delayTicks = respawnDelay * 20L;
            
            plugin.debug("逃亡者 " + player.getName() + " 死亡，" + respawnDelay + "秒后复活");
            
            // 标记玩家正在等待复活
            waitingRespawn.put(player.getUniqueId(), deathLocation);
            
            // 广播消息
            broadcastToGame(game, ChatColor.YELLOW + "逃亡者 " + player.getName() + 
                " 死亡！" + respawnDelay + "秒后复活，剩余复活次数: " + newRemaining);
            
            player.sendMessage(ChatColor.YELLOW + "你将在 " + respawnDelay + " 秒后复活");
            player.sendMessage(ChatColor.GRAY + "请等待，不要点击重生按钮");
            
            // 延迟复活
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    plugin.debug("开始复活玩家 " + player.getName());
                    
                    // 移除等待标记
                    waitingRespawn.remove(player.getUniqueId());
                    
                    // 如果玩家还在死亡状态，复活他
                    if (player.isDead()) {
                        player.spigot().respawn();
                    }
                    
                    // 延迟一tick后传送和恢复状态，确保玩家已经重生
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        if (player.isOnline()) {
                            // 恢复游戏模式
                            player.setGameMode(GameMode.SURVIVAL);
                            
                            // 传送到出生点
                            if (game.getSpawnLocation() != null) {
                                player.teleport(game.getSpawnLocation());
                                plugin.debug("传送玩家到出生点: " + game.getSpawnLocation());
                            }
                            
                            // 恢复状态
                            player.setHealth(player.getMaxHealth());
                            player.setFoodLevel(20);
                            player.setSaturation(20.0f);
                            player.setExhaustion(0.0f);
                            player.setFireTicks(0);
                            
                            player.sendMessage(ChatColor.GREEN + "你已复活！剩余复活次数: " + newRemaining);
                            plugin.debug("玩家 " + player.getName() + " 复活完成");
                        }
                    }, 1L);
                }
            }, delayTicks);
            
        } else {
            // 没有复活次数，淘汰
            game.setPlayerRole(player.getUniqueId(), PlayerRole.SPECTATOR);
            
            // 广播消息
            broadcastToGame(game, ChatColor.RED + "逃亡者 " + player.getName() + 
                " 已被淘汰！");
            
            player.sendMessage(ChatColor.RED + "你已被淘汰，进入观战模式");
            
            // 立即复活并设置为观战模式
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline() && player.isDead()) {
                    player.spigot().respawn();
                    player.setGameMode(org.bukkit.GameMode.SPECTATOR);
                }
            }, 1L);
            
            // 检查游戏是否结束
            if (game.shouldEnd()) {
                // 延迟结束，让玩家看到消息
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    com.minecraft.huntergame.game.GameEndReason reason = 
                        plugin.getManhuntManager().determineEndReason(game);
                    plugin.getManhuntManager().endGame(game.getGameId(), reason);
                }, 100L); // 5秒后结束
            }
        }
    }
    
    /**
     * 处理猎人死亡
     */
    private void handleHunterDeath(ManhuntGame game, Player player, PlayerDeathEvent event) {
        // 猎人死亡正常掉落物品
        event.setKeepInventory(false);
        event.setKeepLevel(false);
        // 不清空掉落物，让物品正常掉落
        // event.getDrops().clear();
        
        // 更新猎人死亡数
        plugin.getStatsManager().addHunterDeath(player.getUniqueId());
        
        // 记录死亡位置
        Location deathLocation = player.getLocation().clone();
        
        // 获取重生延迟配置（秒转tick）
        int respawnDelay = plugin.getManhuntConfig().getRespawnDelay();
        long delayTicks = respawnDelay * 20L;
        
        plugin.debug("猎人 " + player.getName() + " 死亡，" + respawnDelay + "秒后复活");
        
        // 标记玩家正在等待复活
        waitingRespawn.put(player.getUniqueId(), deathLocation);
        
        // 猎人死亡后在出生点复活
        broadcastToGame(game, ChatColor.YELLOW + "猎人 " + player.getName() + 
            " 死亡，" + respawnDelay + "秒后复活");
        
        player.sendMessage(ChatColor.YELLOW + "你将在 " + respawnDelay + " 秒后复活");
        player.sendMessage(ChatColor.GRAY + "请等待，不要点击重生按钮");
        
        // 延迟复活
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                plugin.debug("开始复活猎人 " + player.getName());
                
                // 移除等待标记
                waitingRespawn.remove(player.getUniqueId());
                
                // 如果玩家还在死亡状态，复活他
                if (player.isDead()) {
                    player.spigot().respawn();
                }
                
                // 延迟一tick后传送和恢复状态，确保玩家已经完全重生
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline()) {
                        // 恢复游戏模式
                        player.setGameMode(GameMode.SURVIVAL);
                        
                        // 传送到出生点
                        if (game.getSpawnLocation() != null) {
                            player.teleport(game.getSpawnLocation());
                            plugin.debug("传送猎人到出生点: " + game.getSpawnLocation());
                        }
                        
                        // 恢复状态
                        player.setHealth(player.getMaxHealth());
                        player.setFoodLevel(20);
                        player.setSaturation(20.0f);
                        player.setExhaustion(0.0f);
                        player.setFireTicks(0);
                        
                        player.sendMessage(ChatColor.GREEN + "你已在出生点复活");
                        
                        // 重新给予追踪指南针
                        plugin.getTrackerManager().giveTrackerCompass(player, game);
                        
                        plugin.debug("猎人 " + player.getName() + " 复活完成，已给予追踪指南针");
                    }
                }, 1L);
            }
        }, delayTicks);
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
        
        // 延迟结束游戏
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.getManhuntManager().endGame(
                game.getGameId(), 
                com.minecraft.huntergame.game.GameEndReason.RUNNERS_WIN_DRAGON
            );
        }, 100L); // 5秒后结束
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
        
        // 延迟结束游戏
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.getManhuntManager().endGame(
                game.getGameId(), 
                com.minecraft.huntergame.game.GameEndReason.RUNNERS_WIN_DRAGON
            );
        }, 100L); // 5秒后结束
    }
    
    /**
     * 监听玩家攻击事件（准备阶段禁止逃亡者攻击猎人）
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // 检查是否是玩家攻击玩家
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player attacker = (Player) event.getDamager();
        Player victim = (Player) event.getEntity();
        
        // 检查攻击者是否在游戏中
        ManhuntGame game = plugin.getManhuntManager().getPlayerGame(attacker);
        if (game == null) {
            return;
        }
        
        // 检查是否在准备阶段
        if (!game.isPreparing()) {
            return;
        }
        
        // 获取攻击者和受害者的角色
        PlayerRole attackerRole = game.getPlayerRole(attacker.getUniqueId());
        PlayerRole victimRole = game.getPlayerRole(victim.getUniqueId());
        
        // 如果攻击者是逃亡者，受害者是猎人，取消攻击
        if (attackerRole == PlayerRole.RUNNER && victimRole == PlayerRole.HUNTER) {
            event.setCancelled(true);
            attacker.sendMessage(ChatColor.RED + "准备阶段，逃亡者无法攻击猎人！");
        }
    }
    
    /**
     * 监听玩家移动事件（准备阶段冻结猎人）
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
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
        
        // 提示玩家（使用更可靠的方式）
        long currentTime = System.currentTimeMillis();
        if (currentTime % 3000 < 100) { // 每3秒提示一次
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
     * 监听玩家重生事件
     * 确保玩家在游戏世界重生，并处理延迟复活
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
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
        
        // 获取玩家角色
        PlayerRole role = game.getPlayerRole(player.getUniqueId());
        
        // 检查玩家是否在等待复活
        if (waitingRespawn.containsKey(player.getUniqueId())) {
            plugin.debug("玩家 " + player.getName() + " 提前点击了重生按钮，设置为旁观模式");
            
            // 获取死亡位置
            Location deathLocation = waitingRespawn.get(player.getUniqueId());
            
            // 设置重生位置为死亡位置
            event.setRespawnLocation(deathLocation);
            
            // 延迟1tick后设置为旁观模式（必须在重生后）
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline() && waitingRespawn.containsKey(player.getUniqueId())) {
                    player.setGameMode(GameMode.SPECTATOR);
                    player.sendMessage(ChatColor.YELLOW + "请等待复活时间结束...");
                }
            }, 1L);
            
            return;
        }
        
        // 如果是逃亡者或猎人，设置重生位置为游戏出生点
        if ((role == PlayerRole.RUNNER || role == PlayerRole.HUNTER) && game.getSpawnLocation() != null) {
            event.setRespawnLocation(game.getSpawnLocation());
            plugin.debug("设置玩家 " + player.getName() + " 的重生位置为游戏出生点");
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
}
