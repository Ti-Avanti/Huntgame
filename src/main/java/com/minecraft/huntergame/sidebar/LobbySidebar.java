package com.minecraft.huntergame.sidebar;

import com.minecraft.huntergame.HunterGame;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * 大厅计分板
 * <p>
 * 显示大厅信息，包括：
 * - 服务器状态
 * - 在线玩家数
 * - 欢迎信息
 * </p>
 * 
 * @author YourName
 * @version 1.0.0
 */
public class LobbySidebar extends BaseSidebar {
    
    /**
     * 构造函数
     * 
     * @param plugin 插件实例
     * @param player 玩家
     */
    public LobbySidebar(HunterGame plugin, Player player) {
        super(plugin, player);
        
        String title = plugin.getScoreboardConfig().getLobbyTitle();
        createScoreboard("lobby", title);
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
        for (String lineText : plugin.getScoreboardConfig().getLobbyLines()) {
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
        line = line.replace("{player_count}", "0");
        line = line.replace("{max_players}", String.valueOf(plugin.getManhuntConfig().getMaxPlayers()));
        line = line.replace("{status}", "等待中");
        line = line.replace("{server}", "Server");
        return line;
    }
}
