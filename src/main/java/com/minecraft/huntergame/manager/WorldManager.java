package com.minecraft.huntergame.manager;

import com.minecraft.huntergame.HunterGame;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;

import java.util.Arrays;
import java.util.List;

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
     * 加载或创建游戏世界（包括主世界、地狱、末地）
     * 
     * @param worldName 世界名称
     * @return 主世界对象，如果失败返回null
     */
    public World loadOrCreateWorld(String worldName) {
        try {
            // 如果启用 Multiverse 模式，使用 Multiverse 创建世界
            if (plugin.getManhuntConfig().useMultiverse() && 
                plugin.getIntegrationManager().isMultiverseEnabled()) {
                
                plugin.getLogger().info("使用 Multiverse-Core 创建游戏世界: " + worldName);
                
                boolean success = plugin.getIntegrationManager()
                    .getMultiverseIntegration()
                    .createGameWorlds(worldName);
                
                if (success) {
                    World world = plugin.getServer().getWorld(worldName);
                    if (world != null) {
                        setupWorldSettings(world);
                        return world;
                    }
                }
                
                plugin.getLogger().severe("Multiverse 创建世界失败: " + worldName);
                return null;
            }
            
            // 使用原生方式创建世界（包括地狱和末地）
            plugin.getLogger().info("使用原生方式创建游戏世界: " + worldName);
            
            // 1. 创建主世界
            World mainWorld = createNativeWorld(worldName, World.Environment.NORMAL);
            if (mainWorld == null) {
                plugin.getLogger().severe("创建主世界失败: " + worldName);
                return null;
            }
            
            // 2. 创建地狱
            String netherName = worldName + "_nether";
            World netherWorld = createNativeWorld(netherName, World.Environment.NETHER);
            if (netherWorld == null) {
                plugin.getLogger().warning("创建地狱失败: " + netherName);
            } else {
                plugin.getLogger().info("地狱创建成功: " + netherName);
            }
            
            // 3. 创建末地
            String endName = worldName + "_the_end";
            World endWorld = createNativeWorld(endName, World.Environment.THE_END);
            if (endWorld == null) {
                plugin.getLogger().warning("创建末地失败: " + endName);
            } else {
                plugin.getLogger().info("末地创建成功: " + endName);
            }
            
            plugin.getLogger().info("游戏世界创建完成: " + worldName + " (包括地狱和末地)");
            return mainWorld;
            
        } catch (Exception ex) {
            plugin.getLogger().severe("加载/创建世界失败: " + worldName);
            ex.printStackTrace();
            return null;
        }
    }
    
    /**
     * 使用原生方式创建单个世界
     */
    private World createNativeWorld(String worldName, World.Environment environment) {
        try {
            // 检查世界是否已加载
            World world = plugin.getServer().getWorld(worldName);
            if (world != null) {
                plugin.getLogger().info("世界已加载: " + worldName);
                return world;
            }
            
            // 创建新世界
            plugin.getLogger().info("正在创建世界: " + worldName + " (环境: " + environment + ")");
            
            WorldCreator creator = new WorldCreator(worldName);
            creator.environment(environment);
            
            // 根据配置设置世界类型（仅主世界）
            if (environment == World.Environment.NORMAL) {
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
            } else {
                creator.type(WorldType.NORMAL);
            }
            
            // 设置生成结构
            creator.generateStructures(true);
            
            world = creator.createWorld();
            
            if (world != null) {
                setupWorldSettings(world);
                plugin.getLogger().info("世界创建成功: " + worldName);
            } else {
                plugin.getLogger().severe("世界创建失败: " + worldName);
            }
            
            return world;
            
        } catch (Exception ex) {
            plugin.getLogger().severe("创建世界失败: " + worldName);
            ex.printStackTrace();
            return null;
        }
    }
    
    /**
     * 设置世界配置
     */
    private void setupWorldSettings(World world) {
        // 设置世界边界
        int borderSize = plugin.getManhuntConfig().getWorldBorder();
        if (borderSize > 0) {
            world.getWorldBorder().setCenter(world.getSpawnLocation());
            world.getWorldBorder().setSize(borderSize * 2.0);
            plugin.getLogger().info("世界边界已设置: " + borderSize + " 方块");
        }
        
        // 设置世界规则
        world.setKeepSpawnInMemory(false); // 不保持出生点区块加载
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
            // 安全检查：禁止重置主世界
            List<String> protectedWorlds = Arrays.asList("world", "world_nether", "world_the_end");
            if (protectedWorlds.contains(worldName.toLowerCase())) {
                plugin.getLogger().severe("禁止重置主世界: " + worldName);
                return false;
            }
            
            // 如果启用 Multiverse 模式，使用 Multiverse 删除并重新创建世界
            if (plugin.getManhuntConfig().useMultiverse() && 
                plugin.getIntegrationManager().isMultiverseEnabled()) {
                
                plugin.getLogger().info("使用 Multiverse-Core 重置游戏世界: " + worldName);
                
                // 1. 删除旧世界
                boolean deleteSuccess = plugin.getIntegrationManager()
                    .getMultiverseIntegration()
                    .deleteGameWorlds(worldName);
                
                if (!deleteSuccess) {
                    plugin.getLogger().warning("Multiverse 删除世界失败: " + worldName);
                    return false;
                }
                
                plugin.getLogger().info("旧世界已删除，将在40 tick后重新创建...");
                
                // 2. 延迟重新创建世界（避免阻塞主线程）
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    boolean createSuccess = plugin.getIntegrationManager()
                        .getMultiverseIntegration()
                        .createGameWorlds(worldName);
                    
                    if (createSuccess) {
                        plugin.getLogger().info("游戏世界已通过 Multiverse 重置完成: " + worldName);
                        
                        // 3. 设置新世界的配置
                        World newWorld = plugin.getServer().getWorld(worldName);
                        if (newWorld != null) {
                            setupWorldSettings(newWorld);
                        }
                    } else {
                        plugin.getLogger().severe("Multiverse 重新创建世界失败: " + worldName);
                    }
                }, 40L); // 2秒后重新创建（40 tick = 2秒）
                
                return true;
            }
            
            // 使用原生方式重置世界
            plugin.getLogger().info("使用原生方式重置游戏世界: " + worldName);
            
            // 1. 先卸载所有相关世界（主世界、地狱、末地）
            unloadWorld(worldName);
            unloadWorld(worldName + "_nether");
            unloadWorld(worldName + "_the_end");
            
            // 2. 删除世界文件夹（包括地狱和末地）
            deleteWorldFiles(worldName);
            
            plugin.getLogger().info("旧世界已删除，将在40 tick后重新创建...");
            
            // 3. 延迟重新创建世界（避免阻塞主线程）
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                World world = loadOrCreateWorld(worldName);
                
                if (world != null) {
                    plugin.getLogger().info("游戏世界已通过原生方式重置完成: " + worldName);
                } else {
                    plugin.getLogger().severe("原生方式重新创建世界失败: " + worldName);
                }
            }, 40L); // 2秒后重新创建（40 tick = 2秒）
            
            return true;
            
        } catch (Exception ex) {
            plugin.getLogger().severe("重置世界失败: " + worldName);
            ex.printStackTrace();
            return false;
        }
    }
    
    /**
     * 删除世界文件（包括主世界、地狱、末地）
     */
    private void deleteWorldFiles(String worldName) {
        // 删除主世界
        java.io.File worldFolder = new java.io.File(plugin.getServer().getWorldContainer(), worldName);
        if (worldFolder.exists()) {
            deleteDirectory(worldFolder);
            plugin.getLogger().info("世界文件夹已删除: " + worldName);
        }
        
        // 删除地狱（独立文件夹）
        java.io.File netherFolder = new java.io.File(plugin.getServer().getWorldContainer(), worldName + "_nether");
        if (netherFolder.exists()) {
            deleteDirectory(netherFolder);
            plugin.getLogger().info("地狱文件夹已删除: " + worldName + "_nether");
        }
        
        // 删除末地（独立文件夹）
        java.io.File endFolder = new java.io.File(plugin.getServer().getWorldContainer(), worldName + "_the_end");
        if (endFolder.exists()) {
            deleteDirectory(endFolder);
            plugin.getLogger().info("末地文件夹已删除: " + worldName + "_the_end");
        }
        
        // 同时检查主世界内的DIM-1和DIM1（兼容不同服务器配置）
        java.io.File dimNether = new java.io.File(worldFolder, "DIM-1");
        if (dimNether.exists()) {
            deleteDirectory(dimNether);
            plugin.getLogger().info("地狱维度已删除: " + worldName + "/DIM-1");
        }
        
        java.io.File dimEnd = new java.io.File(worldFolder, "DIM1");
        if (dimEnd.exists()) {
            deleteDirectory(dimEnd);
            plugin.getLogger().info("末地维度已删除: " + worldName + "/DIM1");
        }
    }
    
    /**
     * 递归删除目录
     */
    private void deleteDirectory(java.io.File directory) {
        if (directory == null || !directory.exists()) {
            return;
        }
        
        if (directory.isDirectory()) {
            java.io.File[] files = directory.listFiles();
            if (files != null) {
                for (java.io.File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        
        if (!directory.delete()) {
            plugin.getLogger().warning("无法删除文件: " + directory.getPath());
        }
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
     * 获取地狱世界名称
     */
    public String getNetherWorldName(String mainWorldName) {
        return mainWorldName + "_nether";
    }
    
    /**
     * 获取末地世界名称
     */
    public String getEndWorldName(String mainWorldName) {
        return mainWorldName + "_the_end";
    }
    
    /**
     * 获取地狱世界
     */
    public World getNetherWorld(String mainWorldName) {
        return plugin.getServer().getWorld(getNetherWorldName(mainWorldName));
    }
    
    /**
     * 获取末地世界
     */
    public World getEndWorld(String mainWorldName) {
        return plugin.getServer().getWorld(getEndWorldName(mainWorldName));
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
