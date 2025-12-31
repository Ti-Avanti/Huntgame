package com.minecraft.huntergame.command;

import com.minecraft.huntergame.HunterGame;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * 离开命令处理器
 * 处理 /hgleave 命令
 * 
 * @author YourName
 * @version 2.0.0 - Manhunt模式
 */
public class LeaveCommand implements CommandExecutor {
    
    private final HunterGame plugin;
    
    public LeaveCommand(HunterGame plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 检查权限
        if (!sender.hasPermission("huntergame.leave")) {
            plugin.getLanguageManager().sendMessage(sender, "general.no-permission");
            return true;
        }
        
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
        
        // TODO: 实现Manhunt游戏离开逻辑
        player.sendMessage("§c游戏离开功能将在Manhunt模式实现后可用");
        
        return true;
    }
}
