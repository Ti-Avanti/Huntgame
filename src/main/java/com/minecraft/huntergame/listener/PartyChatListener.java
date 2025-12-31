package com.minecraft.huntergame.listener;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.party.PartyManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * 队伍聊天监听器
 * 
 * @author YourName
 * @version 1.0.0
 */
public class PartyChatListener implements Listener {
    
    private final HunterGame plugin;
    private final PartyManager partyManager;
    
    public PartyChatListener(HunterGame plugin) {
        this.plugin = plugin;
        this.partyManager = plugin.getPartyManager();
    }
    
    /**
     * 处理玩家聊天事件
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        
        // 检查玩家是否开启队伍聊天模式
        if (!partyManager.isInPartyChatMode(player)) {
            return;
        }
        
        // 检查玩家是否在队伍中
        if (!partyManager.hasParty(player)) {
            return;
        }
        
        // 取消原始聊天事件
        event.setCancelled(true);
        
        // 发送队伍聊天消息
        String message = event.getMessage();
        partyManager.sendPartyChat(player, message);
    }
    
    /**
     * 处理玩家离线事件
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // 处理玩家离开队伍
        partyManager.handlePlayerQuit(player);
    }
}
