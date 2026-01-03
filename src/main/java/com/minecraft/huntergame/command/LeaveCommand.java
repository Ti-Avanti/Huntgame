package com.minecraft.huntergame.command;

import com.minecraft.huntergame.HunterGame;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * 离开命令处理器
 * 处理 /hgleave 命令
 * 
 * @author YourName
 * @version 2.0.0 - Manhunt模式
 */
public class LeaveCommand implements CommandExecutor, TabCompleter {
    
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
        
        // 获取玩家所在的游戏
        com.minecraft.huntergame.game.ManhuntGame game = plugin.getManhuntManager().getPlayerGame(player);
        
        if (game == null) {
            player.sendMessage("§c你当前不在任何游戏中！");
            return true;
        }
        
        // 检查游戏状态
        com.minecraft.huntergame.game.GameState state = game.getState();
        
        // 如果游戏正在进行中，警告玩家
        if (state == com.minecraft.huntergame.game.GameState.PLAYING) {
            player.sendMessage("§e警告：游戏正在进行中，离开将被视为放弃！");
        }
        
        // 移除玩家
        plugin.getManhuntManager().removePlayer(player, game);
        
        // 传送玩家到主世界出生点
        if (!plugin.getServer().getWorlds().isEmpty()) {
            Location spawnLocation = plugin.getServer().getWorlds().get(0).getSpawnLocation();
            player.teleport(spawnLocation);
        } else {
            plugin.getLogger().warning("无法传送玩家 " + player.getName() + "：没有可用的世界");
        }
        
        // 清理玩家状态
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.getInventory().clear();
        player.setGameMode(org.bukkit.GameMode.ADVENTURE);
        
        // 移除计分板
        plugin.getSidebarManager().removeSidebar(player);
        
        // 发送消息
        player.sendMessage("§a你已离开游戏！");
        
        // 通知其他玩家
        game.broadcast("§e玩家 §6" + player.getName() + " §e离开了游戏");
        
        // 检查游戏是否需要结束
        if (state == com.minecraft.huntergame.game.GameState.PLAYING) {
            // 检查是否所有逃亡者都离开了
            if (game.getAliveRunners().isEmpty()) {
                game.broadcast("§c所有逃亡者已离开，游戏结束！");
                plugin.getManhuntManager().endGame(game, com.minecraft.huntergame.game.GameEndReason.RUNNERS_LEFT);
            }
            // 检查是否所有猎人都离开了
            else if (game.getHunters().isEmpty()) {
                game.broadcast("§c所有猎人已离开，游戏结束！");
                plugin.getManhuntManager().endGame(game, com.minecraft.huntergame.game.GameEndReason.HUNTERS_LEFT);
            }
        }
        // 如果是等待或准备阶段，检查人数是否足够
        else if (state == com.minecraft.huntergame.game.GameState.WAITING || 
                 state == com.minecraft.huntergame.game.GameState.PREPARING) {
            if (game.getPlayerCount() < game.getMinPlayersToStart()) {
                game.broadcast("§c人数不足，游戏已取消！");
                plugin.getManhuntManager().cancelGame(game);
            }
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // leave命令不需要参数，返回空列表
        return new ArrayList<>();
    }
}
