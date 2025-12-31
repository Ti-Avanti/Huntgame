package com.minecraft.huntergame;

import com.minecraft.huntergame.config.LanguageManager;
import com.minecraft.huntergame.config.MainConfig;
import com.minecraft.huntergame.config.ManhuntConfig;
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
import com.minecraft.huntergame.event.GameEventManager;
import org.bukkit.plugin.java.JavaPlugin;

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
    private MainConfig mainConfig;
    private ManhuntConfig manhuntConfig;
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
    
    // 游戏事件管理器
    private GameEventManager gameEventManager;
    
    // Bungee组件
    private BungeeManager bungeeManager;
    private RedisManager redisManager;
    private LoadBalancer loadBalancer;
    
    // 状态标志
    private boolean fullyLoaded = false;
    private boolean initSuccess = false;
    
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
            
            // 保存所有玩家数据
            if (statsManager != null) {
                statsManager.saveAll();
            }
            
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
            // 检查是否在Bungee网络中
            if (getServer().spigot().getConfig().getBoolean("settings.bungeecord", false)) {
                serverMode = ServerMode.BUNGEE;
            } else {
                serverMode = ServerMode.STANDALONE;
            }
            return true;
        } catch (Exception ex) {
            getLogger().severe("检测服务器模式失败: " + ex.getMessage());
            return false;
        }
    }
    
    /**
     * 初始化配置
     */
    private boolean initConfig() {
        try {
            // 保存默认配置
            saveDefaultConfig();
            
            // 初始化主配置
            mainConfig = new MainConfig(this);
            
            // 初始化Manhunt配置
            manhuntConfig = new ManhuntConfig(this);
            
            // 初始化语言管理器
            languageManager = new LanguageManager(this);
            
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
            
            // 初始化游戏事件管理器
            gameEventManager = new GameEventManager(this);
            gameEventManager.startEventCheckTask();
            
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
            // 初始化Bungee管理器
            bungeeManager = new BungeeManager(this);
            
            // 初始化Redis管理器
            redisManager = new RedisManager(this);
            if (!redisManager.connect()) {
                getLogger().warning("Redis连接失败，Bungee功能可能受限");
            }
            
            // 初始化负载均衡器
            loadBalancer = new LoadBalancer(this, redisManager);
            
            return true;
        } catch (Exception ex) {
            getLogger().severe("Bungee组件初始化失败: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }
    
    /**
     * 注册命令
     */
    private void registerCommands() {
        // 主命令
        getCommand("huntergame").setExecutor(new MainCommand(this));
        
        // 统计命令
        getCommand("hgstats").setExecutor(new StatsCommand(this));
        
        // 队伍命令
        getCommand("party").setExecutor(new PartyCommand(this));
        
        // 管理员命令
        getCommand("hgsetup").setExecutor(new com.minecraft.huntergame.command.SetupCommand(this));
        getCommand("hgreload").setExecutor(new com.minecraft.huntergame.command.ReloadCommand(this));
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
    
    public ManhuntConfig getManhuntConfig() {
        return manhuntConfig;
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
    
    public GameEventManager getGameEventManager() {
        return gameEventManager;
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
}
