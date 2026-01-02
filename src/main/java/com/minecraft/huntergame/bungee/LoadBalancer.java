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
        plugin.debug("开始选择最佳服务器，服务器组前缀: " + serverGroup);
        
        Set<String> serverKeys = redisManager.getOnlineServers();
        plugin.debug("从 Redis 获取到 " + (serverKeys != null ? serverKeys.size() : 0) + " 个服务器键");
        
        if (serverKeys == null || serverKeys.isEmpty()) {
            plugin.getLogger().warning("[LoadBalancer] 没有可用的游戏服务器（Redis 中没有注册的服务器）");
            plugin.getLogger().warning("[LoadBalancer] 请确保子大厅服务器已启动并正确配置 Redis");
            return null;
        }
        
        String bestServer = null;
        int minPlayers = Integer.MAX_VALUE;
        long currentTime = System.currentTimeMillis();
        final long SERVER_TIMEOUT = 60000; // 60秒超时
        
        int checkedServers = 0;
        int filteredByPrefix = 0;
        int filteredByStatus = 0;
        int filteredByTimeout = 0;
        int filteredByFull = 0;
        
        for (String key : serverKeys) {
            // 从key中提取服务器名称
            String serverName = key.replace("huntergame:servers:", "");
            plugin.debug("检查服务器: " + serverName);
            checkedServers++;
            
            // 如果指定了服务器组，检查服务器名称是否匹配
            if (serverGroup != null && !serverName.startsWith(serverGroup)) {
                plugin.debug("  - 跳过（不匹配前缀 '" + serverGroup + "'）");
                filteredByPrefix++;
                continue;
            }
            
            // 获取服务器信息
            Map<String, String> serverInfo = redisManager.getServerInfo(serverName);
            
            if (serverInfo == null || serverInfo.isEmpty()) {
                plugin.debug("  - 跳过（无法获取服务器信息）");
                continue;
            }
            
            plugin.debug("  - 服务器信息: " + serverInfo);
            
            // 检查服务器状态
            String status = serverInfo.get("status");
            if (!"ONLINE".equals(status) && !"WAITING".equals(status)) {
                plugin.debug("  - 跳过（状态不是 ONLINE 或 WAITING: " + status + "）");
                filteredByStatus++;
                continue;
            }
            
            // 检查时间戳（过期的服务器不考虑）
            try {
                long timestamp = Long.parseLong(serverInfo.get("timestamp"));
                long age = currentTime - timestamp;
                
                if (age > SERVER_TIMEOUT) {
                    plugin.getLogger().warning("服务器 " + serverName + " 心跳超时（" + (age/1000) + "秒），跳过");
                    filteredByTimeout++;
                    continue;
                }
                plugin.debug("  - 心跳正常（" + (age/1000) + "秒前）");
            } catch (NumberFormatException ex) {
                plugin.getLogger().warning("服务器 " + serverName + " 时间戳格式错误");
                continue;
            }
            
            // 获取玩家数量
            try {
                int players = Integer.parseInt(serverInfo.get("players"));
                int maxPlayers = Integer.parseInt(serverInfo.get("maxPlayers"));
                
                plugin.debug("  - 玩家数: " + players + "/" + maxPlayers);
                
                // 检查服务器是否已满
                if (players >= maxPlayers) {
                    plugin.debug("  - 跳过（服务器已满）");
                    filteredByFull++;
                    continue;
                }
                
                // 选择玩家数最少的服务器
                if (players < minPlayers) {
                    minPlayers = players;
                    bestServer = serverName;
                    plugin.debug("  - 当前最佳选择");
                }
                
            } catch (NumberFormatException ex) {
                plugin.getLogger().warning("解析服务器 " + serverName + " 玩家数量失败");
                continue;
            }
        }
        
        // 输出统计信息
        plugin.debug("服务器选择统计:");
        plugin.debug("  - 检查的服务器总数: " + checkedServers);
        plugin.debug("  - 前缀不匹配: " + filteredByPrefix);
        plugin.debug("  - 状态不可用: " + filteredByStatus);
        plugin.debug("  - 心跳超时: " + filteredByTimeout);
        plugin.debug("  - 服务器已满: " + filteredByFull);
        
        if (bestServer != null) {
            plugin.getLogger().info("[LoadBalancer] 选择服务器: " + bestServer + " (玩家数: " + minPlayers + ")");
        } else {
            plugin.getLogger().warning("[LoadBalancer] 没有可用的游戏服务器");
            plugin.getLogger().warning("[LoadBalancer] 可能原因：");
            plugin.getLogger().warning("  1. 子大厅服务器未启动");
            plugin.getLogger().warning("  2. 子大厅服务器 Redis 配置错误");
            plugin.getLogger().warning("  3. 子大厅服务器名称前缀不匹配（期望: " + serverGroup + "）");
            plugin.getLogger().warning("  4. 所有子大厅服务器已满");
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
