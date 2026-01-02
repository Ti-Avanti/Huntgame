package com.minecraft.huntergame.listener;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.gui.SpectatorMenuGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 观战菜单监听器
 * 
 * @author YourName
 * @version 1.0.0
 */
public class SpectatorMenuListener implements Listener {
    
    private final HunterGame plugin;
    private final Map<UUID, SpectatorMenuGUI> openMenus;
    
    public SpectatorMenuListener(HunterGame plugin) {
        this.plugin = plugin;
        this.openMenus = new HashMap<>();
    }
    
    /**
     * 注册观战菜单
     */
    public void registerMenu(Player player, SpectatorMenuGUI menu) {
        openMenus.put(player.getUniqueId(), menu);
    }
    
    /**
     * 移除观战菜单
     */
    public void removeMenu(Player player) {
        openMenus.remove(player.getUniqueId());
    }
    
    /**
     * 获取玩家打开的菜单
     */
    public SpectatorMenuGUI getMenu(Player player) {
        return openMenus.get(player.getUniqueId());
    }
    
    /**
     * 检查是否是观战菜单
     */
    public boolean isSpectatorMenu(Inventory inventory) {
        for (SpectatorMenuGUI menu : openMenus.values()) {
            if (menu.getInventory().equals(inventory)) {
                return true;
            }
        }
        return false;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        
        if (clickedInventory == null) {
            return;
        }
        
        // 检查是否是观战菜单
        if (!isSpectatorMenu(clickedInventory)) {
            return;
        }
        
        // 取消事件
        event.setCancelled(true);
        
        // 获取菜单
        SpectatorMenuGUI menu = getMenu(player);
        if (menu == null) {
            return;
        }
        
        // 处理点击
        menu.handleClick(event.getSlot(), event.getCurrentItem());
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getPlayer();
        
        // 检查是否是观战菜单
        if (isSpectatorMenu(event.getInventory())) {
            // 延迟移除，避免在关闭时立即移除
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                // 如果玩家没有打开新的观战菜单，则移除记录
                if (player.getOpenInventory().getTopInventory() == null || 
                    !isSpectatorMenu(player.getOpenInventory().getTopInventory())) {
                    removeMenu(player);
                }
            }, 1L);
        }
    }
}
