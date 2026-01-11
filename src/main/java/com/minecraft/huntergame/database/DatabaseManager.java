package com.minecraft.huntergame.database;

import com.minecraft.huntergame.HunterGame;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 数据库管理器
 * 负责数据库连接和表结构管理
 * 
 * @author YourName
 * @version 1.0.0
 */
public class DatabaseManager {
    
    private final HunterGame plugin;
    private HikariDataSource dataSource;
    private DatabaseType type;
    
    public DatabaseManager(HunterGame plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 连接数据库
     */
    public boolean connect() {
        try {
            connectInternal();
            return true;
        } catch (SQLException ex) {
            plugin.getLogger().severe("数据库连接失败: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }
    
    /**
     * 内部连接方法
     */
    private void connectInternal() throws SQLException {
        String dbType = plugin.getMainConfig().getDatabaseType();
        
        try {
            this.type = DatabaseType.valueOf(dbType.toUpperCase());
        } catch (IllegalArgumentException ex) {
            plugin.getLogger().warning("不支持的数据库类型: " + dbType + ", 使用SQLite");
            this.type = DatabaseType.SQLITE;
        }
        
        switch (type) {
            case SQLITE:
                connectSQLite();
                break;
            case MYSQL:
                connectMySQL();
                break;
            default:
                throw new SQLException("不支持的数据库类型: " + dbType);
        }
        
        plugin.getLogger().info("数据库已连接: " + type);
    }
    
    /**
     * 连接SQLite
     */
    private void connectSQLite() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + plugin.getDataFolder() + "/data.db");
        config.setDriverClassName("org.sqlite.JDBC");
        config.setMaximumPoolSize(1);
        config.setConnectionTestQuery("SELECT 1");
        
        dataSource = new HikariDataSource(config);
    }
    
    /**
     * 连接MySQL
     */
    private void connectMySQL() {
        String host = plugin.getMainConfig().getDatabaseHost();
        int port = plugin.getMainConfig().getDatabasePort();
        String database = plugin.getMainConfig().getDatabaseName();
        String username = plugin.getMainConfig().getDatabaseUsername();
        String password = plugin.getMainConfig().getDatabasePassword();
        
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(String.format("jdbc:mysql://%s:%d/%s?useSSL=false&characterEncoding=utf8&serverTimezone=UTC",
            host, port, database));
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        
        // 连接池配置
        config.setMaximumPoolSize(plugin.getMainConfig().getDatabaseMaxPoolSize());
        config.setMinimumIdle(plugin.getMainConfig().getDatabaseMinIdle());
        config.setConnectionTimeout(plugin.getMainConfig().getDatabaseConnectionTimeout());
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setConnectionTestQuery("SELECT 1");
        
        dataSource = new HikariDataSource(config);
    }
    
    /**
     * 获取数据库连接
     */
    public Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("数据库未连接");
        }
        
        Connection conn = dataSource.getConnection();
        if (conn == null || conn.isClosed()) {
            throw new SQLException("无法获取有效的数据库连接");
        }
        
        return conn;
    }
    
    /**
     * 断开数据库连接
     */
    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            try {
                dataSource.close();
                plugin.getLogger().info("数据库已断开");
            } catch (Exception ex) {
                plugin.getLogger().severe("关闭数据库连接失败: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * 关闭数据库连接（别名）
     */
    public void shutdown() {
        disconnect();
    }
    
    /**
     * 创建表
     */
    public void createTables() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // 创建玩家段位数据表
            String sql = type == DatabaseType.SQLITE ?
                "CREATE TABLE IF NOT EXISTS player_data (" +
                "uuid TEXT PRIMARY KEY," +
                "name TEXT NOT NULL," +
                "score INTEGER DEFAULT 0," +
                "current_rank TEXT DEFAULT 'UNRANKED'," +
                "highest_rank TEXT DEFAULT 'UNRANKED'," +
                "season_id INTEGER DEFAULT 1," +
                "created_at INTEGER," +
                "updated_at INTEGER" +
                ")" :
                "CREATE TABLE IF NOT EXISTS player_data (" +
                "uuid VARCHAR(36) PRIMARY KEY," +
                "name VARCHAR(16) NOT NULL," +
                "score INT DEFAULT 0," +
                "current_rank VARCHAR(32) DEFAULT 'UNRANKED'," +
                "highest_rank VARCHAR(32) DEFAULT 'UNRANKED'," +
                "season_id INT DEFAULT 1," +
                "created_at BIGINT," +
                "updated_at BIGINT," +
                "INDEX idx_name (name)," +
                "INDEX idx_score (score)," +
                "INDEX idx_current_rank (current_rank)," +
                "INDEX idx_season_id (season_id)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
            
            stmt.execute(sql);
            plugin.getLogger().info("数据表已创建");
        }
    }
    
    /**
     * 获取数据库类型
     */
    public DatabaseType getType() {
        return type;
    }
}
