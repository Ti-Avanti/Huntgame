package com.minecraft.huntergame.gui;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.game.GameState;
import com.minecraft.huntergame.game.ManhuntGame;
import com.minecraft.huntergame.game.PlayerRole;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 房间详情GUI
 * 显示房间详细信息和玩家列表
 * 
 * @author YourName
 * @version 1.0.0
 */
public class RoomDetailGUI extends BaseGUI {
    
    private final ManhuntGame game;
    
    public RoomDetailGUI(HunterGame plugin, Player player, ManhuntGame game) {
        super(plugin, player);
        this.game = game;
        
        // 启用自动刷新（每2秒）
        enableAutoRefresh(40);
    }
    
    @Override
    protected String getTitle() {
        return "§6§l房间详情: §f" + game.getGameId();
    }
    
    @Override
    protected int getSize() {
        return 54;
    }
    
    @Override
    protected void buildContent() {
        // 房间信息区域
        addRoomInfo();
        
        // 玩家列表区域
        addPlayerList();
        
        // 操作按钮区域
        addActionButtons();
    }
    
    @Override
    public boolean handleClick(int slot, ItemStack item) {
        // 加入/离开按钮
        if (slot == 48) {
            return handleJoinLeaveButton();
        }
        
        // 开始游戏按钮
        if (slot == 49) {
            return handleStartButton();
        }
        
        // 返回按钮
        if (slot == 50) {
            plugin.getGUIManager().openGameLobby(player);
            return true;
        }
        
        // 刷新按钮
        if (slot == 53) {
            refresh();
            return true;
        }
        
        return true;
    }
    
    /**
     * 处理加入/离开按钮
     */
    private boolean handleJoinLeaveButton() {
        boolean isInGame = plugin.getManhuntManager().isInGame(player);
        boolean isThisGame = isInGame && game.getGameId().equals(plugin.getManhuntManager().getPlayerGameId(player));
        
        if (isThisGame) {
            plugin.getManhuntManager().leaveGame(player);
            player.sendMessage("§a已离开房间");
            plugin.getGUIManager().openGameLobby(player);
        } else if (!isInGame && game.getState().isJoinable() && !game.isFull()) {
            // Bungee 模式：主大厅服务器传送玩家到子大厅
            if (plugin.getServerMode() == com.minecraft.huntergame.ServerMode.BUNGEE &&
                plugin.getManhuntConfig().getServerType() == com.minecraft.huntergame.config.ServerType.MAIN_LOBBY) {
                
                // 从游戏 ID 或 Redis 获取游戏所在的服务器名称
                // 这里简化处理：传送到最佳服务器
                boolean success = plugin.getBungeeManager().sendPlayerToBestServer(player);
                
                if (success) {
                    player.sendMessage("§a正在传送到游戏服务器...");
                    close();
                } else {
                    player.sendMessage("§c传送失败，请稍后重试");
                }
                
                return true;
            }
            
            // 单服务器模式或子大厅：直接加入游戏
            boolean joined = plugin.getManhuntManager().joinGame(player, game.getGameId());
            if (joined) {
                player.sendMessage("§a成功加入房间！");
                refresh();
            } else {
                player.sendMessage("§c加入房间失败！");
            }
        }
        
        return true;
    }
    
    /**
     * 处理开始游戏按钮
     */
    private boolean handleStartButton() {
        if (!player.hasPermission("huntergame.admin.start")) {
            player.sendMessage("§c你没有权限开始游戏！");
            return true;
        }
        
        if (game.getState() != GameState.WAITING) {
            player.sendMessage("§c游戏已经开始了！");
            return true;
        }
        
        if (!game.hasMinPlayers()) {
            player.sendMessage("§c人数不足，无法开始游戏！");
            return true;
        }
        
        plugin.getManhuntManager().startGame(game.getGameId());
        player.sendMessage("§a游戏已开始！");
        close();
        
        return true;
    }
    
