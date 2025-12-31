package com.minecraft.huntergame.event;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.arena.Arena;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 箱子刷新事件
 * 重新填充地图中的箱子
 * 
 * @author YourName
 * @version 1.0.0
 */
public class ChestRefillEvent extends GameEvent {
    
    private final List<Material> lootItems;
    
    public ChestRefillEvent(HunterGame plugin, Arena arena) {
        super(plugin, arena, "箱子刷新", 0.3); // 30%触发概率
        
        // 初始化战利品列表
        this.lootItems = new ArrayList<>();
        lootItems.add(Material.BREAD);
        lootItems.add(Material.COOKED_BEEF);
        lootItems.add(Material.GOLDEN_APPLE);
        lootItems.add(Material.ARROW);
        lootItems.add(Material.IRON_SWORD);
        lootItems.add(Material.IRON_HELMET);
        lootItems.add(Material.IRON_CHESTPLATE);
        lootItems.add(Material.IRON_LEGGINGS);
        lootItems.add(Material.IRON_BOOTS);
        lootItems.add(Material.BOW);
        lootItems.add(Material.ENDER_PEARL);
    }
    
    @Override
    protected void execute() {
        World world = arena.getWorld();
        if (world == null) {
            return;
        }
        
        int refillCount = 0;
        
        // 遍历已加载的区块
        for (Chunk chunk : world.getLoadedChunks()) {
            // 遍历区块中的方块实体
            for (org.bukkit.block.BlockState blockState : chunk.getTileEntities()) {
                if (blockState instanceof Chest) {
                    Chest chest = (Chest) blockState;
                    
                    // 随机决定是否刷新这个箱子
                    if (random.nextDouble() < 0.5) { // 50%概率刷新单个箱子
                        refillChest(chest);
                        refillCount++;
                    }
                }
            }
        }
        
        if (refillCount > 0) {
            broadcastMessage(plugin.getLanguageManager().getMessage("event.chest-refill", 
                String.valueOf(refillCount)));
        }
    }
    
    /**
     * 刷新单个箱子
     */
    private void refillChest(Chest chest) {
        Inventory inventory = chest.getInventory();
        
        // 清空箱子
        inventory.clear();
        
        // 随机添加3-6个物品
        int itemCount = 3 + random.nextInt(4);
        
        for (int i = 0; i < itemCount; i++) {
            // 随机选择物品
            Material material = lootItems.get(random.nextInt(lootItems.size()));
            
            // 随机数量
            int amount = 1;
            if (material == Material.ARROW || material == Material.BREAD || material == Material.COOKED_BEEF) {
                amount = 1 + random.nextInt(16);
            }
            
            // 随机槽位
            int slot = random.nextInt(27);
            
            inventory.setItem(slot, new ItemStack(material, amount));
        }
    }
}
