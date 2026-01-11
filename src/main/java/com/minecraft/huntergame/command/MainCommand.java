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
                
            case "season":
                return handleSeason(sender, args);
                
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
        sender.sendMessage("§e/hg stats [玩家] §7- 查看段位");
        sender.sendMessage("§e/hg list §7- 查看游戏列表");
        sender.sendMessage("§e/hg spectate <玩家> §7- 观战玩家");
        
        if (sender.hasPermission("huntergame.admin")) {
            sender.sendMessage("§c/hg reload §7- 重载配置 §c[管理员]");
            sender.sendMessage("§c/hg season §7- 赛季管理 §c[管理员]");
        }
    }
    
    /**
     * 处理开始游戏命令
     */
    private boolean handleStart(CommandSender sender, String[] args) {
        plugin.debug("handleStart called by " + sender.getName());
        
        if (!(sender instanceof Player)) {
            plugin.getLanguageManager().sendMessage(sender, "command.player-only");
            return true;
        }
        
        Player player = (Player) sender;
        plugin.debug("Player: " + player.getName());
        
        // 检查权限
        if (!player.hasPermission("huntergame.admin.start")) {
            plugin.debug("Player lacks permission: huntergame.admin.start");
            plugin.getLanguageManager().sendMessage(player, "command.no-permission");
            return true;
        }
        
        plugin.debug("Permission check passed");
        
        // 如果指定了游戏ID，则开始该游戏
        if (args.length > 1) {
            String gameId = args[1];
            plugin.debug("Starting existing game: " + gameId);
            
            com.minecraft.huntergame.game.ManhuntGame game = plugin.getManhuntManager().getGame(gameId);
            
            if (game == null) {
                plugin.debug("Game not found: " + gameId);
                player.sendMessage("§c游戏不存在: " + gameId);
                return true;
            }
            
            plugin.debug("Game found, state: " + game.getState());
            
            // 检查游戏状态
            if (game.getState() != com.minecraft.huntergame.game.GameState.WAITING) {
                plugin.debug("Game already started, state: " + game.getState());
                player.sendMessage("§c游戏已经开始了！");
                return true;
            }
            
            // 检查人数
            int minPlayers = plugin.getManhuntConfig().getMinPlayers();
            int currentPlayers = game.getPlayerCount();
            plugin.debug("Player count check: " + currentPlayers + "/" + minPlayers);
            
            if (currentPlayers < minPlayers) {
                plugin.debug("Not enough players: " + currentPlayers + " < " + minPlayers);
                player.sendMessage("§c人数不足！至少需要 " + minPlayers + " 名玩家");
                player.sendMessage("§e当前玩家数: " + currentPlayers);
                return true;
            }
            
            // 开始游戏
            plugin.debug("Starting game: " + gameId);
            plugin.getManhuntManager().startGame(gameId);
            player.sendMessage("§a游戏已开始！");
            return true;
        }
        
        // 如果没有指定游戏ID，则创建新游戏
        plugin.debug("Creating new game");
        
        // Bungee 模式：检查服务器类型
        if (plugin.getServerMode() == com.minecraft.huntergame.ServerMode.BUNGEE) {
            com.minecraft.huntergame.config.ServerType serverType = plugin.getManhuntConfig().getServerType();
            
            if (serverType == com.minecraft.huntergame.config.ServerType.MAIN_LOBBY) {
                plugin.debug("Cannot create game on MAIN_LOBBY server");
                player.sendMessage("§c主大厅服务器不能创建游戏！");
                player.sendMessage("§e请使用 §a/hg gui §e打开游戏大厅，系统会自动分配游戏服务器");
                return true;
            }
        }
        
        // 检查是否可以创建游戏
        if (!plugin.getManhuntManager().canCreateGame()) {
            plugin.debug("Cannot create game: canCreateGame() returned false");
            player.sendMessage("§c当前无法创建游戏！");
            
            // 提供更详细的错误信息
            if (plugin.getManhuntConfig().isSingleGameMode() && !plugin.getManhuntManager().getAllGames().isEmpty()) {
                player.sendMessage("§e原因: 单场比赛模式下已有游戏正在进行");
            }
            
            return true;
        }
        
        // 检查玩家是否已在游戏中
        if (plugin.getManhuntManager().isInGame(player)) {
            plugin.debug("Player already in game");
            player.sendMessage("§c你已经在游戏中了！");
            return true;
        }
        
        // 获取世界名称
        String worldName = player.getWorld().getName();
        plugin.debug("World name: " + worldName);
        
        // 创建游戏
        com.minecraft.huntergame.game.ManhuntGame game = plugin.getManhuntManager().createGame(worldName);
        
        if (game == null) {
            plugin.debug("Failed to create game");
            player.sendMessage("§c创建游戏失败！");
            return true;
        }
        
        plugin.debug("Game created: " + game.getGameId());
        
        // 设置出生点为玩家当前位置
        game.setSpawnLocation(player.getLocation());
        plugin.debug("Spawn location set: " + player.getLocation());
        
        // 玩家加入游戏
        boolean joined = plugin.getManhuntManager().joinGame(player, game.getGameId());
        plugin.debug("Join game result: " + joined);
        
        if (!joined) {
            plugin.debug("Failed to join game");
            player.sendMessage("§c加入游戏失败！");
            plugin.getManhuntManager().removeGame(game.getGameId());
            return true;
        }
        
        player.sendMessage("§a游戏已创建！游戏ID: §e" + game.getGameId());
        player.sendMessage("§e其他玩家可以使用 §a/hg join " + game.getGameId() + " §e加入游戏");
        player.sendMessage("§e当所有玩家准备好后，使用 §a/hg start " + game.getGameId() + " §e开始游戏");
        
        plugin.debug("Game creation completed successfully");
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
        
        // 检查玩家是否已在游戏中
        if (plugin.getManhuntManager().isInGame(player)) {
            player.sendMessage("§c你已经在游戏中了！");
            return true;
        }
        
        // 单服务器模式：自动加入或创建游戏
        if (plugin.getServerMode() == com.minecraft.huntergame.ServerMode.STANDALONE) {
            return handleAutoJoin(player);
        }
        
        // Bungee模式：需要指定游戏ID
        if (args.length < 2) {
            player.sendMessage("§c请指定游戏ID: /hg join <游戏ID>");
            return true;
        }
        
        String gameId = args[1];
        com.minecraft.huntergame.game.ManhuntGame game = plugin.getManhuntManager().getGame(gameId);
        
        if (game == null) {
            player.sendMessage("§c游戏不存在: " + gameId);
            return true;
        }
        
        // 检查游戏状态
        if (!game.getState().isJoinable()) {
            player.sendMessage("§c游戏已经开始，无法加入！");
            return true;
        }
        
        // 加入游戏
        boolean joined = plugin.getManhuntManager().joinGame(player, gameId);
        
        if (joined) {
            player.sendMessage("§a成功加入游戏！");
            player.sendMessage("§e当前玩家数: §a" + game.getPlayerCount() + "§7/§a" + (game.getMaxRunners() + game.getMaxHunters()));
        } else {
            player.sendMessage("§c加入游戏失败！游戏可能已满");
        }
        
        return true;
    }
    
    /**
     * 处理自动加入（单服务器模式）
     */
    private boolean handleAutoJoin(Player player) {
        // Bungee 模式：主大厅服务器应该传送玩家到子大厅
        if (plugin.getServerMode() == com.minecraft.huntergame.ServerMode.BUNGEE) {
            com.minecraft.huntergame.config.ServerType serverType = plugin.getManhuntConfig().getServerType();
            
            if (serverType == com.minecraft.huntergame.config.ServerType.MAIN_LOBBY) {
                plugin.debug("MAIN_LOBBY: Transferring player to SUB_LOBBY");
                
                // 传送玩家到最佳游戏服务器
                boolean success = plugin.getBungeeManager().sendPlayerToBestServer(player);
                
                if (!success) {
                    player.sendMessage("§c当前没有可用的游戏服务器，请稍后重试");
                }
                
                return true;
            }
        }
        
        // 单服务器模式或子大厅模式：检查是否可以创建游戏
        if (!plugin.getManhuntManager().canCreateGame()) {
            plugin.debug("Cannot create game: canCreateGame() returned false");
            player.sendMessage("§c当前无法创建游戏！");
            
            // 提供更详细的错误信息
            if (plugin.getManhuntConfig().isSingleGameMode() && !plugin.getManhuntManager().getAllGames().isEmpty()) {
                player.sendMessage("§e原因: 单场比赛模式下已有游戏正在进行");
                player.sendMessage("§e请等待当前游戏结束后再试");
            }
            
            return true;
        }
        
        // 检查是否已有游戏
        com.minecraft.huntergame.game.ManhuntGame existingGame = null;
        
        for (com.minecraft.huntergame.game.ManhuntGame game : plugin.getManhuntManager().getAllGames()) {
            if (game.getState().isJoinable()) {
                existingGame = game;
                break;
            }
        }
        
        // 如果有可加入的游戏，直接加入
        if (existingGame != null) {
            boolean joined = plugin.getManhuntManager().joinGame(player, existingGame.getGameId());
            
            if (joined) {
                player.sendMessage("§a成功加入游戏！");
                player.sendMessage("§e当前玩家数: §a" + existingGame.getPlayerCount() + "§7/§a" + (existingGame.getMaxRunners() + existingGame.getMaxHunters()));
                
                // 如果达到最小人数，提示可以开始
                if (existingGame.hasMinPlayers() && existingGame.getState() == com.minecraft.huntergame.game.GameState.WAITING) {
                    player.sendMessage("§e已达到最小人数，等待更多玩家加入...");
                }
            } else {
                player.sendMessage("§c加入游戏失败！游戏可能已满");
            }
            
            return true;
        }
        
        // 如果没有游戏，自动创建
        String worldName = plugin.getManhuntConfig().getWorldName();
        
        // 加载或创建游戏世界
        org.bukkit.World gameWorld = plugin.getWorldManager().loadOrCreateWorld(worldName);
        if (gameWorld == null) {
            player.sendMessage("§c无法创建游戏世界！");
            return true;
        }
        
        // 创建游戏
        com.minecraft.huntergame.game.ManhuntGame game = plugin.getManhuntManager().createGame(worldName);
        
        if (game == null) {
            player.sendMessage("§c创建游戏失败！");
            return true;
        }
        
        // 玩家加入游戏
        boolean joined = plugin.getManhuntManager().joinGame(player, game.getGameId());
        
        if (joined) {
            player.sendMessage("§a成功创建并加入游戏！");
            player.sendMessage("§e等待更多玩家加入...");
            player.sendMessage("§7提示: 达到 §a" + plugin.getManhuntConfig().getMinPlayersToStart() + " §7人后将自动开始匹配");
        } else {
            player.sendMessage("§c加入游戏失败！");
            plugin.getManhuntManager().removeGame(game.getGameId());
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
        
        // 如果没有参数，显示自己的统计
        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§c控制台请指定玩家名称！");
                sender.sendMessage("§e用法: /hg stats <玩家名>");
                return true;
            }
            
            Player player = (Player) sender;
            showPlayerStats(player, player);
            return true;
        }
        
        // 显示指定玩家的统计
        if (args.length == 2) {
            String targetName = args[1];
            Player target = plugin.getServer().getPlayer(targetName);
            
            if (target == null) {
                // 玩家不在线，尝试从数据库加载
                sender.sendMessage("§e正在查询玩家 §6" + targetName + " §e的统计数据...");
                
                // 异步查询数据库
                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                    try {
                        // 通过名称查找UUID（这里简化处理，实际应该有名称->UUID的映射）
                        com.minecraft.huntergame.models.PlayerData data = null;
                        
                        // 尝试从缓存中查找
                        for (Player p : plugin.getServer().getOnlinePlayers()) {
                            if (p.getName().equalsIgnoreCase(targetName)) {
                                data = plugin.getStatsManager().getPlayerData(p.getUniqueId());
                                break;
                            }
                        }
                        
                        if (data == null) {
                            plugin.getServer().getScheduler().runTask(plugin, () -> {
                                sender.sendMessage("§c未找到玩家 " + targetName + " 的数据！");
                            });
                            return;
                        }
                        
                        // 在主线程显示统计
                        com.minecraft.huntergame.models.PlayerData finalData = data;
                        plugin.getServer().getScheduler().runTask(plugin, () -> {
                            showPlayerStatsData(sender, finalData);
                        });
                    } catch (Exception ex) {
                        plugin.getServer().getScheduler().runTask(plugin, () -> {
                            sender.sendMessage("§c查询统计数据时出错: " + ex.getMessage());
                        });
                    }
                });
                
                return true;
            }
            
            // 玩家在线，直接显示
            showPlayerStats(sender, target);
            return true;
        }
        
        sender.sendMessage("§c用法: /hg stats [玩家名]");
        return true;
    }
    
    /**
     * 显示玩家统计（玩家在线）
     */
    private void showPlayerStats(CommandSender sender, Player target) {
        com.minecraft.huntergame.models.PlayerData data = plugin.getStatsManager().getPlayerData(target.getUniqueId());
        
        if (data == null) {
            sender.sendMessage("§c未找到玩家 " + target.getName() + " 的统计数据！");
            return;
        }
        
        showPlayerStatsData(sender, data);
    }
    
    /**
     * 显示玩家统计数据
     */
    private void showPlayerStatsData(CommandSender sender, com.minecraft.huntergame.models.PlayerData data) {
        sender.sendMessage("§6========== §e玩家统计 §6==========");
        sender.sendMessage("§e玩家: §a" + data.getName());
        sender.sendMessage("");
        sender.sendMessage("§6基础统计:");
        sender.sendMessage("  §e游戏场次: §a" + data.getGamesPlayed());
        sender.sendMessage("  §e胜利次数: §a" + data.getWins());
        sender.sendMessage("  §e失败次数: §c" + data.getLosses());
        
        // 计算胜率
        if (data.getGamesPlayed() > 0) {
            double winRate = (double) data.getWins() / data.getGamesPlayed() * 100;
            sender.sendMessage("  §e胜率: §a" + String.format("%.1f%%", winRate));
        }
        
        sender.sendMessage("");
        sender.sendMessage("§6猎人统计:");
        sender.sendMessage("  §e击杀数: §a" + data.getHunterKills());
        sender.sendMessage("  §e死亡数: §c" + data.getHunterDeaths());
        sender.sendMessage("  §e胜利次数: §a" + data.getHunterWins());
        
        // 计算KD比
        if (data.getHunterDeaths() > 0) {
            double kd = (double) data.getHunterKills() / data.getHunterDeaths();
            sender.sendMessage("  §eK/D比: §a" + String.format("%.2f", kd));
        }
        
        sender.sendMessage("");
        sender.sendMessage("§6逃亡者统计:");
        sender.sendMessage("  §e逃脱次数: §a" + data.getSurvivorEscapes());
        sender.sendMessage("  §e死亡数: §c" + data.getSurvivorDeaths());
        sender.sendMessage("  §e胜利次数: §a" + data.getRunnerWins());
        sender.sendMessage("  §e击败末影龙: §a" + data.getDragonKills() + " 次");
        
        // 计算平均生存时间
        if (data.getGamesPlayed() > 0) {
            int avgTime = data.getTotalSurvivalTime() / data.getGamesPlayed();
            sender.sendMessage("  §e平均生存时间: §a" + com.minecraft.huntergame.util.TimeUtil.formatTimeChinese(avgTime));
        }
        
        sender.sendMessage("§6================================");
    }
    
    /**
     * 处理重载命令
     */
    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("huntergame.admin.reload")) {
            plugin.getLanguageManager().sendMessage(sender, "command.no-permission");
            return true;
        }
        
        sender.sendMessage("§e正在重载配置...");
        
        try {
            // 1. 先重载ConfigManager中的所有配置文件
            plugin.getConfigManager().reloadAll();
            
            // 2. 然后重载各个配置类（它们会从ConfigManager获取最新的配置）
            plugin.getMainConfig().reload();
            plugin.getManhuntConfig().reload();
            plugin.getScoreboardConfig().reload();
            plugin.getMessagesConfig().reload();
            plugin.getRewardsConfig().reload();
            plugin.getLanguageManager().reload();
            
            sender.sendMessage("§a配置重载成功！");
            plugin.getLogger().info(sender.getName() + " 重载了插件配置");
            
        } catch (Exception ex) {
            sender.sendMessage("§c配置重载失败: " + ex.getMessage());
            plugin.getLogger().severe("配置重载失败: " + ex.getMessage());
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
    
    /**
     * 处理赛季命令
     */
    private boolean handleSeason(CommandSender sender, String[] args) {
        // 检查管理员权限
        if (!sender.hasPermission("huntergame.admin")) {
            plugin.getLanguageManager().sendMessage(sender, "general.no-permission");
            return true;
        }
        
        // 如果没有子命令，显示赛季信息
        if (args.length < 2) {
            showSeasonInfo(sender);
            return true;
        }
        
        String subCommand = args[1].toLowerCase();
        
        switch (subCommand) {
            case "info":
                showSeasonInfo(sender);
                break;
                
            case "reset":
                // 重置当前赛季
                sender.sendMessage("§e正在重置赛季...");
                plugin.getSeasonManager().resetSeason();
                sender.sendMessage("§a赛季已重置！");
                break;
                
            case "new":
            case "start":
                // 开始新赛季
                sender.sendMessage("§e正在开始新赛季...");
                plugin.getSeasonManager().startNewSeasonAndReset();
                sender.sendMessage("§a新赛季已开始！");
                break;
                
            case "duration":
                // 设置赛季持续时间
                if (args.length < 3) {
                    sender.sendMessage("§c用法: /hg season duration <天数>");
                    sender.sendMessage("§7设置为0表示无限期赛季");
                    return true;
                }
                
                try {
                    int days = Integer.parseInt(args[2]);
                    plugin.getSeasonManager().setSeasonDuration(days);
                    
                    if (days > 0) {
                        sender.sendMessage("§a赛季持续时间已设置为: " + days + " 天");
                    } else {
                        sender.sendMessage("§a赛季持续时间已设置为: 无限期");
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage("§c无效的数字: " + args[2]);
                }
                break;
                
            default:
                sender.sendMessage("§c未知的子命令: " + subCommand);
                sender.sendMessage("§e可用命令:");
                sender.sendMessage("§e/hg season info §7- 查看赛季信息");
                sender.sendMessage("§e/hg season reset §7- 重置当前赛季");
                sender.sendMessage("§e/hg season new §7- 开始新赛季");
                sender.sendMessage("§e/hg season duration <天数> §7- 设置赛季持续时间");
                break;
        }
        
        return true;
    }
    
    /**
     * 显示赛季信息
     */
    private void showSeasonInfo(CommandSender sender) {
        int seasonId = plugin.getSeasonManager().getCurrentSeasonId();
        long startTime = plugin.getSeasonManager().getSeasonStartTime();
        long endTime = plugin.getSeasonManager().getSeasonEndTime();
        long remainingTime = plugin.getSeasonManager().getSeasonRemainingTime();
        
        sender.sendMessage("§6========== §e赛季信息 §6==========");
        sender.sendMessage("§e当前赛季: §6S" + seasonId);
        sender.sendMessage("§e开始时间: §7" + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(startTime)));
        
        if (endTime > 0) {
            sender.sendMessage("§e结束时间: §7" + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(endTime)));
            
            if (remainingTime > 0) {
                long days = remainingTime / (24 * 60 * 60 * 1000);
                long hours = (remainingTime % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000);
                sender.sendMessage("§e剩余时间: §a" + days + "天 " + hours + "小时");
            } else {
                sender.sendMessage("§e状态: §c已结束");
            }
        } else {
            sender.sendMessage("§e持续时间: §d无限期");
        }
        
        sender.sendMessage("");
        sender.sendMessage("§7管理命令:");
        sender.sendMessage("§e/hg season reset §7- 重置当前赛季");
        sender.sendMessage("§e/hg season new §7- 开始新赛季");
        sender.sendMessage("§e/hg season duration <天数> §7- 设置持续时间");
    }
    

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // 第一个参数：子命令
            List<String> subCommands = Arrays.asList("help", "start", "stop", "join", "leave", "stats", "list", "spectate");
            
            if (sender.hasPermission("huntergame.admin")) {
                subCommands = new ArrayList<>(subCommands);
                subCommands.add("reload");
                subCommands.add("season");
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
            } else if (args[0].equalsIgnoreCase("season")) {
                // season 命令的第二个参数：子命令
                List<String> seasonSubCommands = Arrays.asList("info", "reset", "new", "start", "duration");
                String input = args[1].toLowerCase();
                for (String sub : seasonSubCommands) {
                    if (sub.startsWith(input)) {
                        completions.add(sub);
                    }
                }
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("season") && args[1].equalsIgnoreCase("duration")) {
                // season duration 命令的第三个参数：天数建议
                completions.addAll(Arrays.asList("7", "14", "30", "60", "90", "0"));
            }
        }
        
        return completions;
    }
}