    /**
     * 添加房间信息
     */
    private void addRoomInfo() {
        // 状态信息
        List<String> statusLore = new ArrayList<>();
        statusLore.add("§7房间ID: §f" + game.getGameId());
        statusLore.add("§7状态: §f" + game.getState().getDisplayNameZh());
        statusLore.add("§7地图: §f" + game.getWorldName());
        statusLore.add("§7玩家: §f" + game.getPlayerCount() + "§7/§f" + (game.getMaxRunners() + game.getMaxHunters()));
        
        if (game.getState() == GameState.MATCHING) {
            statusLore.add("§7匹配倒计时: §e" + game.getMatchingRemainingTime() + "秒");
        }
        
        ItemStack statusItem = GUIUtils.createItem(Material.PAPER, "§e§l房间状态", statusLore);
        setItem(4, statusItem);
        
        // 游戏配置
        List<String> configLore = new ArrayList<>();
        configLore.add("§7逃亡者数量: §f" + game.getMaxRunners());
        configLore.add("§7猎人数量: §f" + game.getMaxHunters());
        configLore.add("§7准备时间: §f" + plugin.getManhuntConfig().getPrepareTime() + "秒");
        configLore.add("§7复活次数: §f" + plugin.getManhuntConfig().getRespawnLimit());
        
        int maxGameTime = plugin.getManhuntConfig().getMaxGameTime();
        if (maxGameTime > 0) {
            configLore.add("§7最大时长: §f" + (maxGameTime / 60) + "分钟");
        } else {
            configLore.add("§7最大时长: §f无限制");
        }
        
        ItemStack configItem = GUIUtils.createItem(Material.BOOK, "§e§l游戏配置", configLore);
        setItem(22, configItem);
    }
    
    /**
     * 添加玩家列表
     */
    private void addPlayerList() {
        List<UUID> allPlayers = new ArrayList<>(game.getAllPlayers());
        
        int slot = 10;
        for (int i = 0; i < allPlayers.size() && slot < 44; i++) {
            UUID uuid = allPlayers.get(i);
            Player p = Bukkit.getPlayer(uuid);
            
            if (p == null) continue;
            
            ItemStack playerItem = createPlayerItem(p, uuid);
            setItem(slot, playerItem);
            
            slot++;
            if (slot % 9 == 8) {
                slot += 2;
            }
        }
    }
    
    /**
     * 创建玩家物品
     */
    private ItemStack createPlayerItem(Player p, UUID uuid) {
        PlayerRole role = game.getPlayerRole(uuid);
        String roleColor = GUIUtils.getRoleColor(role != null ? role.name() : "");
        
        List<String> lore = new ArrayList<>();
        
        if (role != null) {
            switch (role) {
                case RUNNER:
                    lore.add("§7角色: §a逃亡者");
                    break;
                case HUNTER:
                    lore.add("§7角色: §c猎人");
                    break;
                case SPECTATOR:
                    lore.add("§7角色: §7观战者");
                    break;
            }
        } else {
            lore.add("§7角色: §e未分配");
        }
        
        lore.add("§7状态: " + (p.isOnline() ? "§a在线" : "§c离线"));
        
        return GUIUtils.createPlayerHead(p.getName(), roleColor + p.getName(), lore.toArray(new String[0]));
    }
    
    /**
     * 添加操作按钮
     */
    private void addActionButtons() {
        boolean isInGame = plugin.getManhuntManager().isInGame(player);
        boolean isThisGame = isInGame && game.getGameId().equals(plugin.getManhuntManager().getPlayerGameId(player));
        
        // 加入/离开按钮
        if (isThisGame) {
            ItemStack leaveButton = GUIUtils.createItem(
                Material.RED_WOOL,
                "§c§l离开房间",
                "§7点击离开当前房间"
            );
            setItem(48, leaveButton);
        } else if (!isInGame && game.getState().isJoinable() && !game.isFull()) {
            ItemStack joinButton = GUIUtils.createItem(
                Material.LIME_WOOL,
                "§a§l加入房间",
                "§7点击加入此房间"
            );
            setItem(48, joinButton);
        }
        
        // 开始游戏按钮
        if (player.hasPermission("huntergame.admin.start") && 
            game.getState() == GameState.WAITING && 
            game.hasMinPlayers()) {
            ItemStack startButton = GUIUtils.createItem(
                Material.EMERALD,
                "§a§l开始游戏",
                "§7点击立即开始游戏",
                "§7需要管理员权限"
            );
            setItem(49, startButton);
        }
        
        // 返回按钮
        ItemStack backButton = GUIUtils.createBackButton();
        setItem(50, backButton);
        
        // 刷新按钮
        ItemStack refreshButton = GUIUtils.createItem(
            Material.ARROW,
            "§e§l刷新",
            "§7点击刷新房间信息"
        );
        setItem(53, refreshButton);
    }
    
    /**
     * 获取游戏
     */
    public ManhuntGame getGame() {
        return game;
    }
}
