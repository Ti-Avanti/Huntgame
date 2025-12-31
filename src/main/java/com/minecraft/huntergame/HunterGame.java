package com.minecraft.huntergame;

import com.minecraft.huntergame.ability.AbilityManager;
import com.minecraft.huntergame.arena.ArenaManager;
import com.minecraft.huntergame.config.ArenaConfig;
import com.minecraft.huntergame.config.LanguageManager;
import com.minecraft.huntergame.config.MainConfig;
import com.minecraft.huntergame.database.DatabaseManager;
import com.minecraft.huntergame.database.PlayerRepository;
import com.minecraft.huntergame.integration.VaultIntegration;
import com.minecraft.huntergame.manager.ItemManager;
import com.minecraft.huntergame.manager.StatsManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

/**
 * 猎人游戏主类
 * 
 * @author YourName
 * @version 1.0.0
 */
public class HunterGame extends JavaPlugin {
    
    // 单例实例
    private static HunterGame instance;
    
    // 运行模式
    private ServerMode serverMode;
    
    // 配置管理器
    private MainConfig mainConfig;
    private LanguageManager languageManager;
    
    // 数据库管理器
    private DatabaseManager databaseManager;
    private PlayerRepository playerRepository;
    private StatsManager statsManager;
    
    // 竞技场管理器
    private ArenaManager arenaManager;
    
    // 能力管理器
    private AbilityManager abilityManager;
    
    // 道具管理器
    private ItemManager itemManager;
    
    // Vault集成
    private VaultIntegration vaultIntegration;
    
    // 队伍管理器
    private com.minecraft.huntergame.party.PartyManager partyManager;
    
    // Bungee组件
    private com.minecraft.huntergame.bungee.BungeeManager bungeeManager;
    private com.minecraft.huntergame.bungee.RedisManager redisManager;
    private com.minecraft.huntergame.bungee.LoadBalancer loadBalancer;
    
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
            getLogger().info("竞技场管理器已初始化");
            
            // 5. 注册命令
            registerCommands();
            
            // 6. 注册监听器
            registerListeners();
            
            // 7. 加载竞技场
            arenaManager.loadArenas();
            
            // 8. 启动任务
            // TODO: 在后续任务中实现
            
            // 9. 注册第三方集成
            initIntegrations();
            
            // 10. 初始化Bungee组件（如果启用）
            if (serverMode == ServerMode.BUNGEE) {
                initBungeeComponents();
            }
            
            initSuccess = true;
            fullyLoaded = true;
            
