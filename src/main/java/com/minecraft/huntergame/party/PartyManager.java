package com.minecraft.huntergame.party;

import com.minecraft.huntergame.HunterGame;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * 队伍管理器
 * 负责管理所有队伍
 * 
 * @author YourName
 * @version 1.0.0
 */
public class PartyManager {
    
    private final HunterGame plugin;
    
    // 队伍映射
    private final Map<UUID, Party> parties;
    
    // 玩家-队伍映射
    private final Map<UUID, Party> playerPartyMap;
    
    // 队伍聊天模式
    private final Set<UUID> partyChatMode;
    
    public PartyManager(HunterGame plugin) {
        this.plugin = plugin;
        this.parties = new HashMap<>();
        this.playerPartyMap = new HashMap<>();
        this.partyChatMode = new HashSet<>();
        
        // 启动清理任务
        startCleanupTask();
    }
    
    // ==================== 队伍创建与解散 ====================
    
    /**
     * 创建队伍
     */
    public Party createParty(Player leader) {
        UUID uuid = leader.getUniqueId();
        
        // 检查玩家是否已在队伍中
        if (hasParty(uuid)) {
            return null;
        }
        
        // 创建队伍
        Party party = new Party(plugin, uuid);
        parties.put(party.getPartyId(), party);
        playerPartyMap.put(uuid, party);
        
        plugin.getLogger().info("玩家 " + leader.getName() + " 创建了队伍");
        return party;
    }
    
    /**
     * 解散队伍
     */
    public boolean disbandParty(Party party) {
        if (party == null) {
            return false;
        }
        
        // 通知所有成员
        party.broadcast(plugin.getLanguageManager().getMessage("party.disbanded"));
        
        // 移除所有成员的映射
        for (UUID uuid : party.getMembers()) {
            playerPartyMap.remove(uuid);
            partyChatMode.remove(uuid);
        }
        
        // 移除队伍
        parties.remove(party.getPartyId());
        
        plugin.getLogger().info("队伍 " + party.getPartyId() + " 已解散");
        return true;
    }
    
    /**
     * 解散队伍（通过队长）
     */
    public boolean disbandParty(Player leader) {
        Party party = getParty(leader);
        
        if (party == null) {
            return false;
        }
        
        if (!party.isLeader(leader)) {
            return false;
        }
        
        return disbandParty(party);
    }
    
    // ==================== 队伍查询 ====================
    
    /**
     * 获取玩家所在的队伍
     */
    public Party getParty(UUID uuid) {
        return playerPartyMap.get(uuid);
    }
    
    /**
     * 获取玩家所在的队伍
     */
    public Party getParty(Player player) {
        return getParty(player.getUniqueId());
    }
    
    /**
     * 通过ID获取队伍
     */
    public Party getPartyById(UUID partyId) {
        return parties.get(partyId);
    }
    
    /**
     * 检查玩家是否在队伍中
     */
    public boolean hasParty(UUID uuid) {
        return playerPartyMap.containsKey(uuid);
    }
    
    /**
     * 检查玩家是否在队伍中
     */
    public boolean hasParty(Player player) {
        return hasParty(player.getUniqueId());
    }
    
    /**
     * 获取所有队伍
     */
    public Collection<Party> getParties() {
        return parties.values();
    }
    
    /**
     * 获取队伍数量
     */
    public int getPartyCount() {
        return parties.size();
    }
    
    // ==================== 成员管理 ====================
    
    /**
     * 玩家加入队伍
     */
    public boolean joinParty(Player player, Party party) {
        UUID uuid = player.getUniqueId();
        
        // 检查玩家是否已在队伍中
        if (hasParty(uuid)) {
            return false;
        }
        
        // 添加到队伍
        if (!party.addMember(uuid)) {
            return false;
        }
        
        // 更新映射
        playerPartyMap.put(uuid, party);
        
        // 通知队伍成员
        party.broadcast(plugin.getLanguageManager().getMessage("party.member-joined", 
            player.getName()), uuid);
        
        plugin.getLogger().info("玩家 " + player.getName() + " 加入了队伍 " + party.getPartyId());
        return true;
    }
    
