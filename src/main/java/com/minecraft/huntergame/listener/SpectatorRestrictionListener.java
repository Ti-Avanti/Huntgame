package com.minecraft.huntergame.listener;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.game.ManhuntGame;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * 观战者权限限制监听器
 * 防止观战者干扰游戏进程
 * 
 * @author YourName
 * @version 1.0.0
 */
public class SpectatorRestrictionListener implements Listener {
    
    private final HunterGame plugin;
    
    public SpectatorRestrictionListener(HunterGame plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 检查玩家是否是观战者
     */
    private boolean isSpectator(Player player) {
        if (player.getGameMode() != GameMode.SPECTATOR) {
            return false;
        }
        
        ManhuntGame game = plugin.getManhuntManager().getPlayerGame(player);
        if (game == null) {
            return false;
        }
        
        return game.getSpectators().contains(player.getUniqueId());
    }
    
    /**
     * 禁止观战者破坏方块
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (isSpectator(player)) {
            event.setCancelled(true);
        }
    }
    
    /**
     * 禁止观战者放置方块
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (isSpectator(player)) {
            event.setCancelled(true);
        }
    }
    
    /**
     * 禁止观战者攻击实体
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            if (isSpectator(player)) {
                event.setCancelled(true);
            }
        }
    }
    
    /**
     * 禁止观战者拾取物品
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (isSpectator(player)) {
                event.setCancelled(true);
            }
        }
    }
    
    /**
     * 禁止观战者丢弃物品
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (isSpectator(player)) {
            event.setCancelled(true);
        }
    }
    
    /**
     * 禁止观战者与方块交互（打开箱子等）
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (isSpectator(player)) {
            // 允许使用观战道具
            if (event.getItem() != null && plugin.getHotbarManager().isHotbarItem(event.getItem())) {
                return;
            }
            event.setCancelled(true);
        }
    }
    
    /**
     * 禁止观战者与实体交互
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (isSpectator(player)) {
            event.setCancelled(true);
        }
    }
    
    /**
     * 禁止观战者移动背包物品
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        if (isSpectator(player)) {
            // 检查是否是GUI点击
            if (event.getClickedInventory() != null && 
                event.getClickedInventory().getHolder() == null) {
                // 允许GUI交互
                return;
            }
            
            // 禁止移动背包物品
            if (event.getClickedInventory() == player.getInventory()) {
                event.setCancelled(true);
            }
        }
    }
    
    /**
     * 限制观战者使用命令
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!isSpectator(player)) {
            return;
        }
        
        String command = event.getMessage().toLowerCase();
        
        // 允许的命令列表
        String[] allowedCommands = {
            "/manhunt",
            "/huntergame",
            "/msg",
            "/tell",
            "/w",
            "/r",
            "/reply"
        };
        
        // 检查是否是允许的命令
        boolean allowed = false;
        for (String allowedCmd : allowedCommands) {
            if (command.startsWith(allowedCmd)) {
                allowed = true;
                break;
            }
        }
        
        // 如果不是允许的命令，取消执行
        if (!allowed) {
            event.setCancelled(true);
            player.sendMessage("§c观战模式下无法使用此命令！");
        }
    }
}
