package com.minecraft.huntergame.listener;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.game.ManhuntGame;
import com.minecraft.huntergame.game.PlayerRole;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * 指南针监听器
 * 处理追踪指南针的右键点击事件
 * 
 * @author YourName
 * @version 1.0.0
 */
public class CompassListener implements Listener {
    
    private final HunterGame plugin;
    
    public CompassListener(HunterGame plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 监听玩家右键点击事件
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        // 检查是否是右键点击
        if (event.getAction() != Action.RIGHT_CLICK_AIR && 
            event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        // 检查是否持有指南针
        if (item == null || item.getType() != Material.COMPASS) {
            return;
        }
        
        // 检查玩家是否在游戏中
        ManhuntGame game = plugin.getManhuntManager().getPlayerGame(player);
        if (game == null) {
            return;
        }
        
        // 检查玩家是否是猎人
        PlayerRole role = game.getPlayerRole(player.getUniqueId());
        if (role != PlayerRole.HUNTER) {
            return;
        }
        
        // 取消默认行为
        event.setCancelled(true);
        
        // 处理指南针点击
        plugin.getTrackerManager().handleCompassClick(player);
    }
}
