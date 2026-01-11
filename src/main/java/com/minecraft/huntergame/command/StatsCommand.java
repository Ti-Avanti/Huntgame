package com.minecraft.huntergame.command;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.models.PlayerData;
import com.minecraft.huntergame.rank.Rank;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 段位命令
 * 显示玩家段位数据和排行榜
 * 
 * @author YourName
 * @version 2.0.0
 */
public class StatsCommand implements CommandExecutor, TabCompleter {
    
    private final HunterGame plugin;
    
    public StatsCommand(HunterGame plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 检查权限
        if (!sender.hasPermission("huntergame.stats")) {
            plugin.getLanguageManager().sendMessage(sender, "general.no-permission");
            return true;
        }
        
        // 检查是否为玩家
        if (!(sender instanceof Player)) {
            plugin.getLanguageManager().sendMessage(sender, "general.player-only");
            return true;
        }
        
        Player player = (Player) sender;
        
        // 如果没有参数，显示自己的段位
        if (args.length == 0) {
            showPlayerRank(player, player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "rank":
            case "ranks":
            case "leaderboard":
            case "top":
                showTopRanks(player);
                break;
            case "season":
                showSeasonInfo(player);
                break;
            default:
                // 显示指定玩家的段位
                Player target = plugin.getServer().getPlayer(args[0]);
                if (target == null) {
                    plugin.getLanguageManager().sendMessage(player, "general.player-not-found");
                    return true;
                }
                showPlayerRank(player, target);
                break;
        }
        
        return true;
    }
    
    /**
     * 显示玩家段位信息
     */
    private void showPlayerRank(Player viewer, Player target) {
        PlayerData data = plugin.getStatsManager().getPlayerData(target.getUniqueId());
        
        if (data == null) {
            plugin.getLanguageManager().sendMessage(viewer, "stats.no-data");
            return;
        }
        
        Rank currentRank = data.getCurrentRank();
        Rank highestRank = data.getHighestRank();
        int score = data.getScore();
        int scoreToNext = data.getScoreToNextRank();
        int ranking = plugin.getStatsManager().getPlayerRanking(target.getUniqueId());
        int seasonId = data.getSeasonId();
        
        // 发送段位信息
        viewer.sendMessage("§6========== §e" + target.getName() + " 的段位 §6==========");
        viewer.sendMessage("");
        viewer.sendMessage("§e当前段位: " + currentRank.getColoredName());
        viewer.sendMessage("§e当前分数: §a" + score + " §7分");
        
        if (scoreToNext > 0) {
            viewer.sendMessage("§e距离晋级: §c" + scoreToNext + " §7分");
        } else {
            viewer.sendMessage("§e距离晋级: §d已达最高段位");
        }
        
        viewer.sendMessage("");
        viewer.sendMessage("§e历史最高: " + highestRank.getColoredName());
        viewer.sendMessage("§e当前排名: §a#" + (ranking > 0 ? ranking : "未上榜"));
        viewer.sendMessage("§e当前赛季: §6S" + seasonId);
        viewer.sendMessage("");
        viewer.sendMessage("§7提示: 使用 §e/stats rank §7查看排行榜");
    }
    
    /**
     * 显示段位排行榜
     */
    private void showTopRanks(Player player) {
        List<PlayerData> list = plugin.getStatsManager().getTopRanks(10);
        
        player.sendMessage("§6========== §e段位排行榜 §6==========");
        player.sendMessage("§7赛季 S" + plugin.getStatsManager().getCurrentSeasonId());
        player.sendMessage("");
        
        if (list.isEmpty()) {
            player.sendMessage("§7暂无数据");
            return;
        }
        
        int rank = 1;
        for (PlayerData data : list) {
            Rank playerRank = data.getCurrentRank();
            String rankColor = playerRank.getColor().toString();
            
            player.sendMessage("§e#" + rank + " §f" + data.getName() + 
                " §7- " + rankColor + playerRank.getDisplayName() + 
                " §7(§a" + data.getScore() + "§7分)");
            rank++;
        }
        
        player.sendMessage("");
        player.sendMessage("§7你的排名: §a#" + plugin.getStatsManager().getPlayerRanking(player.getUniqueId()));
    }
    
    /**
     * 显示赛季信息
     */
    private void showSeasonInfo(Player player) {
        int seasonId = plugin.getStatsManager().getCurrentSeasonId();
        PlayerData data = plugin.getStatsManager().getPlayerData(player.getUniqueId());
        
        player.sendMessage("§6========== §e赛季信息 §6==========");
        player.sendMessage("");
        player.sendMessage("§e当前赛季: §6S" + seasonId);
        
        if (data != null) {
            player.sendMessage("§e你的段位: " + data.getCurrentRank().getColoredName());
            player.sendMessage("§e你的分数: §a" + data.getScore() + " §7分");
        }
        
        player.sendMessage("");
        player.sendMessage("§7段位加分规则:");
        player.sendMessage("§a  逃亡者获胜: +20分");
        player.sendMessage("§c  逃亡者失败: -10分");
        player.sendMessage("§a  猎人获胜: +10分");
        player.sendMessage("§c  猎人失败: -20分");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // 第一个参数：子命令或玩家名
            List<String> subCommands = Arrays.asList("rank", "ranks", "leaderboard", "top", "season");
            
            String input = args[0].toLowerCase();
            
            // 添加子命令补全
            for (String sub : subCommands) {
                if (sub.startsWith(input)) {
                    completions.add(sub);
                }
            }
            
            // 添加在线玩家名补全
            completions.addAll(plugin.getServer().getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(input))
                .collect(Collectors.toList()));
        }
        
        return completions;
    }
}
