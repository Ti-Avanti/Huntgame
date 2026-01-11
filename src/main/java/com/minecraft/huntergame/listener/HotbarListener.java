package com.minecraft.huntergame.listener;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.game.GameState;
import com.minecraft.huntergame.game.ManhuntGame;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Hotbar快捷道具监听器
 * 
 * @author YourName
 * @version 1.0.0
 */
public class HotbarListener implements Listener {
    
    private final HunterGame plugin;
    
    public HotbarListener(HunterGame plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 处理玩家交互事件
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        // 只处理右键点击
        if (event.getAction() != Action.RIGHT_CLICK_AIR && 
            event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        // 检查是否是快捷道具
        if (plugin.getHotbarManager().isHotbarItem(item)) {
            event.setCancelled(true);
            plugin.getHotbarManager().handleHotbarClick(player, item);
        }
    }
    
    /**
     * 防止玩家丢弃快捷道具
     */
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemDrop().getItemStack();
        
        // 如果是快捷道具且玩家不在游戏中，取消丢弃
        if (plugin.getHotbarManager().isHotbarItem(item)) {
            // 检查玩家是否在游戏中
            if (!plugin.getManhuntManager().isInGame(player) || 
                player.getGameMode() == GameMode.SPECTATOR) {
                event.setCancelled(true);
                player.sendMessage("§c你不能丢弃快捷道具！");
            }
        }
    }
    
    /**
     * 防止玩家在等待/匹配状态下移动hotbar物品
     * 完全锁定物品栏，防止任何物品移动
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        ManhuntGame game = plugin.getManhuntManager().getPlayerGame(player);
        
        // 如果玩家不在游戏中（主大厅），也锁定物品栏
        if (game == null) {
            event.setCancelled(true);
            if (event.getCurrentItem() != null || event.getCursor() != null) {
                player.sendMessage("§c大厅中无法移动物品！");
            }
            return;
        }
        
        GameState state = game.getState();
        
        // 只在等待和匹配状态下锁定物品栏
        if (state == GameState.WAITING || state == GameState.MATCHING) {
            // 完全取消所有物品栏操作
            event.setCancelled(true);
            
            // 只在玩家点击物品时提示（避免刷屏）
            if (event.getCurrentItem() != null || event.getCursor() != null) {
                player.sendMessage("§c匹配期间无法移动物品！");
            }
        }
    }
    
    /**
     * 防止玩家在等待/匹配状态下切换副手物品
     */
    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        ManhuntGame game = plugin.getManhuntManager().getPlayerGame(player);
        
        // 如果玩家不在游戏中（主大厅），也锁定
        if (game == null) {
            event.setCancelled(true);
            return;
        }
        
        GameState state = game.getState();
        
        // 只在等待和匹配状态下锁定
        if (state == GameState.WAITING || state == GameState.MATCHING) {
            event.setCancelled(true);
        }
    }
    
    /**
     * 防止玩家在等待/匹配状态下丢弃物品
     */
    @EventHandler
    public void onPlayerDropItemInGame(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ManhuntGame game = plugin.getManhuntManager().getPlayerGame(player);
        
        // 如果玩家不在游戏中（主大厅），也禁止丢弃
        if (game == null) {
            ItemStack item = event.getItemDrop().getItemStack();
            if (plugin.getHotbarManager().isHotbarItem(item)) {
                event.setCancelled(true);
                player.sendMessage("§c你不能丢弃快捷道具！");
            }
            return;
        }
        
        GameState state = game.getState();
        
        // 在等待和匹配状态下，禁止丢弃任何物品
        if (state == GameState.WAITING || state == GameState.MATCHING) {
            event.setCancelled(true);
            player.sendMessage("§c匹配期间无法丢弃物品！");
            return;
        }
        
        // 在游戏进行中，禁止猎人丢弃追踪指南针
        if (state == GameState.PREPARING || state == GameState.PLAYING) {
            ItemStack item = event.getItemDrop().getItemStack();
            
            // 检查是否是追踪指南针
            if (isTrackerCompass(item)) {
                // 检查玩家是否是猎人
                if (game.getHunters().contains(player.getUniqueId())) {
                    event.setCancelled(true);
                    player.sendMessage("§c你不能丢弃追踪指南针！");
                }
            }
        }
    }
    
    /**
     * 检查物品是否是追踪指南针
     */
    private boolean isTrackerCompass(ItemStack item) {
        if (item == null || item.getType() != org.bukkit.Material.COMPASS) {
            return false;
        }
        
        if (!item.hasItemMeta()) {
            return false;
        }
        
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }
        
        // 检查显示名称是否包含"追踪指南针"
        String displayName = org.bukkit.ChatColor.stripColor(meta.getDisplayName());
        return displayName != null && displayName.contains("追踪指南针");
    }
}
