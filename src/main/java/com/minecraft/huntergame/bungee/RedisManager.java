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
            // 注销服务器
            unregisterServer();
            
            jedisPool.close();
            plugin.getLogger().info("Redis连接已关闭");
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
            serverData.put("maxPlayers", String.valueOf(plugin.getMainConfig().getMaxPlayers()));
            serverData.put("timestamp", String.valueOf(System.currentTimeMillis()));
            
            jedis.hmset(key, serverData);
            jedis.expire(key, 30); // 30秒过期
            
            plugin.getLogger().info("服务器已注册到Redis: " + serverName);
            
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
            return jedis.keys(pattern);
            
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
            return jedis.keys(pattern);
            
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
}
