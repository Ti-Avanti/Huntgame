package com.minecraft.huntergame.gui;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.game.GameState;
import com.minecraft.huntergame.game.ManhuntGame;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 游戏大厅GUI
 * 显示所有可加入的游戏房间
 * 
 * @author YourName
 * @version 1.0.0
 */
public class GameLobbyGUI extends BaseGUI {
    
    public GameLobbyGUI(HunterGame plugin, Player player) {
        super(plugin, player);
        
        // 启用自动刷新（每2秒）
        enableAutoRefresh(40);
    }
    
    @Override
    protected String getTitle() {
        return "§6§l游戏大厅";
    }
    
    @Override
    protected int getSize() {
        return 54;
    }
    
    @Override
    protected void buildContent() {
        // 获取所有游戏
        Collection<ManhuntGame> games = plugin.getManhuntManager().getAllGames();
        
        int slot = 10;
        for (ManhuntGame game : games) {
            if (slot >= 44) break; // 最多显示28个房间
            
            // 跳过已结束的游戏
            if (game.getState() == GameState.ENDING || 
                game.getState() == GameState.RESTARTING ||
                game.getState() == GameState.DISABLED) {
                continue;
            }
            
            ItemStack item = createGameItem(game);
            setItem(slot, item);
            
            slot++;
            if (slot % 9 == 8) {
                slot += 2; // 跳到下一行
            }
        }
        
        // 创建房间按钮
        ItemStack createButton = GUIUtils.createItem(
            Material.EMERALD,
            "§a§l创建房间",
            "§7点击创建新的游戏房间"
        );
        setItem(49, createButton);
        
        // 刷新按钮
        ItemStack refreshButton = GUIUtils.createItem(
            Material.ARROW,
            "§e§l刷新列表",
            "§7点击刷新房间列表"
        );
        setItem(53, refreshButton);
    }
    
    @Override
    public boolean handleClick(int slot, ItemStack item) {
        // 创建房间按钮
        if (slot == 49) {
            plugin.getGUIManager().openCreateRoom(player);
            return true;
        }
        
        // 刷新按钮
        if (slot == 53) {
            refresh();
            return true;
        }
        
        // 房间物品点击
        if (item != null && isGameItem(item)) {
            handleGameItemClick(item);
            return true;
        }
        
        return true;
    }
    
    /**
     * 检查是否是游戏物品
     */
    private boolean isGameItem(ItemStack item) {
        if (item.getType() == Material.LIME_WOOL ||
            item.getType() == Material.YELLOW_WOOL ||
            item.getType() == Material.ORANGE_WOOL ||
            item.getType() == Material.RED_WOOL ||
            item.getType() == Material.GRAY_WOOL) {
            return item.hasItemMeta() && item.getItemMeta().hasDisplayName();
        }
        return false;
    }
    
    /**
     * 处理游戏物品点击
     */
    private void handleGameItemClick(ItemStack item) {
        // 从显示名称提取游戏ID
        String displayName = item.getItemMeta().getDisplayName();
        String gameId = displayName.replace("§6§l房间: §f", "");
        
        ManhuntGame game = plugin.getManhuntManager().getGame(gameId);
        if (game != null) {
            plugin.getGUIManager().openRoomDetail(player, game);
        } else {
            player.sendMessage("§c房间不存在或已关闭");
            refresh();
        }
    }
    
    /**
     * 创建游戏物品
     */
    private ItemStack createGameItem(ManhuntGame game) {
        Material material;
        String statusColor;
        
        // 根据游戏状态选择材质和颜色
        switch (game.getState()) {
            case WAITING:
                material = Material.LIME_WOOL;
                statusColor = "§a";
                break;
            case MATCHING:
                material = Material.YELLOW_WOOL;
                statusColor = "§e";
                break;
            case STARTING:
            case PREPARING:
                material = Material.ORANGE_WOOL;
                statusColor = "§6";
                break;
            case PLAYING:
                material = Material.RED_WOOL;
                statusColor = "§c";
                break;
            default:
                material = Material.GRAY_WOOL;
                statusColor = "§7";
                break;
        }
        
        List<String> lore = new ArrayList<>();
        lore.add("§7状态: " + statusColor + game.getState().getDisplayNameZh());
        lore.add("§7玩家: §f" + game.getPlayerCount() + "§7/§f" + (game.getMaxRunners() + game.getMaxHunters()));
        lore.add("§7地图: §f" + game.getWorldName());
        
        // 根据状态添加额外信息
        if (game.getState() == GameState.MATCHING) {
            long remaining = game.getMatchingRemainingTime();
            lore.add("§7匹配倒计时: §e" + remaining + "秒");
        }
        
        lore.add("");
        
        // 添加操作提示
        if (game.getState().isJoinable()) {
            if (game.isFull()) {
                lore.add("§c房间已满");
            } else {
                lore.add("§a点击加入游戏");
            }
        } else {
            lore.add("§c游戏进行中");
        }
        
        return GUIUtils.createItem(material, "§6§l房间: §f" + game.getGameId(), lore);
    }
}
