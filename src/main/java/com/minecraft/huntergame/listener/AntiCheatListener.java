package com.minecraft.huntergame.listener;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.game.ManhuntGame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;

/**
 * 防作弊监听器
 * 禁止游戏中使用作弊命令
 * 
 * @author YourName
 * @version 1.0.0
 */
public class AntiCheatListener implements Listener {
    
    private final HunterGame plugin;
    
    public AntiCheatListener(HunterGame plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 监听玩家命令
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        
        // 检查玩家是否在游戏中
        ManhuntGame game = plugin.getManhuntManager().getPlayerGame(player);
        if (game == null) {
            return;
        }
        
        // 检查游戏是否正在进行
        if (game.getState() != com.minecraft.huntergame.game.GameState.PLAYING &&
            game.getState() != com.minecraft.huntergame.game.GameState.PREPARING) {
            return;
        }
        
        String command = event.getMessage().toLowerCase();
        
        // 移除开头的斜杠
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        
        // 检查是否是禁用的命令
        if (isCommandBlocked(command)) {
            event.setCancelled(true);
            player.sendMessage("§c游戏中禁止使用此命令！");
            plugin.getLogger().warning("玩家 " + player.getName() + " 尝试在游戏中使用禁用命令: " + command);
        }
    }
    
    /**
     * 检查命令是否被禁用
     */
    private boolean isCommandBlocked(String command) {
        // 获取命令的第一部分（不包括参数）
        String baseCommand = command.split(" ")[0];
        
        // 检查传送命令
        if (plugin.getManhuntConfig().isDisableTeleportCommands()) {
            if (baseCommand.equals("tp") || baseCommand.equals("teleport") ||
                baseCommand.equals("tpa") || baseCommand.equals("tphere") ||
                baseCommand.equals("tpaccept") || baseCommand.equals("tpdeny") ||
                baseCommand.startsWith("minecraft:tp") || baseCommand.startsWith("minecraft:teleport")) {
                return true;
            }
        }
        
        // 检查游戏模式命令
        if (plugin.getManhuntConfig().isDisableGamemodeCommands()) {
            if (baseCommand.equals("gamemode") || baseCommand.equals("gm") ||
                baseCommand.startsWith("minecraft:gamemode")) {
                return true;
            }
        }
        
        // 检查give命令
        if (plugin.getManhuntConfig().isDisableGiveCommands()) {
            if (baseCommand.equals("give") || baseCommand.startsWith("minecraft:give")) {
                return true;
            }
        }
        
        // 检查自定义禁用命令列表
        List<String> disabledCommands = plugin.getManhuntConfig().getDisabledCommands();
        for (String disabled : disabledCommands) {
            if (baseCommand.equals(disabled.toLowerCase())) {
                return true;
            }
        }
        
        return false;
    }
}
