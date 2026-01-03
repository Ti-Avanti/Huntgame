package com.minecraft.huntergame;

import com.minecraft.huntergame.config.LanguageManager;
import com.minecraft.huntergame.config.MainConfig;
import com.minecraft.huntergame.config.ManhuntConfig;
import com.minecraft.huntergame.config.ConfigManager;
import com.minecraft.huntergame.config.ScoreboardConfig;
import com.minecraft.huntergame.config.MessagesConfig;
import com.minecraft.huntergame.config.RewardsConfig;
import com.minecraft.huntergame.config.BungeeConfigValidator;
import com.minecraft.huntergame.database.DatabaseManager;
import com.minecraft.huntergame.database.PlayerRepository;
import com.minecraft.huntergame.integration.IntegrationManager;
import com.minecraft.huntergame.manager.StatsManager;
import com.minecraft.huntergame.manager.ManhuntManager;
import com.minecraft.huntergame.manager.RoleManager;
import com.minecraft.huntergame.manager.WorldManager;
import com.minecraft.huntergame.tracker.TrackerManager;
import com.minecraft.huntergame.sidebar.SidebarManager;
import com.minecraft.huntergame.party.PartyManager;
import com.minecraft.huntergame.bungee.BungeeManager;
import com.minecraft.huntergame.bungee.RedisManager;
import com.minecraft.huntergame.bungee.LoadBalancer;
import com.minecraft.huntergame.command.MainCommand;
import com.minecraft.huntergame.command.StatsCommand;
import com.minecraft.huntergame.command.PartyCommand;
import com.minecraft.huntergame.listener.PartyChatListener;
import com.minecraft.huntergame.listener.CompassListener;
import com.minecraft.huntergame.listener.ManhuntListener;
import com.minecraft.huntergame.listener.PlayerJoinLeaveListener;
import com.minecraft.huntergame.listener.SpectatorListener;
import com.minecraft.huntergame.listener.AntiCheatListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.Set;

/**
 * 猎人游戏主类 - Manhunt模式
 * 
 * @author YourName
 * @version 2.0.0
 */
public class HunterGame extends JavaPlugin {
    
    // 单例实例
    private static HunterGame instance;
    
    // 运行模式
    private ServerMode serverMode;
    
    // 配置管理器
    private ConfigManager configManager;
    private MainConfig mainConfig;
    private ManhuntConfig manhuntConfig;
    private ScoreboardConfig scoreboardConfig;
    private MessagesConfig messagesConfig;
    private RewardsConfig rewardsConfig;
    private LanguageManager languageManager;
    
    // 数据库管理器
    private DatabaseManager databaseManager;
    private PlayerRepository playerRepository;
    private StatsManager statsManager;
    
    // 集成管理器
    private IntegrationManager integrationManager;
    
    // 队伍管理器
    private PartyManager partyManager;
    
    // Manhunt管理器
    private ManhuntManager manhuntManager;
    
    // 追踪管理器
    private TrackerManager trackerManager;
    
    // 角色管理器
    private RoleManager roleManager;
    
    // 侧边栏管理器
    private SidebarManager sidebarManager;
    
    // 世界管理器
    private WorldManager worldManager;
    
    // Hotbar管理器
    private com.minecraft.huntergame.hotbar.HotbarManager hotbarManager;
    
    // Bungee组件
    private BungeeManager bungeeManager;
    private RedisManager redisManager;
    private LoadBalancer loadBalancer;
    
    // 状态标志
    private boolean fullyLoaded = false;
    private boolean initSuccess = false;
    private boolean debugMode = false;
    
    @Override
    public void onLoad() {
        instance = this;
        getLogger().info("猎人游戏插件正在加载...");
    }
    
