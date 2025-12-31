package com.minecraft.huntergame.item;

import com.minecraft.huntergame.HunterGame;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * 烟雾弹道具
 * 生成粒子效果遮挡视线
 * 
 * @author YourName
 * @version 1.0.0
 */
public class SmokeBomb extends SurvivorItem {
    
    private final int duration; // 持续时间(秒)
    private final int radius; // 范围(方块)
    
    public SmokeBomb(HunterGame plugin) {
        super(plugin, "smoke-bomb");
        this.material = Material.SNOWBALL;
        this.duration = plugin.getMainConfig().getSmokeBombDuration();
        this.radius = plugin.getMainConfig().getSmokeBombRadius();
        this.maxUses = plugin.getMainConfig().getSmokeBombCount();
        this.consumable = true;
    }
    
    @Override
    protected boolean onUse(Player player) {
        Location location = player.getLocation();
        
        // 启动烟雾效果任务
        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = duration * 20; // 转换为tick
            
            @Override
            public void run() {
                if (ticks >= maxTicks) {
                    this.cancel();
                    return;
                }
                
                // 生成烟雾粒子效果
                spawnSmokeParticles(location);
                
                ticks += 5; // 每5 tick更新一次
            }
        }.runTaskTimer(plugin, 0L, 5L);
        
        // 记录日志
        plugin.getLogger().info("[道具] 玩家 " + player.getName() + " 使用了烟雾弹");
        
        return true;
    }
    
    /**
     * 生成烟雾粒子
     */
    private void spawnSmokeParticles(Location center) {
        if (center.getWorld() == null) {
            return;
        }
        
        // 在范围内生成多个烟雾粒子
        for (int i = 0; i < 50; i++) {
            double offsetX = (Math.random() - 0.5) * radius * 2;
            double offsetY = Math.random() * 3; // 高度0-3方块
            double offsetZ = (Math.random() - 0.5) * radius * 2;
            
            Location particleLocation = center.clone().add(offsetX, offsetY, offsetZ);
            
            // 生成烟雾粒子
            center.getWorld().spawnParticle(
                Particle.CAMPFIRE_COSY_SMOKE,
                particleLocation,
                1, // 数量
                0.1, 0.1, 0.1, // 偏移
                0.01 // 速度
            );
        }
    }
    
    /**
     * 获取持续时间
     */
    public int getDuration() {
        return duration;
    }
    
    /**
     * 获取范围
     */
    public int getRadius() {
        return radius;
    }
}
