package com.minecraft.huntergame.bungee;

import com.minecraft.huntergame.HunterGame;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;

/**
 * Bungee管理器
 * 负责BungeeCord插件消息通道管理
 * 
 * @author YourName
 * @version 1.0.0
 */
public class BungeeManager {
    
    private final HunterGame plugin;
    private final String lobbyServer;
    private final boolean autoSend;
    private final int sendDelay;
    
    public BungeeManager(HunterGame plugin) {
        this.plugin = plugin;
        this.lobbyServer = plugin.getMainConfig().getLobbyServer();
        this.autoSend = plugin.getMainConfig().isAutoSend();
        this.sendDelay = plugin.getMainConfig().getSendDelay();
        
        // 注册BungeeCord插件消息通道
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
        
        plugin.getLogger().info("Bungee管理器已初始化");
        plugin.getLogger().info("大厅服务器: " + lobbyServer);
    }
    
    /**
     * 发送玩家到指定服务器
     * 
     * @param player 玩家
     * @param server 服务器名称
     */
    public void sendPlayerToServer(Player player, String server) {
        if (player == null || !player.isOnline()) {
            return;
        }
        
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);
        
        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
        
        plugin.getLogger().info("发送玩家 " + player.getName() + " 到服务器: " + server);
    }
    
    /**
     * 发送玩家到大厅服务器
     * 
     * @param player 玩家
     */
    public void sendPlayerToLobby(Player player) {
        sendPlayerToServer(player, lobbyServer);
    }
    
    /**
     * 延迟发送玩家到大厅服务器
     * 
     * @param player 玩家
     */
    public void sendPlayerToLobbyDelayed(Player player) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player != null && player.isOnline()) {
                sendPlayerToLobby(player);
            }
        }, sendDelay * 20L);
    }
    
    /**
     * 游戏结束后自动传送玩家
     * 
     * @param player 玩家
     */
    public void handleGameEnd(Player player) {
        if (!autoSend) {
            return;
        }
        
        sendPlayerToLobbyDelayed(player);
    }
    
    /**
     * 获取大厅服务器名称
     */
    public String getLobbyServer() {
        return lobbyServer;
    }
    
    /**
     * 是否启用自动传送
     */
    public boolean isAutoSend() {
        return autoSend;
    }
    
    /**
     * 获取传送延迟
     */
    public int getSendDelay() {
        return sendDelay;
    }
    
    /**
     * 关闭Bungee管理器
     */
    public void shutdown() {
        plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin, "BungeeCord");
        plugin.getLogger().info("Bungee管理器已关闭");
    }
}
