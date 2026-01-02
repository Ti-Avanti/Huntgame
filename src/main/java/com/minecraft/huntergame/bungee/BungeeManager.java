package com.minecraft.huntergame.bungee;

import com.minecraft.huntergame.HunterGame;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Bungee管理器
 * 负责BungeeCord插件消息通道管理
 * 
 * @author YourName
 * @version 2.0.0 - Bungee 模式增强
 */
public class BungeeManager {
    
    private final HunterGame plugin;
    private final String lobbyServer;
    private final boolean autoSend;
    private final int sendDelay;
    private LoadBalancer loadBalancer;
    
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
     * 设置负载均衡器
     */
    public void setLoadBalancer(LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
    }
    
    /**
     * 发送玩家到指定服务器
     * 
     * @param player 玩家
     * @param server 服务器名称
     */
    public void sendPlayerToServer(Player player, String server) {
        if (player == null || !player.isOnline()) {
            plugin.getLogger().warning("无法传送玩家：玩家为null或不在线");
            return;
        }
        
        if (server == null || server.isEmpty()) {
            plugin.getLogger().warning("无法传送玩家 " + player.getName() + "：服务器名称无效");
            return;
        }
        
        try {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Connect");
            out.writeUTF(server);
            
            player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
            
            plugin.getLogger().info("发送玩家 " + player.getName() + " 到服务器: " + server);
        } catch (Exception ex) {
            plugin.getLogger().severe("传送玩家 " + player.getName() + " 到服务器 " + server + " 失败: " + ex.getMessage());
            ex.printStackTrace();
        }
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
     * 传送玩家到最佳游戏服务器
     * 
     * @param player 玩家
     * @return 是否成功
     */
    public boolean sendPlayerToBestServer(Player player) {
        plugin.getLogger().info("[DEBUG] sendPlayerToBestServer 被调用，玩家: " + player.getName());
        
        if (loadBalancer == null) {
            plugin.getLogger().warning("[ERROR] 负载均衡器未初始化");
            return false;
        }
        
        // 使用子大厅前缀选择服务器
        String subLobbyPrefix = plugin.getManhuntConfig().getSubLobbyPrefix();
        plugin.getLogger().info("[DEBUG] 子大厅前缀: " + subLobbyPrefix);
        
        String bestServer = loadBalancer.selectBestServer(subLobbyPrefix);
        plugin.getLogger().info("[DEBUG] 选择的最佳服务器: " + bestServer);
        
        if (bestServer == null) {
            plugin.getLogger().warning("[WARN] 没有可用的游戏服务器");
            player.sendMessage("§c当前没有可用的游戏服务器，请稍后重试");
            return false;
        }
        
        plugin.getLogger().info("[DEBUG] 准备传送玩家到服务器: " + bestServer);
        sendPlayerToServer(player, bestServer);
        return true;
    }
    
    /**
     * 批量传送玩家回主大厅
     * 
     * @param players 玩家列表
     */
    public void sendPlayersToMainLobby(List<Player> players) {
        if (players == null || players.isEmpty()) {
            return;
        }
        
        String mainLobby = plugin.getManhuntConfig().getMainLobby();
        int delay = plugin.getManhuntConfig().getReturnDelay();
        
        // 显示倒计时消息
        for (Player player : players) {
            if (player != null && player.isOnline()) {
                player.sendMessage("§e游戏结束！§a" + delay + "§e秒后将返回主大厅...");
            }
        }
        
        // 延迟传送
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            for (Player player : players) {
                if (player != null && player.isOnline()) {
                    sendPlayerToServer(player, mainLobby);
                }
            }
        }, delay * 20L);
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
