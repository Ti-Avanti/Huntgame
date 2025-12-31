package com.minecraft.huntergame.listener;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.game.ManhuntGame;
import com.minecraft.huntergame.game.PlayerRole;
import org.bukkit.GameMode;
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
import org.bukkit.event.entity.EntityPickupItemEvent;

/**
 * 观战者监听器
 * 禁止观战者与游戏互动
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
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        
        if (isSpectator(player)) {
            event.setCancelled(true);
        }
    }
    
    /**
     * 禁止观战者放置方块
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        
        if (isSpectator(player)) {
            event.setCancelled(true);
        }
    }
    
    /**
     * 禁止观战者受到伤害
     */
    @EventHandler(priority = EventPriority.HIGH)
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
     * 禁止观战者攻击其他实体
     */
    @EventHandler(priority = EventPriority.HIGH)
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
     * 禁止观战者丢弃物品
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        
        if (isSpectator(player)) {
            event.setCancelled(true);
        }
    }
    
    /**
     * 禁止观战者拾取物品
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        
        if (isSpectator(player)) {
            event.setCancelled(true);
        }
    }
    
    /**
     * 处理观战者右键点击（切换观战目标）
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        if (!isSpectator(player)) {
            return;
        }
        
        // 如果手持指南针，打开观战菜单
        if (player.getInventory().getItemInMainHand().getType() == org.bukkit.Material.COMPASS) {
            event.setCancelled(true);
            openSpectatorMenu(player);
        }
    }
    
    /**
     * 检查玩家是否是观战者
     */
    private boolean isSpectator(Player player) {
        ManhuntGame game = plugin.getManhuntManager().getPlayerGame(player);
        if (game == null) {
            return false;
        }
        
        PlayerRole role = game.getPlayerRole(player.getUniqueId());
        return role == PlayerRole.SPECTATOR;
    }
    
    /**
     * 打开观战菜单
     */
    private void openSpectatorMenu(Player spectator) {
        ManhuntGame game = plugin.getManhuntManager().getPlayerGame(spectator);
        if (game == null) {
            return;
        }
        
        spectator.sendMessage("§6========== §e观战菜单 §6==========");
        spectator.sendMessage("§7点击玩家名称传送到该玩家");
        spectator.sendMessage("");
        
        // 显示所有存活玩家
        int index = 1;
        for (java.util.UUID uuid : game.getAllPlayers()) {
            Player target = plugin.getServer().getPlayer(uuid);
            if (target != null && target.isOnline()) {
                PlayerRole role = game.getPlayerRole(uuid);
                if (role != PlayerRole.SPECTATOR) {
                    String roleColor = role == PlayerRole.RUNNER ? "§a" : "§c";
                    String roleName = role == PlayerRole.RUNNER ? "逃亡者" : "猎人";
                    
                    // 创建可点击的消息（使用命令）
                    spectator.sendMessage("§e" + index + ". " + roleColor + target.getName() + 
                        " §7(" + roleName + ")");
                    index++;
                }
            }
        }
        
        spectator.sendMessage("");
        spectator.sendMessage("§7使用 §e/hg spectate <玩家名> §7传送到玩家");
    }
}
