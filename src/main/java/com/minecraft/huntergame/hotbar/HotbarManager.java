package com.minecraft.huntergame.hotbar;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.game.ManhuntGame;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * Hotbar快捷道具管理器
 * 为玩家提供快捷操作道具
 * 
 * @author YourName
 * @version 1.0.0
 */
public class HotbarManager {
    
    private final HunterGame plugin;
    
    // 道具槽位
    private static final int SLOT_JOIN_GAME = 0;      // 加入游戏
    private static final int SLOT_STATS = 2;          // 查看统计
    private static final int SLOT_LEAVE_ROOM = 4;     // 离开房间
    
    public HotbarManager(HunterGame plugin) {
        this.plugin = plugin;
        plugin.getLogger().info("Hotbar管理器已初始化");
    }
    
    // ==================== 大厅状态道具 ====================
    
    /**
     * 给予大厅道具
     */
    public void giveLobbyItems(Player player) {
        player.getInventory().clear();
        
        // 加入游戏
        ItemStack joinGame = createItem(
            Material.COMPASS,
            "§a§l加入游戏",
            "§7点击打开游戏大厅",
            "§7选择或创建游戏房间"
        );
        player.getInventory().setItem(SLOT_JOIN_GAME, joinGame);
        
        // 查看统计
        ItemStack stats = createItem(
            Material.BOOK,
            "§e§l个人统计",
            "§7点击查看你的游戏数据",
            "§7包括胜率、击杀等信息"
        );
        player.getInventory().setItem(SLOT_STATS, stats);
        
        player.updateInventory();
    }
    
    // ==================== 匹配状态道具 ====================
    
    /**
     * 给予匹配道具
     */
    public void giveMatchingItems(Player player, ManhuntGame game) {
        player.getInventory().clear();
        
        // 离开房间
        ItemStack leaveRoom = createItem(
            Material.RED_BED,
            "§c§l离开房间",
            "§7点击离开当前房间",
            "§7返回游戏大厅"
        );
        player.getInventory().setItem(SLOT_LEAVE_ROOM, leaveRoom);
        
        player.updateInventory();
    }
    
    // ==================== 工具方法 ====================
    
