package com.minecraft.huntergame.party;

import com.minecraft.huntergame.HunterGame;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * 队伍类
 * 代表一个玩家队伍
 * 
 * @author YourName
 * @version 1.0.0
 */
public class Party {
    
    private final HunterGame plugin;
    private final UUID partyId;
    private UUID leader;
    private final List<UUID> members;
    private final Map<UUID, Long> invitations;
    private final int maxMembers;
    
    public Party(HunterGame plugin, UUID leader) {
        this.plugin = plugin;
        this.partyId = UUID.randomUUID();
        this.leader = leader;
        this.members = new ArrayList<>();
        this.invitations = new HashMap<>();
        this.maxMembers = plugin.getMainConfig().getMaxPartySize();
        
        // 队长自动加入
        members.add(leader);
    }
    
    // ==================== 基础信息 ====================
    
    /**
     * 获取队伍ID
     */
    public UUID getPartyId() {
        return partyId;
    }
    
    /**
     * 获取队长UUID
     */
    public UUID getLeader() {
        return leader;
    }
    
    /**
     * 获取队长玩家
     */
    public Player getLeaderPlayer() {
        return Bukkit.getPlayer(leader);
    }
    
    /**
     * 获取所有成员UUID
     */
    public List<UUID> getMembers() {
        return new ArrayList<>(members);
    }
    
    /**
     * 获取成员数量
     */
    public int getMemberCount() {
        return members.size();
    }
    
    /**
     * 获取最大成员数
     */
    public int getMaxMembers() {
        return maxMembers;
    }
    
    /**
     * 检查是否已满
     */
    public boolean isFull() {
        return members.size() >= maxMembers;
    }
    
    // ==================== 成员管理 ====================
    
    /**
     * 检查玩家是否为队长
     */
    public boolean isLeader(UUID uuid) {
        return leader.equals(uuid);
    }
    
    /**
     * 检查玩家是否为队长
     */
    public boolean isLeader(Player player) {
        return isLeader(player.getUniqueId());
    }
    
    /**
     * 检查玩家是否为成员
     */
    public boolean isMember(UUID uuid) {
        return members.contains(uuid);
    }
    
    /**
     * 检查玩家是否为成员
     */
    public boolean isMember(Player player) {
        return isMember(player.getUniqueId());
    }
    
    /**
     * 添加成员
     */
    public boolean addMember(UUID uuid) {
        if (isMember(uuid)) {
            return false;
        }
        
        if (isFull()) {
            return false;
        }
        
        members.add(uuid);
        invitations.remove(uuid);
        
        return true;
    }
    
    /**
     * 移除成员
     */
    public boolean removeMember(UUID uuid) {
        if (!isMember(uuid)) {
            return false;
        }
        
        // 不能移除队长
        if (isLeader(uuid)) {
            return false;
        }
        
        members.remove(uuid);
        return true;
    }
    
    /**
     * 转让队长
     */
    public boolean transferLeadership(UUID newLeader) {
        if (!isMember(newLeader)) {
            return false;
        }
        
        if (isLeader(newLeader)) {
            return false;
        }
        
        this.leader = newLeader;
        return true;
    }
    
    // ==================== 邀请管理 ====================
    
    /**
     * 邀请玩家
     */
    public boolean invite(UUID uuid) {
        if (isMember(uuid)) {
            return false;
        }
        
        if (isFull()) {
            return false;
        }
        
        if (hasInvitation(uuid)) {
            return false;
        }
        
        long expireTime = System.currentTimeMillis() + 60000; // 60秒过期
        invitations.put(uuid, expireTime);
        
        return true;
    }
    
    /**
     * 检查玩家是否有邀请
     */
    public boolean hasInvitation(UUID uuid) {
        if (!invitations.containsKey(uuid)) {
            return false;
        }
        
        long expireTime = invitations.get(uuid);
        if (System.currentTimeMillis() > expireTime) {
            invitations.remove(uuid);
            return false;
        }
        
        return true;
    }
    
    /**
     * 接受邀请
     */
    public boolean acceptInvitation(UUID uuid) {
        if (!hasInvitation(uuid)) {
            return false;
        }
        
        return addMember(uuid);
    }
    
    /**
     * 拒绝邀请
     */
    public boolean declineInvitation(UUID uuid) {
        if (!hasInvitation(uuid)) {
            return false;
        }
        
        invitations.remove(uuid);
        return true;
    }
    
    /**
     * 取消邀请
     */
    public boolean cancelInvitation(UUID uuid) {
        if (!invitations.containsKey(uuid)) {
            return false;
        }
        
        invitations.remove(uuid);
        return true;
    }
    
    /**
     * 清理过期邀请
     */
    public void cleanupExpiredInvitations() {
        long currentTime = System.currentTimeMillis();
        invitations.entrySet().removeIf(entry -> currentTime > entry.getValue());
    }
    
    // ==================== 消息广播 ====================
    
    /**
     * 向所有成员发送消息
     */
    public void broadcast(String message) {
        for (UUID uuid : members) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.sendMessage(message);
            }
        }
    }
    
    /**
     * 向所有成员发送消息（排除指定玩家）
     */
    public void broadcast(String message, UUID exclude) {
        for (UUID uuid : members) {
            if (uuid.equals(exclude)) {
                continue;
            }
            
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.sendMessage(message);
            }
        }
    }
    
    /**
     * 发送队伍聊天消息
     */
    public void sendPartyChat(Player sender, String message) {
        String formatted = plugin.getLanguageManager().getMessage("party.chat-format", 
            sender.getName(), message);
        broadcast(formatted);
    }
    
    // ==================== 队伍状态 ====================
    
    /**
     * 检查队伍是否为空
     */
    public boolean isEmpty() {
        return members.isEmpty();
    }
    
    /**
     * 检查队伍是否只有队长
     */
    public boolean hasOnlyLeader() {
        return members.size() == 1 && isMember(leader);
    }
    
    /**
     * 获取在线成员
     */
    public List<Player> getOnlineMembers() {
        List<Player> online = new ArrayList<>();
        for (UUID uuid : members) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                online.add(player);
            }
        }
        return online;
    }
    
    /**
     * 获取在线成员数量
     */
    public int getOnlineMemberCount() {
        return getOnlineMembers().size();
    }
}
