package com.minecraft.huntergame.listener;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.game.ManhuntGame;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * 传送门监听器
 * 处理游戏中的传送门重定向（地狱门、末地门）
 * 
 * @author YourName
 * @version 1.0.0
 */
public class PortalListener implements Listener {
    
    private final HunterGame plugin;
    
    public PortalListener(HunterGame plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 监听传送门事件
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerPortal(PlayerPortalEvent event) {
        Player player = event.getPlayer();
        
        // 检查玩家是否在游戏中
        ManhuntGame game = plugin.getManhuntManager().getPlayerGame(player);
        if (game == null) {
            return;
        }
        
        // 检查是否是下界门或末地门
        PlayerTeleportEvent.TeleportCause cause = event.getCause();
        if (cause != PlayerTeleportEvent.TeleportCause.NETHER_PORTAL &&
            cause != PlayerTeleportEvent.TeleportCause.END_PORTAL) {
            return;
        }
        
        // 获取游戏世界名称
        String gameWorldName = game.getWorldName();
        Location from = event.getFrom();
        
        plugin.getLogger().info("玩家 " + player.getName() + " 使用传送门: " + cause + 
                               " (从世界: " + from.getWorld().getName() + ")");
        
        // 处理下界门
        if (cause == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            handleNetherPortal(event, player, game, gameWorldName, from);
        }
        // 处理末地门
        else if (cause == PlayerTeleportEvent.TeleportCause.END_PORTAL) {
            handleEndPortal(event, player, game, gameWorldName, from);
        }
    }
    
    /**
     * 处理下界门传送
     */
    private void handleNetherPortal(PlayerPortalEvent event, Player player, 
                                    ManhuntGame game, String gameWorldName, Location from) {
        World fromWorld = from.getWorld();
        
        try {
            // 从主世界进入地狱
            if (fromWorld.getEnvironment() == World.Environment.NORMAL) {
                // 获取地狱世界名称
                String netherWorldName;
                if (plugin.getManhuntConfig().useMultiverse() && 
                    plugin.getIntegrationManager().isMultiverseEnabled()) {
                    netherWorldName = plugin.getIntegrationManager()
                        .getMultiverseIntegration().getNetherWorldName(gameWorldName);
                } else {
                    netherWorldName = plugin.getWorldManager().getNetherWorldName(gameWorldName);
                }
                
                World netherWorld = plugin.getServer().getWorld(netherWorldName);
                
                if (netherWorld == null) {
                    plugin.getLogger().warning("地狱世界未加载: " + netherWorldName);
                    player.sendMessage("§c地狱世界未加载！");
                    event.setCancelled(true);
                    return;
                }
                
                // 计算地狱坐标（主世界坐标 / 8）
                double netherX = from.getX() / 8.0;
                double netherY = Math.max(1, Math.min(126, from.getY())); // 限制Y坐标在有效范围内
                double netherZ = from.getZ() / 8.0;
                
                Location netherLoc = new Location(
                    netherWorld,
                    netherX,
                    netherY,
                    netherZ,
                    from.getYaw(),
                    from.getPitch()
                );
                
                // 确保目标位置安全
                netherLoc = findSafeLocation(netherLoc);
                
                event.setTo(netherLoc);
                plugin.getLogger().info("玩家 " + player.getName() + " 通过传送门进入游戏地狱 " + 
                                       String.format("(%.1f, %.1f, %.1f)", netherX, netherY, netherZ));
            }
            // 从地狱返回主世界
            else if (fromWorld.getEnvironment() == World.Environment.NETHER) {
                World mainWorld = plugin.getServer().getWorld(gameWorldName);
                
                if (mainWorld == null) {
                    plugin.getLogger().warning("主世界未加载: " + gameWorldName);
                    player.sendMessage("§c主世界未加载！");
                    event.setCancelled(true);
                    return;
                }
                
                // 计算主世界坐标（地狱坐标 * 8）
                double mainX = from.getX() * 8.0;
                double mainY = Math.max(1, Math.min(255, from.getY())); // 限制Y坐标在有效范围内
                double mainZ = from.getZ() * 8.0;
                
                Location mainLoc = new Location(
                    mainWorld,
                    mainX,
                    mainY,
                    mainZ,
                    from.getYaw(),
                    from.getPitch()
                );
                
                // 确保目标位置安全
                mainLoc = findSafeLocation(mainLoc);
                
                event.setTo(mainLoc);
                plugin.getLogger().info("玩家 " + player.getName() + " 从游戏地狱返回主世界 " + 
                                       String.format("(%.1f, %.1f, %.1f)", mainX, mainY, mainZ));
            }
        } catch (Exception e) {
            plugin.getLogger().severe("处理下界门传送时发生异常: " + e.getMessage());
            e.printStackTrace();
            player.sendMessage("§c传送失败，请重试！");
            event.setCancelled(true);
        }
    }
    
    /**
     * 处理末地门传送
     */
    private void handleEndPortal(PlayerPortalEvent event, Player player, 
                                 ManhuntGame game, String gameWorldName, Location from) {
        World fromWorld = from.getWorld();
        
        try {
            // 从主世界进入末地
            if (fromWorld.getEnvironment() == World.Environment.NORMAL) {
                // 获取末地世界名称
                String endWorldName;
                if (plugin.getManhuntConfig().useMultiverse() && 
                    plugin.getIntegrationManager().isMultiverseEnabled()) {
                    endWorldName = plugin.getIntegrationManager()
                        .getMultiverseIntegration().getEndWorldName(gameWorldName);
                } else {
                    endWorldName = plugin.getWorldManager().getEndWorldName(gameWorldName);
                }
                
                World endWorld = plugin.getServer().getWorld(endWorldName);
                
                if (endWorld == null) {
                    plugin.getLogger().warning("末地世界未加载: " + endWorldName);
                    player.sendMessage("§c末地世界未加载！");
                    event.setCancelled(true);
                    return;
                }
                
                // 传送到末地出生点（通常是 100, 50, 0）
                Location endLoc = new Location(endWorld, 100.5, 50, 0.5, 0, 0);
                
                // 确保目标位置安全
                endLoc = findSafeLocation(endLoc);
                
                event.setTo(endLoc);
                plugin.getLogger().info("玩家 " + player.getName() + " 通过传送门进入游戏末地");
            }
            // 从末地返回主世界
            else if (fromWorld.getEnvironment() == World.Environment.THE_END) {
                World mainWorld = plugin.getServer().getWorld(gameWorldName);
                
                if (mainWorld == null) {
                    plugin.getLogger().warning("主世界未加载: " + gameWorldName);
                    player.sendMessage("§c主世界未加载！");
                    event.setCancelled(true);
                    return;
                }
                
                // 返回主世界出生点
                Location mainLoc = mainWorld.getSpawnLocation();
                
                // 确保目标位置安全
                mainLoc = findSafeLocation(mainLoc);
                
                event.setTo(mainLoc);
                plugin.getLogger().info("玩家 " + player.getName() + " 从游戏末地返回主世界");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("处理末地门传送时发生异常: " + e.getMessage());
            e.printStackTrace();
            player.sendMessage("§c传送失败，请重试！");
            event.setCancelled(true);
        }
    }
    
    /**
     * 查找安全的传送位置
     * 确保玩家不会传送到岩浆、虚空或其他危险位置
     */
    private Location findSafeLocation(Location location) {
        World world = location.getWorld();
        if (world == null) {
            return location;
        }
        
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        
        // 确保Y坐标在有效范围内
        if (world.getEnvironment() == World.Environment.NETHER) {
            y = Math.max(1, Math.min(126, y));
        } else {
            y = Math.max(1, Math.min(255, y));
        }
        
        // 向上查找安全位置（最多查找10格）
        for (int i = 0; i < 10; i++) {
            Location checkLoc = new Location(world, x + 0.5, y + i, z + 0.5);
            
            // 检查脚下是否有方块，头顶是否有空间
            if (world.getBlockAt(x, y + i - 1, z).getType().isSolid() &&
                !world.getBlockAt(x, y + i, z).getType().isSolid() &&
                !world.getBlockAt(x, y + i + 1, z).getType().isSolid()) {
                
                checkLoc.setYaw(location.getYaw());
                checkLoc.setPitch(location.getPitch());
                return checkLoc;
            }
        }
        
        // 如果找不到安全位置，返回原位置
        return new Location(world, x + 0.5, y, z + 0.5, location.getYaw(), location.getPitch());
    }
}
