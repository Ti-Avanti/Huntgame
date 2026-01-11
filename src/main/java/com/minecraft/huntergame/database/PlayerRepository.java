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
            "(uuid, name, score, current_rank, highest_rank, season_id, created_at, updated_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)" :
            "INSERT INTO player_data " +
            "(uuid, name, score, current_rank, highest_rank, season_id, created_at, updated_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
            "ON DUPLICATE KEY UPDATE " +
            "name=?, score=?, current_rank=?, highest_rank=?, season_id=?, updated_at=?";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, data.getUuid().toString());
            stmt.setString(2, data.getName());
            stmt.setInt(3, data.getScore());
            stmt.setString(4, data.getCurrentRank().name());
            stmt.setString(5, data.getHighestRank().name());
            stmt.setInt(6, data.getSeasonId());
            stmt.setLong(7, data.getCreatedAt());
            stmt.setLong(8, data.getUpdatedAt());
            
            if (databaseManager.getType() == DatabaseType.MYSQL) {
                stmt.setString(9, data.getName());
                stmt.setInt(10, data.getScore());
                stmt.setString(11, data.getCurrentRank().name());
                stmt.setString(12, data.getHighestRank().name());
                stmt.setInt(13, data.getSeasonId());
                stmt.setLong(14, data.getUpdatedAt());
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
     * 获取段位排行榜（按分数排序）
     */
    public List<PlayerData> getTopRanks(int limit) throws SQLException {
        String sql = "SELECT * FROM player_data ORDER BY score DESC LIMIT ?";
        
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
     * 获取当前赛季段位排行榜
     */
    public List<PlayerData> getTopRanksBySeason(int seasonId, int limit) throws SQLException {
        String sql = "SELECT * FROM player_data WHERE season_id = ? ORDER BY score DESC LIMIT ?";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, seasonId);
            stmt.setInt(2, limit);
            
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
     * 获取玩家在排行榜中的排名
     */
    public int getPlayerRanking(UUID uuid) throws SQLException {
        String sql = "SELECT COUNT(*) + 1 as rank FROM player_data WHERE score > (SELECT score FROM player_data WHERE uuid = ?)";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, uuid.toString());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("rank");
                }
            }
        }
        
        return 0;
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
        data.setScore(rs.getInt("score"));
        
        // 解析段位枚举
        try {
            String currentRankStr = rs.getString("current_rank");
            data.setCurrentRank(com.minecraft.huntergame.rank.Rank.valueOf(currentRankStr));
        } catch (Exception e) {
            data.setCurrentRank(com.minecraft.huntergame.rank.Rank.UNRANKED);
        }
        
        try {
            String highestRankStr = rs.getString("highest_rank");
            data.setHighestRank(com.minecraft.huntergame.rank.Rank.valueOf(highestRankStr));
        } catch (Exception e) {
            data.setHighestRank(com.minecraft.huntergame.rank.Rank.UNRANKED);
        }
        
        data.setSeasonId(rs.getInt("season_id"));
        data.setCreatedAt(rs.getLong("created_at"));
        data.setUpdatedAt(rs.getLong("updated_at"));
        
        return data;
    }
}
