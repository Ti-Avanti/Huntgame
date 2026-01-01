package com.minecraft.huntergame.gui;

import com.minecraft.huntergame.HunterGame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Iterator;

/**
 * GUI管理器
 * 管理所有GUI实例和玩家打开的GUI
 * 
 * @author YourName
 * @version 1.0.0
 */
public class GUIManager {
    
    private final HunterGame plugin;
    
    // 存储玩家当前打开的GUI（使用BaseGUI）
    private final Map<UUID, BaseGUI> openGUIs;
    
    // 反向索引：Inventory -> UUID（优化isGUI性能）
    private final Map<Inventory, UUID> inventoryIndex;
    
    // 清理任务ID
    private int cleanupTaskId = -1;
    
    public GUIManager(HunterGame plugin) {
        this.plugin = plugin;
        this.openGUIs = new HashMap<>();
        this.inventoryIndex = new HashMap<>();
        
        // 启动定期清理任务（每5秒）
        startCleanupTask();
    }
    
    /**
     * 注册GUI
     */
    public void registerGUI(Player player, BaseGUI gui) {
        UUID uuid = player.getUniqueId();
        
        // 如果玩家已有打开的GUI，先关闭
        if (openGUIs.containsKey(uuid)) {
            BaseGUI oldGUI = openGUIs.get(uuid);
            if (oldGUI != null && oldGUI.getInventory() != null) {
                inventoryIndex.remove(oldGUI.getInventory());
            }
        }
        
        // 注册新GUI
        openGUIs.put(uuid, gui);
        if (gui.getInventory() != null) {
            inventoryIndex.put(gui.getInventory(), uuid);
        }
    }
    
    /**
     * 注销GUI
     */
    public void unregisterGUI(Player player) {
        UUID uuid = player.getUniqueId();
        BaseGUI gui = openGUIs.remove(uuid);
        
        if (gui != null && gui.getInventory() != null) {
            inventoryIndex.remove(gui.getInventory());
        }
    }
    
    /**
     * 打开游戏大厅GUI（Bungee模式）
     */
    public void openGameLobby(Player player) {
        GameLobbyGUI gui = new GameLobbyGUI(plugin, player);
        gui.open();
    }
    
    /**
     * 打开匹配GUI（单服务器模式）
     */
    public void openMatchingGUI(Player player, com.minecraft.huntergame.game.ManhuntGame game) {
        MatchingGUI gui = new MatchingGUI(plugin, player, game);
        gui.open();
    }
    
    /**
     * 根据服务器模式打开合适的GUI
     */
    public void openGUI(Player player) {
        if (plugin.getServerMode() == com.minecraft.huntergame.ServerMode.STANDALONE) {
            // 单服务器模式：打开匹配GUI
            // 获取玩家所在的游戏，如果没有则获取第一个可加入的游戏
            com.minecraft.huntergame.game.ManhuntGame game = plugin.getManhuntManager().getPlayerGame(player);
            
            if (game == null) {
                // 查找第一个可加入的游戏
                for (com.minecraft.huntergame.game.ManhuntGame g : plugin.getManhuntManager().getAllGames()) {
                    if (g.getState().isJoinable() || g.getState() == com.minecraft.huntergame.game.GameState.PREPARING || 
                        g.getState() == com.minecraft.huntergame.game.GameState.PLAYING) {
                        game = g;
                        break;
                    }
                }
            }
            
            if (game != null) {
                openMatchingGUI(player, game);
            } else {
                player.sendMessage("§c当前没有进行中的游戏！");
                player.sendMessage("§e使用 §a/hg join §e加入或创建游戏");
            }
        } else {
            // Bungee模式：打开房间列表GUI
            openGameLobby(player);
        }
    }
    
    /**
     * 打开房间详情GUI
     */
    public void openRoomDetail(Player player, com.minecraft.huntergame.game.ManhuntGame game) {
        RoomDetailGUI gui = new RoomDetailGUI(plugin, player, game);
        gui.open();
    }
    
