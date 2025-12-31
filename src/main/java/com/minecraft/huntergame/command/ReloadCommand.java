package com.minecraft.huntergame.command;

import com.minecraft.huntergame.HunterGame;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * 重载配置命令
 * 用于重新加载插件配置
 * 
 * @author YourName
 * @version 1.0.0
 */
public class ReloadCommand implements CommandExecutor {
    
    private final HunterGame plugin;
    
    public ReloadCommand(HunterGame plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 检查权限
        if (!sender.hasPermission("huntergame.admin.reload")) {
            sender.sendMessage(ChatColor.RED + "你没有权限使用此命令！");
            return true;
        }
        
        sender.sendMessage(ChatColor.YELLOW + "正在重载配置...");
        
        try {
            // 重载主配置
            plugin.reloadConfig();
            
            // 重载Manhunt配置
            if (plugin.getManhuntConfig() != null) {
                plugin.getManhuntConfig().reload();
            }
            
            // 重载语言配置
            if (plugin.getLanguageManager() != null) {
                plugin.getLanguageManager().reload();
            }
            
            sender.sendMessage(ChatColor.GREEN + "配置重载成功！");
            plugin.getLogger().info(sender.getName() + " 重载了插件配置");
            
        } catch (Exception ex) {
            sender.sendMessage(ChatColor.RED + "配置重载失败: " + ex.getMessage());
            plugin.getLogger().severe("配置重载失败: " + ex.getMessage());
            ex.printStackTrace();
        }
        
        return true;
    }
}
