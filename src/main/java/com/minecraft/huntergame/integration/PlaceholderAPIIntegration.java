package com.minecraft.huntergame.integration;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.arena.Arena;
import com.minecraft.huntergame.game.GameState;
import com.minecraft.huntergame.game.PlayerRole;
import com.minecraft.huntergame.models.PlayerData;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PlaceholderAPI 集成
 * 提供游戏相关的变量支持
 * 
 * @author YourName
 * @version 1.0.0
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
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        // 更新排行榜缓存
        updateLeaderboardCache();
        
        // 玩家统计变量
        if (player != null && player.getPlayer() != null) {
            PlayerData data = plugin.getStatsManager().getPlayerData(player.getPlayer());
            
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
            Arena arena = plugin.getArenaManager().getPlayerArena(player.getPlayer());
            
            if (params.equals("current_role")) {
                if (arena != null) {
                    PlayerRole role = arena.getPlayerRole(player.getUniqueId());
                    if (role != null) {
                        return role.getDisplayNameZh();
                    }
                }
                return "无";
            }
            
            if (params.equals("current_role_color")) {
                if (arena != null) {
                    PlayerRole role = arena.getPlayerRole(player.getUniqueId());
                    if (role != null) {
                        return role.getColor().toString();
                    }
                }
                return "&f";
            }
            
            if (params.equals("current_arena")) {
                return arena != null ? arena.getArenaName() : "无";
            }
            
            if (params.equals("arena_state")) {
                if (arena != null) {
                    return arena.getState().getDisplayNameZh();
                }
                return "无";
            }
            
            if (params.equals("arena_players")) {
                return arena != null ? String.valueOf(arena.getPlayers().size()) : "0";
            }
            
            if (params.equals("arena_max_players")) {
                return arena != null ? String.valueOf(arena.getMaxPlayers()) : "0";
            }
        }
        
        // 角色颜色变量
        if (params.equals("hunter_color")) {
            return PlayerRole.HUNTER.getColor().toString();
        }
        
        if (params.equals("survivor_color")) {
            return PlayerRole.SURVIVOR.getColor().toString();
        }
        
        if (params.equals("spectator_color")) {
            return PlayerRole.SPECTATOR.getColor().toString();
        }
        
        // 竞技场信息变量 - arena_<name>_state
        if (params.startsWith("arena_") && params.endsWith("_state")) {
            String arenaName = params.substring(6, params.length() - 6);
            Arena arena = plugin.getArenaManager().getArena(arenaName);
            if (arena != null) {
                return arena.getState().getDisplayNameZh();
            }
            return "未知";
        }
        
        // 竞技场信息变量 - arena_<name>_players
        if (params.startsWith("arena_") && params.endsWith("_players")) {
            String arenaName = params.substring(6, params.length() - 8);
            Arena arena = plugin.getArenaManager().getArena(arenaName);
            if (arena != null) {
                return String.valueOf(arena.getPlayers().size());
            }
            return "0";
        }
        
        // 竞技场信息变量 - arena_<name>_max_players
        if (params.startsWith("arena_") && params.endsWith("_max_players")) {
            String arenaName = params.substring(6, params.length() - 12);
            Arena arena = plugin.getArenaManager().getArena(arenaName);
            if (arena != null) {
                return String.valueOf(arena.getMaxPlayers());
            }
            return "0";
        }
        
        // 竞技场信息变量 - arena_<name>_mode
        if (params.startsWith("arena_") && params.endsWith("_mode")) {
            String arenaName = params.substring(6, params.length() - 5);
            Arena arena = plugin.getArenaManager().getArena(arenaName);
            if (arena != null) {
                return arena.getGameMode().name().equals("CLASSIC") ? "经典模式" : "团队模式";
            }
            return "未知";
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
        
        // 异步更新缓存
        plugin.getStatsManager().getTopWins(10, list -> {
            leaderboardCache.put("wins", list);
        });
        
        plugin.getStatsManager().getTopKills(10, list -> {
            leaderboardCache.put("kills", list);
        });
        
        plugin.getStatsManager().getTopEscapes(10, list -> {
            leaderboardCache.put("escapes", list);
        });
        
        lastCacheUpdate = now;
    }
    
    /**
     * 格式化时间
     * 将秒数转换为 "Xh Ym Zs" 格式
     */
    private String formatTime(int totalSeconds) {
        if (totalSeconds < 60) {
            return totalSeconds + "s";
        }
        
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        
        StringBuilder sb = new StringBuilder();
        if (hours > 0) {
            sb.append(hours).append("h ");
        }
        if (minutes > 0) {
            sb.append(minutes).append("m ");
        }
        if (seconds > 0 || sb.length() == 0) {
            sb.append(seconds).append("s");
        }
        
        return sb.toString().trim();
    }
}
