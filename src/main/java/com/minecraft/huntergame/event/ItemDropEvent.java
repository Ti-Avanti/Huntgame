package com.minecraft.huntergame.event;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.arena.Arena;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 道具掉落事件
 * 在随机位置生成特殊道具
 * 
 * @author YourName
 * @version 1.0.0
 */
public class ItemDropEvent extends GameEvent {
    
    private final List<Material> specialItems;
    
    public ItemDropEvent(HunterGame plugin, Arena arena) {
        super(plugin, arena, "道具掉落", 0.2); // 20%触发概率
        
        // 初始化特殊道具列表
        this.specialItems = new ArrayList<>();
        specialItems.add(Material.GOLDEN_APPLE);
        specialItems.add(Material.ENDER_PEARL);
        specialItems.add(Material.DIAMOND_SWORD);
        specialItems.add(Material.DIAMOND_HELMET);
        specialItems.add(Material.DIAMOND_CHESTPLATE);
        specialItems.add(Material.DIAMOND_LEGGINGS);
        specialItems.add(Material.DIAMOND_BOOTS);
        specialItems.add(Material.BOW);
        specialItems.add(Material.ARROW);
    }
    
    @Override
    protected void execute() {
        World world = arena.getWorld();
        if (world == null) {
            return;
        }
        
        // 获取所有存活的玩家
        List<UUID> alivePlayers = new ArrayList<>();
        alivePlayers.addAll(arena.getHunters());
        alivePlayers.addAll(arena.getAliveSurvivors());
        
        if (alivePlayers.isEmpty()) {
            return;
        }
        
        // 随机选择一个玩家的位置附近
        UUID targetUuid = alivePlayers.get(random.nextInt(alivePlayers.size()));
        Player targetPlayer = plugin.getServer().getPlayer(targetUuid);
        
        if (targetPlayer == null || !targetPlayer.isOnline()) {
            return;
        }
        
        // 在玩家附近随机位置掉落道具
        Location playerLoc = targetPlayer.getLocation();
        Location dropLoc = playerLoc.clone().add(
            (random.nextDouble() - 0.5) * 20, // X偏移 -10到10
            5, // Y偏移（从上方掉落）
            (random.nextDouble() - 0.5) * 20  // Z偏移 -10到10
        );
        
        // 确保掉落位置在世界高度范围内
        if (dropLoc.getY() > world.getMaxHeight()) {
            dropLoc.setY(world.getMaxHeight() - 1);
        }
        
        // 随机选择道具
        Material material = specialItems.get(random.nextInt(specialItems.size()));
        int amount = 1;
        
        if (material == Material.ARROW) {
            amount = 16 + random.nextInt(17); // 16-32支箭
        }
        
        // 掉落道具
        world.dropItem(dropLoc, new ItemStack(material, amount));
        
        // 广播消息
        broadcastMessage(plugin.getLanguageManager().getMessage("event.item-drop", 
            getItemName(material)));
    }
    
    /**
     * 获取道具名称
     */
    private String getItemName(Material material) {
        switch (material) {
            case GOLDEN_APPLE:
                return "金苹果";
            case ENDER_PEARL:
                return "末影珍珠";
            case DIAMOND_SWORD:
                return "钻石剑";
            case DIAMOND_HELMET:
                return "钻石头盔";
            case DIAMOND_CHESTPLATE:
                return "钻石胸甲";
            case DIAMOND_LEGGINGS:
                return "钻石护腿";
            case DIAMOND_BOOTS:
                return "钻石靴子";
            case BOW:
                return "弓";
            case ARROW:
                return "箭";
            default:
                return material.name();
        }
    }
}
