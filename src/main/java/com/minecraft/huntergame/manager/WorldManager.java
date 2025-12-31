package com.minecraft.huntergame.manager;

import com.minecraft.huntergame.HunterGame;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;

/**
 * 世界管理器
 * 负责游戏世界的创建、加载和管理
 * 
 * @author YourName
 * @version 1.0.0
 */
public class WorldManager {
    
    private final HunterGame plugin;
    
    public WorldManager(HunterGame plugin) {
        this.plugin = plugin;
        plugin.getLogger().info("世界管理器已初始化");
    }
    
    /**
     * 加载或创建游戏世界
     * 
     * @param worldName 世界名称
     * @return 世界对象，如果失败返回null
     */
    public World loadOrCreateWorld(String worldName) {
        try {
            // 检查世界是否已加载
            World world = plugin.getServer().getWorld(worldName);
            if (world != null) {
                plugin.getLogger().info("世界已加载: " + worldName);
                return world;
            }
            
            // 创建新世界
            plugin.getLogger().info("正在创建世界: " + worldName);
            
            WorldCreator creator = new WorldCreator(worldName);
            
            // 根据配置设置世界类型
            String worldType = plugin.getManhuntConfig().getWorldType();
            switch (worldType.toUpperCase()) {
                case "FLAT":
                    creator.type(WorldType.FLAT);
                    break;
                case "LARGE_BIOMES":
                    creator.type(WorldType.LARGE_BIOMES);
                    break;
                case "AMPLIFIED":
                    creator.type(WorldType.AMPLIFIED);
                    break;
                default:
                    creator.type(WorldType.NORMAL);
                    break;
            }
            
            world = creator.createWorld();
            
            if (world != null) {
                // 设置世界边界
                int borderSize = plugin.getManhuntConfig().getWorldBorder();
                if (borderSize > 0) {
                    world.getWorldBorder().setCenter(world.getSpawnLocation());
                    world.getWorldBorder().setSize(borderSize * 2.0);
                    plugin.getLogger().info("世界边界已设置: " + borderSize + " 方块");
                }
                
                plugin.getLogger().info("世界创建成功: " + worldName);
            } else {
                plugin.getLogger().severe("世界创建失败: " + worldName);
            }
            
            return world;
            
        } catch (Exception ex) {
            plugin.getLogger().severe("加载/创建世界失败: " + worldName);
            ex.printStackTrace();
            return null;
        }
    }
    
    /**
     * 卸载世界
     * 
     * @param worldName 世界名称
     * @return 是否成功卸载
     */
    public boolean unloadWorld(String worldName) {
        try {
            World world = plugin.getServer().getWorld(worldName);
            if (world == null) {
                plugin.getLogger().warning("世界不存在: " + worldName);
                return false;
            }
            
            // 传送所有玩家到主世界
            World mainWorld = plugin.getServer().getWorlds().get(0);
            for (org.bukkit.entity.Player player : world.getPlayers()) {
                player.teleport(mainWorld.getSpawnLocation());
            }
            
            // 卸载世界
            boolean success = plugin.getServer().unloadWorld(world, true);
            
            if (success) {
                plugin.getLogger().info("世界已卸载: " + worldName);
            } else {
                plugin.getLogger().warning("世界卸载失败: " + worldName);
            }
            
            return success;
            
        } catch (Exception ex) {
            plugin.getLogger().severe("卸载世界失败: " + worldName);
            ex.printStackTrace();
            return false;
        }
    }
    
    /**
     * 重置世界
     * 注意：这会删除世界文件夹，请谨慎使用
     * 
     * @param worldName 世界名称
     * @return 是否成功重置
     */
    public boolean resetWorld(String worldName) {
        try {
            // 先卸载世界
            if (!unloadWorld(worldName)) {
                return false;
            }
            
            // 删除世界文件夹
            java.io.File worldFolder = new java.io.File(plugin.getServer().getWorldContainer(), worldName);
            if (worldFolder.exists()) {
                deleteDirectory(worldFolder);
                plugin.getLogger().info("世界文件夹已删除: " + worldName);
            }
            
            // 重新创建世界
            World world = loadOrCreateWorld(worldName);
            
            return world != null;
            
        } catch (Exception ex) {
            plugin.getLogger().severe("重置世界失败: " + worldName);
            ex.printStackTrace();
            return false;
        }
    }
    
    /**
     * 递归删除目录
     */
    private void deleteDirectory(java.io.File directory) {
        if (directory.isDirectory()) {
            java.io.File[] files = directory.listFiles();
            if (files != null) {
                for (java.io.File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        directory.delete();
    }
    
    /**
     * 获取世界
     * 
     * @param worldName 世界名称
     * @return 世界对象，如果不存在返回null
     */
    public World getWorld(String worldName) {
        return plugin.getServer().getWorld(worldName);
    }
    
    /**
     * 检查世界是否存在
     * 
     * @param worldName 世界名称
     * @return 是否存在
     */
    public boolean worldExists(String worldName) {
        return plugin.getServer().getWorld(worldName) != null;
    }
    
    /**
     * 关闭管理器
     */
    public void shutdown() {
        plugin.getLogger().info("世界管理器已关闭");
    }
}
