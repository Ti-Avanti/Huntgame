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
                
            case "start":
                return handleStart(sender, args);
                
            case "stop":
                return handleStop(sender, args);
                
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
                
            case "spectate":
                return handleSpectate(sender, args);
                
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
        sender.sendMessage("§e/hg start [世界名] §7- 开始游戏");
        sender.sendMessage("§e/hg stop [游戏ID] §7- 停止游戏");
        sender.sendMessage("§e/hg join [游戏ID] §7- 加入游戏");
        sender.sendMessage("§e/hg leave §7- 离开游戏");
        sender.sendMessage("§e/hg stats [玩家] §7- 查看统计");
        sender.sendMessage("§e/hg list §7- 查看游戏列表");
        sender.sendMessage("§e/hg spectate <玩家> §7- 观战玩家");
        
        if (sender.hasPermission("huntergame.admin")) {
            sender.sendMessage("§c/hg reload §7- 重载配置 §c[管理员]");
        }
    }
    
    /**
     * 处理开始游戏命令
     */
    private boolean handleStart(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getLanguageManager().sendMessage(sender, "command.player-only");
            return true;
        }
        
        Player player = (Player) sender;
        
        // 检查权限
        if (!player.hasPermission("huntergame.admin.start")) {
            plugin.getLanguageManager().sendMessage(player, "command.no-permission");
            return true;
        }
        
        // 如果指定了游戏ID，则开始该游戏
        if (args.length > 1) {
            String gameId = args[1];
            com.minecraft.huntergame.game.ManhuntGame game = plugin.getManhuntManager().getGame(gameId);
            
            if (game == null) {
                player.sendMessage("§c游戏不存在: " + gameId);
                return true;
            }
            
            // 检查游戏状态
            if (game.getState() != com.minecraft.huntergame.game.GameState.WAITING) {
                player.sendMessage("§c游戏已经开始了！");
                return true;
            }
            
            // 检查人数
            int minPlayers = plugin.getManhuntConfig().getMinPlayers();
            if (game.getPlayerCount() < minPlayers) {
                player.sendMessage("§c人数不足！至少需要 " + minPlayers + " 名玩家");
                return true;
            }
            
            // 开始游戏
            plugin.getManhuntManager().startGame(gameId);
            return true;
        }
        
        // 如果没有指定游戏ID，则创建新游戏
        // 检查玩家是否已在游戏中
        if (plugin.getManhuntManager().isInGame(player)) {
            player.sendMessage("§c你已经在游戏中了！");
            return true;
        }
        
        // 获取世界名称
        String worldName = player.getWorld().getName();
        
        // 创建游戏
        com.minecraft.huntergame.game.ManhuntGame game = plugin.getManhuntManager().createGame(worldName);
        
        // 设置出生点为玩家当前位置
        game.setSpawnLocation(player.getLocation());
        
        // 玩家加入游戏
        if (!plugin.getManhuntManager().joinGame(player, game.getGameId())) {
            player.sendMessage("§c加入游戏失败！");
            plugin.getManhuntManager().removeGame(game.getGameId());
            return true;
        }
        
        player.sendMessage("§a游戏已创建！游戏ID: §e" + game.getGameId());
        player.sendMessage("§e其他玩家可以使用 §a/hg join " + game.getGameId() + " §e加入游戏");
        player.sendMessage("§e当所有玩家准备好后，使用 §a/hg start " + game.getGameId() + " §e开始游戏");
        
        return true;
    }
    
    /**
     * 处理停止游戏命令
     */
    private boolean handleStop(CommandSender sender, String[] args) {
        // 检查权限
        if (!sender.hasPermission("huntergame.admin.stop")) {
            plugin.getLanguageManager().sendMessage(sender, "command.no-permission");
            return true;
        }
        
        // 获取游戏ID
        String gameId;
        if (args.length > 1) {
            gameId = args[1];
        } else {
            // 如果是玩家，尝试获取其所在游戏
            if (sender instanceof Player) {
                Player player = (Player) sender;
                gameId = plugin.getManhuntManager().getPlayerGameId(player);
                if (gameId == null) {
                    player.sendMessage("§c你不在任何游戏中！请指定游戏ID");
                    return true;
                }
            } else {
                sender.sendMessage("§c请指定游戏ID: /hg stop <游戏ID>");
                return true;
            }
        }
        
        // 检查游戏是否存在
        com.minecraft.huntergame.game.ManhuntGame game = plugin.getManhuntManager().getGame(gameId);
        if (game == null) {
            sender.sendMessage("§c游戏不存在: " + gameId);
            return true;
        }
        
        // 强制停止游戏
        plugin.getManhuntManager().forceStopGame(gameId);
        sender.sendMessage("§a游戏已强制停止: " + gameId);
        
        return true;
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
        
        // 检查玩家是否已在游戏中
        if (plugin.getManhuntManager().isInGame(player)) {
            player.sendMessage("§c你已经在游戏中了！");
            return true;
        }
        
        // 检查是否指定了游戏ID
        if (args.length < 2) {
            player.sendMessage("§c请指定游戏ID: /hg join <游戏ID>");
            return true;
        }
        
        String gameId = args[1];
        
        // 检查游戏是否存在
        com.minecraft.huntergame.game.ManhuntGame game = plugin.getManhuntManager().getGame(gameId);
        if (game == null) {
            player.sendMessage("§c游戏不存在: " + gameId);
            return true;
        }
        
        // 检查游戏状态
        if (game.getState() != com.minecraft.huntergame.game.GameState.WAITING) {
            player.sendMessage("§c游戏已经开始，无法加入！");
            return true;
        }
        
        // 检查人数限制
        if (game.getPlayerCount() >= plugin.getManhuntConfig().getMaxPlayers()) {
            player.sendMessage("§c游戏人数已满！");
            return true;
        }
        
        // 加入游戏
        if (plugin.getManhuntManager().joinGame(player, gameId)) {
            player.sendMessage("§a成功加入游戏: " + gameId);
            
            // 广播加入消息
            for (java.util.UUID uuid : game.getAllPlayers()) {
                Player p = plugin.getServer().getPlayer(uuid);
                if (p != null && !p.equals(player)) {
                    p.sendMessage("§e玩家 " + player.getName() + " 加入了游戏 §7(" + 
                        game.getPlayerCount() + "/" + plugin.getManhuntConfig().getMaxPlayers() + ")");
                }
            }
        } else {
            player.sendMessage("§c加入游戏失败！");
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
        
        // 检查玩家是否在游戏中
        if (!plugin.getManhuntManager().isInGame(player)) {
            player.sendMessage("§c你不在任何游戏中！");
            return true;
        }
        
        // 获取游戏
        com.minecraft.huntergame.game.ManhuntGame game = plugin.getManhuntManager().getPlayerGame(player);
        String gameId = game != null ? game.getGameId() : "未知";
        
        // 离开游戏
        plugin.getManhuntManager().leaveGame(player);
        player.sendMessage("§a你已离开游戏: " + gameId);
        
        // 广播离开消息
        if (game != null) {
            for (java.util.UUID uuid : game.getAllPlayers()) {
                Player p = plugin.getServer().getPlayer(uuid);
                if (p != null) {
                    p.sendMessage("§e玩家 " + player.getName() + " 离开了游戏");
                }
            }
        }
        
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
            plugin.reloadConfig();
            plugin.getMainConfig().reload();
            plugin.getManhuntConfig().reload();
            plugin.getLanguageManager().reload();
            
            plugin.getLanguageManager().sendMessage(sender, "command.reload-success");
            sender.sendMessage("§a配置已重载！");
        } catch (Exception ex) {
            plugin.getLanguageManager().sendMessage(sender, "command.reload-failed");
            sender.sendMessage("§c重载失败: " + ex.getMessage());
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
        
        sender.sendMessage("§6========== §e游戏列表 §6==========");
        
        java.util.Collection<com.minecraft.huntergame.game.ManhuntGame> games = 
            plugin.getManhuntManager().getAllGames();
        
        if (games.isEmpty()) {
            sender.sendMessage("§7当前没有进行中的游戏");
        } else {
            for (com.minecraft.huntergame.game.ManhuntGame game : games) {
                String status = game.getState().toString();
                int playerCount = game.getPlayerCount();
                int maxPlayers = plugin.getManhuntConfig().getMaxPlayers();
                
                sender.sendMessage("§e" + game.getGameId() + " §7- §a" + status + 
                    " §7(" + playerCount + "/" + maxPlayers + ")");
            }
        }
        
        sender.sendMessage("§7总计: §e" + games.size() + " §7个游戏");
        
        return true;
    }
    
    /**
     * 处理观战命令
     */
    private boolean handleSpectate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getLanguageManager().sendMessage(sender, "command.player-only");
            return true;
        }
        
        Player player = (Player) sender;
        
        // 检查玩家是否在游戏中
        com.minecraft.huntergame.game.ManhuntGame game = plugin.getManhuntManager().getPlayerGame(player);
        if (game == null) {
            player.sendMessage("§c你不在任何游戏中！");
            return true;
        }
        
        // 检查玩家是否是观战者
        com.minecraft.huntergame.game.PlayerRole role = game.getPlayerRole(player.getUniqueId());
        if (role != com.minecraft.huntergame.game.PlayerRole.SPECTATOR) {
            player.sendMessage("§c只有观战者可以使用此命令！");
            return true;
        }
        
        // 检查是否指定了目标玩家
        if (args.length < 2) {
            player.sendMessage("§c请指定目标玩家: /hg spectate <玩家名>");
            return true;
        }
        
        String targetName = args[1];
        Player target = plugin.getServer().getPlayer(targetName);
        
        if (target == null || !target.isOnline()) {
            player.sendMessage("§c玩家不在线: " + targetName);
            return true;
        }
        
        // 检查目标是否在同一游戏中
        com.minecraft.huntergame.game.ManhuntGame targetGame = plugin.getManhuntManager().getPlayerGame(target);
        if (targetGame == null || !targetGame.getGameId().equals(game.getGameId())) {
            player.sendMessage("§c目标玩家不在你的游戏中！");
            return true;
        }
        
        // 检查目标是否是观战者
        com.minecraft.huntergame.game.PlayerRole targetRole = game.getPlayerRole(target.getUniqueId());
        if (targetRole == com.minecraft.huntergame.game.PlayerRole.SPECTATOR) {
            player.sendMessage("§c不能观战其他观战者！");
            return true;
        }
        
        // 传送到目标玩家
        player.teleport(target.getLocation());
        player.sendMessage("§a已传送到 " + target.getName());
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // 第一个参数：子命令
            List<String> subCommands = Arrays.asList("help", "start", "stop", "join", "leave", "stats", "list");
            
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
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("join") || args[0].equalsIgnoreCase("stop")) {
                // join/stop 命令的第二个参数：游戏ID
                String input = args[1].toLowerCase();
                for (com.minecraft.huntergame.game.ManhuntGame game : plugin.getManhuntManager().getAllGames()) {
                    String gameId = game.getGameId();
                    if (gameId.toLowerCase().startsWith(input)) {
                        completions.add(gameId);
                    }
                }
            } else if (args[0].equalsIgnoreCase("start")) {
                // start 命令的第二个参数：世界名称
                String input = args[1].toLowerCase();
                for (org.bukkit.World world : plugin.getServer().getWorlds()) {
                    String worldName = world.getName();
                    if (worldName.toLowerCase().startsWith(input)) {
                        completions.add(worldName);
                    }
                }
            }
        }
        
        return completions;
    }
}
