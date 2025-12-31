package com.minecraft.huntergame.command;

import com.minecraft.huntergame.HunterGame;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 主命令处理器
 * 处理 /huntergame 命令
 * 
 * @author YourName
 * @version 1.0.0
 */
public class MainCommand implements CommandExecutor, TabCompleter {
    
    private final HunterGame plugin;
    
    public MainCommand(HunterGame plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 如果没有参数，显示帮助
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        // 处理子命令
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "help":
                sendHelp(sender);
                return true;
                
            case "join":
                return handleJoin(sender, args);
                
            case "leave":
                return handleLeave(sender);
                
            case "stats":
                return handleStats(sender, args);
                
            case "reload":
                return handleReload(sender);
                
            case "list":
                return handleList(sender);
                
            default:
                plugin.getLanguageManager().sendMessage(sender, "command.unknown-subcommand");
                return true;
        }
    }
    
    /**
     * 发送帮助信息
     */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6========== §e猎人游戏帮助 §6==========");
        sender.sendMessage("§e/hg join [竞技场] §7- 加入游戏");
        sender.sendMessage("§e/hg leave §7- 离开游戏");
        sender.sendMessage("§e/hg stats [玩家] §7- 查看统计");
        sender.sendMessage("§e/hg list §7- 查看竞技场列表");
        
        if (sender.hasPermission("huntergame.admin")) {
            sender.sendMessage("§c/hg reload §7- 重载配置 §c[管理员]");
        }
    }
    
    /**
     * 处理加入命令
     */
    private boolean handleJoin(CommandSender sender, String[] args) {
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
        if (args.length >= 2) {
            String arenaName = args[1];
            plugin.getArenaManager().joinArena(player, arenaName);
        } else {
            // 随机加入
            plugin.getArenaManager().joinRandomArena(player);
        }
        
        return true;
    }
    
    /**
     * 处理离开命令
     */
    private boolean handleLeave(CommandSender sender) {
        if (!(sender instanceof Player)) {
            plugin.getLanguageManager().sendMessage(sender, "command.player-only");
            return true;
        }
        
        Player player = (Player) sender;
        
        // 检查权限
        if (!player.hasPermission("huntergame.leave")) {
            plugin.getLanguageManager().sendMessage(player, "command.no-permission");
            return true;
        }
        
        plugin.getArenaManager().leaveArena(player);
        return true;
    }
    
    /**
     * 处理统计命令
     */
    private boolean handleStats(CommandSender sender, String[] args) {
        if (!sender.hasPermission("huntergame.stats")) {
            plugin.getLanguageManager().sendMessage(sender, "command.no-permission");
            return true;
        }
        
        // TODO: 实现统计显示
        sender.sendMessage("§e统计功能将在后续版本中实现");
        return true;
    }
    
    /**
     * 处理重载命令
     */
    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("huntergame.admin.reload")) {
            plugin.getLanguageManager().sendMessage(sender, "command.no-permission");
            return true;
        }
        
        try {
            // 重载配置
            plugin.getMainConfig().reload();
            plugin.getLanguageManager().reload();
            
            // 重载竞技场
            plugin.getArenaManager().reloadAll();
            
            plugin.getLanguageManager().sendMessage(sender, "command.reload-success");
        } catch (Exception ex) {
            plugin.getLanguageManager().sendMessage(sender, "command.reload-failed");
            plugin.getLogger().severe("重载配置失败: " + ex.getMessage());
            ex.printStackTrace();
        }
        
        return true;
    }
    
    /**
     * 处理列表命令
     */
    private boolean handleList(CommandSender sender) {
        if (!sender.hasPermission("huntergame.list")) {
            plugin.getLanguageManager().sendMessage(sender, "command.no-permission");
            return true;
        }
        
        sender.sendMessage("§6========== §e竞技场列表 §6==========");
        
        if (plugin.getArenaManager().getArenaCount() == 0) {
            sender.sendMessage("§c没有可用的竞技场");
            return true;
        }
        
        plugin.getArenaManager().getArenas().forEach(arena -> {
            String status = arena.getState().getDisplayNameZh();
            int players = arena.getPlayerCount();
            int maxPlayers = arena.getMaxPlayers();
            
            sender.sendMessage(String.format("§e%s §7- §a%s §7(%d/%d)", 
                arena.getDisplayName(), status, players, maxPlayers));
        });
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // 第一个参数：子命令
            List<String> subCommands = Arrays.asList("help", "join", "leave", "stats", "list");
            
            if (sender.hasPermission("huntergame.admin")) {
                subCommands = new ArrayList<>(subCommands);
                subCommands.add("reload");
            }
            
            String input = args[0].toLowerCase();
            for (String sub : subCommands) {
                if (sub.startsWith(input)) {
                    completions.add(sub);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("join")) {
            // join 命令的第二个参数：竞技场名称
            String input = args[1].toLowerCase();
            for (String arenaName : plugin.getArenaManager().getArenaNames()) {
                if (arenaName.toLowerCase().startsWith(input)) {
                    completions.add(arenaName);
                }
            }
        }
        
        return completions;
    }
}
