package com.minecraft.huntergame.ability;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.arena.Arena;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

/**
 * 追踪能力
 * 指南针指向最近的逃生者
 * 
 * @author YourName
 * @version 1.0.0
 */
public class TrackerAbility extends HunterAbility {
    
    public TrackerAbility(HunterGame plugin) {
        super(plugin, "tracker");
        
        // 从配置加载参数
        this.duration = 0;  // 追踪是瞬时效果
        this.cooldown = plugin.getMainConfig().getTrackerCooldown();
        this.enabled = plugin.getMainConfig().isTrackerEnabled();
    }
    
    @Override
    protected boolean onActivate(Player player) {
        // 获取玩家所在的竞技场
        Arena arena = plugin.getArenaManager().getPlayerArena(player);
        if (arena == null) {
            return false;
        }
        
        // 获取所有存活的逃生者
        List<UUID> survivors = arena.getAliveSurvivors();
        if (survivors.isEmpty()) {
            player.sendMessage("§c没有找到逃生者！");
            return false;
        }
        
        // 找到最近的逃生者
        Player nearestSurvivor = findNearestSurvivor(player, survivors);
        if (nearestSurvivor == null) {
            player.sendMessage("§c没有找到逃生者！");
            return false;
        }
        
        // 设置指南针指向
        player.setCompassTarget(nearestSurvivor.getLocation());
        
        // 发送消息
        plugin.getLanguageManager().sendMessage(player, "ability.tracker");
        player.sendMessage("§e最近的逃生者距离: §c" + 
            (int) player.getLocation().distance(nearestSurvivor.getLocation()) + " §e格");
        
        return true;
    }
    
    @Override
    protected void onDeactivate(Player player) {
        // 追踪是瞬时效果，不需要停用操作
    }
    
    /**
     * 找到最近的逃生者
     */
    private Player findNearestSurvivor(Player hunter, List<UUID> survivors) {
        Location hunterLoc = hunter.getLocation();
        Player nearest = null;
        double minDistance = Double.MAX_VALUE;
        
        for (UUID uuid : survivors) {
            Player survivor = plugin.getServer().getPlayer(uuid);
            if (survivor == null || !survivor.isOnline()) {
                continue;
            }
            
            double distance = hunterLoc.distance(survivor.getLocation());
            if (distance < minDistance) {
                minDistance = distance;
                nearest = survivor;
            }
        }
        
        return nearest;
    }
}
