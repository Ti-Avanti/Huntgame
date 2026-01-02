package com.minecraft.huntergame.config;

import com.minecraft.huntergame.HunterGame;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Manhunt游戏配置类
 * 负责加载和管理Manhunt模式的专用配置
 * 
 * @author YourName
 * @version 1.0.0
 */
public class ManhuntConfig {
    
    private final HunterGame plugin;
    private FileConfiguration config;
    
    public ManhuntConfig(HunterGame plugin) {
        this.plugin = plugin;
        load();
    }
    
    /**
     * 加载配置
     */
    public void load() {
        config = plugin.getConfigManager().getManhuntConfig();
        validate();
        plugin.getLogger().info("Manhunt配置已加载");
    }
    
    /**
     * 重载配置
     */
    public void reload() {
        // 重新从ConfigManager获取最新的配置引用（这是关键！）
        config = plugin.getConfigManager().getManhuntConfig();
        
        // 重新验证配置
        validate();
        
        plugin.getLogger().info("Manhunt配置已重载");
    }
    
    /**
     * 验证配置项
     */
    private void validate() {
        // 验证玩家数量配置
        int minPlayers = getMinPlayers();
        int maxPlayers = getMaxPlayers();
        int maxRunners = getMaxRunners();
        int maxHunters = getMaxHunters();
        
        if (minPlayers < 2) {
            plugin.getLogger().warning("最小玩家数不能小于2");
        }
        
        if (maxPlayers < minPlayers) {
            plugin.getLogger().warning("最大玩家数不能小于最小玩家数");
        }
        
        if (maxRunners < 1) {
            plugin.getLogger().warning("最大逃亡者数不能小于1");
        }
        
        if (maxHunters < 1) {
            plugin.getLogger().warning("最大猎人数不能小于1");
        }
        
        // 验证时间配置
        if (getPrepareTime() < 0) {
            plugin.getLogger().warning("准备时间不能为负数");
        }
        
        if (getMaxGameTime() < 0) {
            plugin.getLogger().warning("最大游戏时长不能为负数");
        }
    }
    
    // ==================== 玩家配置 ====================
    
    /**
     * 获取最小玩家数
     */
    public int getMinPlayers() {
        return Math.max(2, config.getInt("players.min", 2));
    }
    
    /**
     * 获取最小开始人数（匹配功能）
     */
    public int getMinPlayersToStart() {
        return Math.max(2, config.getInt("matching.min-players-to-start", 2));
    }
    
    /**
     * 获取最大玩家数
     */
    public int getMaxPlayers() {
        return Math.max(getMinPlayers(), config.getInt("players.max", 10));
    }
    
    /**
     * 获取最大逃亡者数
     */
    public int getMaxRunners() {
        return Math.max(1, config.getInt("players.max-runners", 3));
    }
    
    /**
     * 获取最大猎人数
     */
    public int getMaxHunters() {
        return Math.max(1, config.getInt("players.max-hunters", 5));
    }
    
    /**
     * 获取逃亡者比例(0.0-1.0)
     */
    public double getRunnerRatio() {
        double ratio = config.getDouble("players.runner-ratio", 0.4);
        // 限制在0.1-0.9之间
        return Math.max(0.1, Math.min(0.9, ratio));
    }
    
    // ==================== 游戏配置 ====================
    
    /**
     * 是否启用匹配功能
     */
    public boolean isMatchingEnabled() {
        return config.getBoolean("matching.enabled", true);
    }
    
    /**
     * 获取匹配超时时间(秒)
     */
    public int getMatchingTimeout() {
        return Math.max(10, config.getInt("matching.timeout", 60));
    }
    
    /**
     * 匹配超时后是否自动开始
     */
    public boolean isMatchingAutoStart() {
        return config.getBoolean("matching.auto-start", true);
    }
    
    /**
     * 获取准备时间(秒)
     */
    public int getPrepareTime() {
        return Math.max(0, config.getInt("game.prepare-time", 30));
    }
    
    /**
     * 获取最大游戏时长(秒，0=无限制)
     */
    public int getMaxGameTime() {
        return Math.max(0, config.getInt("game.max-time", 3600));
    }
    
    /**
     * 获取逃亡者复活次数限制
     */
    public int getRespawnLimit() {
        return Math.max(0, config.getInt("game.respawn-limit", 3));
    }
    
    /**
     * 获取复活延迟(秒)
     */
    public int getRespawnDelay() {
        return Math.max(0, config.getInt("game.respawn-delay", 5));
    }
    
    /**
     * 是否允许击败凋灵获胜
     */
    public boolean isWitherVictoryAllowed() {
        return config.getBoolean("game.allow-wither-victory", false);
    }
    
    // ==================== 追踪指南针配置 ====================
    
    /**
     * 获取指南针更新冷却时间(秒)
     */
    public int getCompassCooldown() {
        return Math.max(0, config.getInt("tracker.compass-cooldown", 5));
    }
    
    /**
     * 是否启用跨维度追踪
     */
    public boolean isCrossDimensionTracking() {
        return config.getBoolean("tracker.cross-dimension", true);
    }
    
