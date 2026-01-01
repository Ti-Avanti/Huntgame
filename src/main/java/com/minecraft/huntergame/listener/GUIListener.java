package com.minecraft.huntergame.listener;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.gui.BaseGUI;
import com.minecraft.huntergame.gui.CreateRoomGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

/**
 * GUI监听器
 * 处理GUI相关的事件
 * 
 * @author YourName
 * @version 1.0.0
 */
public class GUIListener implements Listener {
    
    private final HunterGame plugin;
    
    public GUIListener(HunterGame plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 处理GUI点击事件
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        
        // 检查是否是GUI
        if (!plugin.getGUIManager().isGUI(event.getView().getTopInventory())) {
            return;
        }
        
        // 取消所有GUI中的点击事件
        event.setCancelled(true);
        
        // 如果点击的是玩家自己的背包，不处理
        if (event.getClickedInventory() == player.getInventory()) {
            return;
        }
        
        // 如果没有点击物品，不处理
        if (item == null || !item.hasItemMeta()) {
            return;
        }
        
        // 获取玩家当前打开的GUI
        BaseGUI gui = plugin.getGUIManager().getOpenGUI(player);
        
        if (gui == null) {
            return;
        }
        
        // 特殊处理CreateRoomGUI（需要ClickType）
        if (gui instanceof CreateRoomGUI) {
            ((CreateRoomGUI) gui).handleClick(event.getSlot(), item, event.getClick());
            return;
        }
        
        // 调用GUI的点击处理方法
        gui.handleClick(event.getSlot(), item);
    }
    
    /**
     * 处理GUI关闭事件
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getPlayer();
        
        // 检查是否是GUI
        if (!plugin.getGUIManager().isGUI(event.getInventory())) {
            return;
        }
        
        // 延迟移除GUI记录，避免在重新打开GUI时出现问题
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            // 如果玩家没有打开新的GUI，则移除记录
            if (player.getOpenInventory().getTopInventory() == null || 
                !plugin.getGUIManager().isGUI(player.getOpenInventory().getTopInventory())) {
                plugin.getGUIManager().removeGUI(player);
            }
        }, 1L);
    }
}
