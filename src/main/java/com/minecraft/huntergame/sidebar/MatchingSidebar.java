package com.minecraft.huntergame.sidebar;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.game.ManhuntGame;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * 匹配计分板
 * <p>
 * 显示匹配信息，包括：
 * - 当前玩家数
 * - 匹配倒计时
 * - 房间状态
 * </p>
 * 
 * @author YourName
 * @version 1.0.0
 */
public class MatchingSidebar extends BaseSidebar {
    
    private final ManhuntGame game;
    
    /**
     * 构造函数
     * 
     * @param plugin 插件实例
     * @param game 游戏实例
     * @param player 玩家
     */
    public MatchingSidebar(HunterGame plugin, ManhuntGame game, Player player) {
        super(plugin, player);
        this.game = game;
        
        String title = plugin.getScoreboardConfig().getMatchingTitle();
        createScoreboard("matching", title);
    }
    
    /**
     * 更新计分板
     */
    @Override
    public void update() {
        clearLines();
        
        int line = 10;
        int emptyCounter = 0;
        
        // 使用配置的行
        for (String lineText : plugin.getScoreboardConfig().getMatchingLines()) {
            if (lineText.isEmpty()) {
                setLine(line--, generateEmptyLine(emptyCounter++));
            } else {
                String formatted = formatLine(lineText);
                setLine(line--, formatted);
            }
        }
    }
    
    /**
     * 格式化行（替换变量）
     * 
     * @param line 原始行文本
     * @return 格式化后的文本
     */
    private String formatLine(String line) {
        line = ChatColor.translateAlternateColorCodes('&', line);
        line = line.replace("{player_count}", String.valueOf(game.getPlayerCount()));
        line = line.replace("{max_players}", String.valueOf(plugin.getManhuntConfig().getMaxPlayers()));
        line = line.replace("{countdown}", String.valueOf(game.getMatchingRemainingTime()));
        return line;
    }
}
