package com.minecraft.huntergame.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Random;

/**
 * 位置工具类
 * 提供位置相关的实用方法
 * 
 * @author YourName
 * @version 1.0.0
 */
public class LocationUtil {
    
    private static final Random random = new Random();
    
    /**
     * 在指定范围内随机选择一个安全的出生点
     * 
     * @param world 世界
     * @param centerX 中心X坐标
     * @param centerZ 中心Z坐标
     * @param radius 搜索半径
     * @param maxAttempts 最大尝试次数
     * @return 安全的位置，如果找不到返回世界出生点
     */
    public static Location findSafeSpawnLocation(World world, int centerX, int centerZ, int radius, int maxAttempts) {
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            // 在半径范围内随机选择坐标
            int x = centerX + random.nextInt(radius * 2) - radius;
            int z = centerZ + random.nextInt(radius * 2) - radius;
            
            // 获取最高的固体方块
            int y = world.getHighestBlockYAt(x, z);
            
            // 检查位置是否安全
            Location location = new Location(world, x + 0.5, y + 1, z + 0.5);
            if (isSafeLocation(location)) {
                return location;
            }
        }
        
        // 如果找不到安全位置，返回世界出生点
        return world.getSpawnLocation();
    }
    
    /**
     * 在世界边界内随机选择安全出生点
     * 
     * @param world 世界
     * @param maxAttempts 最大尝试次数
     * @return 安全的位置
     */
    public static Location findRandomSafeSpawn(World world, int maxAttempts) {
        // 获取世界边界大小
        double borderSize = world.getWorldBorder().getSize() / 2.0;
        int radius = (int) Math.min(borderSize, 1000); // 最大搜索半径1000
        
        Location center = world.getWorldBorder().getCenter();
        return findSafeSpawnLocation(world, center.getBlockX(), center.getBlockZ(), radius, maxAttempts);
    }
    
    /**
     * 检查位置是否安全（可以作为出生点）
     * 
     * @param location 要检查的位置
     * @return 是否安全
     */
    public static boolean isSafeLocation(Location location) {
        World world = location.getWorld();
        if (world == null) {
            return false;
        }
        
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        
        // 检查脚下的方块
        Block ground = world.getBlockAt(x, y - 1, z);
        if (!ground.getType().isSolid()) {
            return false; // 脚下不是固体方块
        }
        
        // 检查脚下是否是危险方块
        Material groundType = ground.getType();
        if (groundType == Material.LAVA || groundType == Material.FIRE || 
            groundType == Material.MAGMA_BLOCK || groundType == Material.CACTUS) {
            return false;
        }
        
        // 检查头部和身体位置是否有空间
        Block feet = world.getBlockAt(x, y, z);
        Block head = world.getBlockAt(x, y + 1, z);
        
        if (!feet.getType().isAir() || !head.getType().isAir()) {
            return false; // 没有足够的空间
        }
        
        // 检查周围是否有危险方块
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                Block nearby = world.getBlockAt(x + dx, y, z + dz);
                Material type = nearby.getType();
                if (type == Material.LAVA || type == Material.FIRE) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * 获取安全的Y坐标（地面高度）
     * 
     * @param world 世界
     * @param x X坐标
     * @param z Z坐标
     * @return Y坐标
     */
    public static int getSafeY(World world, int x, int z) {
        int y = world.getHighestBlockYAt(x, z);
        
        // 确保不会生成在虚空中
        if (y < 1) {
            y = 64; // 默认海平面高度
        }
        
        return y + 1; // 在地面上方一格
    }
    
    /**
     * 格式化位置信息
     * 
     * @param location 位置
     * @return 格式化的字符串
     */
    public static String formatLocation(Location location) {
        if (location == null || location.getWorld() == null) {
            return "未知位置";
        }
        
        return String.format("%s (%.1f, %.1f, %.1f)", 
            location.getWorld().getName(),
            location.getX(),
            location.getY(),
            location.getZ()
        );
    }
    
    /**
     * 计算两个位置之间的距离
     * 
     * @param loc1 位置1
     * @param loc2 位置2
     * @return 距离
     */
    public static double distance(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null) {
            return Double.MAX_VALUE;
        }
        
        if (loc1.getWorld() != loc2.getWorld()) {
            return Double.MAX_VALUE; // 不同世界
        }
        
        return loc1.distance(loc2);
    }
    
    /**
     * 检查位置是否在世界边界内
     * 
     * @param location 位置
     * @return 是否在边界内
     */
    public static boolean isInsideBorder(Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }
        
        return location.getWorld().getWorldBorder().isInside(location);
    }
}
