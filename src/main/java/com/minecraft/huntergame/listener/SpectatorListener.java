package com.minecraft.huntergame.listener;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.arena.Arena;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

/**
 * 观战者事件监听器
 * 禁止观战者进行各种互动
 * 
 * @author YourName
 * @version 1.0.0
 */
public class SpectatorListener implements Listener {
    
    private final HunterGame plugin;
    
    public SpectatorListener(HunterGame plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 禁止观战者破坏方块
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        
        if (isSpectator(player)) {
            event.setCancelled(true);
        }
    }
    
    /**
     * 禁止观战者放置方块
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        
        if (isSpectator(player)) {
            event.setCancelled(true);
        }
    }
    
    /**
     * 禁止观战者丢弃物品
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        
        if (isSpectator(player)) {
            event.setCancelled(true);
        }
    }
    
    /**
     * 禁止观战者拾取物品
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        
        if (isSpectator(player)) {
            event.setCancelled(true);
        }
    }
    
    /**
     * 禁止观战者交互
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        if (isSpectator(player)) {
            event.setCancelled(true);
        }
    }
    
    /**
     * 禁止观战者受到伤害
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        
        if (isSpectator(player)) {
            event.setCancelled(true);
        }
    }
    
    /**
     * 禁止观战者攻击实体
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getDamager();
        
        if (isSpectator(player)) {
            event.setCancelled(true);
        }
    }
    
    /**
     * 检查玩家是否为观战者
     * 
     * @param player 玩家
     * @return 是否为观战者
     */
    private boolean isSpectator(Player player) {
        Arena arena = plugin.getArenaManager().getPlayerArena(player.getUniqueId());
        
        if (arena == null) {
            return false;
        }
        
        return arena.isSpectator(player);
    }
}
