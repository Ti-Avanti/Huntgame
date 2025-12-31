package com.minecraft.huntergame.database;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.models.PlayerData;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * 玩家数据仓库
 * 负责玩家数据的CRUD操作
 * 
 * @author YourName
 * @version 1.0.0
 */
public class PlayerRepository {
    
    private final HunterGame plugin;
    private final DatabaseManager databaseManager;
    
    public PlayerRepository(HunterGame plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }
    
    /**
     * 保存玩家数据
     */
    public void save(PlayerData data) throws SQLException {
        String sql = databaseManager.getType() == DatabaseType.SQLITE ?
            "INSERT OR REPLACE INTO player_data " +
            "(uuid, name, games_played, games_won, games_lost, hunter_kills, hunter_deaths, " +
            "survivor_escapes, survivor_deaths, total_survival_time, created_at, updated_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)" :
            "INSERT INTO player_data " +
            "(uuid, name, games_played, games_won, games_lost, hunter_kills, hunter_deaths, " +
            "survivor_escapes, survivor_deaths, total_survival_time, created_at, updated_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
            "ON DUPLICATE KEY UPDATE " +
            "name=?, games_played=?, games_won=?, games_lost=?, hunter_kills=?, hunter_deaths=?, " +
            "survivor_escapes=?, survivor_deaths=?, total_survival_time=?, updated_at=?";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, data.getUuid().toString());
            stmt.setString(2, data.getName());
            stmt.setInt(3, data.getGamesPlayed());
            stmt.setInt(4, data.getGamesWon());
            stmt.setInt(5, data.getGamesLost());
            stmt.setInt(6, data.getHunterKills());
            stmt.setInt(7, data.getHunterDeaths());
            stmt.setInt(8, data.getSurvivorEscapes());
            stmt.setInt(9, data.getSurvivorDeaths());
            stmt.setInt(10, data.getTotalSurvivalTime());
            stmt.setLong(11, data.getCreatedAt());
            stmt.setLong(12, data.getUpdatedAt());
            
            if (databaseManager.getType() == DatabaseType.MYSQL) {
                stmt.setString(13, data.getName());
                stmt.setInt(14, data.getGamesPlayed());
                stmt.setInt(15, data.getGamesWon());
                stmt.setInt(16, data.getGamesLost());
                stmt.setInt(17, data.getHunterKills());
                stmt.setInt(18, data.getHunterDeaths());
                stmt.setInt(19, data.getSurvivorEscapes());
                stmt.setInt(20, data.getSurvivorDeaths());
                stmt.setInt(21, data.getTotalSurvivalTime());
                stmt.setLong(22, data.getUpdatedAt());
            }
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * 加载玩家数据
     */
    public PlayerData load(UUID uuid) throws SQLException {
        String sql = "SELECT * FROM player_data WHERE uuid = ?";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, uuid.toString());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return parsePlayerData(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * 删除玩家数据
     */
    public void delete(UUID uuid) throws SQLException {
        String sql = "DELETE FROM player_data WHERE uuid = ?";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, uuid.toString());
            stmt.executeUpdate();
        }
    }
    
    /**
     * 检查玩家数据是否存在
     */
    public boolean exists(UUID uuid) throws SQLException {
        String sql = "SELECT 1 FROM player_data WHERE uuid = ? LIMIT 1";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, uuid.toString());
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
    
    /**
     * 获取胜利排行榜
     */
    public List<PlayerData> getTopWins(int limit) throws SQLException {
        String sql = "SELECT * FROM player_data ORDER BY games_won DESC LIMIT ?";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                List<PlayerData> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(parsePlayerData(rs));
                }
                return list;
            }
        }
    }
    
    /**
     * 获取击杀排行榜
     */
    public List<PlayerData> getTopKills(int limit) throws SQLException {
        String sql = "SELECT * FROM player_data ORDER BY hunter_kills DESC LIMIT ?";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                List<PlayerData> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(parsePlayerData(rs));
                }
                return list;
            }
        }
    }
    
    /**
     * 获取逃脱排行榜
     */
    public List<PlayerData> getTopEscapes(int limit) throws SQLException {
        String sql = "SELECT * FROM player_data ORDER BY survivor_escapes DESC LIMIT ?";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                List<PlayerData> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(parsePlayerData(rs));
                }
                return list;
            }
        }
    }
    
    /**
     * 异步保存数据
     */
    public void saveAsync(PlayerData data, Consumer<Boolean> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                save(data);
                if (callback != null) {
                    Bukkit.getScheduler().runTask(plugin, () -> callback.accept(true));
                }
            } catch (SQLException ex) {
                plugin.getLogger().severe("保存玩家数据失败: " + ex.getMessage());
                ex.printStackTrace();
                if (callback != null) {
                    Bukkit.getScheduler().runTask(plugin, () -> callback.accept(false));
                }
            }
        });
    }
    
    /**
     * 异步加载数据
     */
    public void loadAsync(UUID uuid, Consumer<PlayerData> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                PlayerData data = load(uuid);
                if (callback != null) {
                    Bukkit.getScheduler().runTask(plugin, () -> callback.accept(data));
                }
            } catch (SQLException ex) {
                plugin.getLogger().severe("加载玩家数据失败: " + ex.getMessage());
                ex.printStackTrace();
                if (callback != null) {
                    Bukkit.getScheduler().runTask(plugin, () -> callback.accept(null));
                }
            }
        });
    }
    
    /**
     * 从ResultSet解析PlayerData
     */
    private PlayerData parsePlayerData(ResultSet rs) throws SQLException {
        UUID uuid = UUID.fromString(rs.getString("uuid"));
        String name = rs.getString("name");
        
        PlayerData data = new PlayerData(uuid, name);
        data.setGamesPlayed(rs.getInt("games_played"));
        data.setGamesWon(rs.getInt("games_won"));
        data.setGamesLost(rs.getInt("games_lost"));
        data.setHunterKills(rs.getInt("hunter_kills"));
        data.setHunterDeaths(rs.getInt("hunter_deaths"));
        data.setSurvivorEscapes(rs.getInt("survivor_escapes"));
        data.setSurvivorDeaths(rs.getInt("survivor_deaths"));
        data.setTotalSurvivalTime(rs.getInt("total_survival_time"));
        data.setCreatedAt(rs.getLong("created_at"));
        data.setUpdatedAt(rs.getLong("updated_at"));
        
        return data;
    }
}
