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
 * 匹配GUI（单服务器模式）
 * 显示当前游戏的匹配状态和玩家列表
 * 
 * @author YourName
 * @version 1.0.0
 */
public class MatchingGUI extends BaseGUI {
    
    private final ManhuntGame game;
    
    public MatchingGUI(HunterGame plugin, Player player, ManhuntGame game) {
        super(plugin, player);
        this.game = game;
        
        // 启用自动刷新（每秒刷新一次）
        enableAutoRefresh(20);
    }
    
    @Override
    protected String getTitle() {
        return "§6§l匹配大厅";
    }
    
    @Override
    protected int getSize() {
        return 54;
    }
    
    @Override
    protected void buildContent() {
        // 游戏状态信息
        addGameStatus();
        
        // 玩家列表
        addPlayerList();
        
        // 操作按钮
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
        
        // 刷新按钮
        if (slot == 53) {
            refresh();
            return true;
        }
        
        // 玩家头颅点击（观战传送）
        if (item != null && item.getType() == Material.PLAYER_HEAD) {
            return handlePlayerHeadClick(item);
        }
        
        return true; // 默认取消所有点击
    }
    
    /**
     * 处理加入/离开按钮
     */
    private boolean handleJoinLeaveButton() {
        boolean isInGame = plugin.getManhuntManager().isInGame(player);
        boolean isThisGame = isInGame && game.getGameId().equals(plugin.getManhuntManager().getPlayerGameId(player));
        
        if (isThisGame) {
            // 离开游戏
            if (game.getState() == GameState.WAITING || game.getState() == GameState.MATCHING) {
                plugin.getManhuntManager().leaveGame(player);
                player.sendMessage("§a已离开游戏");
                close();
            }
        } else if (!isInGame && game.getState().isJoinable() && !game.isFull()) {
            // 加入游戏
            boolean joined = plugin.getManhuntManager().joinGame(player, game.getGameId());
            if (joined) {
                player.sendMessage("§a成功加入游戏！");
                refresh();
            } else {
                player.sendMessage("§c加入游戏失败！");
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
        
        if (game.getState() != GameState.WAITING && game.getState() != GameState.MATCHING) {
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
     * 处理玩家头颅点击（观战传送）
     */
    private boolean handlePlayerHeadClick(ItemStack item) {
        PlayerRole role = game.getPlayerRole(player.getUniqueId());
        
        if (role != PlayerRole.SPECTATOR || game.getState() != GameState.PLAYING) {
            return true;
        }
        
        // 从物品名称获取玩家名
        String displayName = item.getItemMeta().getDisplayName();
        String playerName = org.bukkit.ChatColor.stripColor(displayName);
        
        Player target = Bukkit.getPlayer(playerName);
        if (target != null && target.isOnline()) {
            player.teleport(target.getLocation());
            player.sendMessage("§a已传送到 " + target.getName());
        } else {
            player.sendMessage("§c目标玩家不在线！");
        }
        
        return true;
    }
    
    /**
     * 添加游戏状态信息
     */
    private void addGameStatus() {
        // 状态信息
        List<String> statusLore = new ArrayList<>();
        statusLore.add("§7状态: §f" + game.getState().getDisplayNameZh());
        statusLore.add("§7玩家: §f" + game.getPlayerCount() + "§7/§f" + (game.getMaxRunners() + game.getMaxHunters()));
        
        if (game.getState() == GameState.MATCHING) {
            statusLore.add("§7匹配倒计时: §e" + game.getMatchingRemainingTime() + "秒");
            statusLore.add("");
            statusLore.add("§7等待更多玩家加入...");
        } else if (game.getState() == GameState.WAITING) {
            statusLore.add("");
            statusLore.add("§7等待玩家加入");
            statusLore.add("§7最少需要: §a" + plugin.getManhuntConfig().getMinPlayersToStart() + " §7人");
        } else if (game.getState() == GameState.PREPARING) {
            long remaining = (game.getPrepareEndTime() - System.currentTimeMillis()) / 1000;
            statusLore.add("§7准备倒计时: §e" + remaining + "秒");
            statusLore.add("");
            statusLore.add("§7猎人已冻结");
            statusLore.add("§7逃亡者可以移动");
        } else if (game.getState() == GameState.PLAYING) {
            statusLore.add("§7游戏时长: §f" + com.minecraft.huntergame.util.TimeUtil.formatTimeChinese(game.getElapsedTime()));
            statusLore.add("");
            statusLore.add("§a游戏进行中");
        }
        
        ItemStack statusItem = GUIUtils.createItem(Material.PAPER, "§e§l游戏状态", statusLore);
        setItem(4, statusItem);
        
        // 游戏配置
        List<String> configLore = new ArrayList<>();
        configLore.add("§7逃亡者: §a" + game.getMaxRunners() + " §7人");
        configLore.add("§7猎人: §c" + game.getMaxHunters() + " §7人");
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
                slot += 2; // 跳到下一行
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
        lore.add("§7角色: " + getRoleDisplay(role));
        lore.add("§7状态: " + (p.isOnline() ? "§a在线" : "§c离线"));
        
        // 如果是观战者且游戏进行中，显示传送提示
        if (role == PlayerRole.SPECTATOR && game.getState() == GameState.PLAYING) {
            lore.add("");
            lore.add("§7点击传送观战");
        }
        
        return GUIUtils.createPlayerHead(p.getName(), roleColor + p.getName(), lore.toArray(new String[0]));
    }
    
    /**
     * 获取角色显示名称
     */
    private String getRoleDisplay(PlayerRole role) {
        if (role == null) return "§e未分配";
        switch (role) {
            case RUNNER: return "§a逃亡者";
            case HUNTER: return "§c猎人";
            case SPECTATOR: return "§7观战者";
            default: return "§e未分配";
        }
    }
    
    /**
     * 添加操作按钮
     */
    private void addActionButtons() {
        boolean isInGame = plugin.getManhuntManager().isInGame(player);
        boolean isThisGame = isInGame && game.getGameId().equals(plugin.getManhuntManager().getPlayerGameId(player));
        
        // 加入/离开按钮
        if (isThisGame) {
            // 离开按钮（仅在等待/匹配阶段）
            if (game.getState() == GameState.WAITING || game.getState() == GameState.MATCHING) {
                ItemStack leaveButton = GUIUtils.createItem(
                    Material.RED_WOOL,
                    "§c§l离开游戏",
                    "§7点击离开当前游戏"
                );
                setItem(48, leaveButton);
            }
        } else if (!isInGame && game.getState().isJoinable() && !game.isFull()) {
            // 加入按钮
            ItemStack joinButton = GUIUtils.createItem(
                Material.LIME_WOOL,
                "§a§l加入游戏",
                "§7点击加入游戏"
            );
            setItem(48, joinButton);
        }
        
        // 开始游戏按钮（仅管理员且游戏未开始）
        if (player.hasPermission("huntergame.admin.start") && 
            (game.getState() == GameState.WAITING || game.getState() == GameState.MATCHING) && 
            game.hasMinPlayers()) {
            ItemStack startButton = GUIUtils.createItem(
                Material.EMERALD,
                "§a§l立即开始",
                "§7点击立即开始游戏",
                "§7需要管理员权限"
            );
            setItem(49, startButton);
        }
        
        // 刷新按钮
        ItemStack refreshButton = GUIUtils.createItem(
            Material.ARROW,
            "§e§l刷新",
            "§7点击刷新信息"
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
