package com.minecraft.huntergame.sidebar;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.game.ManhuntGame;
import com.minecraft.huntergame.game.PlayerRole;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.UUID;

/**
 * Manhunt侧边栏
 * 显示游戏信息
 * 
 * @author YourName
 * @version 1.0.0
 */
public class ManhuntSidebar {
    
    private final HunterGame plugin;
    private final ManhuntGame game;
    private final Player player;
    private Scoreboard scoreboard;
    private Objective objective;
    
    public ManhuntSidebar(HunterGame plugin, ManhuntGame game, Player player) {
        this.plugin = plugin;
        this.game = game;
        this.player = player;
        
        createScoreboard();
    }
    
    /**
     * 创建计分板
     */
    private void createScoreboard() {
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        objective = scoreboard.registerNewObjective("manhunt", "dummy", 
            ChatColor.GOLD + "" + ChatColor.BOLD + "MANHUNT");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        player.setScoreboard(scoreboard);
    }
    
    /**
     * 更新侧边栏
     */
    public void update() {
        // 清除旧分数
        scoreboard.getEntries().forEach(scoreboard::resetScores);
        
        int line = 15;
        
        // 空行
        setLine(line--, "");
        
        // 游戏状态
        String state = getStateDisplay();
        setLine(line--, ChatColor.YELLOW + "状态: " + state);
        
        // 空行
        setLine(line--, " ");
        
        // 角色信息
        PlayerRole role = game.getPlayerRole(player.getUniqueId());
        if (role != null) {
            String roleDisplay = getRoleDisplay(role);
            setLine(line--, ChatColor.YELLOW + "角色: " + roleDisplay);
            
            // 如果是逃亡者，显示复活次数
            if (role == PlayerRole.RUNNER) {
                int respawns = game.getRemainingRespawns(player.getUniqueId());
                setLine(line--, ChatColor.YELLOW + "复活: " + ChatColor.GREEN + respawns);
            }
        }
        
        // 空行
        setLine(line--, "  ");
        
        // 存活人数
        int aliveRunners = game.getAliveRunners().size();
        int totalRunners = game.getRunners().size();
        setLine(line--, ChatColor.YELLOW + "逃亡者: " + ChatColor.GREEN + aliveRunners + 
            ChatColor.GRAY + "/" + totalRunners);
        
        int hunters = game.getHunters().size();
        setLine(line--, ChatColor.YELLOW + "猎人: " + ChatColor.RED + hunters);
        
        // 空行
        setLine(line--, "   ");
        
        // 游戏时间
        if (game.isPreparing()) {
            long remaining = (game.getPrepareEndTime() - System.currentTimeMillis()) / 1000;
            setLine(line--, ChatColor.YELLOW + "准备: " + ChatColor.GREEN + remaining + "秒");
        } else if (game.getState() == com.minecraft.huntergame.game.GameState.PLAYING) {
            long elapsed = game.getElapsedTime();
            setLine(line--, ChatColor.YELLOW + "时间: " + ChatColor.AQUA + formatTime(elapsed));
        }
        
        // 空行
        setLine(line--, "    ");
    }
    
    /**
     * 设置行内容
     */
    private void setLine(int line, String text) {
        objective.getScore(text).setScore(line);
    }
    
    /**
     * 获取状态显示
     */
    private String getStateDisplay() {
        switch (game.getState()) {
            case WAITING:
                return ChatColor.GRAY + "等待中";
            case PREPARING:
                return ChatColor.YELLOW + "准备中";
            case PLAYING:
                return ChatColor.GREEN + "进行中";
            case ENDING:
                return ChatColor.RED + "结束中";
            default:
                return ChatColor.GRAY + "未知";
        }
    }
    
    /**
     * 获取角色显示
     */
    private String getRoleDisplay(PlayerRole role) {
        switch (role) {
            case RUNNER:
                return ChatColor.GREEN + "逃亡者";
            case HUNTER:
                return ChatColor.RED + "猎人";
            case SPECTATOR:
                return ChatColor.GRAY + "观战者";
            default:
                return ChatColor.GRAY + "未知";
        }
    }
    
    /**
     * 格式化时间
     */
    private String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, secs);
        } else {
            return String.format("%d:%02d", minutes, secs);
        }
    }
    
    /**
     * 移除侧边栏
     */
    public void remove() {
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }
}
