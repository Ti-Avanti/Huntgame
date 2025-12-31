package com.minecraft.huntergame.command;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.arena.Arena;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 管理员命令
 * 处理竞技场管理、配置重载等管理员操作
 * 
 * @author YourName
 * @version 1.0.0
 */
public class AdminCommand implements CommandExecutor, TabCompleter {
    
    private final HunterGame plugin;
    
    public AdminCommand(HunterGame plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 检查权限
        if (!sender.hasPermission("huntergame.admin")) {
            plugin.getLanguageManager().sendMessage(sender, "general.no-permission");
            return true;
        }
        
        // 检查参数
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "reload":
                handleReload(sender);
                break;
            case "list":
                handleList(sender);
                break;
            case "enable":
                if (args.length < 2) {
                    sender.sendMessage("§c用法: /hgadmin enable <竞技场>");
                    return true;
                }
                handleEnable(sender, args[1]);
                break;
            case "disable":
                if (args.length < 2) {
                    sender.sendMessage("§c用法: /hgadmin disable <竞技场>");
                    return true;
                }
                handleDisable(sender, args[1]);
                break;
            case "info":
                if (args.length < 2) {
                    sender.sendMessage("§c用法: /hgadmin info <竞技场>");
                    return true;
                }
                handleInfo(sender, args[1]);
                break;
            default:
                sendHelp(sender);
                break;
        }
        
        return true;
    }
    
    /**
     * 发送帮助信息
     */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§e=== §6猎人游戏管理员命令 §e===");
        sender.sendMessage("§e/hgadmin reload §7- 重载配置");
        sender.sendMessage("§e/hgadmin list §7- 列出所有竞技场");
        sender.sendMessage("§e/hgadmin enable <竞技场> §7- 启用竞技场");
        sender.sendMessage("§e/hgadmin disable <竞技场> §7- 禁用竞技场");
        sender.sendMessage("§e/hgadmin info <竞技场> §7- 查看竞技场信息");
    }
    
    /**
     * 处理重载命令
     */
    private void handleReload(CommandSender sender) {
        try {
            // 重载主配置
            plugin.reloadConfig();
            plugin.getMainConfig().reload();
            
            // 重载语言文件
            plugin.getLanguageManager().reload();
            
            // 重载所有竞技场配置
            for (String arenaName : plugin.getArenaManager().getArenaNames()) {
                Arena arena = plugin.getArenaManager().getArena(arenaName);
                if (arena != null) {
                    arena.getConfig().reload();
                }
            }
            
            plugin.getLanguageManager().sendMessage(sender, "general.config-reloaded");
            plugin.getLogger().info("配置已被 " + sender.getName() + " 重载");
            
        } catch (Exception ex) {
            sender.sendMessage("§c重载配置失败: " + ex.getMessage());
            plugin.getLogger().severe("重载配置失败: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    /**
     * 处理列表命令
     */
    private void handleList(CommandSender sender) {
        List<String> arenaNames = new ArrayList<>(plugin.getArenaManager().getArenaNames());
        
        if (arenaNames.isEmpty()) {
            sender.sendMessage("§c没有已加载的竞技场");
            return;
        }
        
        sender.sendMessage("§e=== §6竞技场列表 §e===");
        for (String arenaName : arenaNames) {
            Arena arena = plugin.getArenaManager().getArena(arenaName);
            if (arena != null) {
                String status = arena.getConfig().isEnabled() ? "§a启用" : "§c禁用";
                String state = arena.getState().toString();
                int players = arena.getPlayerCount();
                int maxPlayers = arena.getMaxPlayers();
                
                sender.sendMessage(String.format("§e%s §7- %s §7| §e%s §7| §e%d/%d",
                    arenaName, status, state, players, maxPlayers));
            }
        }
    }
    
    /**
     * 处理启用命令
     */
    private void handleEnable(CommandSender sender, String arenaName) {
        Arena arena = plugin.getArenaManager().getArena(arenaName);
        
        if (arena == null) {
            sender.sendMessage("§c竞技场不存在: " + arenaName);
            return;
        }
        
        arena.getConfig().setEnabled(true);
        arena.getConfig().save();
        arena.enable();
        
        sender.sendMessage("§a已启用竞技场: " + arenaName);
        plugin.getLogger().info("竞技场 " + arenaName + " 已被 " + sender.getName() + " 启用");
    }
    
    /**
     * 处理禁用命令
     */
    private void handleDisable(CommandSender sender, String arenaName) {
        Arena arena = plugin.getArenaManager().getArena(arenaName);
        
        if (arena == null) {
            sender.sendMessage("§c竞技场不存在: " + arenaName);
            return;
        }
        
        arena.getConfig().setEnabled(false);
        arena.getConfig().save();
        arena.disable();
        
        sender.sendMessage("§c已禁用竞技场: " + arenaName);
        plugin.getLogger().info("竞技场 " + arenaName + " 已被 " + sender.getName() + " 禁用");
    }
    
    /**
     * 处理信息命令
     */
    private void handleInfo(CommandSender sender, String arenaName) {
        Arena arena = plugin.getArenaManager().getArena(arenaName);
        
        if (arena == null) {
            sender.sendMessage("§c竞技场不存在: " + arenaName);
            return;
        }
        
        sender.sendMessage("§e=== §6" + arena.getDisplayName() + " §e===");
        sender.sendMessage("§e名称: §f" + arenaName);
        sender.sendMessage("§e状态: §f" + (arena.getConfig().isEnabled() ? "§a启用" : "§c禁用"));
        sender.sendMessage("§e游戏状态: §f" + arena.getState());
        sender.sendMessage("§e玩家: §f" + arena.getPlayerCount() + "/" + arena.getMaxPlayers());
        sender.sendMessage("§e最小玩家: §f" + arena.getMinPlayers());
        sender.sendMessage("§e游戏时长: §f" + arena.getGameTime() + "秒");
        sender.sendMessage("§e世界: §f" + arena.getWorld().getName());
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (!sender.hasPermission("huntergame.admin")) {
            return completions;
        }
        
        if (args.length == 1) {
            completions.addAll(Arrays.asList("reload", "list", "enable", "disable", "info"));
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("enable") || subCommand.equals("disable") || subCommand.equals("info")) {
                completions.addAll(new ArrayList<>(plugin.getArenaManager().getArenaNames()));
            }
        }
        
        return completions;
    }
}
