package com.minecraft.huntergame.tracker;

import com.minecraft.huntergame.game.ManhuntGame;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
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
     * 更新指南针指向（自动选择最近目标）
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
        
        // 更新指南针指向（使用Lodestone机制）
        updateCompassLodestone(hunter, target.getLocation());
        
        // 更新指南针显示信息
        updateCompassDisplay(hunter, target);
        
        // 更新时间
        lastUpdateTime = System.currentTimeMillis();
    }
    
    /**
     * 切换到下一个目标（手动切换）
     */
    public void switchToNextTarget(Player hunter) {
        List<UUID> runners = game.getAliveRunners();
        if (runners.isEmpty()) {
            targetRunnerUUID = null;
            updateCompassDisplay(hunter, null);
            return;
        }
        
        // 如果只有一个目标，不需要切换
        if (runners.size() == 1) {
            targetRunnerUUID = runners.get(0);
            Player target = Bukkit.getPlayer(targetRunnerUUID);
            if (target != null && target.isOnline()) {
                updateCompassLodestone(hunter, target.getLocation());
                updateCompassDisplay(hunter, target);
            }
            hunter.sendMessage(ChatColor.YELLOW + "只有一个逃亡者，无需切换");
            return;
        }
        
        // 找到当前目标在列表中的位置
        int currentIndex = -1;
        if (targetRunnerUUID != null) {
            currentIndex = runners.indexOf(targetRunnerUUID);
        }
        
        // 切换到下一个目标
        int nextIndex = (currentIndex + 1) % runners.size();
        targetRunnerUUID = runners.get(nextIndex);
        
        Player target = Bukkit.getPlayer(targetRunnerUUID);
        if (target != null && target.isOnline()) {
            updateCompassLodestone(hunter, target.getLocation());
            updateCompassDisplay(hunter, target);
            hunter.sendMessage(ChatColor.GREEN + "已切换追踪目标: " + target.getName());
        }
        
        // 更新时间
        lastUpdateTime = System.currentTimeMillis();
    }
    
    /**
     * 使用Lodestone机制更新指南针指向
     */
    private void updateCompassLodestone(Player hunter, Location targetLocation) {
        ItemStack compass = findCompassInInventory(hunter);
        if (compass == null || compass.getType() != Material.COMPASS) {
            return;
        }
        
        ItemMeta meta = compass.getItemMeta();
        if (!(meta instanceof CompassMeta)) {
            return;
        }
        
        CompassMeta compassMeta = (CompassMeta) meta;
        
        // 设置Lodestone位置（即使没有实际的Lodestone方块）
        compassMeta.setLodestone(targetLocation);
        // 设置为追踪Lodestone
        compassMeta.setLodestoneTracked(false); // false表示不需要实际的Lodestone方块
        
        compass.setItemMeta(compassMeta);
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
        if (!(meta instanceof CompassMeta)) {
            return;
        }
        
        CompassMeta compassMeta = (CompassMeta) meta;
        
        if (target == null) {
            compassMeta.setDisplayName(ChatColor.RED + "追踪指南针 - 无目标");
            compassMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "右键更新目标",
                ChatColor.RED + "当前无存活逃亡者"
            ));
            // 清除Lodestone追踪
            compassMeta.setLodestone(null);
            compassMeta.setLodestoneTracked(false);
        } else {
            String dimension = getDimensionName(target.getWorld());
            int distance = calculateDistance(hunter.getLocation(), target.getLocation());
            
            String distanceStr = distance >= 0 ? distance + "方块" : "跨维度";
            
            compassMeta.setDisplayName(ChatColor.GREEN + "追踪指南针 - " + target.getName());
            compassMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "右键更新目标",
                ChatColor.GRAY + "潜行+右键切换目标",
                ChatColor.YELLOW + "维度: " + dimension,
                ChatColor.YELLOW + "距离: " + distanceStr,
                ChatColor.GRAY + "自动追踪中..."
            ));
        }
        
        compass.setItemMeta(compassMeta);
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