    @Override
    public void onEnable() {
        try {
            instance = this;
            
            // 1. 检测服务器模式
            if (!detectServerMode()) {
                disablePlugin("服务器模式检测失败");
                return;
            }
            getLogger().info("服务器模式: " + serverMode);
            
            // 2. 加载配置
            if (!initConfig()) {
                disablePlugin("配置初始化失败");
                return;
            }
            
            // 读取debug模式配置
            debugMode = getConfig().getBoolean("debug", false);
            if (debugMode) {
                getLogger().info("§e[DEBUG] Debug模式已启用");
            }
            
            getLogger().info("配置系统已初始化");
            
            // 3. 初始化数据库
            if (!initDatabase()) {
                disablePlugin("数据库初始化失败");
                return;
            }
            getLogger().info("数据库系统已初始化");
            
            // 4. 初始化管理器
            if (!initManagers()) {
                disablePlugin("管理器初始化失败");
                return;
            }
            getLogger().info("管理器系统已初始化");
            
            // 5. 初始化Bungee组件（如果是Bungee模式）
            if (serverMode == ServerMode.BUNGEE) {
                // 验证 Bungee 配置
                BungeeConfigValidator validator = new BungeeConfigValidator(this);
                if (!validator.validate()) {
                    disablePlugin("Bungee 配置验证失败");
                    return;
                }
                
                if (!initBungee()) {
                    disablePlugin("Bungee组件初始化失败");
                    return;
                }
                getLogger().info("Bungee组件已初始化");
            }
            
            // 6. 注册命令
            registerCommands();
            getLogger().info("命令系统已注册");
            
            // 7. 注册监听器
            registerListeners();
            getLogger().info("监听器已注册");
            
            // 8. 初始化集成
            initIntegrations();
            getLogger().info("第三方集成已初始化");
            
            // 标记为完全加载
            fullyLoaded = true;
            initSuccess = true;
            
            getLogger().info("猎人游戏插件已成功启用！");
            getLogger().info("版本: " + getDescription().getVersion());
            getLogger().info("模式: " + serverMode);
            
        } catch (Exception ex) {
            getLogger().severe("插件启用失败: " + ex.getMessage());
            ex.printStackTrace();
            disablePlugin("插件启用过程中发生异常");
        }
    }
    
