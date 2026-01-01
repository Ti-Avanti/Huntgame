package com.minecraft.huntergame.event;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.game.ManhuntGame;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Random;
import java.util.UUID;

/**
 * 随机传送事件
 * 随机传送玩家到附近位置
 * 
 * @author YourName
 * @version 1.0.0
 */
public class TeleportEvent extends GameEvent {
    
    private final Random random;
    private final int maxDistance = 500; // 最大传送距离
    
    public TeleportEvent(HunterGame plugin) {
        super(plugin, "随机传送", "随机传送玩家到附近位置");
        this.random = new Random();
    }
    
    @Override
    public void trigger(ManhuntGame game) {
        // 随机选择一个玩家
        UUID targetUuid = selectRandomPlayer(game);
        if (targetUuid == null) {
            return;
        }
        
        Player target = plugin.getServer().getPlayer(targetUuid);
        if (target == null || !target.isOnline()) {
            return;
        }
        
        Location currentLoc = target.getLocation();
        World world = currentLoc.getWorld();
        
        // 生成随机偏移
        int offsetX = random.nextInt(maxDistance * 2) - maxDistance;
        int offsetZ = random.nextInt(maxDistance * 2) - maxDistance;
        
        // 计算新位置
        int newX = (int)(currentLoc.getX() + offsetX);
        int newZ = (int)(currentLoc.getZ() + offsetZ);
        int newY = world.getHighestBlockYAt(newX, newZ) + 1;
        
        // 安全检查：确保Y坐标在有效范围内
        if (newY < world.getMinHeight()) {
            newY = world.getMinHeight() + 1;
        } else if (newY > world.getMaxHeight() - 2) {
            newY = world.getMaxHeight() - 2;
        }
        
        Location newLoc = new Location(world, newX + 0.5, newY, newZ + 0.5, currentLoc.getYaw(), currentLoc.getPitch());
        
        // 传送玩家
        target.teleport(newLoc);
        
        // 广播消息
        broadcastToGame(game, "§d§l[事件] §e玩家 §b" + target.getName() + " §e被随机传送了！");
        target.sendMessage("§d你被随机传送到了一个新位置！");
        
        plugin.getLogger().info("触发随机传送事件: " + target.getName());
    }
    
    @Override
    public boolean canTrigger(ManhuntGame game) {
        return game.getState() == com.minecraft.huntergame.game.GameState.PLAYING &&
               !game.getAllPlayers().isEmpty();
    }
    
    /**
     * 随机选择一个玩家
     */
    private UUID selectRandomPlayer(ManhuntGame game) {
        // 只选择逃亡者和猎人，不包括观战者
        java.util.List<UUID> players = new java.util.ArrayList<>();
        players.addAll(game.getRunners());
        players.addAll(game.getHunters());
        
        if (players.isEmpty()) {
            return null;
        }
        
        return players.get(random.nextInt(players.size()));
    }
    
    /**
     * 向游戏内所有玩家广播消息
     */
    private void broadcastToGame(ManhuntGame game, String message) {
        for (UUID uuid : game.getAllPlayers()) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.sendMessage(message);
            }
        }
    }
}
