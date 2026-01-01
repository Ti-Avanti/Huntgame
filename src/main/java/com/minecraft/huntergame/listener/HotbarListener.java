package com.minecraft.huntergame.listener;

import com.minecraft.huntergame.HunterGame;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
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
}
