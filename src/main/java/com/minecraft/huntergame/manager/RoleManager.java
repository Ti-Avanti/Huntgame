package com.minecraft.huntergame.manager;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.arena.Arena;
import com.minecraft.huntergame.game.GameMode;
import com.minecraft.huntergame.game.PlayerRole;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * 角色管理器
 * 负责游戏中的角色分配
 * 
 * @author YourName
 * @version 1.0.0
 */
public class RoleManager {
    
    private final HunterGame plugin;
    private final Arena arena;
    
    public RoleManager(HunterGame plugin, Arena arena) {
        this.plugin = plugin;
        this.arena = arena;
    }
    
    /**
     * 分配角色
     * 根据游戏模式随机分配猎人和逃生者
     * 支持队伍成员分配到同一阵营
     */
    public void assignRoles() {
        List<UUID> players = new ArrayList<>(arena.getPlayers());
        
        if (players.isEmpty()) {
            plugin.getLogger().warning("[" + arena.getArenaName() + "] 没有玩家可以分配角色");
            return;
        }
        
        // 获取队伍管理器
        com.minecraft.huntergame.party.PartyManager partyManager = plugin.getPartyManager();
        
        // 将玩家按队伍分组
        Map<UUID, List<UUID>> partyGroups = new HashMap<>();
        List<UUID> soloPlayers = new ArrayList<>();
        
        for (UUID uuid : players) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) {
                continue;
            }
            
