package com.minecraft.huntergame.sidebar;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 侧边栏管理器
 * 负责管理玩家的侧边栏显示
 * 
 * @author YourName
 * @version 1.0.0
 */
public class SidebarManager {
    
    private final HunterGame plugin;
    private final Arena arena;
    
    // 玩家侧边栏映射
    private final Map<UUID, Scoreboard> scoreboards;
    
    public SidebarManager(HunterGame plugin, Arena arena) {
        this.plugin = plugin;
        this.arena = arena;
        this.scoreboards = new HashMap<>();
    }
    
    /**
     * 为玩家创建侧边栏
     * 
     * @param player 玩家
     */
    public void createSidebar(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        scoreboards.put(player.getUniqueId(), scoreboard);
        player.setScoreboard(scoreboard);
    }
    
    /**
     * 移除玩家侧边栏
     * 
     * @param player 玩家
     */
    public void removeSidebar(Player player) {
        scoreboards.remove(player.getUniqueId());
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }
    
    /**
     * 更新玩家侧边栏
     * 
     * @param player 玩家
     */
    public void updateSidebar(Player player) {
        Scoreboard scoreboard = scoreboards.get(player.getUniqueId());
        if (scoreboard == null) {
            return;
        }
        
        // 根据游戏状态显示不同的侧边栏
        switch (arena.getState()) {
            case WAITING:
            case STARTING:
                updateWaitingSidebar(player, scoreboard);
                break;
            case PLAYING:
                updateGameSidebar(player, scoreboard);
                break;
            default:
                break;
        }
    }
    
    /**
     * 更新等待侧边栏
     * 
     * @param player 玩家
     * @param scoreboard 计分板
     */
    private void updateWaitingSidebar(Player player, Scoreboard scoreboard) {
        // 清除旧的objective
        Objective objective = scoreboard.getObjective("waiting");
        if (objective != null) {
            objective.unregister();
        }
        
        // 创建新的objective
        objective = scoreboard.registerNewObjective("waiting", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName("§e§l猎人游戏");
        
        // 设置内容
        int line = 10;
        objective.getScore("§7").setScore(line--);
        objective.getScore("§e竞技场: §f" + arena.getDisplayName()).setScore(line--);
        objective.getScore("§a").setScore(line--);
        objective.getScore("§e玩家: §f" + arena.getPlayerCount() + "/" + arena.getMaxPlayers()).setScore(line--);
        objective.getScore("§b").setScore(line--);
        
        if (arena.getState() == com.minecraft.huntergame.game.GameState.STARTING) {
            objective.getScore("§e状态: §a即将开始").setScore(line--);
        } else {
            objective.getScore("§e状态: §7等待中...").setScore(line--);
            objective.getScore("§c").setScore(line--);
            objective.getScore("§7需要 §e" + arena.getMinPlayers() + " §7名玩家").setScore(line--);
        }
        
        objective.getScore("§d").setScore(line--);
    }
    
    /**
     * 更新游戏侧边栏
     * 
     * @param player 玩家
     * @param scoreboard 计分板
     */
    private void updateGameSidebar(Player player, Scoreboard scoreboard) {
        // 清除旧的objective
        Objective objective = scoreboard.getObjective("game");
        if (objective != null) {
            objective.unregister();
        }
        
        // 创建新的objective
        objective = scoreboard.registerNewObjective("game", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName("§e§l猎人游戏");
        
        // 设置内容
        int line = 12;
        objective.getScore("§7").setScore(line--);
        objective.getScore("§e竞技场: §f" + arena.getDisplayName()).setScore(line--);
        objective.getScore("§a").setScore(line--);
        
        // 显示角色
        com.minecraft.huntergame.game.PlayerRole role = arena.getPlayerRole(player.getUniqueId());
        if (role != null) {
            String roleColor = role == com.minecraft.huntergame.game.PlayerRole.HUNTER ? "§c" : "§a";
            String roleName = role == com.minecraft.huntergame.game.PlayerRole.HUNTER ? "猎人" : "逃生者";
            objective.getScore("§e角色: " + roleColor + roleName).setScore(line--);
            objective.getScore("§b").setScore(line--);
        }
        
        // 显示剩余时间
        int remainingTime = arena.getRemainingTime();
        int minutes = remainingTime / 60;
        int seconds = remainingTime % 60;
        objective.getScore("§e时间: §f" + String.format("%02d:%02d", minutes, seconds)).setScore(line--);
        objective.getScore("§c").setScore(line--);
        
        // 显示存活人数
        objective.getScore("§e猎人: §c" + arena.getHunters().size()).setScore(line--);
        objective.getScore("§e逃生者: §a" + arena.getAliveSurvivors().size()).setScore(line--);
        objective.getScore("§e已逃脱: §2" + arena.getEscaped().size()).setScore(line--);
        
        objective.getScore("§d").setScore(line--);
    }
    
    /**
     * 更新所有玩家的侧边栏
     */
    public void updateAll() {
        for (UUID uuid : arena.getPlayers()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                updateSidebar(player);
            }
        }
    }
    
    /**
     * 清除所有侧边栏
     */
    public void clearAll() {
        for (UUID uuid : new HashMap<>(scoreboards).keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                removeSidebar(player);
            }
        }
        scoreboards.clear();
    }
}
