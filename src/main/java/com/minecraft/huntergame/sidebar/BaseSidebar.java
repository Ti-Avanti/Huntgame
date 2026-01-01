package com.minecraft.huntergame.sidebar;

import com.minecraft.huntergame.HunterGame;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

/**
 * 计分板基类
 * <p>
 * 提供计分板的通用功能，包括：
 * - 使用 Team 方式隐藏右侧数字
 * - 生成唯一的 entry
 * - 设置行内容
 * </p>
 * 
 * @author YourName
 * @version 1.0.0
 */
public abstract class BaseSidebar {
    
    /**
     * Team prefix 最大长度
     */
    protected static final int MAX_PREFIX_LENGTH = 16;
    
    /**
     * Team suffix 最大长度
     */
    protected static final int MAX_SUFFIX_LENGTH = 32;
    
    protected final HunterGame plugin;
    protected final Player player;
    protected Scoreboard scoreboard;
    protected Objective objective;
    
    /**
     * 构造函数
     * 
     * @param plugin 插件实例
     * @param player 玩家
     */
    public BaseSidebar(HunterGame plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }
    
    /**
     * 创建计分板
     * 
     * @param objectiveName Objective 名称
     * @param title 计分板标题
     */
    protected void createScoreboard(String objectiveName, String title) {
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        objective = scoreboard.registerNewObjective(objectiveName, "dummy", title);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        player.setScoreboard(scoreboard);
    }
    
    /**
     * 更新计分板
     * <p>
     * 子类必须实现此方法来定义具体的显示内容
     * </p>
     */
    public abstract void update();
    
    /**
     * 使用 Team 设置行内容（高版本适配，移除右侧数字）
     * 
     * @param line 行号（用于排序）
     * @param text 显示文本
     */
    protected void setLine(int line, String text) {
        // 生成唯一的 entry（使用不可见字符）
        String entry = generateUniqueEntry(line);
        
        // 创建或获取 Team
        String teamName = "line_" + line;
        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
        }
        
        // 清除旧的 entry
        for (String oldEntry : team.getEntries()) {
            team.removeEntry(oldEntry);
        }
        
        // 设置显示文本
        if (text.length() <= MAX_PREFIX_LENGTH) {
            team.setPrefix(text);
            team.setSuffix("");
        } else {
            // 文本过长，分割显示
            team.setPrefix(text.substring(0, MAX_PREFIX_LENGTH));
            team.setSuffix(text.substring(MAX_PREFIX_LENGTH, Math.min(text.length(), MAX_SUFFIX_LENGTH)));
        }
        
        // 添加 entry 到 team
        team.addEntry(entry);
        
        // 设置分数（用于排序，但不会显示因为使用了 Team）
        objective.getScore(entry).setScore(line);
    }
    
    /**
     * 生成唯一的 entry（使用不可见字符）
     * <p>
     * 使用重复的颜色代码生成不可见但唯一的 entry
     * 每个 line 使用不同数量的 §r 来确保唯一性
     * </p>
     * 
     * @param line 行号
     * @return 唯一的 entry 字符串
     */
    protected String generateUniqueEntry(int line) {
        StringBuilder entry = new StringBuilder();
        for (int i = 0; i < line; i++) {
            entry.append("§r");
        }
        // 添加一个空格确保 entry 不为空
        entry.append(" ");
        return entry.toString();
    }
    
    /**
     * 生成空行（使用不同数量的空格确保唯一性）
     * 
     * @param counter 空行计数器
     * @return 空行字符串
     */
    protected String generateEmptyLine(int counter) {
        StringBuilder spaces = new StringBuilder();
        for (int i = 0; i <= counter; i++) {
            spaces.append(" ");
        }
        return spaces.toString();
    }
    
    /**
     * 清除所有行
     */
    protected void clearLines() {
        scoreboard.getEntries().forEach(scoreboard::resetScores);
    }
    
    /**
     * 移除计分板
     */
    public void remove() {
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }
}