            getLogger().info("猎人游戏插件已成功启用! 版本: " + getDescription().getVersion());
            
        } catch (Exception ex) {
            getLogger().severe("插件启用失败!");
            ex.printStackTrace();
            disablePlugin("启用过程中发生异常");
        }
    }
    
    @Override
    public void onDisable() {
        if (!initSuccess) {
            return;
        }
        
        try {
            // 1. 停止所有游戏
            if (arenaManager != null) {
                for (String arenaName : new ArrayList<>(arenaManager.getArenaNames())) {
                    arenaManager.unloadArena(arenaName);
                }
            }
            
            // 2. 保存数据
            if (statsManager != null) {
                statsManager.saveAll();
            }
            
            // 3. 关闭数据库
            if (databaseManager != null) {
                databaseManager.disconnect();
            }
            
            // 4. 停止任务
            getServer().getScheduler().cancelTasks(this);
            
            // 5. 关闭Bungee组件（如果启用）
            if (serverMode == ServerMode.BUNGEE) {
                shutdownBungeeComponents();
            }
            
            getLogger().info("猎人游戏插件已禁用!");
            
        } catch (Exception ex) {
            getLogger().severe("插件禁用时发生错误!");
            ex.printStackTrace();
        } finally {
            fullyLoaded = false;
            initSuccess = false;
        }
    }
    
    // ==================== 初始化方法 ====================
    
    /**
     * 检测服务器模式
     */
    private boolean detectServerMode() {
        try {
            String mode = getConfig().getString("mode", "MULTIARENA");
            serverMode = ServerMode.valueOf(mode.toUpperCase());
            return true;
        } catch (IllegalArgumentException ex) {
            getLogger().warning("无效的服务器模式，使用默认模式: MULTIARENA");
            serverMode = ServerMode.MULTIARENA;
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
            saveDefaultConfig();
            mainConfig = new MainConfig(this);
            languageManager = new LanguageManager(this);
            ArenaConfig.createExample(this);
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
            databaseManager = new DatabaseManager(this);
            databaseManager.connect();
            databaseManager.createTables();
            playerRepository = new PlayerRepository(this, databaseManager);
            statsManager = new StatsManager(this, playerRepository);
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
            arenaManager = new ArenaManager(this);
            abilityManager = new AbilityManager(this);
            itemManager = new ItemManager(this);
            partyManager = new com.minecraft.huntergame.party.PartyManager(this);
            // EscapeManager 将在每个Arena初始化时创建
            return true;
        } catch (Exception ex) {
            getLogger().severe("管理器初始化失败: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }
    
    /**
     * 初始化第三方集成
     */
    private void initIntegrations() {
        try {
            // 初始化Vault集成
            vaultIntegration = new VaultIntegration(this);
            
            getLogger().info("第三方集成已初始化");
        } catch (Exception ex) {
            getLogger().severe("第三方集成初始化失败: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    /**
     * 初始化Bungee组件
     */
    private void initBungeeComponents() {
        try {
            getLogger().info("正在初始化Bungee组件...");
            
            // 初始化BungeeManager
            bungeeManager = new com.minecraft.huntergame.bungee.BungeeManager(this);
            
            // 初始化RedisManager
            if (mainConfig.isRedisEnabled()) {
                redisManager = new com.minecraft.huntergame.bungee.RedisManager(this);
                
                if (redisManager.connect()) {
                    // 注册服务器到Redis
                    redisManager.registerServer();
                    
                    // 初始化负载均衡器
                    loadBalancer = new com.minecraft.huntergame.bungee.LoadBalancer(this, redisManager);
                    
                    // 启动状态同步任务
                    startStatusSyncTask();
                    
                    getLogger().info("Bungee组件已初始化（包含Redis）");
                } else {
                    getLogger().warning("Redis连接失败，Bungee功能将受限");
                }
            } else {
                getLogger().info("Bungee组件已初始化（不包含Redis）");
            }
            
        } catch (Exception ex) {
            getLogger().severe("Bungee组件初始化失败: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    /**
     * 启动状态同步任务
     */
    private void startStatusSyncTask() {
        int interval = mainConfig.getStatusSyncInterval();
        
        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            if (redisManager != null && redisManager.isConnected()) {
                // 更新服务器状态
                int playerCount = arenaManager.getTotalPlayers();
                redisManager.updateServerStatus(playerCount, "ONLINE");
            }
        }, 20L * interval, 20L * interval);
        
        getLogger().info("状态同步任务已启动（间隔: " + interval + "秒）");
    }
    
    /**
     * 禁用插件
     */
    private void disablePlugin(String reason) {
        getLogger().severe("插件禁用原因: " + reason);
        getServer().getPluginManager().disablePlugin(this);
    }
    
    /**
     * 注册命令
     */
    private void registerCommands() {
        try {
            // 导入命令类
            com.minecraft.huntergame.command.MainCommand mainCommand = 
                new com.minecraft.huntergame.command.MainCommand(this);
            com.minecraft.huntergame.command.JoinCommand joinCommand = 
                new com.minecraft.huntergame.command.JoinCommand(this);
            com.minecraft.huntergame.command.LeaveCommand leaveCommand = 
                new com.minecraft.huntergame.command.LeaveCommand(this);
            com.minecraft.huntergame.command.StatsCommand statsCommand = 
                new com.minecraft.huntergame.command.StatsCommand(this);
            com.minecraft.huntergame.command.AdminCommand adminCommand = 
                new com.minecraft.huntergame.command.AdminCommand(this);
            com.minecraft.huntergame.command.PartyCommand partyCommand = 
                new com.minecraft.huntergame.command.PartyCommand(this);
            
            // 注册主命令
            getCommand("huntergame").setExecutor(mainCommand);
            getCommand("huntergame").setTabCompleter(mainCommand);
            
            // 注册快捷命令
            getCommand("hgjoin").setExecutor(joinCommand);
            getCommand("hgjoin").setTabCompleter(joinCommand);
            getCommand("hgleave").setExecutor(leaveCommand);
            getCommand("hgstats").setExecutor(statsCommand);
            getCommand("hgadmin").setExecutor(adminCommand);
            getCommand("hgadmin").setTabCompleter(adminCommand);
            getCommand("party").setExecutor(partyCommand);
            getCommand("party").setTabCompleter(partyCommand);
            
            getLogger().info("命令系统已注册");
        } catch (Exception ex) {
            getLogger().severe("注册命令失败: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    /**
     * 注册监听器
     */
    private void registerListeners() {
        try {
            // 导入监听器类
            com.minecraft.huntergame.listener.PlayerListener playerListener = 
                new com.minecraft.huntergame.listener.PlayerListener(this);
            com.minecraft.huntergame.listener.CombatListener combatListener = 
                new com.minecraft.huntergame.listener.CombatListener(this);
            com.minecraft.huntergame.listener.ItemListener itemListener = 
                new com.minecraft.huntergame.listener.ItemListener(this);
            com.minecraft.huntergame.listener.SpectatorListener spectatorListener = 
                new com.minecraft.huntergame.listener.SpectatorListener(this);
            com.minecraft.huntergame.listener.PartyChatListener partyChatListener = 
                new com.minecraft.huntergame.listener.PartyChatListener(this);
            
            // 注册监听器
            getServer().getPluginManager().registerEvents(playerListener, this);
            getServer().getPluginManager().registerEvents(combatListener, this);
            getServer().getPluginManager().registerEvents(itemListener, this);
            getServer().getPluginManager().registerEvents(spectatorListener, this);
            getServer().getPluginManager().registerEvents(partyChatListener, this);
            
            getLogger().info("事件监听器已注册");
        } catch (Exception ex) {
            getLogger().severe("注册监听器失败: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    /**
     * 关闭Bungee组件
     */
    private void shutdownBungeeComponents() {
        try {
            if (redisManager != null) {
                redisManager.disconnect();
            }
            
            if (bungeeManager != null) {
                bungeeManager.shutdown();
            }
            
            getLogger().info("Bungee组件已关闭");
        } catch (Exception ex) {
            getLogger().severe("关闭Bungee组件时发生错误: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    // ==================== Getter 方法 ====================
    
    /**
     * 获取插件实例
     * 
     * @return 插件实例
     */
    public static HunterGame getInstance() {
        return instance;
    }
    
    /**
     * 获取服务器模式
     * 
     * @return 服务器模式
     */
    public ServerMode getServerMode() {
        return serverMode;
    }
    
    /**
     * 获取主配置管理器
     * 
     * @return 主配置管理器
     */
    public MainConfig getMainConfig() {
        return mainConfig;
    }
    
    /**
     * 获取语言管理器
     * 
     * @return 语言管理器
     */
    public LanguageManager getLanguageManager() {
        return languageManager;
    }
    
    /**
     * 获取数据库管理器
     * 
     * @return 数据库管理器
     */
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    /**
     * 获取玩家数据仓库
     * 
     * @return 玩家数据仓库
     */
    public PlayerRepository getPlayerRepository() {
        return playerRepository;
    }
    
    /**
     * 获取统计管理器
     * 
     * @return 统计管理器
     */
    public StatsManager getStatsManager() {
        return statsManager;
    }
    
    /**
     * 获取竞技场管理器
     * 
     * @return 竞技场管理器
     */
    public ArenaManager getArenaManager() {
        return arenaManager;
    }
    
    /**
     * 是否完全加载
     * 
     * @return 是否完全加载
     */
    public boolean isFullyLoaded() {
        return fullyLoaded;
    }
    
    /**
     * 获取能力管理器
     * 
     * @return 能力管理器
     */
    public AbilityManager getAbilityManager() {
        return abilityManager;
    }
    
    /**
     * 获取道具管理器
     * 
     * @return 道具管理器
     */
    public ItemManager getItemManager() {
        return itemManager;
    }
    
    /**
     * 获取Vault集成
     * 
     * @return Vault集成
     */
    public VaultIntegration getVaultIntegration() {
        return vaultIntegration;
    }
    
    /**
     * 获取队伍管理器
     * 
     * @return 队伍管理器
     */
    public com.minecraft.huntergame.party.PartyManager getPartyManager() {
        return partyManager;
    }
    
    /**
     * 获取Bungee管理器
     * 
     * @return Bungee管理器
     */
    public com.minecraft.huntergame.bungee.BungeeManager getBungeeManager() {
        return bungeeManager;
    }
    
    /**
     * 获取Redis管理器
     * 
     * @return Redis管理器
     */
    public com.minecraft.huntergame.bungee.RedisManager getRedisManager() {
        return redisManager;
    }
    
    /**
     * 获取负载均衡器
     * 
     * @return 负载均衡器
     */
    public com.minecraft.huntergame.bungee.LoadBalancer getLoadBalancer() {
        return loadBalancer;
    }
}
