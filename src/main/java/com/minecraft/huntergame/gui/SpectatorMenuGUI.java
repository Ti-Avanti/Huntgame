package com.minecraft.huntergame.gui;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.game.ManhuntGame;
import com.minecraft.huntergame.game.PlayerRole;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 观战菜单GUI
 * 显示可观战的玩家列表
 * 
 * @author YourName
 * @version 1.0.0
 */
public class SpectatorMenuGUI extends BaseGUI {
    
    private final ManhuntGame game;
    
    public SpectatorMenuGUI(HunterGame plugin, Player player, ManhuntGame game) {
        super(plugin, player);
        this.game = game;
    }
    
    @Override
    protected String getTitle() {
        return "§8观战菜单";
    }
    
    @Override
    protected int getSize() {
        return 54;
    }
    
    @Override
    protected void buildContent() {
        // 获取所有存活的玩家
        List<UUID> alivePlayers = new ArrayList<>();
        
        // 添加逃亡者
        for (UUID uuid : game.getRunners()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline() && !game.getSpectators().contains(uuid)) {
                alivePlayers.add(uuid);
            }
        }
        
        // 添加猎人
        for (UUID uuid : game.getHunters()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                alivePlayers.add(uuid);
            }
        }
        
        // 如果没有可观战的玩家
        if (alivePlayers.isEmpty()) {
            ItemStack noPlayers = GUIUtils.createItem(
                Material.BARRIER,
                "§c没有可观战的玩家",
                "§7当前没有存活的玩家"
            );
            inventory.setItem(22, noPlayers);
            return;
        }
        
        // 显示玩家头颅
        int slot = 10;
        for (UUID uuid : alivePlayers) {
            Player target = Bukkit.getPlayer(uuid);
            if (target == null) continue;
            
            // 创建玩家头颅
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            
            if (meta != null) {
                meta.setOwningPlayer(target);
                
                // 获取角色
                PlayerRole role = game.getPlayerRole(uuid);
                String roleColor = role == PlayerRole.RUNNER ? "§a" : "§c";
                String roleName = role == PlayerRole.RUNNER ? "逃亡者" : "猎人";
                
                meta.setDisplayName(roleColor + target.getName());
                
                List<String> lore = new ArrayList<>();
                lore.add("§7角色: " + roleColor + roleName);
                lore.add("§7生命: §c" + String.format("%.1f", target.getHealth()) + "§7/§c20.0");
                lore.add("");
                lore.add("§e点击观战此玩家");
                meta.setLore(lore);
                
                skull.setItemMeta(meta);
            }
            
            inventory.setItem(slot, skull);
            
            // 计算下一个槽位
            slot++;
            if (slot == 17) slot = 19;
            if (slot == 26) slot = 28;
            if (slot == 35) slot = 37;
            if (slot >= 44) break; // 最多显示27个玩家
        }
        
        // 返回按钮
        ItemStack back = GUIUtils.createItem(
            Material.ARROW,
            "§c返回",
            "§7点击关闭菜单"
        );
        inventory.setItem(49, back);
    }
    
    @Override
    public boolean handleClick(int slot, ItemStack clickedItem) {
        if (clickedItem == null || !clickedItem.hasItemMeta()) {
            return true;
        }
        
        Material type = clickedItem.getType();
        
        // 返回按钮
        if (type == Material.ARROW) {
            player.closeInventory();
            return true;
        }
        
        // 玩家头颅
        if (type == Material.PLAYER_HEAD) {
            SkullMeta meta = (SkullMeta) clickedItem.getItemMeta();
            if (meta != null && meta.getOwningPlayer() != null) {
                Player target = Bukkit.getPlayer(meta.getOwningPlayer().getUniqueId());
                if (target != null && target.isOnline()) {
                    // 传送到目标玩家
                    player.teleport(target);
                    player.sendMessage("§a正在观战 " + target.getName());
                    player.closeInventory();
                } else {
                    player.sendMessage("§c该玩家已离线");
                }
            }
        }
        
        return true;
    }
}
