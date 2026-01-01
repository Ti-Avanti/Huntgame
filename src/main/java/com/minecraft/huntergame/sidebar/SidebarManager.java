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
    private final Map<UUID, ManhuntSidebar> gameSidebars;
    private final Map<UUID, LobbySidebar> lobbySidebars;
    private final Map<UUID, MatchingSidebar> matchingSidebars;
    
    public SidebarManager(HunterGame plugin) {
        this.plugin = plugin;
        this.gameSidebars = new ConcurrentHashMap<>();
        this.lobbySidebars = new ConcurrentHashMap<>();
        this.matchingSidebars = new ConcurrentHashMap<>();
        
        // 启动更新任务
        startUpdateTask();
        
        plugin.getLogger().info("侧边栏管理器已初始化");
    }
    
    /**
     * 为玩家创建游戏侧边栏
     */
    public void createSidebar(Player player, ManhuntGame game) {
        // 检查是否启用计分板
        if (!plugin.getScoreboardConfig().isEnabled()) {
            return;
        }
        
        // 移除所有旧的侧边栏
        removeAllSidebars(player);
        
        // 创建新侧边栏
        ManhuntSidebar sidebar = new ManhuntSidebar(plugin, game, player);
        gameSidebars.put(player.getUniqueId(), sidebar);
        
        // 立即更新
        sidebar.update();
    }
    
    /**
     * 为玩家创建大厅侧边栏
     */
    public void createLobbySidebar(Player player) {
        // 检查是否启用计分板
        if (!plugin.getScoreboardConfig().isEnabled()) {
            return;
        }
        
        // 检查是否启用大厅计分板
        if (!plugin.getScoreboardConfig().isLobbyEnabled()) {
            return;
        }
        
        // 移除所有旧的侧边栏
        removeAllSidebars(player);
        
        // 创建新侧边栏
        LobbySidebar sidebar = new LobbySidebar(plugin, player);
        lobbySidebars.put(player.getUniqueId(), sidebar);
        
        // 立即更新
        sidebar.update();
    }
    
    /**
     * 为玩家创建匹配侧边栏
     */
    public void createMatchingSidebar(Player player, ManhuntGame game) {
        // 检查是否启用计分板
        if (!plugin.getScoreboardConfig().isEnabled()) {
            return;
        }
        
        // 检查是否启用匹配计分板
        if (!plugin.getScoreboardConfig().isMatchingEnabled()) {
            return;
        }
        
        // 移除所有旧的侧边栏
        removeAllSidebars(player);
        
        // 创建新侧边栏
        MatchingSidebar sidebar = new MatchingSidebar(plugin, game, player);
        matchingSidebars.put(player.getUniqueId(), sidebar);
        
        // 立即更新
        sidebar.update();
    }
    
    /**
     * 移除玩家的所有侧边栏
     */
    private void removeAllSidebars(Player player) {
        removeSidebar(player);
        removeLobbySidebar(player);
        removeMatchingSidebar(player);
    }
    
    /**
     * 移除玩家的游戏侧边栏
     */
    public void removeSidebar(Player player) {
        ManhuntSidebar sidebar = gameSidebars.remove(player.getUniqueId());
        if (sidebar != null) {
            sidebar.remove();
        }
    }
    
    /**
     * 移除玩家的大厅侧边栏
     */
    public void removeLobbySidebar(Player player) {
        LobbySidebar sidebar = lobbySidebars.remove(player.getUniqueId());
        if (sidebar != null) {
            sidebar.remove();
        }
    }
    
    /**
     * 移除玩家的匹配侧边栏
     */
    public void removeMatchingSidebar(Player player) {
        MatchingSidebar sidebar = matchingSidebars.remove(player.getUniqueId());
        if (sidebar != null) {
            sidebar.remove();
        }
    }
    
    /**
     * 更新玩家的侧边栏
     */
    public void updateSidebar(Player player) {
        // 尝试更新游戏侧边栏
        ManhuntSidebar gameSidebar = gameSidebars.get(player.getUniqueId());
        if (gameSidebar != null) {
            gameSidebar.update();
            return;
        }
        
        // 尝试更新大厅侧边栏
        LobbySidebar lobbySidebar = lobbySidebars.get(player.getUniqueId());
        if (lobbySidebar != null) {
            lobbySidebar.update();
            return;
        }
        
        // 尝试更新匹配侧边栏
        MatchingSidebar matchingSidebar = matchingSidebars.get(player.getUniqueId());
        if (matchingSidebar != null) {
            matchingSidebar.update();
        }
    }
    
    /**
     * 更新所有侧边栏
     */
    public void updateAll() {
        // 性能优化：如果没有侧边栏，跳过更新
        if (gameSidebars.isEmpty() && lobbySidebars.isEmpty() && matchingSidebars.isEmpty()) {
            return;
        }
        
        // 更新游戏侧边栏
        if (!gameSidebars.isEmpty()) {
            if (gameSidebars.size() > com.minecraft.huntergame.util.Constants.SIDEBAR_PARALLEL_THRESHOLD) {
                gameSidebars.values().parallelStream().forEach(ManhuntSidebar::update);
            } else {
                gameSidebars.values().forEach(ManhuntSidebar::update);
            }
        }
        
        // 更新大厅侧边栏
        if (!lobbySidebars.isEmpty()) {
            lobbySidebars.values().forEach(LobbySidebar::update);
        }
        
        // 更新匹配侧边栏
        if (!matchingSidebars.isEmpty()) {
            matchingSidebars.values().forEach(MatchingSidebar::update);
        }
    }
    
    /**
     * 启动更新任务
     */
    private void startUpdateTask() {
        // 使用配置的更新间隔
        long interval = plugin.getScoreboardConfig().getUpdateInterval();
        
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            // 性能优化：只在有侧边栏时更新
            if (!gameSidebars.isEmpty() || !lobbySidebars.isEmpty() || !matchingSidebars.isEmpty()) {
                updateAll();
            }
        }, interval, interval);
        
        plugin.getLogger().info("侧边栏更新任务已启动 (间隔: " + (interval / 20.0) + "秒)");
    }
    
    /**
     * 清理所有侧边栏
     */
    public void clearAll() {
        for (UUID uuid : gameSidebars.keySet()) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null) {
                removeSidebar(player);
            }
        }
        for (UUID uuid : lobbySidebars.keySet()) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null) {
                removeLobbySidebar(player);
            }
        }
        for (UUID uuid : matchingSidebars.keySet()) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null) {
                removeMatchingSidebar(player);
            }
        }
        gameSidebars.clear();
        lobbySidebars.clear();
        matchingSidebars.clear();
    }
    
    /**
     * 关闭管理器
     */
    public void shutdown() {
        clearAll();
        plugin.getLogger().info("侧边栏管理器已关闭");
    }
}
