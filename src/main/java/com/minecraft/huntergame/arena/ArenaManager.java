package com.minecraft.huntergame.arena;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.config.ArenaConfig;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

/**
 * 竞技场管理器
 * 负责管理所有竞技场实例
 * 
 * @author YourName
 * @version 1.0.0
 */
public class ArenaManager {
    
    private final HunterGame plugin;
    
    // 竞技场映射
    private final Map<String, Arena> arenas;
    
    // 玩家-竞技场映射
    private final Map<UUID, Arena> playerArenaMap;
    
    public ArenaManager(HunterGame plugin) {
        this.plugin = plugin;
        this.arenas = new HashMap<>();
        this.playerArenaMap = new HashMap<>();
    }
    
    /**
     * 加载所有竞技场
     */
    public void loadArenas() {
        File arenaFolder = new File(plugin.getDataFolder(), "arenas");
        
        if (!arenaFolder.exists()) {
            arenaFolder.mkdirs();
            plugin.getLogger().info("竞技场文件夹不存在，已创建");
            return;
        }
        
        File[] files = arenaFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        
        if (files == null || files.length == 0) {
            plugin.getLogger().warning("未找到竞技场配置文件");
            return;
        }
        
        int loaded = 0;
        for (File file : files) {
            String arenaName = file.getName().replace(".yml", "");
            
            if (loadArena(arenaName)) {
                loaded++;
            }
        }
        
        plugin.getLogger().info("已加载 " + loaded + " 个竞技场");
    }
    
