package com.minecraft.huntergame.listener;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.arena.Arena;
import com.minecraft.huntergame.game.PlayerRole;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * 战斗事件监听器
 * 监听玩家战斗相关事件
 * 
 * @author YourName
 * @version 1.0.0
 */
public class CombatListener implements Listener {
    
    private final HunterGame plugin;
    
    public CombatListener(HunterGame plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 监听玩家攻击事件
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // 检查是否为玩家攻击玩家
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player attacker = (Player) event.getDamager();
        Player victim = (Player) event.getEntity();
        
        // 检查攻击者是否在竞技场中
        Arena attackerArena = plugin.getArenaManager().getPlayerArena(attacker.getUniqueId());
        if (attackerArena == null) {
            return;
        }
        
        // 检查受害者是否在同一竞技场中
        Arena victimArena = plugin.getArenaManager().getPlayerArena(victim.getUniqueId());
        if (victimArena == null || !victimArena.equals(attackerArena)) {
            event.setCancelled(true);
            return;
        }
        
        Arena arena = attackerArena;
        
        // 检查游戏是否在进行中
        if (!arena.getStateManager().isPlaying()) {
            event.setCancelled(true);
            return;
        }
        
        // 获取玩家角色
        PlayerRole attackerRole = arena.getPlayerRole(attacker.getUniqueId());
        PlayerRole victimRole = arena.getPlayerRole(victim.getUniqueId());
        
        // 验证攻击合法性
        if (!isValidAttack(attackerRole, victimRole)) {
            event.setCancelled(true);
            plugin.getLanguageManager().sendMessage(attacker, "combat.invalid-target");
            return;
        }
        
        plugin.getLogger().info("[" + arena.getArenaName() + "] " + attacker.getName() + 
            " 攻击了 " + victim.getName());
    }
    
    /**
     * 验证攻击是否合法
     * 
     * @param attackerRole 攻击者角色
     * @param victimRole 受害者角色
     * @return 是否合法
     */
    private boolean isValidAttack(PlayerRole attackerRole, PlayerRole victimRole) {
        // 观战者不能攻击任何人
        if (attackerRole == PlayerRole.SPECTATOR) {
            return false;
        }
        
        // 不能攻击观战者
        if (victimRole == PlayerRole.SPECTATOR) {
            return false;
        }
        
        // 猎人可以攻击逃生者
        if (attackerRole == PlayerRole.HUNTER && victimRole == PlayerRole.SURVIVOR) {
            return true;
        }
        
        // 逃生者可以攻击猎人
        if (attackerRole == PlayerRole.SURVIVOR && victimRole == PlayerRole.HUNTER) {
            return true;
        }
        
        // 同阵营不能互相攻击
        return false;
    }
}