    /**
     * 玩家离开队伍
     */
    public boolean leaveParty(Player player) {
        UUID uuid = player.getUniqueId();
        Party party = getParty(uuid);
        
        if (party == null) {
            return false;
        }
        
        // 如果是队长，解散队伍
        if (party.isLeader(uuid)) {
            // 如果队伍中还有其他成员，转让队长
            if (party.getMemberCount() > 1) {
                // 找到第一个不是队长的成员
                UUID newLeader = party.getMembers().stream()
                    .filter(memberId -> !memberId.equals(uuid))
                    .findFirst()
                    .orElse(null);
                
                if (newLeader != null) {
                    party.transferLeadership(newLeader);
                    party.removeMember(uuid);
                    playerPartyMap.remove(uuid);
                    partyChatMode.remove(uuid);
                    
                    Player newLeaderPlayer = plugin.getServer().getPlayer(newLeader);
                    String newLeaderName = newLeaderPlayer != null ? newLeaderPlayer.getName() : "Unknown";
                    
                    party.broadcast(plugin.getLanguageManager().getMessage("party.member-left", 
                        player.getName()));
                    party.broadcast(plugin.getLanguageManager().getMessage("party.new-leader", 
                        newLeaderName));
                    
                    plugin.getLogger().info("玩家 " + player.getName() + " 离开队伍，队长转让给 " + newLeaderName);
                    return true;
                }
            }
            
            // 队伍只有队长，解散队伍
            return disbandParty(party);
        }
        
        // 移除成员
        party.removeMember(uuid);
        playerPartyMap.remove(uuid);
        partyChatMode.remove(uuid);
        
        // 通知队伍成员
        party.broadcast(plugin.getLanguageManager().getMessage("party.member-left", 
            player.getName()));
        
        plugin.getLogger().info("玩家 " + player.getName() + " 离开了队伍");
        return true;
    }
    
    /**
     * 踢出成员
     */
    public boolean kickMember(Player leader, Player target) {
        Party party = getParty(leader);
        
        if (party == null) {
            return false;
        }
        
        if (!party.isLeader(leader)) {
            return false;
        }
        
        UUID targetUuid = target.getUniqueId();
        
        if (!party.isMember(targetUuid)) {
            return false;
        }
        
        if (party.isLeader(targetUuid)) {
            return false;
        }
        
        // 移除成员
        party.removeMember(targetUuid);
        playerPartyMap.remove(targetUuid);
        partyChatMode.remove(targetUuid);
        
        // 通知被踢出的玩家
        plugin.getLanguageManager().sendMessage(target, "party.kicked");
        
        // 通知队伍成员
        party.broadcast(plugin.getLanguageManager().getMessage("party.member-kicked", 
            target.getName()));
        
        plugin.getLogger().info("玩家 " + target.getName() + " 被踢出队伍");
        return true;
    }
    
    // ==================== 邀请管理 ====================
    
    /**
     * 邀请玩家
     */
    public boolean invitePlayer(Player leader, Player target) {
        Party party = getParty(leader);
        
        if (party == null) {
            return false;
        }
        
        if (!party.isLeader(leader)) {
            return false;
        }
        
        UUID targetUuid = target.getUniqueId();
        
        // 检查目标玩家是否已在队伍中
        if (hasParty(targetUuid)) {
            return false;
        }
        
        // 发送邀请
        if (!party.invite(targetUuid)) {
            return false;
        }
        
        // 通知目标玩家
        plugin.getLanguageManager().sendMessage(target, "party.invited", leader.getName());
        plugin.getLanguageManager().sendMessage(target, "party.invite-accept-hint");
        
        // 通知队长
        plugin.getLanguageManager().sendMessage(leader, "party.invite-sent", target.getName());
        
        plugin.getLogger().info("玩家 " + leader.getName() + " 邀请 " + target.getName() + " 加入队伍");
        return true;
    }
    
