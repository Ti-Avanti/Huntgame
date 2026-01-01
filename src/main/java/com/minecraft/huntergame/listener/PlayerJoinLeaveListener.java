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
            // 检查玩家是否在游戏中
            ManhuntGame game = plugin.getManhuntManager().getPlayerGame(player);
            
            if (game == null) {
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
}
