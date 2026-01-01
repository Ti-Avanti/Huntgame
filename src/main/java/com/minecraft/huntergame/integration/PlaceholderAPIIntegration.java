package com.minecraft.huntergame.integration;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.game.PlayerRole;
import com.minecraft.huntergame.models.PlayerData;
import com.minecraft.huntergame.util.TimeUtil;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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
        
        // 玩家统计变量
        PlayerData data = plugin.getStatsManager().getPlayerData(player);
        
        if (params.equals("games_played")) {
            return data != null ? String.valueOf(data.getGamesPlayed()) : "0";
        }
        
        if (params.equals("games_won")) {
            return data != null ? String.valueOf(data.getGamesWon()) : "0";
        }
        
        if (params.equals("hunter_kills")) {
            return data != null ? String.valueOf(data.getHunterKills()) : "0";
        }
        
        if (params.equals("survivor_escapes")) {
            return data != null ? String.valueOf(data.getSurvivorEscapes()) : "0";
        }
        
        if (params.equals("survival_time")) {
            if (data != null) {
                return formatTime(data.getTotalSurvivalTime());
            }
            return "0s";
        }
        
        // 当前游戏状态变量
        if (params.equals("current_role")) {
            com.minecraft.huntergame.game.ManhuntGame game = plugin.getManhuntManager().getPlayerGame(player);
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
        
        if (params.equals("current_role_color")) {
            com.minecraft.huntergame.game.ManhuntGame game = plugin.getManhuntManager().getPlayerGame(player);
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
        
        if (params.equals("current_game")) {
            com.minecraft.huntergame.game.ManhuntGame game = plugin.getManhuntManager().getPlayerGame(player);
            return game != null ? game.getGameId() : "无";
        }
        
        if (params.equals("game_state")) {
            com.minecraft.huntergame.game.ManhuntGame game = plugin.getManhuntManager().getPlayerGame(player);
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
        
        if (params.equals("game_players")) {
            com.minecraft.huntergame.game.ManhuntGame game = plugin.getManhuntManager().getPlayerGame(player);
            return game != null ? String.valueOf(game.getPlayerCount()) : "0";
        }
        
        if (params.equals("game_max_players")) {
            com.minecraft.huntergame.game.ManhuntGame game = plugin.getManhuntManager().getPlayerGame(player);
            if (game != null) {
                return String.valueOf(game.getMaxRunners() + game.getMaxHunters());
            }
            return "0";
        }
        
        if (params.equals("game_runners")) {
            com.minecraft.huntergame.game.ManhuntGame game = plugin.getManhuntManager().getPlayerGame(player);
            return game != null ? String.valueOf(game.getRunners().size()) : "0";
        }
        
        if (params.equals("game_hunters")) {
            com.minecraft.huntergame.game.ManhuntGame game = plugin.getManhuntManager().getPlayerGame(player);
            return game != null ? String.valueOf(game.getHunters().size()) : "0";
        }
        
        if (params.equals("game_alive_runners")) {
            com.minecraft.huntergame.game.ManhuntGame game = plugin.getManhuntManager().getPlayerGame(player);
            return game != null ? String.valueOf(game.getAliveRunners().size()) : "0";
        }
        
        if (params.equals("game_time")) {
            com.minecraft.huntergame.game.ManhuntGame game = plugin.getManhuntManager().getPlayerGame(player);
            if (game != null && game.getState() == com.minecraft.huntergame.game.GameState.PLAYING) {
                return formatTime((int) game.getElapsedTime());
            }
            return "0s";
        }
        
        if (params.equals("respawn_count")) {
            com.minecraft.huntergame.game.ManhuntGame game = plugin.getManhuntManager().getPlayerGame(player);
            if (game != null) {
                return String.valueOf(game.getRemainingRespawns(player.getUniqueId()));
            }
            return "0";
        }
        
        // 角色颜色变量
        if (params.equals("hunter_color")) {
            return PlayerRole.HUNTER.getColor().toString();
        }
        
        if (params.equals("runner_color")) {
            return PlayerRole.RUNNER.getColor().toString();
        }
        
        if (params.equals("spectator_color")) {
            return PlayerRole.SPECTATOR.getColor().toString();
        }
        
        // 排行榜变量 - top_wins_<rank>
        if (params.startsWith("top_wins_")) {
            try {
                int rank = Integer.parseInt(params.substring(9));
                List<PlayerData> topWins = leaderboardCache.get("wins");
                if (topWins != null && rank > 0 && rank <= topWins.size()) {
                    return topWins.get(rank - 1).getName();
                }
            } catch (NumberFormatException ignored) {
            }
            return "";
        }
        
        // 排行榜变量 - top_wins_<rank>_value
        if (params.startsWith("top_wins_") && params.endsWith("_value")) {
            try {
                String rankStr = params.substring(9, params.length() - 6);
                int rank = Integer.parseInt(rankStr);
                List<PlayerData> topWins = leaderboardCache.get("wins");
                if (topWins != null && rank > 0 && rank <= topWins.size()) {
                    return String.valueOf(topWins.get(rank - 1).getGamesWon());
                }
            } catch (NumberFormatException ignored) {
            }
            return "";
        }
        
        // 排行榜变量 - top_kills_<rank>
        if (params.startsWith("top_kills_")) {
            try {
                int rank = Integer.parseInt(params.substring(10));
                List<PlayerData> topKills = leaderboardCache.get("kills");
                if (topKills != null && rank > 0 && rank <= topKills.size()) {
                    return topKills.get(rank - 1).getName();
                }
            } catch (NumberFormatException ignored) {
            }
            return "";
        }
        
        // 排行榜变量 - top_kills_<rank>_value
        if (params.startsWith("top_kills_") && params.endsWith("_value")) {
            try {
                String rankStr = params.substring(10, params.length() - 6);
                int rank = Integer.parseInt(rankStr);
                List<PlayerData> topKills = leaderboardCache.get("kills");
                if (topKills != null && rank > 0 && rank <= topKills.size()) {
                    return String.valueOf(topKills.get(rank - 1).getHunterKills());
                }
            } catch (NumberFormatException ignored) {
            }
            return "";
        }
        
        // 排行榜变量 - top_escapes_<rank>
        if (params.startsWith("top_escapes_")) {
            try {
                int rank = Integer.parseInt(params.substring(12));
                List<PlayerData> topEscapes = leaderboardCache.get("escapes");
                if (topEscapes != null && rank > 0 && rank <= topEscapes.size()) {
                    return topEscapes.get(rank - 1).getName();
                }
            } catch (NumberFormatException ignored) {
            }
            return "";
        }
        
        // 排行榜变量 - top_escapes_<rank>_value
        if (params.startsWith("top_escapes_") && params.endsWith("_value")) {
            try {
                String rankStr = params.substring(12, params.length() - 6);
                int rank = Integer.parseInt(rankStr);
                List<PlayerData> topEscapes = leaderboardCache.get("escapes");
                if (topEscapes != null && rank > 0 && rank <= topEscapes.size()) {
                    return String.valueOf(topEscapes.get(rank - 1).getSurvivorEscapes());
                }
            } catch (NumberFormatException ignored) {
            }
            return "";
        }
        
        return null; // 未知占位符
    }
    
    /**
     * 更新排行榜缓存
     */
    private void updateLeaderboardCache() {
        long now = System.currentTimeMillis();
        if (now - lastCacheUpdate < CACHE_DURATION) {
            return; // 缓存未过期
        }
        
        // 使用StatsManager的缓存系统（同步获取）
        leaderboardCache.put("wins", plugin.getStatsManager().getTopWins(10));
        leaderboardCache.put("kills", plugin.getStatsManager().getTopKills(10));
        leaderboardCache.put("runner_wins", plugin.getStatsManager().getTopRunnerWins(10));
        leaderboardCache.put("hunter_wins", plugin.getStatsManager().getTopHunterWins(10));
        leaderboardCache.put("dragon_kills", plugin.getStatsManager().getTopDragonKills(10));
        
        lastCacheUpdate = now;
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
