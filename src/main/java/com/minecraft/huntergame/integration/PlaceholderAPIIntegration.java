package com.minecraft.huntergame.integration;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.config.ServerType;
import com.minecraft.huntergame.game.PlayerRole;
import com.minecraft.huntergame.models.PlayerData;
import com.minecraft.huntergame.util.TimeUtil;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PlaceholderAPI 集成
 * 提供游戏相关的变量支持
 * 
 * @author YourName
 * @version 2.0.0 - Manhunt模式
 */
public class PlaceholderAPIIntegration extends PlaceholderExpansion {
    
    private final HunterGame plugin;
    
    // 排行榜缓存
    private final Map<String, List<PlayerData>> leaderboardCache;
    private long lastCacheUpdate;
    private static final long CACHE_DURATION = 5 * 60 * 1000; // 5分钟
    
    public PlaceholderAPIIntegration(HunterGame plugin) {
        this.plugin = plugin;
        this.leaderboardCache = new ConcurrentHashMap<>();
        this.lastCacheUpdate = 0;
    }
    
    @Override
    @NotNull
    public String getIdentifier() {
        return "huntergame";
    }
    
    @Override
    @NotNull
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }
    
    @Override
    @NotNull
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }
    
    @Override
    public boolean persist() {
        return true; // 插件重载时保持注册
    }
    
    @Override
    public String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        // 更新排行榜缓存
        updateLeaderboardCache();
        
        // 检查玩家是否在线
        if (offlinePlayer == null || !offlinePlayer.isOnline()) {
            return null;
        }
        
        Player player = offlinePlayer.getPlayer();
        if (player == null) {
            return null;
        }
        
        // ==================== 玩家段位变量 ====================
        PlayerData data = plugin.getStatsManager().getPlayerData(player);
        
        // 当前段位
        if (params.equals("rank") || params.equals("current_rank")) {
            return data != null ? data.getCurrentRank().getDisplayName() : "未定级";
        }
        
        // 当前段位颜色
        if (params.equals("rank_color") || params.equals("current_rank_color")) {
            return data != null ? data.getCurrentRank().getColor().toString() : "&7";
        }
        
        // 当前段位颜色（MiniMessage格式）
        if (params.equals("rank_color_mm") || params.equals("current_rank_color_mm")) {
            return data != null ? data.getCurrentRank().getMiniMessageColor() : "<gray>";
        }
        
        // 当前分数
        if (params.equals("score") || params.equals("rank_score")) {
            return data != null ? String.valueOf(data.getScore()) : "0";
        }
        
        // 历史最高段位
        if (params.equals("highest_rank")) {
            return data != null ? data.getHighestRank().getDisplayName() : "未定级";
        }
        
        // 历史最高段位颜色
        if (params.equals("highest_rank_color")) {
            return data != null ? data.getHighestRank().getColor().toString() : "&7";
        }
        
        // 历史最高段位颜色（MiniMessage格式）
        if (params.equals("highest_rank_color_mm")) {
            return data != null ? data.getHighestRank().getMiniMessageColor() : "<gray>";
        }
        
        // 当前赛季
        if (params.equals("season") || params.equals("season_id")) {
            return data != null ? String.valueOf(data.getSeasonId()) : "1";
        }
        
        // 到下一段位所需分数
        if (params.equals("score_to_next") || params.equals("score_to_next_rank")) {
            if (data != null) {
                int scoreToNext = data.getScoreToNextRank();
                return scoreToNext > 0 ? String.valueOf(scoreToNext) : "已满级";
            }
            return "0";
        }
        
        // 玩家排名
        if (params.equals("ranking") || params.equals("rank_position")) {
            int ranking = plugin.getStatsManager().getPlayerRanking(player.getUniqueId());
            return ranking > 0 ? String.valueOf(ranking) : "未上榜";
        }
        
        // ==================== 当前游戏状态变量 ====================
        com.minecraft.huntergame.game.ManhuntGame game = plugin.getManhuntManager().getPlayerGame(player);
        
        // 基础玩家信息
        if (params.equals("player_name")) {
            return player.getName();
        }
        
        if (params.equals("player_uuid")) {
            return player.getUniqueId().toString();
        }
        
        // 游戏状态
        if (params.equals("in_game")) {
            return game != null ? "true" : "false";
        }
        
        if (params.equals("game_state")) {
            if (game != null) {
                switch (game.getState()) {
                    case WAITING:
                        return "等待中";
                    case PREPARING:
                        return "准备中";
                    case PLAYING:
                        return "进行中";
                    case ENDING:
                        return "结束中";
                    default:
                        return "未知";
                }
            }
            return "无";
        }
        
        if (params.equals("game_state_color")) {
            if (game != null) {
                switch (game.getState()) {
                    case WAITING:
                        return "&e";
                    case PREPARING:
                        return "&6";
                    case PLAYING:
                        return "&c";
                    case ENDING:
                        return "&7";
                    default:
                        return "&f";
                }
            }
            return "&f";
        }
        
        // 角色信息
        if (params.equals("current_role") || params.equals("player_role")) {
            if (game != null) {
                com.minecraft.huntergame.game.PlayerRole role = game.getPlayerRole(player.getUniqueId());
                if (role != null) {
                    switch (role) {
                        case RUNNER:
                            return "逃亡者";
                        case HUNTER:
                            return "猎人";
                        case SPECTATOR:
                            return "观战者";
                        default:
                            return "未知";
                    }
                }
            }
            return "无";
        }
        
        if (params.equals("current_role_color") || params.equals("player_role_color")) {
            if (game != null) {
                com.minecraft.huntergame.game.PlayerRole role = game.getPlayerRole(player.getUniqueId());
                if (role != null) {
                    switch (role) {
                        case RUNNER:
                            return "&a";
                        case HUNTER:
                            return "&c";
                        case SPECTATOR:
                            return "&7";
                        default:
                            return "&f";
                    }
                }
            }
            return "&f";
        }
        
        if (params.equals("is_runner")) {
            if (game != null) {
                com.minecraft.huntergame.game.PlayerRole role = game.getPlayerRole(player.getUniqueId());
                return role == com.minecraft.huntergame.game.PlayerRole.RUNNER ? "true" : "false";
            }
            return "false";
        }
        
        if (params.equals("is_hunter")) {
            if (game != null) {
                com.minecraft.huntergame.game.PlayerRole role = game.getPlayerRole(player.getUniqueId());
                return role == com.minecraft.huntergame.game.PlayerRole.HUNTER ? "true" : "false";
            }
            return "false";
        }
        
        if (params.equals("is_spectator")) {
            if (game != null) {
                com.minecraft.huntergame.game.PlayerRole role = game.getPlayerRole(player.getUniqueId());
                return role == com.minecraft.huntergame.game.PlayerRole.SPECTATOR ? "true" : "false";
            }
            return "false";
        }
        
        // 游戏房间信息
        if (params.equals("current_game") || params.equals("game_id") || params.equals("room_id")) {
            return game != null ? game.getGameId() : "无";
        }
        
        if (params.equals("room_name")) {
            return game != null ? game.getGameId() : "无";
        }
        
        if (params.equals("room_mode")) {
            return "Manhunt"; // 当前只有一种模式
        }
        
        // 玩家数量
        if (params.equals("game_players") || params.equals("player_count")) {
            return game != null ? String.valueOf(game.getPlayerCount()) : "0";
        }
        
        if (params.equals("game_max_players") || params.equals("max_players")) {
            if (game != null) {
                return String.valueOf(game.getMaxRunners() + game.getMaxHunters());
            }
            return "0";
        }
        
        if (params.equals("game_runners") || params.equals("total_runners")) {
            return game != null ? String.valueOf(game.getRunners().size()) : "0";
        }
        
        if (params.equals("game_hunters") || params.equals("total_hunters")) {
            return game != null ? String.valueOf(game.getHunters().size()) : "0";
        }
        
        if (params.equals("game_alive_runners") || params.equals("alive_runners")) {
            return game != null ? String.valueOf(game.getAliveRunners().size()) : "0";
        }
        
        if (params.equals("alive_hunters")) {
            return game != null ? String.valueOf(game.getHunters().size()) : "0";
        }
        
        // 游戏时间
        if (params.equals("game_time")) {
            if (game != null && game.getState() == com.minecraft.huntergame.game.GameState.PLAYING) {
                return formatTime((int) game.getElapsedTime());
            }
            return "0s";
        }
        
        if (params.equals("game_time_seconds")) {
            if (game != null && game.getState() == com.minecraft.huntergame.game.GameState.PLAYING) {
                return String.valueOf((int) game.getElapsedTime());
            }
            return "0";
        }
        
        if (params.equals("remaining_time")) {
            if (game != null && game.getState() == com.minecraft.huntergame.game.GameState.PLAYING) {
                int maxTime = game.getMaxGameTime();
                if (maxTime > 0) {
                    int remaining = maxTime - (int) game.getElapsedTime();
                    return formatTime(Math.max(0, remaining));
                }
            }
            return "无限制";
        }
        
        if (params.equals("remaining_time_seconds")) {
            if (game != null && game.getState() == com.minecraft.huntergame.game.GameState.PLAYING) {
                int maxTime = game.getMaxGameTime();
                if (maxTime > 0) {
                    int remaining = maxTime - (int) game.getElapsedTime();
                    return String.valueOf(Math.max(0, remaining));
                }
            }
            return "0";
        }
        
        // 复活次数
        if (params.equals("respawn_count") || params.equals("respawns")) {
            if (game != null) {
                return String.valueOf(game.getRemainingRespawns(player.getUniqueId()));
            }
            return "0";
        }
        
        if (params.equals("max_respawns")) {
            if (game != null) {
                return String.valueOf(game.getRespawnLimit());
            }
            return "0";
        }
        
        // 倒计时
        if (params.equals("countdown")) {
            if (game != null) {
                if (game.getState() == com.minecraft.huntergame.game.GameState.WAITING) {
                    return String.valueOf(game.getMatchingTimeRemaining());
                } else if (game.getState() == com.minecraft.huntergame.game.GameState.PREPARING) {
                    return String.valueOf(game.getPrepareTimeRemaining());
                }
            }
            return "0";
        }
        
        // ==================== 角色颜色变量 ====================
        if (params.equals("hunter_color")) {
            return PlayerRole.HUNTER.getColor().toString();
        }
        
        if (params.equals("runner_color")) {
            return PlayerRole.RUNNER.getColor().toString();
        }
        
        if (params.equals("spectator_color")) {
            return PlayerRole.SPECTATOR.getColor().toString();
        }
        
        // MiniMessage 格式的角色颜色
        if (params.equals("player_role_color_mm") || params.equals("current_role_color_mm")) {
            if (game != null) {
                com.minecraft.huntergame.game.PlayerRole role = game.getPlayerRole(player.getUniqueId());
                if (role != null) {
                    switch (role) {
                        case RUNNER:
                            return "<green>";
                        case HUNTER:
                            return "<red>";
                        case SPECTATOR:
                            return "<gray>";
                        default:
                            return "<white>";
                    }
                }
            }
            return "<white>";
        }
        
        if (params.equals("hunter_color_mm")) {
            return "<red>";
        }
        
        if (params.equals("runner_color_mm")) {
            return "<green>";
        }
        
        if (params.equals("spectator_color_mm")) {
            return "<gray>";
        }
        
        // ==================== 服务器模式变量 ====================
        if (params.equals("server_mode")) {
            return plugin.getServerMode().name();
        }
        
        if (params.equals("status")) {
            if (game != null) {
                switch (game.getState()) {
                    case WAITING:
                        return "等待中";
                    case PREPARING:
                        return "准备中";
                    case PLAYING:
                        return "游戏中";
                    case ENDING:
                        return "结束中";
                    default:
                        return "未知";
                }
            }
            return "空闲";
        }
        
        if (params.equals("server")) {
            if (plugin.getRedisManager() != null) {
                return plugin.getRedisManager().getServerName();
            }
            return "本地服务器";
        }
        
        // ==================== 段位排行榜变量 ====================
        // top_rank_<rank> - 排行榜玩家名称
        if (params.startsWith("top_rank_")) {
            try {
                int rank = Integer.parseInt(params.substring(9));
                List<PlayerData> topRanks = leaderboardCache.get("ranks");
                if (topRanks != null && rank > 0 && rank <= topRanks.size()) {
                    return topRanks.get(rank - 1).getName();
                }
            } catch (NumberFormatException ignored) {
            }
            return "";
        }
        
        // top_rank_<rank>_score - 排行榜玩家分数
        if (params.startsWith("top_rank_") && params.endsWith("_score")) {
            try {
                String rankStr = params.substring(9, params.length() - 6);
                int rank = Integer.parseInt(rankStr);
                List<PlayerData> topRanks = leaderboardCache.get("ranks");
                if (topRanks != null && rank > 0 && rank <= topRanks.size()) {
                    return String.valueOf(topRanks.get(rank - 1).getScore());
                }
            } catch (NumberFormatException ignored) {
            }
            return "";
        }
        
        // top_rank_<rank>_rank - 排行榜玩家段位
        if (params.startsWith("top_rank_") && params.endsWith("_rank")) {
            try {
                String rankStr = params.substring(9, params.length() - 5);
                int rank = Integer.parseInt(rankStr);
                List<PlayerData> topRanks = leaderboardCache.get("ranks");
                if (topRanks != null && rank > 0 && rank <= topRanks.size()) {
                    return topRanks.get(rank - 1).getCurrentRank().getDisplayName();
                }
            } catch (NumberFormatException ignored) {
            }
            return "";
        }
        
        // top_rank_<rank>_rank_color - 排行榜玩家段位颜色
        if (params.startsWith("top_rank_") && params.endsWith("_rank_color")) {
            try {
                String rankStr = params.substring(9, params.length() - 11);
                int rank = Integer.parseInt(rankStr);
                List<PlayerData> topRanks = leaderboardCache.get("ranks");
                if (topRanks != null && rank > 0 && rank <= topRanks.size()) {
                    return topRanks.get(rank - 1).getCurrentRank().getColor().toString();
                }
            } catch (NumberFormatException ignored) {
            }
            return "";
        }
        
        // ==================== Bungee 模式变量 ====================
        
        // 服务器名称
        if (params.equals("server_name")) {
            if (plugin.getRedisManager() != null) {
                return plugin.getRedisManager().getServerName();
            }
            return "unknown";
        }
        
        // 服务器类型
        if (params.equals("server_type")) {
            ServerType type = plugin.getManhuntConfig().getServerType();
            return type == ServerType.MAIN_LOBBY ? "主大厅" : "子大厅";
        }
        
        // 服务器状态
        if (params.equals("server_status")) {
            return getServerStatus();
        }
        
        // 当前服务器玩家数
        if (params.equals("server_players")) {
            return String.valueOf(plugin.getServer().getOnlinePlayers().size());
        }
        
        // 服务器最大玩家数
        if (params.equals("server_max_players")) {
            return String.valueOf(plugin.getServer().getMaxPlayers());
        }
        
        // 总游戏服务器数量
        if (params.equals("total_servers")) {
            if (plugin.getRedisManager() != null && plugin.getRedisManager().isConnected()) {
                return String.valueOf(plugin.getRedisManager().getOnlineServers().size());
            }
            return "1";
        }
        
        // 可用游戏服务器数量
        if (params.equals("available_servers")) {
            if (plugin.getRedisManager() != null && plugin.getRedisManager().isConnected()) {
                return String.valueOf(getAvailableServerCount());
            }
            return "0";
        }
        
        // 所有服务器总玩家数
        if (params.equals("total_players")) {
            if (plugin.getRedisManager() != null && plugin.getRedisManager().isConnected()) {
                return String.valueOf(getTotalPlayerCount());
            }
            return String.valueOf(plugin.getServer().getOnlinePlayers().size());
        }
        
        // 匹配剩余时间
        if (params.equals("matching_time")) {
            if (game != null && game.getState() == com.minecraft.huntergame.game.GameState.WAITING) {
                int remaining = game.getMatchingTimeRemaining();
                return formatTime(remaining);
            }
            return "0s";
        }
        
        // 准备剩余时间
        if (params.equals("prepare_time")) {
            if (game != null && game.getState() == com.minecraft.huntergame.game.GameState.PREPARING) {
                int remaining = game.getPrepareTimeRemaining();
                return formatTime(remaining);
            }
            return "0s";
        }
        
        return null; // 未知占位符
    }
    
    /**
     * 获取服务器状态
     */
    private String getServerStatus() {
        // 检查是否有游戏正在进行
        if (plugin.getManhuntManager().getAllGames().isEmpty()) {
            return "空闲";
        }
        
        // 检查是否已满
        int currentPlayers = plugin.getServer().getOnlinePlayers().size();
        int maxPlayers = plugin.getServer().getMaxPlayers();
        if (currentPlayers >= maxPlayers) {
            return "已满";
        }
        
        // 检查游戏状态
        for (com.minecraft.huntergame.game.ManhuntGame game : plugin.getManhuntManager().getAllGames()) {
            if (game.getState() == com.minecraft.huntergame.game.GameState.PLAYING) {
                return "游戏中";
            }
        }
        
        return "等待中";
    }
    
    /**
     * 获取可用服务器数量
     */
    private int getAvailableServerCount() {
        int count = 0;
        for (String serverKey : plugin.getRedisManager().getOnlineServers()) {
            String serverName = serverKey.replace("huntergame:servers:", "");
            java.util.Map<String, String> serverInfo = plugin.getRedisManager().getServerInfo(serverName);
            
            String status = serverInfo.get("status");
            if ("ONLINE".equals(status) || "WAITING".equals(status)) {
                int players = Integer.parseInt(serverInfo.getOrDefault("players", "0"));
                int maxPlayers = Integer.parseInt(serverInfo.getOrDefault("maxPlayers", "0"));
                if (players < maxPlayers) {
                    count++;
                }
            }
        }
        return count;
    }
    
    /**
     * 获取所有服务器总玩家数
     */
    private int getTotalPlayerCount() {
        int total = 0;
        for (String serverKey : plugin.getRedisManager().getOnlineServers()) {
            String serverName = serverKey.replace("huntergame:servers:", "");
            java.util.Map<String, String> serverInfo = plugin.getRedisManager().getServerInfo(serverName);
            
            int players = Integer.parseInt(serverInfo.getOrDefault("players", "0"));
            total += players;
        }
        return total;
    }
    
    /**
     * 更新排行榜缓存
     */
    private void updateLeaderboardCache() {
        long now = System.currentTimeMillis();
        if (now - lastCacheUpdate < CACHE_DURATION) {
            return; // 缓存未过期
        }
        
        try {
            // 使用StatsManager的段位排行榜缓存系统
            leaderboardCache.put("ranks", plugin.getStatsManager().getTopRanks(10));
            
            lastCacheUpdate = now;
        } catch (Exception e) {
            plugin.getLogger().warning("更新段位排行榜缓存失败: " + e.getMessage());
        }
    }
    
    /**
     * 格式化时间
     * 将秒数转换为 "Xh Ym Zs" 格式
     */
    private String formatTime(int totalSeconds) {
        // 使用TimeUtil工具类
        return TimeUtil.formatTime(totalSeconds);
    }
}