    /**
     * 创建道具
     */
    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore.length > 0) {
                meta.setLore(Arrays.asList(lore));
            }
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * 检查是否是快捷道具
     */
    public boolean isHotbarItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }
        
        String name = meta.getDisplayName();
        return name.contains("加入游戏") || 
               name.contains("个人统计") ||
               name.contains("离开房间");
    }
    
    /**
     * 处理快捷道具点击
     */
    public void handleHotbarClick(Player player, ItemStack item) {
        if (!isHotbarItem(item)) {
            return;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        
        String name = meta.getDisplayName();
        
        if (name.contains("加入游戏")) {
            // 直接加入或创建游戏进行匹配
            handleJoinGame(player);
        } else if (name.contains("个人统计")) {
            // 执行统计命令
            player.performCommand("manhunt stats");
        } else if (name.contains("离开房间")) {
            // 离开游戏
            ManhuntGame game = plugin.getManhuntManager().getPlayerGame(player);
            if (game != null) {
                plugin.getManhuntManager().leaveGame(player);
                player.sendMessage("§a你已离开游戏房间");
                
                // 给予大厅道具
                giveLobbyItems(player);
            }
        }
    }
    
    /**
     * 清除玩家道具
     */
    public void clearItems(Player player) {
        player.getInventory().clear();
        player.updateInventory();
    }
    
    /**
     * 处理加入游戏
     */
    private void handleJoinGame(Player player) {
        // 检查玩家是否已在游戏中
        if (plugin.getManhuntManager().isInGame(player)) {
            player.sendMessage("§c你已经在游戏中了！");
            return;
        }
        
        // 调试日志
        plugin.getLogger().info("[DEBUG] 玩家 " + player.getName() + " 尝试加入游戏");
        plugin.getLogger().info("[DEBUG] 服务器模式: " + plugin.getServerMode());
        
        // Bungee 模式处理
        if (plugin.getServerMode() == com.minecraft.huntergame.ServerMode.BUNGEE) {
            com.minecraft.huntergame.config.ServerType serverType = plugin.getManhuntConfig().getServerType();
            plugin.getLogger().info("[DEBUG] 服务器类型: " + serverType);
            
            // 主大厅服务器：传送玩家到子大厅
            if (serverType == com.minecraft.huntergame.config.ServerType.MAIN_LOBBY) {
                plugin.getLogger().info("[DEBUG] 主大厅服务器 - 准备传送玩家到子大厅");
                player.sendMessage("§e正在为你寻找可用的游戏服务器...");
                
                // 检查 BungeeManager 是否存在
                if (plugin.getBungeeManager() == null) {
                    plugin.getLogger().severe("[ERROR] BungeeManager 未初始化！");
                    player.sendMessage("§c系统错误：Bungee管理器未初始化");
                    return;
                }
                
                // 使用负载均衡器传送到最佳子大厅
                boolean success = plugin.getBungeeManager().sendPlayerToBestServer(player);
                plugin.getLogger().info("[DEBUG] 传送结果: " + success);
                
                if (!success) {
                    player.sendMessage("§c当前没有可用的游戏服务器，请稍后重试！");
                }
                return;
            }
            
            // 子大厅服务器：自动加入游戏进行匹配
            if (serverType == com.minecraft.huntergame.config.ServerType.SUB_LOBBY) {
                plugin.getLogger().info("[DEBUG] 子大厅服务器 - 自动加入游戏");
                
                // 查找可加入的游戏
                ManhuntGame availableGame = null;
                
                for (ManhuntGame game : plugin.getManhuntManager().getAllGames()) {
                    // 只加入等待或匹配状态的游戏
                    if (game.getState() == com.minecraft.huntergame.game.GameState.WAITING ||
                        game.getState() == com.minecraft.huntergame.game.GameState.MATCHING) {
                        
                        // 检查游戏是否未满
                        if (!game.isFull()) {
                            availableGame = game;
                            plugin.getLogger().info("[DEBUG] 找到可加入的游戏: " + game.getGameId());
                            break;
                        }
                    }
                }
                
                // 如果没有可用游戏，创建新游戏
                if (availableGame == null) {
                    if (!plugin.getManhuntManager().canCreateGame()) {
                        player.sendMessage("§c当前无法创建游戏！");
                        plugin.getLogger().warning("[DEBUG] 无法创建游戏");
                        return;
                    }
                    
                    // 获取默认世界名称
                    String worldName = plugin.getManhuntConfig().getWorldName();
                    if (worldName == null || worldName.isEmpty()) {
                        worldName = "manhunt_world";
                    }
                    
                    // 创建新游戏
                    availableGame = plugin.getManhuntManager().createGame(worldName);
                    
                    if (availableGame == null) {
                        player.sendMessage("§c创建游戏失败！");
                        plugin.getLogger().severe("[ERROR] 创建游戏失败");
                        return;
                    }
                    
                    plugin.getLogger().info("[DEBUG] 创建了新游戏: " + availableGame.getGameId());
                    player.sendMessage("§a创建了新的游戏房间");
                }
                
                // 加入游戏
                boolean joined = plugin.getManhuntManager().joinGame(player, availableGame.getGameId());
                
                if (joined) {
                    player.sendMessage("§a成功加入游戏！");
                    player.sendMessage("§e等待其他玩家加入...");
                    plugin.getLogger().info("[DEBUG] 玩家 " + player.getName() + " 成功加入游戏 " + availableGame.getGameId());
                } else {
                    player.sendMessage("§c加入游戏失败！");
                    plugin.getLogger().warning("[DEBUG] 玩家 " + player.getName() + " 加入游戏失败");
                }
                
                return;
            }
            
            // 如果不是 MAIN_LOBBY 也不是 SUB_LOBBY，记录警告
            plugin.getLogger().warning("[WARN] 未知的服务器类型: " + serverType);
            player.sendMessage("§c服务器配置错误，请联系管理员");
            return;
        }
        
        // 单服务器模式：查找可加入的游戏
        ManhuntGame availableGame = null;
        
        for (ManhuntGame game : plugin.getManhuntManager().getAllGames()) {
            // 只加入等待或匹配状态的游戏
            if (game.getState() == com.minecraft.huntergame.game.GameState.WAITING ||
                game.getState() == com.minecraft.huntergame.game.GameState.MATCHING) {
                
                // 检查游戏是否未满
                if (!game.isFull()) {
                    availableGame = game;
                    break;
                }
            }
        }
        
        // 如果没有可用游戏，提示无空闲房间
        if (availableGame == null) {
            player.sendMessage("§c当前没有空闲的游戏房间！");
            player.sendMessage("§e所有房间都已开始游戏，请稍后再试或观战游戏");
            return;
        }
        
        // 加入游戏
        boolean joined = plugin.getManhuntManager().joinGame(player, availableGame.getGameId());
        
        if (joined) {
            player.sendMessage("§a成功加入游戏！");
            player.sendMessage("§e等待其他玩家加入...");
        } else {
            player.sendMessage("§c加入游戏失败！");
        }
    }
}
