package com.minecraft.huntergame.listener;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.gui.SpectateGameGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 观战游戏GUI监听器
 * 
 * @author YourName
 * @version 1.0.0
 */
public class SpectateGameListener implements Listener {
    
    private final HunterGame plugin;
    private final Map<UUID, SpectateGameGUI> activeGUIs;
    
    public SpectateGameListener(HunterGame plugin) {
        this.plugin = plugin;
        this.activeGUIs = new HashMap<>();
    }
    
    /**
     * 注册GUI
     */
    public void registerGUI(Player player, SpectateGameGUI gui) {
        activeGUIs.put(player.getUniqueId(), gui);
    }
    
    /**
     * 取消注册GUI
     */
    public void unregisterGUI(Player player) {
        activeGUIs.remove(player.getUniqueId());
    }
    
    /**
     * 监听GUI点击
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        
        if (clickedInventory == null) {
            return;
        }
        
        // 检查是否是观战游戏GUI
        SpectateGameGUI gui = activeGUIs.get(player.getUniqueId());
        if (gui == null) {
            return;
        }
        
        // 检查是否点击的是GUI
        if (!clickedInventory.equals(gui.getInventory())) {
            return;
        }
        
        // 取消事件
        event.setCancelled(true);
        
        // 处理点击
        int slot = event.getSlot();
        gui.handleClick(slot, event.getCurrentItem());
    }
    
    /**
     * 监听GUI关闭
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getPlayer();
        
        // 取消注册GUI
        unregisterGUI(player);
    }
}
