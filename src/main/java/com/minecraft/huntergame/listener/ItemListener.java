package com.minecraft.huntergame.listener;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.arena.Arena;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * 道具事件监听器
 * 监听道具使用和拾取事件
 * 
 * @author YourName
 * @version 1.0.0
 */
public class ItemListener implements Listener {
    
    private final HunterGame plugin;
    
    public ItemListener(HunterGame plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 监听玩家交互事件（道具使用）
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        // 检查是否有物品
        if (item == null || !item.hasItemMeta()) {
            return;
        }
        
        // 检查玩家是否在竞技场中
        Arena arena = plugin.getArenaManager().getPlayerArena(player.getUniqueId());
        if (arena == null) {
            return;
        }
        
        // 检查游戏是否在进行中
        if (!arena.getStateManager().isPlaying()) {
            return;
        }
        
        // 检查是否为道具物品
        if (!item.getItemMeta().hasDisplayName()) {
            return;
        }
        
        String displayName = item.getItemMeta().getDisplayName();
        
        // 尝试使用道具
        boolean used = plugin.getItemManager().useItem(player, getItemNameFromDisplay(displayName));
        
        if (used) {
            event.setCancelled(true);
            plugin.getLogger().info("[" + arena.getArenaName() + "] 玩家 " + player.getName() + 
                " 使用了道具: " + displayName);
        }
    }
    
    /**
     * 从显示名称获取道具名称
     * 
     * @param displayName 显示名称
     * @return 道具名称
     */
    private String getItemNameFromDisplay(String displayName) {
        // 移除颜色代码
        String cleanName = displayName.replaceAll("§[0-9a-fk-or]", "");
        
        // 根据显示名称映射到道具名称
        switch (cleanName) {
            case "隐身药水":
                return "invisibility-potion";
            case "速度药水":
                return "speed-potion";
            case "烟雾弹":
                return "smoke-bomb";
            case "诱饵":
                return "decoy";
            default:
                return cleanName.toLowerCase().replace(" ", "-");
        }
    }
}
