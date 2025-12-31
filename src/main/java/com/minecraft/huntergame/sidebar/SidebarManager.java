package com.minecraft.huntergame.sidebar;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.game.ManhuntGame;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 侧边栏管理器
 * 管理所有玩家的侧边栏显示
 * 
 * @author YourName
 * @version 1.0.0
 */
public class SidebarManager {
    
    private final HunterGame plugin;
    private final Map<UUID, ManhuntSidebar> sidebars;
    
    public SidebarManager(HunterGame plugin) {
        this.plugin = plugin;
        this.sidebars = new ConcurrentHashMap<>();
        
        // 启动更新任务
        startUpdateTask();
        
        plugin.getLogger().info("侧边栏管理器已初始化");
    }
    
    /**
     * 为玩家创建侧边栏
     */
    public void createSidebar(Player player, ManhuntGame game) {
        // 移除旧的侧边栏
        removeSidebar(player);
        
        // 创建新侧边栏
        ManhuntSidebar sidebar = new ManhuntSidebar(plugin, game, player);
        sidebars.put(player.getUniqueId(), sidebar);
        
        // 立即更新
        sidebar.update();
    }
    
    /**
     * 移除玩家的侧边栏
     */
    public void removeSidebar(Player player) {
        ManhuntSidebar sidebar = sidebars.remove(player.getUniqueId());
        if (sidebar != null) {
            sidebar.remove();
        }
    }
    
    /**
     * 更新玩家的侧边栏
     */
    public void updateSidebar(Player player) {
        ManhuntSidebar sidebar = sidebars.get(player.getUniqueId());
        if (sidebar != null) {
            sidebar.update();
        }
    }
    
    /**
     * 更新所有侧边栏
     */
    public void updateAll() {
        // 性能优化：如果没有侧边栏，跳过更新
        if (sidebars.isEmpty()) {
            return;
        }
        
        // 使用并行流提高性能（如果侧边栏数量较多）
        if (sidebars.size() > com.minecraft.huntergame.util.Constants.SIDEBAR_PARALLEL_THRESHOLD) {
            sidebars.values().parallelStream().forEach(ManhuntSidebar::update);
        } else {
            sidebars.values().forEach(ManhuntSidebar::update);
        }
    }
    
    /**
     * 启动更新任务
     */
    private void startUpdateTask() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            // 性能优化：只在有侧边栏时更新
            if (!sidebars.isEmpty()) {
                updateAll();
            }
        }, 20L, 20L); // 每秒更新一次
        
        plugin.getLogger().info("侧边栏更新任务已启动");
    }
    
    /**
     * 清理所有侧边栏
     */
    public void clearAll() {
        for (UUID uuid : sidebars.keySet()) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null) {
                removeSidebar(player);
            }
        }
        sidebars.clear();
    }
    
    /**
     * 关闭管理器
     */
    public void shutdown() {
        clearAll();
        plugin.getLogger().info("侧边栏管理器已关闭");
    }
}
