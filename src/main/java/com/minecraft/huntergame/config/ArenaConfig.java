package com.minecraft.huntergame.config;

import com.minecraft.huntergame.HunterGame;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 竞技场配置管理类
 * 负责加载和管理竞技场配置文件
 * 
 * @author YourName
 * @version 1.0.0
 */
public class ArenaConfig {
    
    private final HunterGame plugin;
    private final String arenaName;
    private final File configFile;
    private FileConfiguration config;
    
    public ArenaConfig(HunterGame plugin, String arenaName) {
        this.plugin = plugin;
        this.arenaName = arenaName;
        this.configFile = new File(plugin.getDataFolder() + "/arenas", arenaName + ".yml");
        load();
    }
    
    /**
     * 加载配置文件
     */
    public void load() {
        if (!configFile.exists()) {
            plugin.getLogger().warning("竞技场配置文件不存在: " + arenaName);
            return;
        }
        
        config = YamlConfiguration.loadConfiguration(configFile);
        plugin.getLogger().info("竞技场配置已加载: " + arenaName);
    }
    
    /**
     * 保存配置文件
     */
    public void save() {
        try {
            config.save(configFile);
            plugin.getLogger().info("竞技场配置已保存: " + arenaName);
        } catch (IOException ex) {
            plugin.getLogger().severe("保存竞技场配置失败: " + arenaName);
            ex.printStackTrace();
        }
    }
    
    /**
     * 重载配置文件
     */
    public void reload() {
        load();
    }
    
    /**
     * 获取竞技场名称
     */
    public String getArenaName() {
        return arenaName;
    }
    
    /**
     * 获取显示名称
     */
    public String getDisplayName() {
        return config.getString("display-name", arenaName);
    }
    
    /**
     * 设置显示名称
     */
    public void setDisplayName(String displayName) {
        config.set("display-name", displayName);
    }
    
    /**
     * 竞技场是否启用
     */
    public boolean isEnabled() {
        return config.getBoolean("enabled", true);
    }
    
    /**
     * 设置竞技场启用状态
     */
    public void setEnabled(boolean enabled) {
        config.set("enabled", enabled);
    }
    
    /**
     * 获取世界名称
     */
    public String getWorldName() {
        return config.getString("world");
    }
    
    /**
     * 设置世界名称
     */
    public void setWorldName(String worldName) {
        config.set("world", worldName);
    }
    
    /**
     * 获取世界对象
     */
    public World getWorld() {
        String worldName = getWorldName();
        if (worldName == null) {
            return null;
        }
        return Bukkit.getWorld(worldName);
    }
    
    /**
     * 获取最小玩家数
     */
    public int getMinPlayers() {
        return config.getInt("min-players", plugin.getConfig().getInt("game.min-players", 2));
    }
    
    /**
     * 设置最小玩家数
     */
    public void setMinPlayers(int minPlayers) {
        config.set("min-players", minPlayers);
    }
    
    /**
     * 获取最大玩家数
     */
    public int getMaxPlayers() {
        return config.getInt("max-players", plugin.getConfig().getInt("game.max-players", 16));
    }
    
    /**
     * 设置最大玩家数
     */
    public void setMaxPlayers(int maxPlayers) {
        config.set("max-players", maxPlayers);
    }
    
    /**
     * 获取游戏时长(秒)
     */
    public int getGameDuration() {
        return config.getInt("game-duration", plugin.getConfig().getInt("game.duration", 600));
    }
    
    /**
     * 设置游戏时长
     */
    public void setGameDuration(int duration) {
        config.set("game-duration", duration);
    }
    
    /**
     * 获取开始倒计时时间(秒)
     */
    public int getCountdownTime() {
        return config.getInt("countdown-time", plugin.getConfig().getInt("game.countdown-time", 10));
    }
    
    /**
     * 设置开始倒计时时间
     */
    public void setCountdownTime(int countdown) {
        config.set("countdown-time", countdown);
    }
    
    /**
     * 获取等待大厅位置
     */
    public Location getLobbyLocation() {
        return getLocation("lobby");
    }
    
    /**
     * 设置等待大厅位置
     */
    public void setLobbyLocation(Location location) {
        setLocation("lobby", location);
    }
    
    /**
     * 获取观战位置
     */
    public Location getSpectatorLocation() {
        return getLocation("spectator");
    }
    
    /**
     * 设置观战位置
     */
    public void setSpectatorLocation(Location location) {
        setLocation("spectator", location);
    }
    
    /**
     * 获取猎人出生点列表
     */
    public List<Location> getHunterSpawns() {
        return getLocationList("hunter-spawns");
    }
    
    /**
     * 添加猎人出生点
     */
    public void addHunterSpawn(Location location) {
        List<Location> spawns = getHunterSpawns();
        spawns.add(location);
        setLocationList("hunter-spawns", spawns);
    }
    
    /**
     * 获取逃生者出生点列表
     */
    public List<Location> getSurvivorSpawns() {
        return getLocationList("survivor-spawns");
    }
    
    /**
     * 添加逃生者出生点
     */
    public void addSurvivorSpawn(Location location) {
        List<Location> spawns = getSurvivorSpawns();
        spawns.add(location);
        setLocationList("survivor-spawns", spawns);
    }
    
    /**
     * 获取逃生点列表
     */
    public List<Location> getEscapePoints() {
        return getLocationList("escape-points");
    }
    
    /**
     * 添加逃生点
     */
    public void addEscapePoint(Location location) {
        List<Location> points = getEscapePoints();
        points.add(location);
        setLocationList("escape-points", points);
    }
    
    /**
     * 获取边界最小点
     */
    public Location getBoundaryMin() {
        return getLocation("boundary.min");
    }
    
