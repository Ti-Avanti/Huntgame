package com.minecraft.huntergame.tracker;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.game.ManhuntGame;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 追踪管理器
 * 管理所有猎人的追踪指南针
 * 
 * @author YourName
 * @version 1.0.0
 */
public class TrackerManager {
    
    private final HunterGame plugin;
    private final Map<UUID, TrackerCompass> compasses;  // 猎人UUID -> 追踪指南针
    private final int updateCooldown;  // 更新冷却时间(秒)
    private final boolean autoUpdate;  // 是否自动更新
    private final int autoUpdateInterval;  // 自动更新间隔(秒)
    
    public TrackerManager(HunterGame plugin) {
        this.plugin = plugin;
        this.compasses = new ConcurrentHashMap<>();
        this.updateCooldown = plugin.getManhuntConfig().getCompassCooldown();
        this.autoUpdate = plugin.getManhuntConfig().isAutoUpdateCompass();
        this.autoUpdateInterval = plugin.getManhuntConfig().getAutoUpdateInterval();
        
        // 启动自动更新任务
        if (autoUpdate) {
            startAutoUpdateTask();
        }
        
        plugin.getLogger().info("追踪管理器已初始化 (冷却: " + updateCooldown + "秒, 自动更新: " + autoUpdate + ")");
    }
    
    /**
     * 为猎人创建追踪指南针
     */
    public void giveTrackerCompass(Player hunter, ManhuntGame game) {
        // 创建指南针物品
        ItemStack compass = new ItemStack(Material.COMPASS);
        org.bukkit.inventory.meta.CompassMeta meta = (org.bukkit.inventory.meta.CompassMeta) compass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + "追踪指南针");
            meta.setLore(Arrays.asList(
                ChatColor.GRAY + "右键更新目标",
                ChatColor.GRAY + "潜行+右键切换目标",
                ChatColor.GRAY + "指向最近的逃亡者",
                ChatColor.GRAY + "使用Lodestone追踪技术"
            ));
            // 初始化Lodestone追踪（但不设置位置）
            meta.setLodestoneTracked(false);
            compass.setItemMeta(meta);
        }
        
        // 给予玩家
        hunter.getInventory().setItem(0, compass);
        
        // 创建追踪器对象
        TrackerCompass tracker = new TrackerCompass(hunter.getUniqueId(), game, updateCooldown);
        compasses.put(hunter.getUniqueId(), tracker);
        
        // 立即更新一次目标
        tracker.updateTarget(hunter);
        
        plugin.getLogger().info("给予玩家 " + hunter.getName() + " 追踪指南针（Lodestone模式）");
    }
    
    /**
     * 移除猎人的追踪指南针
     */
    public void removeTrackerCompass(UUID hunterUUID) {
        compasses.remove(hunterUUID);
        
        Player hunter = Bukkit.getPlayer(hunterUUID);
        if (hunter != null && hunter.isOnline()) {
            // 移除背包中的指南针
            hunter.getInventory().remove(Material.COMPASS);
        }
    }
    
    /**
     * 处理指南针右键点击
     */
    public void handleCompassClick(Player hunter) {
        TrackerCompass tracker = compasses.get(hunter.getUniqueId());
        if (tracker == null) {
            hunter.sendMessage(ChatColor.RED + "你没有追踪指南针");
            return;
        }
        
        // 检查是否潜行（切换目标）
        if (hunter.isSneaking()) {
            // 切换到下一个目标
            tracker.switchToNextTarget(hunter);
            return;
        }
        
        // 检查冷却
        if (tracker.isOnCooldown()) {
            hunter.sendMessage(ChatColor.RED + "冷却中，请等待 " + 
                tracker.getRemainingCooldown() + " 秒");
            return;
        }
        
        // 更新目标（自动选择最近的）
        tracker.updateTarget(hunter);
        
        hunter.sendMessage(ChatColor.GREEN + "已更新追踪目标");
    }
    
    /**
     * 自动更新指南针
     */
    public void autoUpdateCompass(UUID hunterUUID) {
        TrackerCompass tracker = compasses.get(hunterUUID);
        if (tracker == null) {
            return;
        }
        
        Player hunter = Bukkit.getPlayer(hunterUUID);
        if (hunter == null || !hunter.isOnline()) {
            return;
        }
        
        // 检查玩家背包中是否有指南针（不仅限于手持）
        ItemStack mainHand = hunter.getInventory().getItemInMainHand();
        ItemStack offHand = hunter.getInventory().getItemInOffHand();
        ItemStack slot0 = hunter.getInventory().getItem(0);
        
        boolean hasCompass = (mainHand != null && mainHand.getType() == Material.COMPASS) ||
                            (offHand != null && offHand.getType() == Material.COMPASS) ||
                            (slot0 != null && slot0.getType() == Material.COMPASS);
        
        // 只有当玩家有指南针时才更新（避免不必要的计算）
        if (!hasCompass) {
            return;
        }
        
        // 自动更新不受冷却限制（Lodestone模式下需要持续更新）
        tracker.updateTarget(hunter);
    }
    
    /**
     * 更新所有指南针
     */
    public void updateAllCompasses() {
        // 使用并行流提高性能（如果追踪器数量较多）
        if (compasses.size() > com.minecraft.huntergame.util.Constants.PARALLEL_PROCESSING_THRESHOLD) {
            compasses.keySet().parallelStream().forEach(this::autoUpdateCompass);
        } else {
            compasses.keySet().forEach(this::autoUpdateCompass);
        }
    }
    
    /**
     * 启动自动更新任务
     */
    private void startAutoUpdateTask() {
        // 使用更短的更新间隔（1秒 = 20 ticks）以实现实时追踪
        long interval = 20L; // 每秒更新一次
        
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            updateAllCompasses();
        }, interval, interval);
        
        plugin.getLogger().info("追踪指南针自动更新任务已启动 (Lodestone模式，间隔: 1秒)");
    }
    
    /**
     * 获取追踪器
     */
    public TrackerCompass getTracker(UUID hunterUUID) {
        return compasses.get(hunterUUID);
    }
    
    /**
     * 检查玩家是否有追踪指南针
     */
    public boolean hasTracker(UUID hunterUUID) {
        return compasses.containsKey(hunterUUID);
    }
    
    /**
     * 清理所有追踪器
     */
    public void clearAll() {
        for (UUID hunterUUID : compasses.keySet()) {
            removeTrackerCompass(hunterUUID);
        }
        compasses.clear();
    }
    
    /**
     * 关闭管理器
     */
    public void shutdown() {
        clearAll();
        plugin.getLogger().info("追踪管理器已关闭");
    }
}
