package com.minecraft.huntergame.bungee;

import com.minecraft.huntergame.HunterGame;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Redis管理器
 * 负责Redis连接和服务器状态管理
 * 
 * @author YourName
 * @version 1.0.0
 */
public class RedisManager {
    
    private final HunterGame plugin;
    private JedisPool jedisPool;
    private final String serverName;
    private final String keyPrefix = "huntergame:";
    
    public RedisManager(HunterGame plugin) {
        this.plugin = plugin;
        this.serverName = plugin.getMainConfig().getRedisServerName();
    }
    
    /**
     * 连接Redis
     */
    public boolean connect() {
        try {
            String host = plugin.getMainConfig().getRedisHost();
            int port = plugin.getMainConfig().getRedisPort();
            String password = plugin.getMainConfig().getRedisPassword();
            int database = plugin.getMainConfig().getRedisDatabase();
            int timeout = plugin.getMainConfig().getRedisTimeout();
            
            // 配置连接池
            JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setMaxTotal(plugin.getMainConfig().getRedisMaxTotal());
            poolConfig.setMaxIdle(plugin.getMainConfig().getRedisMaxIdle());
            poolConfig.setMinIdle(plugin.getMainConfig().getRedisMinIdle());
            poolConfig.setTestOnBorrow(true);
            poolConfig.setTestOnReturn(true);
            poolConfig.setTestWhileIdle(true);
            
            // 创建连接池
            if (password != null && !password.isEmpty()) {
                jedisPool = new JedisPool(poolConfig, host, port, timeout, password, database);
            } else {
                jedisPool = new JedisPool(poolConfig, host, port, timeout);
            }
            
            // 测试连接
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.ping();
            }
            
            plugin.getLogger().info("Redis连接成功: " + host + ":" + port);
            return true;
            
        } catch (Exception ex) {
            plugin.getLogger().severe("Redis连接失败: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }
    
    /**
     * 断开Redis连接
     */
    public void disconnect() {
        if (jedisPool != null && !jedisPool.isClosed()) {
            try {
                // 注销服务器
                unregisterServer();
                
                jedisPool.close();
                plugin.getLogger().info("Redis连接已关闭");
            } catch (Exception ex) {
                plugin.getLogger().severe("关闭Redis连接失败: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * 关闭Redis连接（别名）
     */
    public void shutdown() {
        disconnect();
    }
    
    /**
     * 注册服务器到Redis
     */
    public void registerServer() {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = keyPrefix + "servers:" + serverName;
            
            Map<String, String> serverData = new HashMap<>();
            serverData.put("name", serverName);
            serverData.put("status", "ONLINE");
            serverData.put("players", "0");
            serverData.put("maxPlayers", String.valueOf(plugin.getManhuntConfig().getMaxPlayers()));
            serverData.put("type", plugin.getManhuntConfig().getServerType().name());
            serverData.put("timestamp", String.valueOf(System.currentTimeMillis()));
            
            jedis.hmset(key, serverData);
            jedis.expire(key, 30); // 30秒过期
            
            plugin.getLogger().info("服务器已注册到Redis: " + serverName + " (类型: " + plugin.getManhuntConfig().getServerType() + ")");
            plugin.debug("Redis 键: " + key);
            plugin.debug("服务器数据: " + serverData);
            
        } catch (Exception ex) {
            plugin.getLogger().severe("注册服务器到Redis失败: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    /**
     * 注销服务器
     */
    public void unregisterServer() {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = keyPrefix + "servers:" + serverName;
            jedis.del(key);
            
            plugin.getLogger().info("服务器已从Redis注销: " + serverName);
            
        } catch (Exception ex) {
            plugin.getLogger().severe("从Redis注销服务器失败: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    /**
     * 更新服务器状态
     */
    public void updateServerStatus(int playerCount, String status) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = keyPrefix + "servers:" + serverName;
            
            Map<String, String> updates = new HashMap<>();
            updates.put("players", String.valueOf(playerCount));
            updates.put("status", status);
            updates.put("timestamp", String.valueOf(System.currentTimeMillis()));
            
            jedis.hmset(key, updates);
            jedis.expire(key, 30); // 30秒过期
            
        } catch (Exception ex) {
            plugin.getLogger().warning("更新服务器状态失败: " + ex.getMessage());
        }
    }
    
    /**
     * 获取所有在线服务器
     */
    public Set<String> getOnlineServers() {
        try (Jedis jedis = jedisPool.getResource()) {
            String pattern = keyPrefix + "servers:*";
            Set<String> keys = jedis.keys(pattern);
            return keys != null ? keys : Set.of();
            
        } catch (Exception ex) {
            plugin.getLogger().warning("获取在线服务器列表失败: " + ex.getMessage());
            return Set.of();
        }
    }
    
    /**
     * 获取服务器信息
     */
    public Map<String, String> getServerInfo(String serverName) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = keyPrefix + "servers:" + serverName;
            return jedis.hgetAll(key);
            
        } catch (Exception ex) {
            plugin.getLogger().warning("获取服务器信息失败: " + ex.getMessage());
            return new HashMap<>();
        }
    }
    
    /**
     * 发布服务器状态
     */
    public void publishServerStatus(String message) {
        try (Jedis jedis = jedisPool.getResource()) {
            String channel = keyPrefix + "status";
            jedis.publish(channel, message);
            
        } catch (Exception ex) {
            plugin.getLogger().warning("发布服务器状态失败: " + ex.getMessage());
        }
    }
    
    /**
     * 同步Manhunt游戏状态到Redis
     */
    public void syncManhuntGameState(String gameId, String state, int playerCount) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = keyPrefix + "manhunt:games:" + gameId;
            
            Map<String, String> gameData = new HashMap<>();
            gameData.put("gameId", gameId);
            gameData.put("server", serverName);
            gameData.put("state", state);
            gameData.put("players", String.valueOf(playerCount));
            gameData.put("timestamp", String.valueOf(System.currentTimeMillis()));
            
            jedis.hmset(key, gameData);
            jedis.expire(key, 300); // 5分钟过期
            
        } catch (Exception ex) {
            plugin.getLogger().warning("同步Manhunt游戏状态失败: " + ex.getMessage());
        }
    }
    
    /**
     * 移除Manhunt游戏状态
     */
    public void removeManhuntGameState(String gameId) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = keyPrefix + "manhunt:games:" + gameId;
            jedis.del(key);
            
        } catch (Exception ex) {
            plugin.getLogger().warning("移除Manhunt游戏状态失败: " + ex.getMessage());
        }
    }
    
    /**
     * 获取所有Manhunt游戏
     */
    public Set<String> getAllManhuntGames() {
        try (Jedis jedis = jedisPool.getResource()) {
            String pattern = keyPrefix + "manhunt:games:*";
            Set<String> keys = jedis.keys(pattern);
            return keys != null ? keys : Set.of();
            
        } catch (Exception ex) {
            plugin.getLogger().warning("获取Manhunt游戏列表失败: " + ex.getMessage());
            return Set.of();
        }
    }
    
    /**
     * 获取Manhunt游戏信息
     */
    public Map<String, String> getManhuntGameInfo(String gameId) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = keyPrefix + "manhunt:games:" + gameId;
            return jedis.hgetAll(key);
            
        } catch (Exception ex) {
            plugin.getLogger().warning("获取Manhunt游戏信息失败: " + ex.getMessage());
            return new HashMap<>();
        }
    }
    
