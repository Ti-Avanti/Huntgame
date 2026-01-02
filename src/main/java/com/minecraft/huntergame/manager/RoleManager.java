package com.minecraft.huntergame.manager;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.game.ManhuntGame;
import com.minecraft.huntergame.game.PlayerRole;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * 角色管理器
 * 负责Manhunt游戏的角色分配
 * 
 * @author YourName
 * @version 1.0.0
 */
public class RoleManager {
    
    private final HunterGame plugin;
    private final Random random;
    
    public RoleManager(HunterGame plugin) {
        this.plugin = plugin;
        this.random = new Random();
        
        plugin.getLogger().info("角色管理器已初始化");
    }
    
    /**
     * 为游戏分配角色
     * 
     * @param game Manhunt游戏
     * @param players 参与游戏的玩家列表
     */
    public void assignRoles(ManhuntGame game, List<UUID> players) {
        if (players.isEmpty()) {
            plugin.getLogger().warning("无法分配角色：玩家列表为空");
            return;
        }
        
        int totalPlayers = players.size();
        int maxRunners = game.getMaxRunners();
        int maxHunters = game.getMaxHunters();
        
        // 计算实际的逃亡者和猎人数量
        int runnerCount = calculateRunnerCount(totalPlayers, maxRunners);
        int hunterCount = totalPlayers - runnerCount;
        
        // 验证数量
        if (hunterCount < 1) {
            plugin.getLogger().warning("猎人数量不足，调整为1");
            hunterCount = 1;
            runnerCount = totalPlayers - 1;
        }
        
        if (runnerCount < 1) {
            plugin.getLogger().warning("逃亡者数量不足，调整为1");
            runnerCount = 1;
            hunterCount = totalPlayers - 1;
        }
        
        plugin.getLogger().info("角色分配: " + runnerCount + " 逃亡者, " + hunterCount + " 猎人");
        
        // 随机打乱玩家列表
        List<UUID> shuffled = new ArrayList<>(players);
        Collections.shuffle(shuffled, random);
        
        // 分配逃亡者
        for (int i = 0; i < runnerCount; i++) {
            UUID uuid = shuffled.get(i);
            game.setPlayerRole(uuid, PlayerRole.RUNNER);
            
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                plugin.getLogger().info("玩家 " + player.getName() + " 被分配为逃亡者");
            }
        }
        
