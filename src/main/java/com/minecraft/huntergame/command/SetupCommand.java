package com.minecraft.huntergame.command;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.game.ManhuntGame;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
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
 * 游戏设置命令
 * 用于设置游戏相关配置
 * 
 * @author YourName
 * @version 1.0.0
 */
public class SetupCommand implements CommandExecutor, TabCompleter {
    
    private final HunterGame plugin;
    
    public SetupCommand(HunterGame plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 检查权限
        if (!sender.hasPermission("huntergame.admin.setup")) {
            sender.sendMessage(ChatColor.RED + "你没有权限使用此命令！");
            return true;
        }
        
        // 检查参数
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "spawn":
                handleSetSpawn(sender, args);
                break;
            
            case "world":
                handleSetWorld(sender, args);
                break;
            
            case "lobby":
                handleSetLobby(sender, args);
                break;
            
            case "info":
                handleInfo(sender, args);
                break;
            
            default:
                sendUsage(sender);
                break;
        }
        
        return true;
    }
    
    /**
     * 设置出生点
     */
    private void handleSetSpawn(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "此命令只能由玩家执行！");
            return;
        }
        
        Player player = (Player) sender;
        
        // 检查是否指定游戏ID
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "用法: /hgsetup spawn <游戏ID>");
            return;
        }
        
        String gameId = args[1];
        ManhuntGame game = plugin.getManhuntManager().getGame(gameId);
        
        if (game == null) {
            sender.sendMessage(ChatColor.RED + "游戏不存在: " + gameId);
            return;
        }
        
        // 设置出生点
        Location location = player.getLocation();
        game.setSpawnLocation(location);
        
        sender.sendMessage(ChatColor.GREEN + "已设置游戏 " + gameId + " 的出生点");
        sender.sendMessage(ChatColor.GRAY + "位置: " + formatLocation(location));
    }
    
    /**
     * 设置游戏世界
     */
    private void handleSetWorld(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "用法: /hgsetup world <游戏ID> <世界名>");
            return;
        }
        
        String gameId = args[1];
        String worldName = args[2];
        
        // 检查世界是否存在
        if (plugin.getServer().getWorld(worldName) == null) {
            sender.sendMessage(ChatColor.RED + "世界不存在: " + worldName);
            return;
        }
        
        ManhuntGame game = plugin.getManhuntManager().getGame(gameId);
        
        if (game == null) {
            sender.sendMessage(ChatColor.RED + "游戏不存在: " + gameId);
            return;
        }
        
        sender.sendMessage(ChatColor.GREEN + "已设置游戏 " + gameId + " 的世界为: " + worldName);
        sender.sendMessage(ChatColor.YELLOW + "注意: 需要重启游戏才能生效");
    }
    
    /**
     * 设置大厅位置
     */
    private void handleSetLobby(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "此命令只能由玩家执行！");
            return;
        }
        
        Player player = (Player) sender;
        Location location = player.getLocation();
        
        // 保存大厅位置到配置
        plugin.getManhuntConfig().setLobbyLocation(location);
        
        sender.sendMessage(ChatColor.GREEN + "已设置游戏大厅位置");
        sender.sendMessage(ChatColor.GRAY + "位置: " + formatLocation(location));
    }
    
    /**
     * 显示游戏信息
     */
    private void handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "用法: /hgsetup info <游戏ID>");
            return;
        }
        
        String gameId = args[1];
        ManhuntGame game = plugin.getManhuntManager().getGame(gameId);
        
        if (game == null) {
            sender.sendMessage(ChatColor.RED + "游戏不存在: " + gameId);
            return;
        }
        
        sender.sendMessage(ChatColor.GOLD + "========== 游戏信息 ==========");
        sender.sendMessage(ChatColor.YELLOW + "游戏ID: " + ChatColor.WHITE + game.getGameId());
        sender.sendMessage(ChatColor.YELLOW + "世界: " + ChatColor.WHITE + game.getWorldName());
        sender.sendMessage(ChatColor.YELLOW + "状态: " + ChatColor.WHITE + game.getState());
        sender.sendMessage(ChatColor.YELLOW + "玩家数: " + ChatColor.WHITE + game.getPlayerCount());
        sender.sendMessage(ChatColor.YELLOW + "逃亡者: " + ChatColor.WHITE + game.getRunners().size());
        sender.sendMessage(ChatColor.YELLOW + "猎人: " + ChatColor.WHITE + game.getHunters().size());
        sender.sendMessage(ChatColor.YELLOW + "观战者: " + ChatColor.WHITE + game.getSpectators().size());
        
        if (game.getSpawnLocation() != null) {
            sender.sendMessage(ChatColor.YELLOW + "出生点: " + ChatColor.WHITE + 
                formatLocation(game.getSpawnLocation()));
        } else {
            sender.sendMessage(ChatColor.YELLOW + "出生点: " + ChatColor.RED + "未设置");
        }
        
        sender.sendMessage(ChatColor.GOLD + "============================");
    }
    
    /**
     * 发送用法信息
     */
    private void sendUsage(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "========== 游戏设置命令 ==========");
        sender.sendMessage(ChatColor.YELLOW + "/hgsetup spawn <游戏ID> " + 
            ChatColor.GRAY + "- 设置出生点");
        sender.sendMessage(ChatColor.YELLOW + "/hgsetup world <游戏ID> <世界名> " + 
            ChatColor.GRAY + "- 设置游戏世界");
        sender.sendMessage(ChatColor.YELLOW + "/hgsetup lobby " + 
            ChatColor.GRAY + "- 设置游戏大厅");
        sender.sendMessage(ChatColor.YELLOW + "/hgsetup info <游戏ID> " + 
            ChatColor.GRAY + "- 查看游戏信息");
        sender.sendMessage(ChatColor.GOLD + "================================");
    }
    
    /**
     * 格式化位置信息
     */
    private String formatLocation(Location loc) {
        return String.format("%s (%.1f, %.1f, %.1f)", 
            loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ());
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // 第一个参数：子命令
            List<String> subCommands = Arrays.asList("spawn", "world", "lobby", "info");
            
            String input = args[0].toLowerCase();
            for (String sub : subCommands) {
                if (sub.startsWith(input)) {
                    completions.add(sub);
                }
            }
        } else if (args.length == 2) {
            // 第二个参数：游戏ID
            String input = args[1].toLowerCase();
            for (ManhuntGame game : plugin.getManhuntManager().getAllGames()) {
                String gameId = game.getGameId();
                if (gameId.toLowerCase().startsWith(input)) {
                    completions.add(gameId);
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("world")) {
            // world命令的第三个参数：世界名
            String input = args[2].toLowerCase();
            for (World world : plugin.getServer().getWorlds()) {
                String worldName = world.getName();
                if (worldName.toLowerCase().startsWith(input)) {
                    completions.add(worldName);
                }
            }
        }
        
        return completions;
    }
}
