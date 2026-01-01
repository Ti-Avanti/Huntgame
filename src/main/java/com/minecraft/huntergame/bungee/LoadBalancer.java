package com.minecraft.huntergame.bungee;

import com.minecraft.huntergame.HunterGame;

import java.util.*;

/**
 * 负载均衡器
 * 负责选择最佳游戏服务器
 * 
 * @author YourName
 * @version 1.0.0
 */
public class LoadBalancer {
    
    private final HunterGame plugin;
    private final RedisManager redisManager;
    
    public LoadBalancer(HunterGame plugin, RedisManager redisManager) {
        this.plugin = plugin;
        this.redisManager = redisManager;
    }
    
    /**
     * 选择最佳服务器
     * 
     * @return 服务器名称，如果没有可用服务器则返回null
     */
    public String selectBestServer() {
        return selectBestServer(null);
    }
    
    /**
     * 选择最佳服务器（指定服务器组）
     * 
     * @param serverGroup 服务器组前缀（可为null）
     * @return 服务器名称，如果没有可用服务器则返回null
     */
    public String selectBestServer(String serverGroup) {
        Set<String> serverKeys = redisManager.getOnlineServers();
        
        if (serverKeys == null || serverKeys.isEmpty()) {
            plugin.getLogger().warning("没有可用的游戏服务器");
            return null;
        }
        
        String bestServer = null;
        int minPlayers = Integer.MAX_VALUE;
        long currentTime = System.currentTimeMillis();
        final long SERVER_TIMEOUT = 60000; // 60秒超时
        
        for (String key : serverKeys) {
            // 从key中提取服务器名称
            String serverName = key.replace("huntergame:servers:", "");
            
            // 如果指定了服务器组，检查服务器名称是否匹配
            if (serverGroup != null && !serverName.startsWith(serverGroup)) {
                continue;
            }
            
            // 获取服务器信息
            Map<String, String> serverInfo = redisManager.getServerInfo(serverName);
            
            if (serverInfo == null || serverInfo.isEmpty()) {
                continue;
            }
            
            // 检查服务器状态
            String status = serverInfo.get("status");
            if (!"ONLINE".equals(status)) {
                continue;
            }
            
            // 检查时间戳（过期的服务器不考虑）
            try {
                long timestamp = Long.parseLong(serverInfo.get("timestamp"));
                
                if (currentTime - timestamp > SERVER_TIMEOUT) {
                    plugin.getLogger().warning("服务器 " + serverName + " 心跳超时，跳过");
                    continue;
                }
            } catch (NumberFormatException ex) {
                plugin.getLogger().warning("服务器 " + serverName + " 时间戳格式错误");
                continue;
            }
            
            // 获取玩家数量
            try {
                int players = Integer.parseInt(serverInfo.get("players"));
                int maxPlayers = Integer.parseInt(serverInfo.get("maxPlayers"));
                
                // 检查服务器是否已满
                if (players >= maxPlayers) {
                    continue;
                }
                
                // 选择玩家数最少的服务器
                if (players < minPlayers) {
                    minPlayers = players;
                    bestServer = serverName;
                }
                
            } catch (NumberFormatException ex) {
                plugin.getLogger().warning("解析服务器 " + serverName + " 玩家数量失败");
                continue;
            }
        }
        
        if (bestServer != null) {
            plugin.getLogger().info("选择服务器: " + bestServer + " (玩家数: " + minPlayers + ")");
        } else {
            plugin.getLogger().warning("没有可用的游戏服务器");
        }
        
        return bestServer;
    }
    
    /**
     * 获取服务器负载信息
     * 
     * @return 服务器名称 -> 玩家数量
     */
    public Map<String, Integer> getServerLoads() {
        Map<String, Integer> loads = new HashMap<>();
        Set<String> serverKeys = redisManager.getOnlineServers();
        
        for (String key : serverKeys) {
            String serverName = key.replace("huntergame:servers:", "");
            Map<String, String> serverInfo = redisManager.getServerInfo(serverName);
            
            if (!serverInfo.isEmpty()) {
                try {
                    int players = Integer.parseInt(serverInfo.get("players"));
                    loads.put(serverName, players);
                } catch (Exception ex) {
                    // 忽略解析错误
                }
            }
        }
        
        return loads;
    }
    
    /**
     * 获取可用服务器数量
     */
    public int getAvailableServerCount() {
        Set<String> serverKeys = redisManager.getOnlineServers();
        int count = 0;
        
        for (String key : serverKeys) {
            String serverName = key.replace("huntergame:servers:", "");
            Map<String, String> serverInfo = redisManager.getServerInfo(serverName);
            
            if (!serverInfo.isEmpty()) {
                String status = serverInfo.get("status");
                if ("ONLINE".equals(status)) {
                    count++;
                }
            }
        }
        
        return count;
    }
    
    /**
     * 检查服务器是否在线
     */
    public boolean isServerOnline(String serverName) {
        Map<String, String> serverInfo = redisManager.getServerInfo(serverName);
        
        if (serverInfo.isEmpty()) {
            return false;
        }
        
        String status = serverInfo.get("status");
        return "ONLINE".equals(status);
    }
}
