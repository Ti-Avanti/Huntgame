package com.minecraft.huntergame.sidebar;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.game.ManhuntGame;
import com.minecraft.huntergame.game.PlayerRole;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Manhunt 游戏计分板
 * <p>
 * 显示游戏进行中的信息，包括：
 * - 游戏状态
 * - 玩家角色
 * - 存活人数
 * - 游戏时间
 * </p>
 * 
 * @author YourName
 * @version 1.0.0
 */
public class ManhuntSidebar extends BaseSidebar {
    
    private final ManhuntGame game;
    
    /**
     * 构造函数
     * 
     * @param plugin 插件实例
     * @param game 游戏实例
     * @param player 玩家
     */
    public ManhuntSidebar(HunterGame plugin, ManhuntGame game, Player player) {
        super(plugin, player);
        this.game = game;
        
        // 使用配置的标题
        String title = plugin.getScoreboardConfig().getGameTitle();
        createScoreboard("manhunt", title);
    }
    
    /**
     * 更新侧边栏
     */
    @Override
    public void update() {
        // 清除旧分数
        clearLines();
        
        int line = 15;
        int emptyLineCounter = 0; // 用于生成唯一的空行
        
        // 空行
        setLine(line--, generateEmptyLine(emptyLineCounter++));
        
        // 游戏状态
        if (plugin.getScoreboardConfig().isShowState()) {
            String state = getStateDisplay();
            setLine(line--, ChatColor.YELLOW + "状态: " + state);
            
            // 空行
            setLine(line--, generateEmptyLine(emptyLineCounter++));
        }
        
        // 角色信息
        if (plugin.getScoreboardConfig().isShowRole()) {
            PlayerRole role = game.getPlayerRole(player.getUniqueId());
            if (role != null) {
                String roleDisplay = getRoleDisplay(role);
                setLine(line--, ChatColor.YELLOW + "角色: " + roleDisplay);
                
                // 如果是逃亡者，显示复活次数
                if (role == PlayerRole.RUNNER && plugin.getScoreboardConfig().isShowRespawns()) {
                    int respawns = game.getRemainingRespawns(player.getUniqueId());
                    setLine(line--, ChatColor.YELLOW + "复活: " + ChatColor.GREEN + respawns);
                }
                
                // 空行
                setLine(line--, generateEmptyLine(emptyLineCounter++));
            }
        }
        
        // 存活人数
        if (plugin.getScoreboardConfig().isShowAliveCount()) {
            int aliveRunners = game.getAliveRunners().size();
            int totalRunners = game.getRunners().size();
            setLine(line--, ChatColor.YELLOW + "逃亡者: " + ChatColor.GREEN + aliveRunners + 
                ChatColor.GRAY + "/" + totalRunners);
            
            int hunters = game.getHunters().size();
            setLine(line--, ChatColor.YELLOW + "猎人: " + ChatColor.RED + hunters);
            
            // 空行
            setLine(line--, generateEmptyLine(emptyLineCounter++));
        }
        
        // 游戏时间
        if (plugin.getScoreboardConfig().isShowTime()) {
            if (game.isPreparing()) {
                long remaining = (game.getPrepareEndTime() - System.currentTimeMillis()) / 1000;
                setLine(line--, ChatColor.YELLOW + "准备: " + ChatColor.GREEN + remaining + "秒");
            } else if (game.getState() == com.minecraft.huntergame.game.GameState.PLAYING) {
                long elapsed = game.getElapsedTime();
                setLine(line--, ChatColor.YELLOW + "时间: " + ChatColor.AQUA + formatTime(elapsed));
            }
            
            // 空行
            setLine(line--, generateEmptyLine(emptyLineCounter++));
        }
    }
    
    /**
     * 获取状态显示
     * 
     * @return 格式化的状态文本
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
     * 
     * @param role 玩家角色
     * @return 格式化的角色文本
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
     * 
     * @param seconds 秒数
     * @return 格式化的时间字符串（HH:MM:SS 或 MM:SS）
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
}