    @Override
    public void onDisable() {
        try {
            getLogger().info("猎人游戏插件正在关闭...");
            
            // 关闭Manhunt管理器
            if (manhuntManager != null) {
                manhuntManager.shutdown();
            }
            
            // 关闭追踪管理器
            if (trackerManager != null) {
                trackerManager.shutdown();
            }
            
            // 关闭侧边栏管理器
            if (sidebarManager != null) {
                sidebarManager.shutdown();
            }
            
            // 保存所有玩家数据(同步保存,避免异步任务问题)
            if (statsManager != null) {
                statsManager.saveAllSync();
            }
            
            // 关闭数据库连接
            if (databaseManager != null) {
                databaseManager.shutdown();
            }
            
            // 注销集成
            if (integrationManager != null) {
                integrationManager.unregisterAll();
            }
            
            // 关闭Bungee组件
            if (redisManager != null) {
                redisManager.shutdown();
            }
            
            getLogger().info("猎人游戏插件已关闭");
            
        } catch (Exception ex) {
            getLogger().severe("插件关闭失败: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    /**
     * 检测服务器模式
     */
    private boolean detectServerMode() {
        try {
            // 先加载配置以读取mode设置
            saveDefaultConfig();
            reloadConfig();
            
            // 从config.yml读取模式配置
            String modeStr = getConfig().getString("mode", "STANDALONE");
            
            try {
                serverMode = ServerMode.valueOf(modeStr.toUpperCase());
            } catch (IllegalArgumentException ex) {
                getLogger().warning("无效的服务器模式: " + modeStr + ", 使用默认值: STANDALONE");
                serverMode = ServerMode.STANDALONE;
            }
            
            // 如果配置为BUNGEE模式，额外检查是否真的在Bungee网络中
            if (serverMode == ServerMode.BUNGEE) {
                boolean isBungeeCord = getServer().spigot().getConfig().getBoolean("settings.bungeecord", false);
                if (!isBungeeCord) {
                    getLogger().warning("配置为BUNGEE模式，但spigot.yml中未启用bungeecord！");
                    getLogger().warning("请在spigot.yml中设置 settings.bungeecord: true");
                }
            }
            
            return true;
        } catch (Exception ex) {
            getLogger().severe("检测服务器模式失败: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }
    
    /**
     * 初始化配置
     */
    private boolean initConfig() {
        try {
            // 初始化配置管理器
            configManager = new ConfigManager(this);
            configManager.loadAll();
            
            // 初始化主配置
            mainConfig = new MainConfig(this);
            
            // 初始化Manhunt配置
            manhuntConfig = new ManhuntConfig(this);
            
            // 初始化计分板配置
            scoreboardConfig = new ScoreboardConfig(this);
            
            // 初始化消息配置
            messagesConfig = new MessagesConfig(this);
            
            // 初始化奖励配置
            rewardsConfig = new RewardsConfig(this);
            
            // 初始化语言管理器
            languageManager = new LanguageManager(this);
            
            // 所有配置类初始化完成后，进行配置验证
            configManager.validateConfigs();
            
            return true;
        } catch (Exception ex) {
            getLogger().severe("配置初始化失败: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }
    
    /**
     * 初始化数据库
     */
    private boolean initDatabase() {
        try {
            // 初始化数据库管理器
            databaseManager = new DatabaseManager(this);
            if (!databaseManager.connect()) {
                return false;
            }
            
            // 创建数据表
            databaseManager.createTables();
            
            // 初始化玩家数据仓库
            playerRepository = new PlayerRepository(this, databaseManager);
            
            // 初始化统计管理器
            statsManager = new StatsManager(this, playerRepository);
            statsManager.startCacheUpdateTask();
            
            return true;
        } catch (Exception ex) {
            getLogger().severe("数据库初始化失败: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }
    
    /**
     * 初始化管理器
     */
    private boolean initManagers() {
        try {
            // 初始化队伍管理器
            partyManager = new PartyManager(this);
            
            // 初始化Manhunt管理器
            manhuntManager = new ManhuntManager(this);
            manhuntManager.startGameCheckTask();
            
            // 初始化追踪管理器
            trackerManager = new TrackerManager(this);
            
            // 初始化角色管理器
            roleManager = new RoleManager(this);
            
            // 初始化侧边栏管理器
            sidebarManager = new SidebarManager(this);
            
            // 初始化世界管理器
            worldManager = new WorldManager(this);
            
            // 初始化Hotbar管理器
            hotbarManager = new com.minecraft.huntergame.hotbar.HotbarManager(this);
            
            return true;
        } catch (Exception ex) {
            getLogger().severe("管理器初始化失败: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }
    
    /**
     * 初始化Bungee组件
     */
    private boolean initBungee() {
        try {
            // 获取服务器类型
            com.minecraft.huntergame.config.ServerType serverType = manhuntConfig.getServerType();
            debug("初始化 Bungee 组件，服务器类型: " + serverType);
            
            // 初始化Bungee管理器
            bungeeManager = new BungeeManager(this);
            debug("BungeeManager 已初始化");
            
            // 初始化Redis管理器
            redisManager = new RedisManager(this);
            if (!redisManager.connect()) {
                getLogger().warning("Redis连接失败，Bungee功能可能受限");
            } else {
                debug("Redis 连接成功");
                
                // 只有子大厅服务器才注册到 Redis
                if (serverType == com.minecraft.huntergame.config.ServerType.SUB_LOBBY) {
                    redisManager.registerServer();
                    debug("子大厅服务器已注册到 Redis: " + redisManager.getServerName());
                } else {
                    debug("主大厅服务器不注册到 Redis（仅查询子服务器）");
                }
                
                // 启动定时同步任务
                startRedisSync();
            }
            
            // 初始化负载均衡器
            loadBalancer = new LoadBalancer(this, redisManager);
            debug("LoadBalancer 已初始化");
            
            // 设置负载均衡器到BungeeManager
            bungeeManager.setLoadBalancer(loadBalancer);
            debug("LoadBalancer 已设置到 BungeeManager");
            
            return true;
        } catch (Exception ex) {
            getLogger().severe("Bungee组件初始化失败: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }
    
    /**
     * 启动Redis状态同步任务
     */
    private void startRedisSync() {
        if (redisManager == null || !redisManager.isConnected()) {
            debug("Redis 未连接，跳过同步任务启动");
            return;
        }
        
        // 获取服务器类型
        com.minecraft.huntergame.config.ServerType serverType = manhuntConfig.getServerType();
        
        // 获取同步间隔（从配置读取，默认10秒）
        int interval = mainConfig.getRedisUpdateInterval();
        
        // 启动定时同步任务
        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            try {
                if (redisManager.isConnected()) {
                    // 只有子大厅服务器才同步自己的状态
                    if (serverType == com.minecraft.huntergame.config.ServerType.SUB_LOBBY) {
                        // 同步服务器状态
                        int playerCount = getServer().getOnlinePlayers().size();
                        String status = determineServerStatus();
                        
                        redisManager.syncServerStatus(redisManager.getServerName(), status, playerCount);
                        debug("同步服务器状态: " + status + ", 玩家数: " + playerCount);
                        
                        // 同步游戏状态
                        if (manhuntManager != null) {
                            for (com.minecraft.huntergame.game.ManhuntGame game : manhuntManager.getAllGames()) {
                                redisManager.syncManhuntGameState(
                                    game.getGameId(),
                                    game.getState().name(),
                                    game.getPlayerCount()
                                );
                            }
                        }
                    } else {
                        // 主大厅服务器：定期检查可用的子服务器
                        Set<String> servers = redisManager.getOnlineServers();
                        debug("可用子服务器数量: " + servers.size());
                        if (debugMode && !servers.isEmpty()) {
                            for (String serverKey : servers) {
                                String serverName = serverKey.replace("huntergame:servers:", "");
                                Map<String, String> info = redisManager.getServerInfo(serverName);
                                debug("  - " + serverName + ": " + info.get("status") + 
                                      ", 玩家: " + info.get("players") + "/" + info.get("maxPlayers"));
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                getLogger().warning("Redis状态同步失败: " + ex.getMessage());
            }
        }, 20L, interval * 20L); // 1秒后开始，每interval秒执行一次
        
        getLogger().info("Redis状态同步任务已启动 (间隔: " + interval + "秒, 服务器类型: " + serverType + ")");
    }
    
    /**
     * 确定服务器状态
     */
    private String determineServerStatus() {
        if (manhuntManager == null) {
            return "ONLINE";
        }
        return manhuntManager.determineServerStatus();
    }
    
    /**
     * 注册命令
     */
    private void registerCommands() {
        // 主命令
        MainCommand mainCommand = new MainCommand(this);
        getCommand("huntergame").setExecutor(mainCommand);
        getCommand("huntergame").setTabCompleter(mainCommand);
        
        // 统计命令
        StatsCommand statsCommand = new StatsCommand(this);
        getCommand("hgstats").setExecutor(statsCommand);
        getCommand("hgstats").setTabCompleter(statsCommand);
        
        // 队伍命令
        PartyCommand partyCommand = new PartyCommand(this);
        getCommand("party").setExecutor(partyCommand);
        getCommand("party").setTabCompleter(partyCommand);
        
        // 管理员命令
        com.minecraft.huntergame.command.SetupCommand setupCommand = new com.minecraft.huntergame.command.SetupCommand(this);
        getCommand("hgsetup").setExecutor(setupCommand);
        getCommand("hgsetup").setTabCompleter(setupCommand);
    }
    
    /**
     * 注册监听器
     */
    private void registerListeners() {
        // 队伍聊天监听器
        getServer().getPluginManager().registerEvents(new PartyChatListener(this), this);
        
        // 指南针监听器
        getServer().getPluginManager().registerEvents(new CompassListener(this), this);
        
        // Manhunt核心监听器
        getServer().getPluginManager().registerEvents(new ManhuntListener(this), this);
        
        // 玩家加入/离开监听器
        getServer().getPluginManager().registerEvents(new PlayerJoinLeaveListener(this), this);
        
        // 观战者监听器
        getServer().getPluginManager().registerEvents(new SpectatorListener(this), this);
        
        // 防作弊监听器
        getServer().getPluginManager().registerEvents(new AntiCheatListener(this), this);
        
        // Hotbar监听器
        getServer().getPluginManager().registerEvents(new com.minecraft.huntergame.listener.HotbarListener(this), this);
        
        // 传送门监听器（原生模式和Multiverse模式都需要）
        getServer().getPluginManager().registerEvents(new com.minecraft.huntergame.listener.PortalListener(this), this);
        getLogger().info("传送门监听器已注册");
    }
    
    /**
     * 初始化集成
     */
    private void initIntegrations() {
        try {
            // 初始化集成管理器
            integrationManager = new IntegrationManager(this);
            integrationManager.registerAll();
        } catch (Exception ex) {
            getLogger().warning("集成初始化失败: " + ex.getMessage());
        }
    }
    
    /**
     * 禁用插件
     */
    private void disablePlugin(String reason) {
        getLogger().severe("插件禁用: " + reason);
        getServer().getPluginManager().disablePlugin(this);
    }
    
    // ==================== Getter方法 ====================
    
    public static HunterGame getInstance() {
        return instance;
    }
    
    public ServerMode getServerMode() {
        return serverMode;
    }
    
    public MainConfig getMainConfig() {
        return mainConfig;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public ManhuntConfig getManhuntConfig() {
        return manhuntConfig;
    }
    
    public ScoreboardConfig getScoreboardConfig() {
        return scoreboardConfig;
    }
    
    public MessagesConfig getMessagesConfig() {
        return messagesConfig;
    }
    
    public RewardsConfig getRewardsConfig() {
        return rewardsConfig;
    }
    
    public LanguageManager getLanguageManager() {
        return languageManager;
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public PlayerRepository getPlayerRepository() {
        return playerRepository;
    }
    
    public StatsManager getStatsManager() {
        return statsManager;
    }
    
    public IntegrationManager getIntegrationManager() {
        return integrationManager;
    }
    
    public PartyManager getPartyManager() {
        return partyManager;
    }
    
    public ManhuntManager getManhuntManager() {
        return manhuntManager;
    }
    
    public TrackerManager getTrackerManager() {
        return trackerManager;
    }
    
    public RoleManager getRoleManager() {
        return roleManager;
    }
    
    public SidebarManager getSidebarManager() {
        return sidebarManager;
    }
    
    public WorldManager getWorldManager() {
        return worldManager;
    }
    
    public com.minecraft.huntergame.hotbar.HotbarManager getHotbarManager() {
        return hotbarManager;
    }
    
    public BungeeManager getBungeeManager() {
        return bungeeManager;
    }
    
    public RedisManager getRedisManager() {
        return redisManager;
    }
    
    public LoadBalancer getLoadBalancer() {
        return loadBalancer;
    }
    
    public boolean isFullyLoaded() {
        return fullyLoaded;
    }
    
    public boolean isInitSuccess() {
        return initSuccess;
    }
    
    public boolean isDebugMode() {
        return debugMode;
    }
    
    /**
     * 输出debug日志
     */
    public void debug(String message) {
        if (debugMode) {
            getLogger().info("§e[DEBUG] " + message);
        }
    }
}
