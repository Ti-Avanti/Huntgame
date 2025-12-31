package com.minecraft.huntergame.listener;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.arena.Arena;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * 玩家事件监听器
 * 监听玩家相关事件
 * 
 * @author YourName
 * @version 1.0.0
 */
public class PlayerListener implements Listener {
    
    private final HunterGame plugin;
    
    public PlayerListener(HunterGame plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 监听玩家加入服务器
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // 加载玩家数据
        plugin.getStatsManager().loadPlayer(player.getUniqueId());
        
        plugin.getLogger().info("玩家 " + player.getName() + " 加入服务器，数据已加载");
    }
    
    /**
     * 监听玩家退出服务器
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // 如果玩家在竞技场中，让其离开
        Arena arena = plugin.getArenaManager().getPlayerArena(player.getUniqueId());
        if (arena != null) {
            arena.leavePlayer(player);
        }
        
        // 保存玩家数据
        plugin.getStatsManager().savePlayer(player.getUniqueId());
        
        plugin.getLogger().info("玩家 " + player.getName() + " 退出服务器，数据已保存");
    }
    
    /**
     * 监听玩家死亡事件
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Player killer = player.getKiller();
        
        // 检查玩家是否在竞技场中
        Arena arena = plugin.getArenaManager().getPlayerArena(player.getUniqueId());
        if (arena == null) {
            return;
        }
        
        // 检查游戏是否在进行中
        if (!arena.getStateManager().isPlaying()) {
            return;
        }
        
        // 取消死亡掉落
        event.setKeepInventory(true);
        event.setKeepLevel(true);
        event.getDrops().clear();
        event.setDroppedExp(0);
        
        // 取消死亡消息
        event.setDeathMessage(null);
        
        // 淘汰玩家
        arena.eliminatePlayer(player, killer);
        
        plugin.getLogger().info("[" + arena.getArenaName() + "] 玩家 " + player.getName() + " 死亡");
    }
}
