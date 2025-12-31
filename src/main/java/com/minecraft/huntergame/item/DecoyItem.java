package com.minecraft.huntergame.item;

import com.minecraft.huntergame.HunterGame;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * 诱饵道具
 * 生成假玩家实体吸引猎人
 * 
 * @author YourName
 * @version 1.0.0
 */
public class DecoyItem extends SurvivorItem {
    
    private final int duration; // 持续时间(秒)
    
    public DecoyItem(HunterGame plugin) {
        super(plugin, "decoy");
        this.material = Material.ARMOR_STAND;
        this.duration = plugin.getMainConfig().getDecoyDuration();
        this.maxUses = plugin.getMainConfig().getDecoyCount();
        this.consumable = true;
    }
    
    @Override
    protected boolean onUse(Player player) {
        Location location = player.getLocation();
        
        if (location.getWorld() == null) {
            return false;
        }
        
        // 生成盔甲架作为诱饵
        ArmorStand decoy = (ArmorStand) location.getWorld().spawnEntity(
            location,
            EntityType.ARMOR_STAND
        );
        
        // 配置盔甲架
        setupDecoy(decoy, player);
        
        // 启动诱饵移动任务
        startDecoyMovement(decoy);
        
        // 设置自动移除
        new BukkitRunnable() {
            @Override
            public void run() {
                if (decoy.isValid()) {
                    decoy.remove();
                }
            }
        }.runTaskLater(plugin, duration * 20L);
        
        // 记录日志
        plugin.getLogger().info("[道具] 玩家 " + player.getName() + " 使用了诱饵");
        
        return true;
    }
    
    /**
     * 配置诱饵盔甲架
     */
    private void setupDecoy(ArmorStand decoy, Player player) {
        // 设置名称
        decoy.setCustomName("§e" + player.getName() + " §7(诱饵)");
        decoy.setCustomNameVisible(true);
        
        // 设置外观
        decoy.setVisible(true);
        decoy.setGravity(true);
        decoy.setBasePlate(false);
        decoy.setArms(true);
        
        // 复制玩家装备
        decoy.setHelmet(player.getInventory().getHelmet());
        decoy.setChestplate(player.getInventory().getChestplate());
        decoy.setLeggings(player.getInventory().getLeggings());
        decoy.setBoots(player.getInventory().getBoots());
        
        // 手持物品
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand != null && mainHand.getType() != Material.AIR) {
            decoy.setItemInHand(mainHand.clone());
        }
        
        // 设置为无敌（不会被攻击破坏）
        decoy.setInvulnerable(true);
    }
    
    /**
     * 启动诱饵移动任务
     * 让诱饵随机移动，更像真实玩家
     */
    private void startDecoyMovement(ArmorStand decoy) {
        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = duration * 20;
            
            @Override
            public void run() {
                if (!decoy.isValid() || ticks >= maxTicks) {
                    this.cancel();
                    return;
                }
                
                // 每2秒随机移动一次
                if (ticks % 40 == 0) {
                    Location current = decoy.getLocation();
                    
                    // 随机移动方向
                    double offsetX = (Math.random() - 0.5) * 2;
                    double offsetZ = (Math.random() - 0.5) * 2;
                    
                    Location newLocation = current.clone().add(offsetX, 0, offsetZ);
                    
                    // 确保新位置是安全的
                    if (newLocation.getBlock().getType() == Material.AIR) {
                        decoy.teleport(newLocation);
                    }
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    /**
     * 获取持续时间
     */
    public int getDuration() {
        return duration;
    }
}
