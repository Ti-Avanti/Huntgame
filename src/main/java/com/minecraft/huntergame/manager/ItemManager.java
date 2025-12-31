package com.minecraft.huntergame.manager;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.item.*;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 道具管理器
 * 负责管理所有逃生者道具
 * 
 * @author YourName
 * @version 1.0.0
 */
public class ItemManager {
    
    private final HunterGame plugin;
    
    // 道具实例映射
    private final Map<String, SurvivorItem> items;
    
    public ItemManager(HunterGame plugin) {
        this.plugin = plugin;
        this.items = new HashMap<>();
        
        // 初始化所有道具
        initItems();
    }
    
    /**
     * 初始化所有道具
     */
    private void initItems() {
        // 注册隐身药水
        registerItem(new InvisibilityPotion(plugin));
        
        // 注册速度药水
        registerItem(new SpeedPotion(plugin));
        
        // 注册烟雾弹
        registerItem(new SmokeBomb(plugin));
        
        // 注册诱饵
        registerItem(new DecoyItem(plugin));
        
        plugin.getLogger().info("道具管理器已初始化，共注册 " + items.size() + " 个道具");
    }
    
    /**
     * 注册道具
     */
    private void registerItem(SurvivorItem item) {
        items.put(item.getItemName(), item);
    }
    
    /**
     * 获取道具
     */
    public SurvivorItem getItem(String itemName) {
        return items.get(itemName);
    }
    
    /**
     * 检查道具是否存在
     */
    public boolean hasItem(String itemName) {
        return items.containsKey(itemName);
    }
    
    /**
     * 使用道具
     * 
     * @param player 玩家
     * @param itemName 道具名称
     * @return 是否成功使用
     */
    public boolean useItem(Player player, String itemName) {
        SurvivorItem item = getItem(itemName);
        
        if (item == null) {
            plugin.getLogger().warning("尝试使用不存在的道具: " + itemName);
            return false;
        }
        
        return item.use(player);
    }
    
    /**
     * 给予玩家道具
     * 
     * @param player 玩家
     * @param itemName 道具名称
     * @param amount 数量
     */
    public void giveItem(Player player, String itemName, int amount) {
        SurvivorItem item = getItem(itemName);
        
        if (item == null) {
            plugin.getLogger().warning("尝试给予不存在的道具: " + itemName);
            return;
        }
        
        item.giveItem(player, amount);
    }
    
    /**
     * 给予玩家初始道具
     * 根据配置给予逃生者初始道具
     * 
     * @param player 玩家
     */
    public void giveStartingItems(Player player) {
        // 隐身药水
        int invisibilityCount = plugin.getMainConfig().getInvisibilityPotionCount();
        if (invisibilityCount > 0) {
            giveItem(player, "invisibility-potion", invisibilityCount);
        }
        
        // 速度药水
        int speedCount = plugin.getMainConfig().getSpeedPotionCount();
        if (speedCount > 0) {
            giveItem(player, "speed-potion", speedCount);
        }
        
        // 烟雾弹
        int smokeBombCount = plugin.getMainConfig().getSmokeBombCount();
        if (smokeBombCount > 0) {
            giveItem(player, "smoke-bomb", smokeBombCount);
        }
        
        // 诱饵
        int decoyCount = plugin.getMainConfig().getDecoyCount();
        if (decoyCount > 0) {
            giveItem(player, "decoy", decoyCount);
        }
        
        plugin.getLogger().info("[道具] 已给予玩家 " + player.getName() + " 初始道具");
    }
    
    /**
     * 给予玩家初始道具（别名方法）
     * 
     * @param player 玩家
     */
    public void giveInitialItems(Player player) {
        giveStartingItems(player);
    }
    
    /**
     * 获取玩家道具剩余使用次数
     * 
     * @param player 玩家
     * @param itemName 道具名称
     * @return 剩余使用次数
     */
    public int getRemainingUses(Player player, String itemName) {
        SurvivorItem item = getItem(itemName);
        
        if (item == null) {
            return 0;
        }
        
        return item.getRemainingUses(player.getUniqueId());
    }
    
    /**
     * 设置玩家道具使用次数
     * 
     * @param player 玩家
     * @param itemName 道具名称
     * @param uses 使用次数
     */
    public void setUses(Player player, String itemName, int uses) {
        SurvivorItem item = getItem(itemName);
        
        if (item == null) {
            return;
        }
        
        item.setUses(player.getUniqueId(), uses);
    }
    
    /**
     * 清除玩家道具数据
     * 
     * @param uuid 玩家UUID
     */
    public void clearPlayer(UUID uuid) {
        for (SurvivorItem item : items.values()) {
            item.clearPlayer(uuid);
        }
    }
    
    /**
     * 清除所有玩家道具数据
     */
    public void clearAll() {
        for (SurvivorItem item : items.values()) {
            item.clearAll();
        }
    }
    
    /**
     * 获取所有道具名称
     */
    public java.util.Set<String> getItemNames() {
        return items.keySet();
    }
    
    /**
     * 获取道具数量
     */
    public int getItemCount() {
        return items.size();
    }
}
