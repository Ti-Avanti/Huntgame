package com.minecraft.huntergame.command;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.party.Party;
import com.minecraft.huntergame.party.PartyManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 队伍命令处理器
 * 
 * @author YourName
 * @version 1.0.0
 */
public class PartyCommand implements CommandExecutor, TabCompleter {
    
    private final HunterGame plugin;
    private final PartyManager partyManager;
    
    public PartyCommand(HunterGame plugin) {
        this.plugin = plugin;
        this.partyManager = plugin.getPartyManager();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 检查权限
        if (!sender.hasPermission("huntergame.party")) {
            plugin.getLanguageManager().sendMessage(sender, "general.no-permission");
            return true;
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("general.player-only"));
            return true;
        }
        
        Player player = (Player) sender;
        
        // 显示帮助
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "create":
                handleCreate(player);
                break;
                
            case "disband":
                handleDisband(player);
                break;
                
            case "invite":
                if (args.length < 2) {
                    plugin.getLanguageManager().sendMessage(player, "party.usage-invite");
                    return true;
                }
                handleInvite(player, args[1]);
                break;
                
            case "accept":
                if (args.length < 2) {
                    plugin.getLanguageManager().sendMessage(player, "party.usage-accept");
                    return true;
                }
                handleAccept(player, args[1]);
                break;
                
            case "decline":
                if (args.length < 2) {
                    plugin.getLanguageManager().sendMessage(player, "party.usage-decline");
                    return true;
                }
                handleDecline(player, args[1]);
                break;
                
            case "leave":
                handleLeave(player);
                break;
                
            case "kick":
                if (args.length < 2) {
                    plugin.getLanguageManager().sendMessage(player, "party.usage-kick");
                    return true;
                }
                handleKick(player, args[1]);
                break;
                
            case "list":
                handleList(player);
                break;
                
            case "chat":
            case "c":
                handleChat(player);
                break;
                
            case "info":
                handleInfo(player);
                break;
                