    /**
     * 加载单个竞技场
     */
    public boolean loadArena(String arenaName) {
        try {
            // 加载配置
            ArenaConfig config = new ArenaConfig(plugin, arenaName);
            
            // 验证配置
            if (!config.validate()) {
                plugin.getLogger().warning("竞技场 " + arenaName + " 配置不完整，跳过加载");
                return false;
            }
            
            // 创建竞技场实例
            Arena arena = new Arena(plugin, arenaName, config);
            
            // 启用竞技场
            if (config.isEnabled()) {
                arena.enable();
            }
            
            // 添加到映射
            arenas.put(arenaName, arena);
            
            plugin.getLogger().info("竞技场 " + arenaName + " 已加载");
            return true;
            
        } catch (Exception ex) {
            plugin.getLogger().severe("加载竞技场 " + arenaName + " 失败: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }
    
    /**
     * 卸载竞技场
     */
    public boolean unloadArena(String arenaName) {
        Arena arena = arenas.get(arenaName);
        
        if (arena == null) {
            return false;
        }
        
        // 禁用竞技场
        arena.disable();
        
        // 移除玩家映射
        for (UUID uuid : arena.getPlayers()) {
            playerArenaMap.remove(uuid);
        }
        
        // 移除竞技场
        arenas.remove(arenaName);
        
        plugin.getLogger().info("竞技场 " + arenaName + " 已卸载");
        return true;
    }
    
    /**
     * 重载竞技场
     */
    public boolean reloadArena(String arenaName) {
        unloadArena(arenaName);
        return loadArena(arenaName);
    }
    
    /**
     * 重载所有竞技场
     */
    public void reloadAll() {
        plugin.getLogger().info("正在重载所有竞技场...");
        
        // 卸载所有竞技场
        for (String arenaName : new ArrayList<>(arenas.keySet())) {
            unloadArena(arenaName);
        }
        
        // 重新加载
        loadArenas();
    }
    
    /**
     * 获取竞技场
     */
    public Arena getArena(String arenaName) {
        return arenas.get(arenaName);
    }
    
    /**
     * 获取所有竞技场
     */
    public Collection<Arena> getArenas() {
        return arenas.values();
    }
    
    /**
     * 获取所有竞技场名称
     */
    public Set<String> getArenaNames() {
        return arenas.keySet();
    }
    
    /**
     * 获取竞技场数量
     */
    public int getArenaCount() {
        return arenas.size();
    }
    
    /**
     * 检查竞技场是否存在
     */
    public boolean hasArena(String arenaName) {
        return arenas.containsKey(arenaName);
    }
    
    // ==================== 玩家-竞技场映射 ====================
    
    /**
     * 获取玩家所在的竞技场
     */
    public Arena getPlayerArena(UUID uuid) {
        return playerArenaMap.get(uuid);
    }
    
    /**
     * 获取玩家所在的竞技场
     */
    public Arena getPlayerArena(Player player) {
        return getPlayerArena(player.getUniqueId());
    }
    
    /**
     * 设置玩家所在的竞技场
     */
    public void setPlayerArena(UUID uuid, Arena arena) {
        if (arena == null) {
            playerArenaMap.remove(uuid);
        } else {
            playerArenaMap.put(uuid, arena);
        }
    }
    
    /**
     * 检查玩家是否在竞技场中
     */
    public boolean isInArena(UUID uuid) {
        return playerArenaMap.containsKey(uuid);
    }
    
    /**
     * 检查玩家是否在竞技场中
     */
    public boolean isInArena(Player player) {
        return isInArena(player.getUniqueId());
    }
    
    // ==================== 竞技场查找 ====================
    
    /**
     * 查找可用的竞技场
     */
    public List<Arena> getAvailableArenas() {
        List<Arena> available = new ArrayList<>();
        
        for (Arena arena : arenas.values()) {
            if (arena.getStateManager().isJoinable() && !arena.isFull()) {
                available.add(arena);
            }
        }
        
        return available;
    }
    
    /**
     * 查找最佳竞技场（玩家数最多但未满的）
     */
    public Arena findBestArena() {
        return getAvailableArenas().stream()
            .max(Comparator.comparingInt(Arena::getPlayerCount))
            .orElse(null);
    }
    
    /**
     * 查找随机可用竞技场
     */
    public Arena findRandomArena() {
        List<Arena> available = getAvailableArenas();
        
        if (available.isEmpty()) {
            return null;
        }
        
        return available.get(new Random().nextInt(available.size()));
    }
    
    // ==================== 统计信息 ====================
    
    /**
     * 获取总玩家数
     */
    public int getTotalPlayers() {
        return playerArenaMap.size();
    }
    
    /**
     * 获取正在游戏的竞技场数量
     */
    public int getPlayingArenaCount() {
        return (int) arenas.values().stream()
            .filter(arena -> arena.getStateManager().isPlaying())
            .count();
    }
    
    /**
     * 获取等待中的竞技场数量
     */
    public int getWaitingArenaCount() {
        return (int) arenas.values().stream()
            .filter(arena -> arena.getStateManager().isJoinable())
            .count();
    }
    
    // ==================== 玩家加入便捷方法 ====================
    
    /**
     * 玩家加入指定竞技场
     * 
     * @param player 玩家
     * @param arenaName 竞技场名称
     * @return 是否成功加入
     */
    public boolean joinArena(Player player, String arenaName) {
        Arena arena = getArena(arenaName);
        
        if (arena == null) {
            plugin.getLanguageManager().sendMessage(player, "join.arena-not-found");
            return false;
        }
        
        return arena.joinPlayer(player);
    }
    
    /**
     * 玩家随机加入竞技场
     * 
     * @param player 玩家
     * @return 是否成功加入
     */
    public boolean joinRandomArena(Player player) {
        // 检查玩家是否已在竞技场中
        if (isInArena(player)) {
            plugin.getLanguageManager().sendMessage(player, "join.already-in-game");
            return false;
        }
        
        // 查找最佳竞技场（玩家数最多但未满的）
        Arena arena = findBestArena();
        
        if (arena == null) {
            plugin.getLanguageManager().sendMessage(player, "join.no-arena-available");
            return false;
        }
        
        return arena.joinPlayer(player);
    }
    
    /**
     * 玩家离开竞技场
     * 
     * @param player 玩家
     * @return 是否成功离开
     */
    public boolean leaveArena(Player player) {
        Arena arena = getPlayerArena(player);
        
        if (arena == null) {
            plugin.getLanguageManager().sendMessage(player, "leave.not-in-game");
            return false;
        }
        
        return arena.leavePlayer(player);
    }
    
    /**
     * 队伍加入指定竞技场
     * 
     * @param partyMembers 队伍成员列表
     * @param arenaName 竞技场名称
     * @return 是否成功加入
     */
    public boolean joinArenaAsParty(java.util.List<Player> partyMembers, String arenaName) {
        Arena arena = getArena(arenaName);
        
        if (arena == null) {
            for (Player member : partyMembers) {
                plugin.getLanguageManager().sendMessage(member, "join.arena-not-found");
            }
            return false;
        }
        
        return arena.joinParty(partyMembers);
    }
    
    /**
     * 队伍随机加入竞技场
     * 
     * @param partyMembers 队伍成员列表
     * @return 是否成功加入
     */
    public boolean joinRandomArenaAsParty(java.util.List<Player> partyMembers) {
        if (partyMembers == null || partyMembers.isEmpty()) {
            return false;
        }
        
        // 检查是否有成员已在竞技场中
        for (Player member : partyMembers) {
            if (isInArena(member)) {
                for (Player p : partyMembers) {
                    plugin.getLanguageManager().sendMessage(p, "party.member-already-in-game", 
                        member.getName());
                }
                return false;
            }
        }
        
        // 查找有足够空间的竞技场
        Arena bestArena = null;
        int maxPlayers = 0;
        
        for (Arena arena : getAvailableArenas()) {
            int availableSlots = arena.getMaxPlayers() - arena.getPlayerCount();
            if (availableSlots >= partyMembers.size()) {
                if (arena.getPlayerCount() > maxPlayers) {
                    maxPlayers = arena.getPlayerCount();
                    bestArena = arena;
                }
            }
        }
        
        if (bestArena == null) {
            for (Player member : partyMembers) {
                plugin.getLanguageManager().sendMessage(member, "party.no-arena-available");
            }
            return false;
        }
        
        return bestArena.joinParty(partyMembers);
    }
}
