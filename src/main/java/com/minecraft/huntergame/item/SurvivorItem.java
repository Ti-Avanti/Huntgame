package com.minecraft.huntergame.item;

import com.minecraft.huntergame.HunterGame;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 逃生者道具抽象基类
 * 
 * @author YourName
 * @version 1.0.0
 */
public abstract class SurvivorItem {
    
    protected final HunterGame plugin;
    protected final String itemName;
    protected Material material;
    protected int maxUses;
    protected boolean consumable;
    
    // 玩家道具使用次数映射
    protected final Map<UUID, Integer> usesRemaining;
    
    public SurvivorItem(HunterGame plugin, String itemName) {
        this.plugin = plugin;
        this.itemName = itemName;
        this.usesRemaining = new HashMap<>();
        this.consumable = true;
    }
    
    /**
     * 使用道具
     * 
     * @param player 玩家
     * @return 是否成功使用
     */
    public boolean use(Player player) {
        UUID uuid = player.getUniqueId();
        
        // 检查是否还有使用次数
        int remaining = usesRemaining.getOrDefault(uuid, 0);
        if (remaining <= 0) {
            plugin.getLanguageManager().sendMessage(player, "item.no-uses");
            return false;
        }
        
        // 执行道具效果
        if (onUse(player)) {
            // 减少使用次数
            if (consumable) {
                usesRemaining.put(uuid, remaining - 1);
            }
            
            // 发送消息
            String itemDisplayName = plugin.getLanguageManager().getMessage("item." + itemName);
            plugin.getLanguageManager().sendMessage(player, "item.used", itemDisplayName);
            
            return true;
        }
        
        return false;
    }
    
    /**
     * 道具使用时的具体实现
     * 
     * @param player 玩家
     * @return 是否成功
     */
    protected abstract boolean onUse(Player player);
    
    /**
     * 给予玩家道具
     * 
     * @param player 玩家
     * @param amount 数量
     */
    public void giveItem(Player player, int amount) {
        UUID uuid = player.getUniqueId();
        
        // 设置使用次数
        usesRemaining.put(uuid, amount);
        
        // 创建物品
        ItemStack item = createItemStack(amount);
        player.getInventory().addItem(item);
    }
    
    /**
     * 创建物品堆
     */
    protected ItemStack createItemStack(int amount) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            // 设置显示名称
            String displayName = plugin.getLanguageManager().getMessage("item." + itemName);
            meta.setDisplayName(displayName);
            
            // 设置Lore（使用次数）
            meta.setLore(java.util.Arrays.asList(
                "§7使用次数: §e" + amount
            ));
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * 获取剩余使用次数
     */
    public int getRemainingUses(UUID uuid) {
        return usesRemaining.getOrDefault(uuid, 0);
    }
    
    /**
     * 设置使用次数
     */
    public void setUses(UUID uuid, int uses) {
        usesRemaining.put(uuid, uses);
    }
    
    /**
     * 清除玩家数据
     */
    public void clearPlayer(UUID uuid) {
        usesRemaining.remove(uuid);
    }
    
    /**
     * 清除所有玩家数据
     */
    public void clearAll() {
        usesRemaining.clear();
    }
    
    // ==================== Getter 和 Setter ====================
    
    public String getItemName() {
        return itemName;
    }
    
    public Material getMaterial() {
        return material;
    }
    
    public void setMaterial(Material material) {
        this.material = material;
    }
    
    public int getMaxUses() {
        return maxUses;
    }
    
    public void setMaxUses(int maxUses) {
        this.maxUses = maxUses;
    }
    
    public boolean isConsumable() {
        return consumable;
    }
    
    public void setConsumable(boolean consumable) {
        this.consumable = consumable;
    }
}
