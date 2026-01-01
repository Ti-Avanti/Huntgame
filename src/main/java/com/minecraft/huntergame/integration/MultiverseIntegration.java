package com.minecraft.huntergame.integration;

import com.minecraft.huntergame.HunterGame;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.plugin.Plugin;

/**
 * Multiverse-Core 集成
 * 用于创建和管理独立的游戏世界（包括地狱和末地）
 * 
 * @author YourName
 * @version 1.0.0
 */
public class MultiverseIntegration {
    
    private final HunterGame plugin;
    private Object mvCore; // 使用 Object 避免类加载时的依赖
    private boolean enabled;
    
    public MultiverseIntegration(HunterGame plugin) {
        this.plugin = plugin;
        this.enabled = false;
    }
    
    /**
     * 初始化 Multiverse 集成
     */
    public boolean initialize() {
        try {
            Plugin mvPlugin = plugin.getServer().getPluginManager().getPlugin("Multiverse-Core");
            
            if (mvPlugin == null || !mvPlugin.isEnabled()) {
                plugin.getLogger().warning("Multiverse-Core 未安装或未启用");
                return false;
            }
            
            // 使用反射检查类型，避免直接依赖
            Class<?> mvCoreClass = Class.forName("com.onarandombox.MultiverseCore.MultiverseCore");
            if (!mvCoreClass.isInstance(mvPlugin)) {
                plugin.getLogger().warning("Multiverse-Core 版本不兼容");
                return false;
            }
            
            mvCore = mvPlugin;
            enabled = true;
            
            plugin.getLogger().info("Multiverse-Core 集成已启用");
            return true;
            
        } catch (ClassNotFoundException e) {
            plugin.getLogger().warning("Multiverse-Core 类未找到，集成已禁用");
            return false;
        } catch (Exception e) {
            plugin.getLogger().warning("初始化 Multiverse-Core 集成时发生异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 创建游戏世界（主世界、地狱、末地）
     * 
     * @param worldName 世界名称前缀
     * @return 是否成功创建
     */
    public boolean createGameWorlds(String worldName) {
        if (!enabled) {
            plugin.getLogger().warning("Multiverse 未启用，无法创建游戏世界");
            return false;
        }
        
        try {
            // 使用反射调用 Multiverse API
            Class<?> mvCoreClass = Class.forName("com.onarandombox.MultiverseCore.MultiverseCore");
            Class<?> mvWorldManagerClass = Class.forName("com.onarandombox.MultiverseCore.api.MVWorldManager");
            
            Object worldManager = mvCoreClass.getMethod("getMVWorldManager").invoke(mvCore);
            
            plugin.getLogger().info("开始创建游戏世界: " + worldName);
            
            // 检查世界是否已存在
            Object existingWorld = mvWorldManagerClass.getMethod("getMVWorld", String.class).invoke(worldManager, worldName);
            if (existingWorld != null) {
                plugin.getLogger().warning("主世界已存在: " + worldName + "，跳过创建");
                return true;
            }
            
            // 创建主世界
            plugin.getLogger().info("正在创建主世界: " + worldName);
            Boolean mainWorld = (Boolean) mvWorldManagerClass.getMethod(
                "addWorld",
                String.class,
                World.Environment.class,
                String.class,
                WorldType.class,
                Boolean.TYPE,
                String.class
            ).invoke(worldManager, worldName, World.Environment.NORMAL, null, WorldType.NORMAL, true, null);
            
            if (!mainWorld) {
                plugin.getLogger().severe("创建主世界失败: " + worldName);
                return false;
            }
            plugin.getLogger().info("主世界创建成功: " + worldName);
            
            // 创建地狱
            String netherName = worldName + "_nether";
            plugin.getLogger().info("正在创建地狱: " + netherName);
            
            Object existingNether = mvWorldManagerClass.getMethod("getMVWorld", String.class).invoke(worldManager, netherName);
            if (existingNether != null) {
                plugin.getLogger().warning("地狱已存在: " + netherName + "，跳过创建");
            } else {
                Boolean netherWorld = (Boolean) mvWorldManagerClass.getMethod(
                    "addWorld",
                    String.class,
                    World.Environment.class,
                    String.class,
                    WorldType.class,
                    Boolean.TYPE,
                    String.class
                ).invoke(worldManager, netherName, World.Environment.NETHER, null, WorldType.NORMAL, true, null);
                
                if (!netherWorld) {
                    plugin.getLogger().severe("创建地狱失败: " + netherName);
                    // 清理已创建的主世界
                    plugin.getLogger().info("清理已创建的主世界: " + worldName);
                    mvWorldManagerClass.getMethod("deleteWorld", String.class).invoke(worldManager, worldName);
                    return false;
                }
                plugin.getLogger().info("地狱创建成功: " + netherName);
            }
            
            // 创建末地
            String endName = worldName + "_the_end";
            plugin.getLogger().info("正在创建末地: " + endName);
            
            Object existingEnd = mvWorldManagerClass.getMethod("getMVWorld", String.class).invoke(worldManager, endName);
            if (existingEnd != null) {
                plugin.getLogger().warning("末地已存在: " + endName + "，跳过创建");
            } else {
                Boolean endWorld = (Boolean) mvWorldManagerClass.getMethod(
                    "addWorld",
                    String.class,
                    World.Environment.class,
                    String.class,
                    WorldType.class,
                    Boolean.TYPE,
                    String.class
                ).invoke(worldManager, endName, World.Environment.THE_END, null, WorldType.NORMAL, true, null);
                
                if (!endWorld) {
                    plugin.getLogger().severe("创建末地失败: " + endName);
                    // 清理已创建的世界
                    plugin.getLogger().info("清理已创建的世界");
                    mvWorldManagerClass.getMethod("deleteWorld", String.class).invoke(worldManager, worldName);
                    mvWorldManagerClass.getMethod("deleteWorld", String.class).invoke(worldManager, netherName);
                    return false;
                }
                plugin.getLogger().info("末地创建成功: " + endName);
            }
            
            plugin.getLogger().info("游戏世界创建完成: " + worldName + " (包括地狱和末地)");
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().severe("创建游戏世界时发生异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 删除游戏世界（主世界、地狱、末地）
     * 
     * @param worldName 世界名称前缀
     * @return 是否成功删除
     */
    public boolean deleteGameWorlds(String worldName) {
        if (!enabled) {
            plugin.getLogger().warning("Multiverse 未启用，无法删除游戏世界");
            return false;
        }
        
        try {
            // 使用反射调用 Multiverse API
            Class<?> mvCoreClass = Class.forName("com.onarandombox.MultiverseCore.MultiverseCore");
            Class<?> mvWorldManagerClass = Class.forName("com.onarandombox.MultiverseCore.api.MVWorldManager");
            
            Object worldManager = mvCoreClass.getMethod("getMVWorldManager").invoke(mvCore);
            boolean allSuccess = true;
            
            plugin.getLogger().info("开始删除游戏世界: " + worldName);
            
            // 先传送所有在这些世界中的玩家到主世界
            World mainWorld = plugin.getServer().getWorlds().get(0);
            String[] worldNames = {worldName, worldName + "_nether", worldName + "_the_end"};
            
            for (String wName : worldNames) {
                World world = plugin.getServer().getWorld(wName);
                if (world != null) {
                    for (org.bukkit.entity.Player player : world.getPlayers()) {
                        player.teleport(mainWorld.getSpawnLocation());
                        plugin.getLogger().info("玩家 " + player.getName() + " 已从 " + wName + " 传送到主世界");
                    }
                }
            }
            
            // 删除主世界
            plugin.getLogger().info("正在删除主世界: " + worldName);
            Object mvWorld = mvWorldManagerClass.getMethod("getMVWorld", String.class).invoke(worldManager, worldName);
            if (mvWorld != null) {
                Boolean deleted = (Boolean) mvWorldManagerClass.getMethod("deleteWorld", String.class).invoke(worldManager, worldName);
                if (deleted) {
                    plugin.getLogger().info("主世界删除成功: " + worldName);
                } else {
                    plugin.getLogger().warning("主世界删除失败: " + worldName);
                    allSuccess = false;
                }
            } else {
                plugin.getLogger().warning("主世界不存在，跳过删除: " + worldName);
            }
            
            // 删除地狱
            String netherName = worldName + "_nether";
            plugin.getLogger().info("正在删除地狱: " + netherName);
            Object mvNether = mvWorldManagerClass.getMethod("getMVWorld", String.class).invoke(worldManager, netherName);
            if (mvNether != null) {
                Boolean deleted = (Boolean) mvWorldManagerClass.getMethod("deleteWorld", String.class).invoke(worldManager, netherName);
                if (deleted) {
                    plugin.getLogger().info("地狱删除成功: " + netherName);
                } else {
                    plugin.getLogger().warning("地狱删除失败: " + netherName);
                    allSuccess = false;
                }
            } else {
                plugin.getLogger().warning("地狱不存在，跳过删除: " + netherName);
            }
            
            // 删除末地
            String endName = worldName + "_the_end";
            plugin.getLogger().info("正在删除末地: " + endName);
            Object mvEnd = mvWorldManagerClass.getMethod("getMVWorld", String.class).invoke(worldManager, endName);
            if (mvEnd != null) {
                Boolean deleted = (Boolean) mvWorldManagerClass.getMethod("deleteWorld", String.class).invoke(worldManager, endName);
                if (deleted) {
                    plugin.getLogger().info("末地删除成功: " + endName);
                } else {
                    plugin.getLogger().warning("末地删除失败: " + endName);
                    allSuccess = false;
                }
            } else {
                plugin.getLogger().warning("末地不存在，跳过删除: " + endName);
            }
            
            if (allSuccess) {
                plugin.getLogger().info("游戏世界删除完成: " + worldName);
            } else {
                plugin.getLogger().warning("游戏世界删除部分失败: " + worldName);
            }
            
            return allSuccess;
            
        } catch (Exception e) {
            plugin.getLogger().severe("删除游戏世界时发生异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 获取地狱世界名称
     */
    public String getNetherWorldName(String worldName) {
        return worldName + "_nether";
    }
    
    /**
     * 获取末地世界名称
     */
    public String getEndWorldName(String worldName) {
        return worldName + "_the_end";
    }
    
    /**
     * 检查是否启用
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * 获取 Multiverse Core 实例
     */
    public Object getCore() {
        return mvCore;
    }
}