    /**
     * 是否自动更新指南针
     */
    public boolean isAutoUpdateCompass() {
        return config.getBoolean("tracker.auto-update", true);
    }
    
    /**
     * 获取自动更新间隔(秒)
     */
    public int getAutoUpdateInterval() {
        return Math.max(1, config.getInt("tracker.auto-update-interval", 10));
    }
    
    // ==================== 世界管理配置 ====================
    
    /**
     * 是否使用独立游戏世界
     */
    public boolean useCustomWorld() {
        return config.getBoolean("world.use-custom", false);
    }
    
    /**
     * 获取游戏世界名称
     */
    public String getWorldName() {
        return config.getString("world.name", "manhunt_world");
    }
    
    /**
     * 获取世界边界大小(半径)
     */
    public int getWorldBorder() {
        return Math.max(0, config.getInt("world.border", 5000));
    }
    
    /**
     * 获取世界类型
     */
    public String getWorldType() {
        return config.getString("world.type", "DEFAULT");
    }
    
    /**
     * 游戏结束后是否重置世界
     */
    public boolean isResetWorldOnEnd() {
        return config.getBoolean("world.reset-on-end", true);
    }
    
    /**
     * 是否使用 Multiverse-Core 管理世界
     */
    public boolean useMultiverse() {
        return config.getBoolean("world.use-multiverse", false);
    }
    
    // ==================== 大厅配置 ====================
    
    /**
     * 是否启用大厅传送
     */
    public boolean isLobbyEnabled() {
        return config.getBoolean("lobby.enabled", true);
    }
    
    /**
     * 获取大厅世界名称
     */
    public String getLobbyWorld() {
        return config.getString("lobby.world", "world");
    }
    
    /**
     * 获取大厅位置
     */
    public Location getLobbyLocation() {
        String worldName = getLobbyWorld();
        World world = plugin.getServer().getWorld(worldName);
        
        if (world == null) {
            plugin.getLogger().warning("大厅世界不存在: " + worldName);
            // 返回主世界出生点
            if (!plugin.getServer().getWorlds().isEmpty()) {
                return plugin.getServer().getWorlds().get(0).getSpawnLocation();
            }
            return null;
        }
        
        double x = config.getDouble("lobby.location.x", 0.0);
        double y = config.getDouble("lobby.location.y", 64.0);
        double z = config.getDouble("lobby.location.z", 0.0);
        float yaw = (float) config.getDouble("lobby.location.yaw", 0.0);
        float pitch = (float) config.getDouble("lobby.location.pitch", 0.0);
        
        return new Location(world, x, y, z, yaw, pitch);
    }
    