        // 分配猎人
        for (int i = runnerCount; i < totalPlayers; i++) {
            UUID uuid = shuffled.get(i);
            game.setPlayerRole(uuid, PlayerRole.HUNTER);
            
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                plugin.getLogger().info("玩家 " + player.getName() + " 被分配为猎人");
            }
        }
    }
    
    /**
     * 计算逃亡者数量
     * 
     * @param totalPlayers 总玩家数
     * @param maxRunners 最大逃亡者数
     * @return 实际逃亡者数量
     */
    private int calculateRunnerCount(int totalPlayers, int maxRunners) {
        // 至少1个逃亡者
        if (totalPlayers <= 2) {
            return 1;
        }
        
        // 使用配置的比例计算逃亡者数量
        double runnerRatio = plugin.getManhuntConfig().getRunnerRatio();
        int calculated = (int) Math.ceil(totalPlayers * runnerRatio);
        
        // 限制在1到maxRunners之间
        calculated = Math.max(1, Math.min(calculated, maxRunners));
        
        // 确保至少有1个猎人
        if (calculated >= totalPlayers) {
            calculated = totalPlayers - 1;
        }
        
        return calculated;
    }
    
    /**
     * 通知玩家角色
     * 
     * @param game Manhunt游戏
     */
    public void notifyRoles(ManhuntGame game) {
        for (UUID uuid : game.getAllPlayers()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) {
                continue;
            }
            
            PlayerRole role = game.getPlayerRole(uuid);
            if (role == null) {
                continue;
            }
            
            // 发送角色通知
            switch (role) {
                case RUNNER:
                    // 使用新的Title API（兼容1.11+）
                    try {
                        // 尝试使用新API（1.11+）
                        player.sendTitle(
                            "§a§l你是逃亡者",
                            "§7击败末影龙获胜",
                            10, 60, 10
                        );
                    } catch (NoSuchMethodError e) {
                        // 降级到旧API
                        player.sendMessage("§a§l========== 你的角色 ==========");
                        player.sendMessage("§a§l你是逃亡者");
                    }
                    player.sendMessage("§a§l========== 你的角色 ==========");
                    player.sendMessage("§a你是 §l逃亡者");
                    player.sendMessage("§7目标: 击败末影龙");
                    player.sendMessage("§7你有 §e" + game.getRespawnLimit() + " §7次复活机会");
                    player.sendMessage("§7躲避猎人的追杀，前往末地击败末影龙！");
                    player.sendMessage("§a§l==============================");
                    break;
                    
                case HUNTER:
                    // 使用新的Title API（兼容1.11+）
                    try {
                        // 尝试使用新API（1.11+）
                        player.sendTitle(
                            "§c§l你是猎人",
                            "§7击杀所有逃亡者获胜",
                            10, 60, 10
                        );
                    } catch (NoSuchMethodError e) {
                        // 降级到旧API
                        player.sendMessage("§c§l========== 你的角色 ==========");
                        player.sendMessage("§c§l你是猎人");
                    }
                    player.sendMessage("§c§l========== 你的角色 ==========");
                    player.sendMessage("§c你是 §l猎人");
                    player.sendMessage("§7目标: 击杀所有逃亡者");
                    player.sendMessage("§7使用追踪指南针定位逃亡者");
                    player.sendMessage("§7右键点击指南针更新目标位置");
                    player.sendMessage("§c§l==============================");
                    break;
                    
                default:
                    break;
            }
        }
    }
    
    /**
     * 给予初始装备
     * 
     * @param game Manhunt游戏
     */
    public void giveStartingItems(ManhuntGame game) {
        for (UUID uuid : game.getAllPlayers()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) {
                continue;
            }
            
            PlayerRole role = game.getPlayerRole(uuid);
            if (role == null) {
                continue;
            }
            
            // 清空背包
            player.getInventory().clear();
            
            switch (role) {
                case RUNNER:
                    // 逃亡者初始装备
                    giveRunnerItems(player);
                    break;
                    
                case HUNTER:
                    // 猎人初始装备
                    giveHunterItems(player, game);
                    break;
                    
                default:
                    break;
            }
        }
    }
    
    /**
     * 给予逃亡者初始装备
     * 抑制PotionData过时警告（为了向后兼容1.16-1.20.4版本）
     */
    @SuppressWarnings("deprecation")
    private void giveRunnerItems(Player player) {
        // 检查是否启用逃亡者初始道具
        if (!plugin.getManhuntConfig().isRunnerStartItemsEnabled()) {
            return;
        }
        
        // 基础道具
        int bread = plugin.getManhuntConfig().getRunnerStartItem("bread");
        if (bread > 0) {
            player.getInventory().addItem(new org.bukkit.inventory.ItemStack(org.bukkit.Material.BREAD, bread));
        }
        
        int wood = plugin.getManhuntConfig().getRunnerStartItem("wood");
        if (wood > 0) {
            player.getInventory().addItem(new org.bukkit.inventory.ItemStack(org.bukkit.Material.OAK_LOG, wood));
        }
        
        int cobblestone = plugin.getManhuntConfig().getRunnerStartItem("cobblestone");
        if (cobblestone > 0) {
            player.getInventory().addItem(new org.bukkit.inventory.ItemStack(org.bukkit.Material.COBBLESTONE, cobblestone));
        }
        
        // 特殊道具 - 隐身药水
        int invisibilityPotion = plugin.getManhuntConfig().getRunnerStartItem("invisibility-potion");
        if (invisibilityPotion > 0) {
            org.bukkit.inventory.ItemStack potion = new org.bukkit.inventory.ItemStack(
                org.bukkit.Material.POTION, invisibilityPotion);
            org.bukkit.inventory.meta.PotionMeta meta = 
                (org.bukkit.inventory.meta.PotionMeta) potion.getItemMeta();
            if (meta != null) {
                // 使用新API设置药水类型（兼容1.20.5+）
                try {
                    meta.setBasePotionType(org.bukkit.potion.PotionType.INVISIBILITY);
                } catch (NoSuchMethodError e) {
                    // 降级到旧API（1.16-1.20.4）
                    meta.setBasePotionData(new org.bukkit.potion.PotionData(
                        org.bukkit.potion.PotionType.INVISIBILITY, false, false));
                }
                meta.setDisplayName(org.bukkit.ChatColor.GRAY + "隐身药水");
                potion.setItemMeta(meta);
            }
            player.getInventory().addItem(potion);
        }
        
        // 特殊道具 - 速度药水
        int speedPotion = plugin.getManhuntConfig().getRunnerStartItem("speed-potion");
        if (speedPotion > 0) {
            org.bukkit.inventory.ItemStack potion = new org.bukkit.inventory.ItemStack(
                org.bukkit.Material.POTION, speedPotion);
            org.bukkit.inventory.meta.PotionMeta meta = 
                (org.bukkit.inventory.meta.PotionMeta) potion.getItemMeta();
            if (meta != null) {
                // 使用新API设置药水类型（兼容1.20.5+）
                try {
                    meta.setBasePotionType(org.bukkit.potion.PotionType.SWIFTNESS);
                } catch (NoSuchMethodError e) {
                    // 降级到旧API（1.16-1.20.4）
                    meta.setBasePotionData(new org.bukkit.potion.PotionData(
                        org.bukkit.potion.PotionType.SWIFTNESS, false, true));
                }
                meta.setDisplayName(org.bukkit.ChatColor.AQUA + "速度药水");
                potion.setItemMeta(meta);
            }
            player.getInventory().addItem(potion);
        }
        
        // 特殊道具 - 末影珍珠
        int enderPearl = plugin.getManhuntConfig().getRunnerStartItem("ender-pearl");
        if (enderPearl > 0) {
            org.bukkit.inventory.ItemStack pearl = new org.bukkit.inventory.ItemStack(
                org.bukkit.Material.ENDER_PEARL, enderPearl);
            org.bukkit.inventory.meta.ItemMeta meta = pearl.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(org.bukkit.ChatColor.LIGHT_PURPLE + "传送珍珠");
                pearl.setItemMeta(meta);
            }
            player.getInventory().addItem(pearl);
        }
        
        plugin.getLogger().info("给予玩家 " + player.getName() + " 逃亡者初始装备");
    }
    
    /**
     * 给予猎人初始装备
     */
    private void giveHunterItems(Player player, ManhuntGame game) {
        // 给予追踪指南针
        plugin.getTrackerManager().giveTrackerCompass(player, game);
        
        // 应用猎人能力增强
        applyHunterAbilities(player);
        
        plugin.getLogger().info("给予玩家 " + player.getName() + " 猎人初始装备（追踪指南针）");
    }
    
    /**
     * 应用猎人能力增强
     */
    private void applyHunterAbilities(Player player) {
        // 速度提升
        if (plugin.getManhuntConfig().isHunterSpeedBoost()) {
            int level = plugin.getManhuntConfig().getHunterSpeedLevel();
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.SPEED,
                Integer.MAX_VALUE,
                level - 1,
                false,
                false
            ));
        }
        
        // 力量提升
        if (plugin.getManhuntConfig().isHunterStrengthBoost()) {
            int level = plugin.getManhuntConfig().getHunterStrengthLevel();
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.STRENGTH,
                Integer.MAX_VALUE,
                level - 1,
                false,
                false
            ));
        }
        
        // 生命值提升
        if (plugin.getManhuntConfig().isHunterHealthBoost()) {
            int extraHealth = plugin.getManhuntConfig().getHunterExtraHealth();
            player.setMaxHealth(20.0 + extraHealth);
            player.setHealth(player.getMaxHealth());
        }
        
        // 夜视
        if (plugin.getManhuntConfig().isHunterNightVision()) {
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.NIGHT_VISION,
                Integer.MAX_VALUE,
                0,
                false,
                false
            ));
        }
        
        plugin.getLogger().info("应用猎人能力增强: " + player.getName());
    }
    
    /**
     * 关闭管理器
     */
    public void shutdown() {
        plugin.getLogger().info("角色管理器已关闭");
    }
}