            com.minecraft.huntergame.party.Party party = partyManager.getParty(player);
            if (party != null) {
                // 玩家在队伍中
                UUID partyId = party.getPartyId();
                partyGroups.computeIfAbsent(partyId, k -> new ArrayList<>()).add(uuid);
            } else {
                // 单人玩家
                soloPlayers.add(uuid);
            }
        }
        
        // 计算猎人数量
        int hunterCount = calculateHunterCount(players.size());
        
        plugin.getLogger().info("[" + arena.getArenaName() + "] 分配角色: " + 
            hunterCount + " 猎人, " + (players.size() - hunterCount) + " 逃生者");
        
        // 创建分配单位列表（队伍或单人）
        List<List<UUID>> assignmentUnits = new ArrayList<>();
        assignmentUnits.addAll(partyGroups.values());
        for (UUID solo : soloPlayers) {
            assignmentUnits.add(Collections.singletonList(solo));
        }
        
        // 打乱分配单位
        Collections.shuffle(assignmentUnits);
        
        // 分配角色
        int assignedHunters = 0;
        List<UUID> hunterList = new ArrayList<>();
        List<UUID> survivorList = new ArrayList<>();
        
        for (List<UUID> unit : assignmentUnits) {
            // 如果这个单位加入猎人后不会超过猎人数量，则分配为猎人
            if (assignedHunters + unit.size() <= hunterCount) {
                hunterList.addAll(unit);
                assignedHunters += unit.size();
            } else {
                survivorList.addAll(unit);
            }
        }
        
        // 如果没有猎人，强制分配第一个单位为猎人
        if (hunterList.isEmpty() && !assignmentUnits.isEmpty()) {
            List<UUID> firstUnit = assignmentUnits.get(0);
            hunterList.addAll(firstUnit);
            survivorList.removeAll(firstUnit);
        }
        
        // 设置角色
        for (UUID uuid : hunterList) {
            arena.setPlayerRole(uuid, PlayerRole.HUNTER);
        }
        
        for (UUID uuid : survivorList) {
            arena.setPlayerRole(uuid, PlayerRole.SURVIVOR);
        }
        
        plugin.getLogger().info("[" + arena.getArenaName() + "] 实际分配: " + 
            hunterList.size() + " 猎人, " + survivorList.size() + " 逃生者");
        
        // 通知玩家角色
        notifyRoles();
        
        // 传送玩家到出生点
        teleportToSpawns();
    }
    
    /**
     * 计算猎人数量
     * 
     * @param totalPlayers 总玩家数
     * @return 猎人数量
     */
    private int calculateHunterCount(int totalPlayers) {
        GameMode gameMode = arena.getGameMode();
        
        switch (gameMode) {
            case CLASSIC:
                // 经典模式：固定数量的猎人
                return plugin.getMainConfig().getClassicHunterCount();
            case TEAM:
                // 团队模式：按比例分配
                double ratio = plugin.getMainConfig().getTeamHunterRatio();
                return Math.max(1, (int) (totalPlayers * ratio));
            default:
                return 1;
        }
    }
    
    /**
     * 通知玩家角色
     */
    private void notifyRoles() {
        for (UUID uuid : arena.getPlayers()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) {
                continue;
            }
            
            PlayerRole role = arena.getPlayerRole(uuid);
            if (role == null) {
                continue;
            }
            
            // 发送角色分配消息
            switch (role) {
                case HUNTER:
                    plugin.getLanguageManager().sendMessage(player, "role.assigned-hunter");
                    // 显示猎人说明
                    player.sendMessage("");
                    player.sendMessage("§c§l=== 猎人 ===");
                    player.sendMessage("§7你的目标是追捕所有逃生者");
                    player.sendMessage("§7使用特殊能力来追踪和击杀逃生者");
                    player.sendMessage("§7右键点击物品使用能力");
                    player.sendMessage("");
                    break;
                case SURVIVOR:
                    plugin.getLanguageManager().sendMessage(player, "role.assigned-survivor");
                    // 显示逃生者说明
                    player.sendMessage("");
                    player.sendMessage("§a§l=== 逃生者 ===");
                    player.sendMessage("§7你的目标是躲避猎人并逃脱");
                    player.sendMessage("§7使用道具来帮助你逃脱");
                    player.sendMessage("§7前往逃生点并停留" + plugin.getMainConfig().getEscapeTime() + "秒即可逃脱");
                    player.sendMessage("");
                    break;
            }
        }
    }
    
    /**
     * 传送玩家到出生点
     */
    private void teleportToSpawns() {
        // 传送猎人
        List<UUID> hunters = arena.getHunters();
        List<Location> hunterSpawns = arena.getHunterSpawns();
        
        if (!hunterSpawns.isEmpty()) {
            for (int i = 0; i < hunters.size(); i++) {
                UUID uuid = hunters.get(i);
                Player player = Bukkit.getPlayer(uuid);
                
                if (player != null && player.isOnline()) {
                    // 循环使用出生点
                    Location spawn = hunterSpawns.get(i % hunterSpawns.size());
                    player.teleport(spawn);
                }
            }
        }
        
        // 传送逃生者
        List<UUID> survivors = arena.getSurvivors();
        List<Location> survivorSpawns = arena.getSurvivorSpawns();
        
        if (!survivorSpawns.isEmpty()) {
            for (int i = 0; i < survivors.size(); i++) {
                UUID uuid = survivors.get(i);
                Player player = Bukkit.getPlayer(uuid);
                
                if (player != null && player.isOnline()) {
                    // 循环使用出生点
                    Location spawn = survivorSpawns.get(i % survivorSpawns.size());
                    player.teleport(spawn);
                }
            }
        }
    }
    
    /**
     * 清除所有角色
     */
    public void clearRoles() {
        for (UUID uuid : arena.getPlayers()) {
            arena.setPlayerRole(uuid, null);
        }
    }
    
    /**
     * 获取玩家角色
     */
    public PlayerRole getPlayerRole(UUID uuid) {
        return arena.getPlayerRole(uuid);
    }
    
    /**
     * 检查玩家是否是猎人
     */
    public boolean isHunter(UUID uuid) {
        PlayerRole role = getPlayerRole(uuid);
        return role != null && role.isHunter();
    }
    
    /**
     * 检查玩家是否是逃生者
     */
    public boolean isSurvivor(UUID uuid) {
        PlayerRole role = getPlayerRole(uuid);
        return role != null && role.isSurvivor();
    }
}