    /**
     * 打开创建房间GUI
     */
    public void openCreateRoom(Player player) {
        CreateRoomGUI gui = new CreateRoomGUI(plugin, player);
        gui.open();
    }
    
    /**
     * 打开观战菜单GUI
     */
    public void openSpectatorMenu(Player player, com.minecraft.huntergame.game.ManhuntGame game) {
        SpectatorMenuGUI gui = new SpectatorMenuGUI(plugin, player, game);
        gui.open();
    }
    
    /**
     * 获取玩家当前打开的GUI
     */
    public BaseGUI getOpenGUI(Player player) {
        return openGUIs.get(player.getUniqueId());
    }
    
    /**
     * 获取玩家当前打开的游戏大厅GUI
     */
    public GameLobbyGUI getGameLobbyGUI(Player player) {
        BaseGUI gui = openGUIs.get(player.getUniqueId());
        return gui instanceof GameLobbyGUI ? (GameLobbyGUI) gui : null;
    }
    
    /**
     * 获取玩家当前打开的房间详情GUI
     */
    public RoomDetailGUI getRoomDetailGUI(Player player) {
        BaseGUI gui = openGUIs.get(player.getUniqueId());
        return gui instanceof RoomDetailGUI ? (RoomDetailGUI) gui : null;
    }
    
    /**
     * 获取玩家当前打开的创建房间GUI
     */
    public CreateRoomGUI getCreateRoomGUI(Player player) {
        BaseGUI gui = openGUIs.get(player.getUniqueId());
        return gui instanceof CreateRoomGUI ? (CreateRoomGUI) gui : null;
    }
    
    /**
     * 获取玩家当前打开的匹配GUI
     */
    public MatchingGUI getMatchingGUI(Player player) {
        BaseGUI gui = openGUIs.get(player.getUniqueId());
        return gui instanceof MatchingGUI ? (MatchingGUI) gui : null;
    }
    
    /**
     * 检查Inventory是否是GUI（优化版本）
     */
    public boolean isGUI(Inventory inventory) {
        return inventory != null && inventoryIndex.containsKey(inventory);
    }
    
    /**
     * 移除玩家的GUI记录（兼容旧代码）
     */
    public void removeGUI(Player player) {
        unregisterGUI(player);
    }
    
    /**
     * 启动清理任务
     */
    private void startCleanupTask() {
        cleanupTaskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(
            plugin,
            this::cleanupOfflinePlayers,
            100L, // 5秒后开始
            100L  // 每5秒执行一次
        );
    }
    
    /**
     * 清理离线玩家的GUI
     */
    private void cleanupOfflinePlayers() {
        Iterator<Map.Entry<UUID, BaseGUI>> iterator = openGUIs.entrySet().iterator();
        
        while (iterator.hasNext()) {
            Map.Entry<UUID, BaseGUI> entry = iterator.next();
            Player player = plugin.getServer().getPlayer(entry.getKey());
            
            // 如果玩家离线或不在线，清理GUI
            if (player == null || !player.isOnline()) {
                BaseGUI gui = entry.getValue();
                if (gui != null && gui.getInventory() != null) {
                    inventoryIndex.remove(gui.getInventory());
                }
                iterator.remove();
            }
        }
    }
    
    /**
     * 清理所有GUI
     */
    public void clearAll() {
        // 停止所有GUI的自动刷新
        for (BaseGUI gui : openGUIs.values()) {
            if (gui != null && gui.isAutoRefresh()) {
                gui.disableAutoRefresh();
            }
        }
        
        openGUIs.clear();
        inventoryIndex.clear();
    }
    
    /**
     * 关闭管理器
     */
    public void shutdown() {
        // 停止清理任务
        if (cleanupTaskId != -1) {
            plugin.getServer().getScheduler().cancelTask(cleanupTaskId);
            cleanupTaskId = -1;
        }
        
        // 清理所有GUI
        clearAll();
    }
}
