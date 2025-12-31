package com.minecraft.huntergame.util;

/**
 * 常量类
 * 统一管理插件中使用的常量
 * 
 * @author YourName
 * @version 1.0.0
 */
public class Constants {
    
    // ==================== 时间常量 ====================
    
    /** 一秒的tick数 */
    public static final long TICKS_PER_SECOND = 20L;
    
    /** 一分钟的tick数 */
    public static final long TICKS_PER_MINUTE = 1200L;
    
    /** 一分钟的秒数 */
    public static final int SECONDS_PER_MINUTE = 60;
    
    /** 一小时的秒数 */
    public static final int SECONDS_PER_HOUR = 3600;
    
    // ==================== 缓存常量 ====================
    
    /** 排行榜缓存更新间隔（秒） */
    public static final int LEADERBOARD_CACHE_UPDATE_INTERVAL = 300;
    
    /** Redis键过期时间（秒） */
    public static final int REDIS_KEY_EXPIRE_TIME = 30;
    
    /** Redis游戏状态过期时间（秒） */
    public static final int REDIS_GAME_STATE_EXPIRE_TIME = 300;
    
    // ==================== 性能常量 ====================
    
    /** 并行处理的最小数量阈值 */
    public static final int PARALLEL_PROCESSING_THRESHOLD = 10;
    
    /** 侧边栏并行更新阈值 */
    public static final int SIDEBAR_PARALLEL_THRESHOLD = 20;
    
    // ==================== 游戏常量 ====================
    
    /** 最小玩家数 */
    public static final int MIN_PLAYERS = 2;
    
    /** 默认最大玩家数 */
    public static final int DEFAULT_MAX_PLAYERS = 10;
    
    /** 默认准备时间（秒） */
    public static final int DEFAULT_PREPARE_TIME = 30;
    
    /** 默认最大游戏时长（秒） */
    public static final int DEFAULT_MAX_GAME_TIME = 3600;
    
    /** 默认复活次数 */
    public static final int DEFAULT_RESPAWN_LIMIT = 3;
    
    /** 默认复活延迟（秒） */
    public static final int DEFAULT_RESPAWN_DELAY = 5;
    
    // ==================== 追踪器常量 ====================
    
    /** 默认指南针冷却时间（秒） */
    public static final int DEFAULT_COMPASS_COOLDOWN = 5;
    
    /** 默认自动更新间隔（秒） */
    public static final int DEFAULT_AUTO_UPDATE_INTERVAL = 10;
    
    /** 跨维度距离（表示无法直接到达） */
    public static final double CROSS_DIMENSION_DISTANCE = 999999.0;
    
    // ==================== 数据库常量 ====================
    
    /** 默认数据库连接池最大连接数 */
    public static final int DEFAULT_DB_MAX_POOL_SIZE = 10;
    
    /** 默认数据库连接池最小空闲连接数 */
    public static final int DEFAULT_DB_MIN_IDLE = 2;
    
    /** 默认数据库连接超时时间（毫秒） */
    public static final int DEFAULT_DB_CONNECTION_TIMEOUT = 30000;
    
    // ==================== Redis常量 ====================
    
    /** 默认Redis端口 */
    public static final int DEFAULT_REDIS_PORT = 6379;
    
    /** 默认Redis超时时间（毫秒） */
    public static final int DEFAULT_REDIS_TIMEOUT = 2000;
    
    /** 默认Redis连接池最大连接数 */
    public static final int DEFAULT_REDIS_MAX_TOTAL = 8;
    
    /** 默认Redis连接池最大空闲连接数 */
    public static final int DEFAULT_REDIS_MAX_IDLE = 8;
    
    /** 默认Redis连接池最小空闲连接数 */
    public static final int DEFAULT_REDIS_MIN_IDLE = 0;
    
    // ==================== 颜色常量 ====================
    
    /** 逃亡者颜色 */
    public static final String COLOR_RUNNER = "§a";
    
    /** 猎人颜色 */
    public static final String COLOR_HUNTER = "§c";
    
    /** 观战者颜色 */
    public static final String COLOR_SPECTATOR = "§7";
    
    /** 成功颜色 */
    public static final String COLOR_SUCCESS = "§a";
    
    /** 错误颜色 */
    public static final String COLOR_ERROR = "§c";
    
    /** 警告颜色 */
    public static final String COLOR_WARNING = "§e";
    
    /** 信息颜色 */
    public static final String COLOR_INFO = "§b";
    
    // ==================== 消息常量 ====================
    
    /** 分隔线 */
    public static final String SEPARATOR = "§6========================================";
    
    /** 短分隔线 */
    public static final String SHORT_SEPARATOR = "§6====================";
    
    // 私有构造函数，防止实例化
    private Constants() {
        throw new AssertionError("Constants class cannot be instantiated");
    }
}
