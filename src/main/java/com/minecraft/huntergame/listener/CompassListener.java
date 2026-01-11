package com.minecraft.huntergame.listener;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.game.ManhuntGame;
import com.minecraft.huntergame.game.PlayerRole;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * 指南针监听器
 * 处理追踪指南针的丢弃事件（Q键切换目标）
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
     * 监听玩家丢弃物品事件（Q键）
     * 用于切换追踪目标
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemDrop().getItemStack();
        
        // 检查是否是指南针
        if (item == null || item.getType() != Material.COMPASS) {
            return;
        }
        
        // 检查是否是追踪指南针（通过名称判断）
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return;
        }
        
        String displayName = ChatColor.stripColor(meta.getDisplayName());
        if (!displayName.contains("追踪指南针")) {
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
        
        // 取消丢弃事件（禁止丢弃追踪指南针）
        // 取消事件后，Minecraft会自动将物品放回玩家手中/原位置
        event.setCancelled(true);
        
        // 切换追踪目标
        plugin.getTrackerManager().switchTarget(player);
    }
    

}