    /**
     * 设置边界最小点
     */
    public void setBoundaryMin(Location location) {
        setLocation("boundary.min", location);
    }
    
    /**
     * 获取边界最大点
     */
    public Location getBoundaryMax() {
        return getLocation("boundary.max");
    }
    
    /**
     * 设置边界最大点
     */
    public void setBoundaryMax(Location location) {
        setLocation("boundary.max", location);
    }
    
    /**
     * 从配置中获取位置
     */
    private Location getLocation(String path) {
        ConfigurationSection section = config.getConfigurationSection(path);
        if (section == null) {
            return null;
        }
        
        String worldName = section.getString("world");
        if (worldName == null) {
            worldName = getWorldName();
        }
        
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }
        
        double x = section.getDouble("x");
        double y = section.getDouble("y");
        double z = section.getDouble("z");
        float yaw = (float) section.getDouble("yaw", 0);
        float pitch = (float) section.getDouble("pitch", 0);
        
        return new Location(world, x, y, z, yaw, pitch);
    }
    
    /**
     * 设置位置到配置
     */
    private void setLocation(String path, Location location) {
        if (location == null) {
            config.set(path, null);
            return;
        }
        
        config.set(path + ".world", location.getWorld().getName());
        config.set(path + ".x", location.getX());
        config.set(path + ".y", location.getY());
        config.set(path + ".z", location.getZ());
        config.set(path + ".yaw", location.getYaw());
        config.set(path + ".pitch", location.getPitch());
    }
    
    /**
     * 从配置中获取位置列表
     */
    private List<Location> getLocationList(String path) {
        List<Location> locations = new ArrayList<>();
        
        if (!config.contains(path)) {
            return locations;
        }
        
        List<?> list = config.getList(path);
        if (list == null) {
            return locations;
        }
        
        for (Object obj : list) {
            if (obj instanceof ConfigurationSection) {
                ConfigurationSection section = (ConfigurationSection) obj;
                Location location = parseLocationSection(section);
                if (location != null) {
                    locations.add(location);
                }
            }
        }
        
        return locations;
    }
    
    /**
     * 设置位置列表到配置
     */
    private void setLocationList(String path, List<Location> locations) {
        List<Object> list = new ArrayList<>();
        
        for (Location location : locations) {
            if (location != null) {
                list.add(serializeLocation(location));
            }
        }
        
        config.set(path, list);
    }
    
    /**
     * 解析位置配置节
     */
    private Location parseLocationSection(ConfigurationSection section) {
        String worldName = section.getString("world");
        if (worldName == null) {
            worldName = getWorldName();
        }
        
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }
        
        double x = section.getDouble("x");
        double y = section.getDouble("y");
        double z = section.getDouble("z");
        float yaw = (float) section.getDouble("yaw", 0);
        float pitch = (float) section.getDouble("pitch", 0);
        
        return new Location(world, x, y, z, yaw, pitch);
    }
    
    /**
     * 序列化位置对象
     */
    private Object serializeLocation(Location location) {
        ConfigurationSection section = config.createSection("temp");
        section.set("world", location.getWorld().getName());
        section.set("x", location.getX());
        section.set("y", location.getY());
        section.set("z", location.getZ());
        section.set("yaw", location.getYaw());
        section.set("pitch", location.getPitch());
        return section;
    }
    
    /**
     * 验证配置完整性
     */
    public boolean validate() {
        List<String> errors = new ArrayList<>();
        
        // 检查世界
        if (getWorldName() == null) {
            errors.add("世界名称未设置");
        } else if (getWorld() == null) {
            errors.add("世界不存在: " + getWorldName());
        }
        
        // 检查必要位置
        if (getLobbyLocation() == null) {
            errors.add("等待大厅位置未设置");
        }
        
        if (getSpectatorLocation() == null) {
            errors.add("观战位置未设置");
        }
        
        // 检查出生点
        if (getHunterSpawns().isEmpty()) {
            errors.add("猎人出生点未设置");
        }
        
        if (getSurvivorSpawns().isEmpty()) {
            errors.add("逃生者出生点未设置");
        }
        
        // 检查逃生点
        if (getEscapePoints().isEmpty()) {
            errors.add("逃生点未设置");
        }
        
        // 输出错误
        if (!errors.isEmpty()) {
            plugin.getLogger().warning("竞技场 " + arenaName + " 配置不完整:");
            for (String error : errors) {
                plugin.getLogger().warning("  - " + error);
            }
            return false;
        }
        
        return true;
    }
    
    /**
     * 创建示例配置文件
     */
    public static void createExample(HunterGame plugin) {
        File arenaFolder = new File(plugin.getDataFolder(), "arenas");
        if (!arenaFolder.exists()) {
            arenaFolder.mkdirs();
        }
        
        File exampleFile = new File(arenaFolder, "example.yml");
        if (exampleFile.exists()) {
            return;
        }
        
        FileConfiguration config = new YamlConfiguration();
        
        config.set("display-name", "示例竞技场");
        config.set("enabled", false);
        config.set("world", "world");
        config.set("min-players", 2);
        config.set("max-players", 16);
        config.set("game-duration", 600);
        
        // 示例位置
        config.set("lobby.world", "world");
        config.set("lobby.x", 0.5);
        config.set("lobby.y", 64.0);
        config.set("lobby.z", 0.5);
        config.set("lobby.yaw", 0.0);
        config.set("lobby.pitch", 0.0);
        
        try {
            config.save(exampleFile);
            plugin.getLogger().info("已创建示例竞技场配置文件");
        } catch (IOException ex) {
            plugin.getLogger().severe("创建示例配置文件失败");
            ex.printStackTrace();
        }
    }
}
