package com.minecraft.huntergame.command;

import com.minecraft.huntergame.HunterGame;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * 加入命令处理器
 * 处理 /hgjoin 命令
 * 
 * @author YourName
 * @version 1.0.0
 */
public class JoinCommand implements CommandExecutor, TabCompleter {
    
    private final HunterGame plugin;
    
    public JoinCommand(HunterGame plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 检查权限
        if (!sender.hasPermission("huntergame.join")) {
            plugin.getLanguageManager().sendMessage(sender, "general.no-permission");
            return true;
        }
        
        if (!(sender instanceof Player)) {
            plugin.getLanguageManager().sendMessage(sender, "command.player-only");
            return true;
        }
        
        Player player = (Player) sender;
        
        // 检查权限
        if (!player.hasPermission("huntergame.join")) {
            plugin.getLanguageManager().sendMessage(player, "command.no-permission");
            return true;
        }
        
        // 检查是否已在游戏中
        if (plugin.getArenaManager().isInArena(player)) {
            plugin.getLanguageManager().sendMessage(player, "join.already-in-game");
            return true;
        }
        
        // 如果指定了竞技场名称
        if (args.length >= 1) {
            String arenaName = args[0];
            plugin.getArenaManager().joinArena(player, arenaName);
        } else {
            // 随机加入
            plugin.getArenaManager().joinRandomArena(player);
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // 竞技场名称补全
            String input = args[0].toLowerCase();
            for (String arenaName : plugin.getArenaManager().getArenaNames()) {
                if (arenaName.toLowerCase().startsWith(input)) {
                    completions.add(arenaName);
                }
            }
        }
        
        return completions;
    }
}