    /**
     * 设置大厅位置
     */
    public void setLobbyLocation(Location location) {
        try {
            // 读取原始文件
            java.io.File configFile = new java.io.File(plugin.getDataFolder(), "manhunt.yml");
            java.util.List<String> lines = java.nio.file.Files.readAllLines(configFile.toPath(), java.nio.charset.StandardCharsets.UTF_8);
            
            // 标记是否在lobby配置块中
            boolean inLobbySection = false;
            boolean inLocationSection = false;
            
            // 需要更新的值
            String worldName = location.getWorld().getName();
            double x = location.getX();
            double y = location.getY();
            double z = location.getZ();
            float yaw = location.getYaw();
            float pitch = location.getPitch();
            
            // 遍历并修改配置行
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                String trimmed = line.trim();
                
                // 检测lobby配置块
                if (trimmed.startsWith("lobby:")) {
                    inLobbySection = true;
                    continue;
                }
                
                // 检测location配置块
                if (inLobbySection && trimmed.startsWith("location:")) {
                    inLocationSection = true;
                    continue;
                }
                
                // 如果遇到新的顶级配置块，退出lobby块
                if (inLobbySection && !line.startsWith(" ") && !line.startsWith("\t") && !trimmed.isEmpty() && !trimmed.startsWith("#")) {
                    inLobbySection = false;
                    inLocationSection = false;
                }
                
                // 修改world配置
                if (inLobbySection && !inLocationSection && trimmed.startsWith("world:")) {
                    lines.set(i, line.substring(0, line.indexOf("world:")) + "world: \"" + worldName + "\"");
                }
                
                // 修改location下的配置
                if (inLocationSection) {
                    if (trimmed.startsWith("x:")) {
                        lines.set(i, line.substring(0, line.indexOf("x:")) + "x: " + x);
                    } else if (trimmed.startsWith("y:")) {
                        lines.set(i, line.substring(0, line.indexOf("y:")) + "y: " + y);
                    } else if (trimmed.startsWith("z:")) {
                        lines.set(i, line.substring(0, line.indexOf("z:")) + "z: " + z);
                    } else if (trimmed.startsWith("yaw:")) {
                        lines.set(i, line.substring(0, line.indexOf("yaw:")) + "yaw: " + yaw);
                    } else if (trimmed.startsWith("pitch:")) {
                        lines.set(i, line.substring(0, line.indexOf("pitch:")) + "pitch: " + pitch);
                    }
                }
            }
            
            // 写回文件
            java.nio.file.Files.write(configFile.toPath(), lines, java.nio.charset.StandardCharsets.UTF_8);
            
            // 重新加载配置到内存
            config = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(configFile);
            
            plugin.getLogger().info("大厅位置已保存: " + worldName + " (" + x + ", " + y + ", " + z + ")");
            
        } catch (Exception e) {
            plugin.getLogger().severe("保存大厅位置失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // ==================== 猎人能力配置 ====================
    
    /**
     * 是否启用猎人速度提升
     */
    public boolean isHunterSpeedBoost() {
        return config.getBoolean("hunter.abilities.speed-boost", false);
    }
    
    /**
     * 获取猎人速度等级
     */
    public int getHunterSpeedLevel() {
        return Math.max(0, config.getInt("hunter.abilities.speed-level", 1));
    }
    
    /**
     * 是否启用猎人力量提升
     */
    public boolean isHunterStrengthBoost() {
        return config.getBoolean("hunter.abilities.strength-boost", false);
    }
    
    /**
     * 获取猎人力量等级
     */
    public int getHunterStrengthLevel() {
        return Math.max(0, config.getInt("hunter.abilities.strength-level", 1));
    }
    
    /**
     * 是否启用猎人生命值提升
     */
    public boolean isHunterHealthBoost() {
        return config.getBoolean("hunter.abilities.health-boost", false);
    }
    
    /**
     * 获取猎人额外生命值
     */
    public int getHunterExtraHealth() {
        return Math.max(0, config.getInt("hunter.abilities.extra-health", 4));
    }
    
    /**
     * 是否启用猎人夜视
     */
    public boolean isHunterNightVision() {
        return config.getBoolean("hunter.abilities.night-vision", false);
    }
    
    // ==================== 逃亡者道具配置 ====================
    
    /**
     * 获取逃亡者初始道具配置
     */
    public int getRunnerStartItem(String itemType) {
        return Math.max(0, config.getInt("runner.start-items." + itemType, 0));
    }
    
    /**
     * 是否启用逃亡者初始道具
     */
    public boolean isRunnerStartItemsEnabled() {
        return config.getBoolean("runner.start-items.enabled", false);
    }
    
    // ==================== 游戏事件配置 ====================
    
    /**
     * 是否启用随机游戏事件
     */
    public boolean isRandomEventsEnabled() {
        return config.getBoolean("events.enabled", false);
    }
    
    /**
     * 获取事件触发间隔(秒)
     */
    public int getEventTriggerInterval() {
        return Math.max(60, config.getInt("events.trigger-interval", 300));
    }
    
    /**
     * 获取事件触发概率
     */
    public double getEventTriggerChance() {
        return Math.max(0.0, Math.min(1.0, config.getDouble("events.trigger-chance", 0.3)));
    }
    
    // ==================== 防作弊配置 ====================
    
    /**
     * 是否禁用传送命令
     */
    public boolean isDisableTeleportCommands() {
        return config.getBoolean("anti-cheat.disable-tp", true);
    }
    
    /**
     * 是否禁用游戏模式命令
     */
    public boolean isDisableGamemodeCommands() {
        return config.getBoolean("anti-cheat.disable-gamemode", true);
    }
    
    /**
     * 是否禁用give命令
     */
    public boolean isDisableGiveCommands() {
        return config.getBoolean("anti-cheat.disable-give", true);
    }
    
    /**
     * 获取禁用命令列表
     */
    public java.util.List<String> getDisabledCommands() {
        return config.getStringList("anti-cheat.disabled-commands");
    }
    
    // ==================== Bungee 模式增强配置 ====================
    
    /**
     * 获取主大厅服务器名称
     */
    public String getMainLobby() {
        return plugin.getConfig().getString("bungee.main-lobby", "lobby");
    }
    
    /**
     * 获取子大厅服务器名称前缀
     */
    public String getSubLobbyPrefix() {
        String prefix = plugin.getConfig().getString("bungee.sub-lobby-prefix", "game-");
        plugin.debug("从配置读取 sub-lobby-prefix: " + prefix);
        return prefix;
    }
    
    /**
     * 获取当前服务器类型
     */
    public ServerType getServerType() {
        String typeStr = plugin.getConfig().getString("bungee.server-type", "SUB_LOBBY");
        plugin.debug("从配置读取 server-type: " + typeStr);
        
        try {
            ServerType type = ServerType.valueOf(typeStr.toUpperCase());
            plugin.debug("解析后的 ServerType: " + type);
            return type;
        } catch (IllegalArgumentException ex) {
            plugin.getLogger().warning("无效的服务器类型: " + typeStr + "，使用默认值: SUB_LOBBY");
            plugin.getLogger().warning("有效值: MAIN_LOBBY, SUB_LOBBY");
            return ServerType.SUB_LOBBY;
        }
    }
    
    /**
     * 是否启用单场比赛模式
     */
    public boolean isSingleGameMode() {
        return plugin.getConfig().getBoolean("bungee.single-game-mode", true);
    }
    
    /**
     * 获取游戏结束后返回主大厅的延迟时间(秒)
     */
    public int getReturnDelay() {
        return Math.max(0, plugin.getConfig().getInt("bungee.return-delay", 5));
    }
}