    /**
     * 接受邀请
     */
    public boolean acceptInvitation(Player player, Player leader) {
        Party party = getParty(leader);
        
        if (party == null) {
            return false;
        }
        
        UUID uuid = player.getUniqueId();
        
        // 检查是否有邀请
        if (!party.hasInvitation(uuid)) {
            return false;
        }
        
        // 接受邀请
        if (!party.acceptInvitation(uuid)) {
            return false;
        }
        
        // 更新映射
        playerPartyMap.put(uuid, party);
        
        // 通知队伍成员
        party.broadcast(plugin.getLanguageManager().getMessage("party.member-joined", 
            player.getName()));
        
        plugin.getLogger().info("玩家 " + player.getName() + " 接受了队伍邀请");
        return true;
    }
    
    /**
     * 拒绝邀请
     */
    public boolean declineInvitation(Player player, Player leader) {
        Party party = getParty(leader);
        
        if (party == null) {
            return false;
        }
        
        UUID uuid = player.getUniqueId();
        
        // 检查是否有邀请
        if (!party.hasInvitation(uuid)) {
            return false;
        }
        
        // 拒绝邀请
        party.declineInvitation(uuid);
        
        // 通知队长
        plugin.getLanguageManager().sendMessage(leader, "party.invite-declined", player.getName());
        
        // 通知玩家
        plugin.getLanguageManager().sendMessage(player, "party.invite-declined-self", leader.getName());
        
        plugin.getLogger().info("玩家 " + player.getName() + " 拒绝了队伍邀请");
        return true;
    }
    
    // ==================== 队伍聊天 ====================
    
    /**
     * 切换队伍聊天模式
     */
    public boolean togglePartyChatMode(Player player) {
        UUID uuid = player.getUniqueId();
        
        if (!hasParty(uuid)) {
            return false;
        }
        
        if (partyChatMode.contains(uuid)) {
            partyChatMode.remove(uuid);
            plugin.getLanguageManager().sendMessage(player, "party.chat-mode-disabled");
        } else {
            partyChatMode.add(uuid);
            plugin.getLanguageManager().sendMessage(player, "party.chat-mode-enabled");
        }
        
        return true;
    }
    
    /**
     * 检查玩家是否开启队伍聊天模式
     */
    public boolean isInPartyChatMode(UUID uuid) {
        return partyChatMode.contains(uuid);
    }
    
    /**
     * 检查玩家是否开启队伍聊天模式
     */
    public boolean isInPartyChatMode(Player player) {
        return isInPartyChatMode(player.getUniqueId());
    }
    
    /**
     * 发送队伍聊天消息
     */
    public void sendPartyChat(Player sender, String message) {
        Party party = getParty(sender);
        
        if (party == null) {
            return;
        }
        
        party.sendPartyChat(sender, message);
    }
    
    // ==================== 清理任务 ====================
    
    /**
     * 启动清理任务
     */
    private void startCleanupTask() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            // 清理过期邀请
            for (Party party : parties.values()) {
                party.cleanupExpiredInvitations();
            }
            
            // 清理空队伍
            List<Party> emptyParties = new ArrayList<>();
            for (Party party : parties.values()) {
                if (party.isEmpty()) {
                    emptyParties.add(party);
                }
            }
            
            for (Party party : emptyParties) {
                disbandParty(party);
            }
        }, 20L * 60, 20L * 60); // 每分钟执行一次
    }
    
    /**
     * 玩家离线处理
     */
    public void handlePlayerQuit(Player player) {
        UUID uuid = player.getUniqueId();
        Party party = getParty(uuid);
        
        if (party == null) {
            return;
        }
        
        // 玩家离开队伍
        leaveParty(player);
    }
}
