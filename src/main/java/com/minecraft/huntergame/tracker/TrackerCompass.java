package com.minecraft.huntergame.tracker;

import com.minecraft.huntergame.game.ManhuntGame;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 追踪指南针类
 * 猎人使用的追踪指南针，指向最近的逃亡者
 * 
 * @author YourName
 * @version 1.0.0
 */
public class TrackerCompass {
    
    private final UUID hunterUUID;
    private final ManhuntGame game;
    private UUID targetRunnerUUID;  // 当前追踪目标
    private long lastUpdateTime;    // 上次更新时间
    private int cooldown;           // 冷却时间(秒)
    
    public TrackerCompass(UUID hunterUUID, ManhuntGame game, int cooldown) {
        this.hunterUUID = hunterUUID;
        this.game = game;
        this.cooldown = cooldown;
        this.lastUpdateTime = 0;
    }
    
    /**
     * 更新指南针指向
     */
    public void updateTarget(Player hunter) {
        // 查找最近的存活逃亡者
        UUID nearest = findNearestRunner(hunter);
        
        if (nearest == null) {
            // 无目标
            targetRunnerUUID = null;
            updateCompassDisplay(hunter, null);
            return;
        }
        
        targetRunnerUUID = nearest;
        Player target = Bukkit.getPlayer(nearest);
        
        if (target == null || !target.isOnline()) {
            return;
        }
        
        // 更新指南针指向
        hunter.setCompassTarget(target.getLocation());
        
        // 更新指南针显示信息
        updateCompassDisplay(hunter, target);
        
        // 更新时间
        lastUpdateTime = System.currentTimeMillis();
    }
    
    /**
     * 更新指南针显示信息
     */
    private void updateCompassDisplay(Player hunter, Player target) {
        ItemStack compass = findCompassInInventory(hunter);
        if (compass == null) {
            return;
        }
        
        ItemMeta meta = compass.getItemMeta();
        if (meta == null) {
            return;
        }
        
        if (target == null) {
            meta.setDisplayName(ChatColor.RED + "追踪指南针 - 无目标");
            meta.setLore(Arrays.asList(
                ChatColor.GRAY + "右键更新目标",
                ChatColor.RED + "当前无存活逃亡者"
            ));
        } else {
            String dimension = getDimensionName(target.getWorld());
            int distance = calculateDistance(hunter.getLocation(), target.getLocation());
            
            meta.setDisplayName(ChatColor.GREEN + "追踪指南针 - " + target.getName());
            meta.setLore(Arrays.asList(
                ChatColor.GRAY + "右键更新目标",
                ChatColor.YELLOW + "维度: " + dimension,
                ChatColor.YELLOW + "距离: " + distance + "方块"
            ));
        }
        
        compass.setItemMeta(meta);
    }
    
    /**
     * 在玩家背包中查找指南针
     */
    private ItemStack findCompassInInventory(Player player) {
        // 优先检查主手
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand != null && mainHand.getType() == Material.COMPASS) {
            return mainHand;
        }
        
        // 检查副手
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (offHand != null && offHand.getType() == Material.COMPASS) {
            return offHand;
        }
        
        // 检查背包第一格（默认位置）
        ItemStack slot0 = player.getInventory().getItem(0);
        if (slot0 != null && slot0.getType() == Material.COMPASS) {
            return slot0;
        }
        
        return null;
    }
    
    /**
     * 查找最近的逃亡者
     */
    private UUID findNearestRunner(Player hunter) {
        List<UUID> runners = game.getAliveRunners();
        if (runners.isEmpty()) {
            return null;
        }
        
        UUID nearest = null;
        double minDistance = Double.MAX_VALUE;
        
        for (UUID runnerUUID : runners) {
            Player runner = Bukkit.getPlayer(runnerUUID);
            if (runner == null || !runner.isOnline()) {
                continue;
            }
            
            // 跨维度距离计算
            double distance = calculateCrossDimensionDistance(
                hunter.getLocation(), 
                runner.getLocation()
            );
            
            if (distance < minDistance) {
                minDistance = distance;
                nearest = runnerUUID;
            }
        }
        
        return nearest;
    }
    
    /**
     * 跨维度距离计算
     */
    private double calculateCrossDimensionDistance(Location loc1, Location loc2) {
        if (loc1.getWorld().equals(loc2.getWorld())) {
            return loc1.distance(loc2);
        }
        
        // 不同维度，返回一个很大的值表示无法直接到达
        return com.minecraft.huntergame.util.Constants.CROSS_DIMENSION_DISTANCE;
    }
    
    /**
     * 计算距离（整数）
     */
    private int calculateDistance(Location loc1, Location loc2) {
        if (!loc1.getWorld().equals(loc2.getWorld())) {
            return -1; // 不同维度
        }
        return (int) loc1.distance(loc2);
    }
    
    /**
     * 获取维度名称
     */
    private String getDimensionName(World world) {
        switch (world.getEnvironment()) {
            case NORMAL:
                return "主世界";
            case NETHER:
                return "下界";
            case THE_END:
                return "末地";
            default:
                return "未知";
        }
    }
    
    /**
     * 检查是否在冷却中
     */
    public boolean isOnCooldown() {
        if (lastUpdateTime == 0) {
            return false;
        }
        
        long elapsed = (System.currentTimeMillis() - lastUpdateTime) / 1000;
        return elapsed < cooldown;
    }
    
    /**
     * 获取剩余冷却时间(秒)
     */
    public int getRemainingCooldown() {
        if (!isOnCooldown()) {
            return 0;
        }
        
        long elapsed = (System.currentTimeMillis() - lastUpdateTime) / 1000;
        return (int) Math.max(0, cooldown - elapsed);
    }
    
    /**
     * 重置冷却时间
     */
    public void resetCooldown() {
        lastUpdateTime = 0;
    }
    
    // ==================== Getter 和 Setter ====================
    
    public UUID getHunterUUID() {
        return hunterUUID;
    }
    
    public UUID getTargetRunnerUUID() {
        return targetRunnerUUID;
    }
    
    public int getCooldown() {
        return cooldown;
    }
    
    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }
}
