package com.minecraft.huntergame.command;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.models.PlayerData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * 统计命令
 * 显示玩家统计数据和排行榜
 * 
 * @author YourName
 * @version 1.0.0
 */
public class StatsCommand implements CommandExecutor {
    
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
        
        // 如果没有参数，显示自己的统计
        if (args.length == 0) {
            showPlayerStats(player, player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "wins":
                showTopWins(player);
                break;
            case "kills":
                showTopKills(player);
                break;
            case "escapes":
                showTopEscapes(player);
                break;
            default:
                // 显示指定玩家的统计
                Player target = plugin.getServer().getPlayer(args[0]);
                if (target == null) {
                    plugin.getLanguageManager().sendMessage(player, "general.player-not-found");
                    return true;
                }
                showPlayerStats(player, target);
                break;
        }
        
        return true;
    }
    
    /**
     * 显示玩家统计
     */
    private void showPlayerStats(Player viewer, Player target) {
        PlayerData data = plugin.getStatsManager().getPlayerData(target.getUniqueId());
        
        if (data == null) {
            plugin.getLanguageManager().sendMessage(viewer, "stats.no-data");
            return;
        }
        
        // 发送统计信息
        viewer.sendMessage(plugin.getLanguageManager().getMessage("stats.title", target.getName()));
        viewer.sendMessage(plugin.getLanguageManager().getMessage("stats.games-played", 
            String.valueOf(data.getGamesPlayed())));
        viewer.sendMessage(plugin.getLanguageManager().getMessage("stats.wins", 
            String.valueOf(data.getWins())));
        viewer.sendMessage(plugin.getLanguageManager().getMessage("stats.losses", 
            String.valueOf(data.getLosses())));
        viewer.sendMessage(plugin.getLanguageManager().getMessage("stats.kills", 
            String.valueOf(data.getHunterKills())));
        viewer.sendMessage(plugin.getLanguageManager().getMessage("stats.deaths", 
            String.valueOf(data.getHunterDeaths() + data.getSurvivorDeaths())));
        viewer.sendMessage(plugin.getLanguageManager().getMessage("stats.escapes", 
            String.valueOf(data.getSurvivorEscapes())));
    }
    
    /**
     * 显示胜利排行榜
     */
    private void showTopWins(Player player) {
        plugin.getStatsManager().getTopWins(10, list -> {
            player.sendMessage(plugin.getLanguageManager().getMessage("leaderboard.title"));
            player.sendMessage(plugin.getLanguageManager().getMessage("leaderboard.wins"));
            player.sendMessage("§7");
            
            if (list.isEmpty()) {
                player.sendMessage("§7暂无数据");
                return;
            }
            
            int rank = 1;
            for (PlayerData data : list) {
                player.sendMessage(plugin.getLanguageManager().getMessage("leaderboard.entry", 
                    String.valueOf(rank), data.getName(), String.valueOf(data.getWins())));
                rank++;
            }
        });
    }
    
    /**
     * 显示击杀排行榜
     */
    private void showTopKills(Player player) {
        plugin.getStatsManager().getTopKills(10, list -> {
            player.sendMessage(plugin.getLanguageManager().getMessage("leaderboard.title"));
            player.sendMessage(plugin.getLanguageManager().getMessage("leaderboard.kills"));
            player.sendMessage("§7");
            
            if (list.isEmpty()) {
                player.sendMessage("§7暂无数据");
                return;
            }
            
            int rank = 1;
            for (PlayerData data : list) {
                player.sendMessage(plugin.getLanguageManager().getMessage("leaderboard.entry", 
                    String.valueOf(rank), data.getName(), String.valueOf(data.getHunterKills())));
                rank++;
            }
        });
    }
    
    /**
     * 显示逃脱排行榜
     */
    private void showTopEscapes(Player player) {
        plugin.getStatsManager().getTopEscapes(10, list -> {
            player.sendMessage(plugin.getLanguageManager().getMessage("leaderboard.title"));
            player.sendMessage(plugin.getLanguageManager().getMessage("leaderboard.escapes"));
            player.sendMessage("§7");
            
            if (list.isEmpty()) {
                player.sendMessage("§7暂无数据");
                return;
            }
            
            int rank = 1;
            for (PlayerData data : list) {
                player.sendMessage(plugin.getLanguageManager().getMessage("leaderboard.entry", 
                    String.valueOf(rank), data.getName(), String.valueOf(data.getSurvivorEscapes())));
                rank++;
            }
        });
    }
}