            default:
                sendHelp(player);
                break;
        }
        
        return true;
    }
    
    /**
     * 创建队伍
     */
    private void handleCreate(Player player) {
        if (partyManager.hasParty(player)) {
            plugin.getLanguageManager().sendMessage(player, "party.already-in-party");
            return;
        }
        
        Party party = partyManager.createParty(player);
        
        if (party == null) {
            plugin.getLanguageManager().sendMessage(player, "party.create-failed");
            return;
        }
        
        plugin.getLanguageManager().sendMessage(player, "party.created");
    }
    
    /**
     * 解散队伍
     */
    private void handleDisband(Player player) {
        Party party = partyManager.getParty(player);
        
        if (party == null) {
            plugin.getLanguageManager().sendMessage(player, "party.not-in-party");
            return;
        }
        
        if (!party.isLeader(player)) {
            plugin.getLanguageManager().sendMessage(player, "party.not-leader");
            return;
        }
        
        if (partyManager.disbandParty(player)) {
            plugin.getLanguageManager().sendMessage(player, "party.disbanded-self");
        } else {
            plugin.getLanguageManager().sendMessage(player, "party.disband-failed");
        }
    }
    
    /**
     * 邀请玩家
     */
    private void handleInvite(Player player, String targetName) {
        Party party = partyManager.getParty(player);
        
        if (party == null) {
            plugin.getLanguageManager().sendMessage(player, "party.not-in-party");
            return;
        }
        
        if (!party.isLeader(player)) {
            plugin.getLanguageManager().sendMessage(player, "party.not-leader");
            return;
        }
        
        Player target = Bukkit.getPlayer(targetName);
        
        if (target == null || !target.isOnline()) {
            plugin.getLanguageManager().sendMessage(player, "general.player-not-found");
            return;
        }
        
        if (target.equals(player)) {
            plugin.getLanguageManager().sendMessage(player, "party.cannot-invite-self");
            return;
        }
        
        if (party.isFull()) {
            plugin.getLanguageManager().sendMessage(player, "party.full");
            return;
        }
        
        if (partyManager.hasParty(target)) {
            plugin.getLanguageManager().sendMessage(player, "party.target-already-in-party", 
                target.getName());
            return;
        }
        
        if (partyManager.invitePlayer(player, target)) {
            // 消息已在PartyManager中发送
        } else {
            plugin.getLanguageManager().sendMessage(player, "party.invite-failed");
        }
    }
    
    /**
     * 接受邀请
     */
    private void handleAccept(Player player, String leaderName) {
        if (partyManager.hasParty(player)) {
            plugin.getLanguageManager().sendMessage(player, "party.already-in-party");
            return;
        }
        
        Player leader = Bukkit.getPlayer(leaderName);
        
        if (leader == null || !leader.isOnline()) {
            plugin.getLanguageManager().sendMessage(player, "general.player-not-found");
            return;
        }
        
        if (partyManager.acceptInvitation(player, leader)) {
            plugin.getLanguageManager().sendMessage(player, "party.joined", leader.getName());
        } else {
            plugin.getLanguageManager().sendMessage(player, "party.no-invitation");
        }
    }
    
    /**
     * 拒绝邀请
     */
    private void handleDecline(Player player, String leaderName) {
        Player leader = Bukkit.getPlayer(leaderName);
        
        if (leader == null || !leader.isOnline()) {
            plugin.getLanguageManager().sendMessage(player, "general.player-not-found");
            return;
        }
        
        if (!partyManager.declineInvitation(player, leader)) {
            plugin.getLanguageManager().sendMessage(player, "party.no-invitation");
        }
    }
    
    /**
     * 离开队伍
     */
    private void handleLeave(Player player) {
        if (!partyManager.hasParty(player)) {
            plugin.getLanguageManager().sendMessage(player, "party.not-in-party");
            return;
        }
        
        if (partyManager.leaveParty(player)) {
            plugin.getLanguageManager().sendMessage(player, "party.left");
        } else {
            plugin.getLanguageManager().sendMessage(player, "party.leave-failed");
        }
    }
    
    /**
     * 踢出成员
     */
    private void handleKick(Player player, String targetName) {
        Party party = partyManager.getParty(player);
        
        if (party == null) {
            plugin.getLanguageManager().sendMessage(player, "party.not-in-party");
            return;
        }
        
        if (!party.isLeader(player)) {
            plugin.getLanguageManager().sendMessage(player, "party.not-leader");
            return;
        }
        
        Player target = Bukkit.getPlayer(targetName);
        
        if (target == null || !target.isOnline()) {
            plugin.getLanguageManager().sendMessage(player, "general.player-not-found");
            return;
        }
        
        if (target.equals(player)) {
            plugin.getLanguageManager().sendMessage(player, "party.cannot-kick-self");
            return;
        }
        
        if (!party.isMember(target)) {
            plugin.getLanguageManager().sendMessage(player, "party.not-member", target.getName());
            return;
        }
        
        if (partyManager.kickMember(player, target)) {
            plugin.getLanguageManager().sendMessage(player, "party.kicked-member", target.getName());
        } else {
            plugin.getLanguageManager().sendMessage(player, "party.kick-failed");
        }
    }
    
    /**
     * 列出队伍成员
     */
    private void handleList(Player player) {
        Party party = partyManager.getParty(player);
        
        if (party == null) {
            plugin.getLanguageManager().sendMessage(player, "party.not-in-party");
            return;
        }
        
        player.sendMessage("§6========== §e队伍信息 §6==========");
        
        Player leader = party.getLeaderPlayer();
        String leaderName = leader != null ? leader.getName() : "Unknown";
        player.sendMessage("§e队长: §a" + leaderName);
        player.sendMessage("§e成员数: §a" + party.getMemberCount() + "/" + party.getMaxMembers());
        player.sendMessage("§e成员列表:");
        
        for (UUID uuid : party.getMembers()) {
            Player member = Bukkit.getPlayer(uuid);
            if (member != null && member.isOnline()) {
                String prefix = party.isLeader(uuid) ? "§6[队长] " : "§7";
                player.sendMessage("  " + prefix + member.getName());
            }
        }
    }
    
    /**
     * 切换队伍聊天模式
     */
    private void handleChat(Player player) {
        if (!partyManager.hasParty(player)) {
            plugin.getLanguageManager().sendMessage(player, "party.not-in-party");
            return;
        }
        
        partyManager.togglePartyChatMode(player);
    }
    
    /**
     * 显示队伍信息
     */
    private void handleInfo(Player player) {
        handleList(player);
    }
    
    /**
     * 发送帮助信息
     */
    private void sendHelp(Player player) {
        player.sendMessage("§6========== §e队伍命令帮助 §6==========");
        player.sendMessage("§e/party create §7- 创建队伍");
        player.sendMessage("§e/party disband §7- 解散队伍（队长）");
        player.sendMessage("§e/party invite <玩家> §7- 邀请玩家（队长）");
        player.sendMessage("§e/party accept <队长> §7- 接受邀请");
        player.sendMessage("§e/party decline <队长> §7- 拒绝邀请");
        player.sendMessage("§e/party leave §7- 离开队伍");
        player.sendMessage("§e/party kick <玩家> §7- 踢出成员（队长）");
        player.sendMessage("§e/party list §7- 查看队伍成员");
        player.sendMessage("§e/party chat §7- 切换队伍聊天模式");
        player.sendMessage("§e/party info §7- 查看队伍信息");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }
        
        Player player = (Player) sender;
        
        if (args.length == 1) {
            List<String> subCommands = Arrays.asList(
                "create", "disband", "invite", "accept", "decline", 
                "leave", "kick", "list", "chat", "info"
            );
            
            return subCommands.stream()
                .filter(cmd -> cmd.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            
            // 需要玩家名称的子命令
            if (subCommand.equals("invite") || subCommand.equals("kick") || 
                subCommand.equals("accept") || subCommand.equals("decline")) {
                
                return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
            }
        }
        
        return new ArrayList<>();
    }
}