    /**
     * 检查Redis连接是否可用
     */
    public boolean isConnected() {
        if (jedisPool == null || jedisPool.isClosed()) {
            return false;
        }
        
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.ping();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
    
    /**
     * 获取服务器名称
     */
    public String getServerName() {
        return serverName;
    }
    
    // ==================== Bungee 模式增强方法 ====================
    
    /**
     * 设置玩家的待处理动作
     * 用于跨服务器传递玩家意图（如创建房间）
     * 
     * @param playerUUID 玩家 UUID
     * @param action 动作类型（CREATE_ROOM, JOIN_GAME 等）
     * @param data 附加数据（可选）
     */
    public void setPlayerPendingAction(String playerUUID, String action, String data) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = keyPrefix + "player:pending:" + playerUUID;
            
            Map<String, String> actionData = new HashMap<>();
            actionData.put("action", action);
            actionData.put("data", data != null ? data : "");
            actionData.put("timestamp", String.valueOf(System.currentTimeMillis()));
            
            jedis.hmset(key, actionData);
            jedis.expire(key, 60); // 60秒过期
            
            plugin.getLogger().info("设置玩家待处理动作: " + playerUUID + " -> " + action);
            
        } catch (Exception ex) {
            plugin.getLogger().warning("设置玩家待处理动作失败: " + ex.getMessage());
        }
    }
    
    /**
     * 获取并清除玩家的待处理动作
     * 
     * @param playerUUID 玩家 UUID
     * @return 动作数据，如果没有则返回 null
     */
    public Map<String, String> getAndClearPlayerPendingAction(String playerUUID) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = keyPrefix + "player:pending:" + playerUUID;
            
            Map<String, String> actionData = jedis.hgetAll(key);
            
            if (actionData != null && !actionData.isEmpty()) {
                // 删除已读取的动作
                jedis.del(key);
                plugin.getLogger().info("获取玩家待处理动作: " + playerUUID + " -> " + actionData.get("action"));
                return actionData;
            }
            
            return null;
            
        } catch (Exception ex) {
            plugin.getLogger().warning("获取玩家待处理动作失败: " + ex.getMessage());
            return null;
        }
    }
    
    /**
     * 同步服务器状态到 Redis（增强版）
     * 
     * @param serverName 服务器名称
     * @param status 服务器状态
     * @param playerCount 玩家数量
     */
    public void syncServerStatus(String serverName, String status, int playerCount) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = keyPrefix + "servers:" + serverName;
            
            Map<String, String> serverData = new HashMap<>();
            serverData.put("name", serverName);
            serverData.put("status", status);
            serverData.put("players", String.valueOf(playerCount));
            serverData.put("maxPlayers", String.valueOf(plugin.getManhuntConfig().getMaxPlayers()));
            serverData.put("type", plugin.getManhuntConfig().getServerType().name());
            serverData.put("timestamp", String.valueOf(System.currentTimeMillis()));
            
            jedis.hmset(key, serverData);
            jedis.expire(key, 30); // 30秒过期
            
            // 发布状态变化事件
            publishStatusChange(serverName, status);
            
        } catch (Exception ex) {
            plugin.getLogger().warning("同步服务器状态失败: " + ex.getMessage());
        }
    }
    
    /**
     * 查询所有游戏服务器状态
     * 
     * @return 服务器信息列表
     */
    public java.util.List<Map<String, String>> getAllServerStatus() {
        java.util.List<Map<String, String>> serverList = new java.util.ArrayList<>();
        
        try (Jedis jedis = jedisPool.getResource()) {
            String pattern = keyPrefix + "servers:*";
            Set<String> keys = jedis.keys(pattern);
            
            if (keys != null) {
                for (String key : keys) {
                    Map<String, String> serverInfo = jedis.hgetAll(key);
                    if (!serverInfo.isEmpty()) {
                        serverList.add(serverInfo);
                    }
                }
            }
            
        } catch (Exception ex) {
            plugin.getLogger().warning("查询所有服务器状态失败: " + ex.getMessage());
        }
        
        return serverList;
    }
    
    /**
     * 查询可用的游戏服务器
     * 
     * @return 可用服务器列表
     */
    public java.util.List<Map<String, String>> getAvailableServers() {
        java.util.List<Map<String, String>> availableServers = new java.util.ArrayList<>();
        
        for (Map<String, String> serverInfo : getAllServerStatus()) {
            String status = serverInfo.get("status");
            
            // 只选择在线或等待中的服务器
            if ("ONLINE".equals(status) || "WAITING".equals(status)) {
                try {
                    int players = Integer.parseInt(serverInfo.getOrDefault("players", "0"));
                    int maxPlayers = Integer.parseInt(serverInfo.getOrDefault("maxPlayers", "0"));
                    
                    // 检查是否未满
                    if (players < maxPlayers) {
                        availableServers.add(serverInfo);
                    }
                } catch (NumberFormatException ex) {
                    // 忽略解析错误
                }
            }
        }
        
        return availableServers;
    }
    
    /**
     * 发布服务器状态变化事件
     * 
     * @param serverName 服务器名称
     * @param status 新状态
     */
    public void publishStatusChange(String serverName, String status) {
        try (Jedis jedis = jedisPool.getResource()) {
            String channel = keyPrefix + "status:change";
            String message = serverName + ":" + status + ":" + System.currentTimeMillis();
            jedis.publish(channel, message);
            
        } catch (Exception ex) {
            plugin.getLogger().warning("发布状态变化事件失败: " + ex.getMessage());
        }
    }
    
    /**
     * 订阅服务器状态变化（需要在异步线程中调用）
     * 
     * @param handler 事件处理器
     */
    public void subscribeStatusChange(java.util.function.Consumer<String> handler) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Jedis jedis = jedisPool.getResource()) {
                String channel = keyPrefix + "status:change";
                
                jedis.subscribe(new redis.clients.jedis.JedisPubSub() {
                    @Override
                    public void onMessage(String channel, String message) {
                        handler.accept(message);
                    }
                }, channel);
                
            } catch (Exception ex) {
                plugin.getLogger().warning("订阅状态变化失败: " + ex.getMessage());
            }
        });
    }
}
