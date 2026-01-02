package com.minecraft.huntergame.gui;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.game.GameState;
import com.minecraft.huntergame.game.ManhuntGame;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * 观战游戏列表GUI
 * 显示所有进行中的游戏，允许玩家选择观战
 * 
 * @author YourName
 * @version 1.0.0
 */
public class SpectateGameGUI {
    
    private final HunterGame plugin;
    private final Player player;
    private Inventory inventory;
    
    public SpectateGameGUI(HunterGame plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }
    
    /**
     * 打开GUI
     */
    public void open() {
        // 获取所有进行中的游戏
        List<ManhuntGame> playingGames = new ArrayList<>();
        for (ManhuntGame game : plugin.getManhuntManager().getAllGames()) {
            if (game.getState() == GameState.PLAYING) {
                playingGames.add(game);
            }
        }
        
        // 如果没有进行中的游戏
        if (playingGames.isEmpty()) {
            player.sendMessage("§c当前没有正在进行的游戏！");
            return;
        }
        
        // 创建GUI
        int size = Math.min(54, ((playingGames.size() + 8) / 9) * 9);
        inventory = Bukkit.createInventory(null, size, "§6§l观战游戏");
        
        // 添加游戏项
        for (int i = 0; i < playingGames.size() && i < 45; i++) {
            ManhuntGame game = playingGames.get(i);
            ItemStack item = createGameItem(game);
            inventory.setItem(i, item);
        }
        
        // 添加返回按钮
        ItemStack backButton = createItem(
            Material.BARRIER,
            "§c§l返回",
            "§7点击返回大厅"
        );
        inventory.setItem(size - 1, backButton);
        
        // 注册GUI到监听器
        plugin.getSpectateGameListener().registerGUI(player, this);
        
        // 打开GUI
        player.openInventory(inventory);
    }
    
    /**
     * 创建游戏项
     */
    private ItemStack createGameItem(ManhuntGame game) {
        Material material = Material.DIAMOND_SWORD;
        
        List<String> lore = new ArrayList<>();
        lore.add("§7游戏ID: §e" + game.getGameId());
        lore.add("§7状态: §a进行中");
        lore.add("§7逃亡者: §b" + game.getRunners().size());
        lore.add("§7猎人: §c" + game.getHunters().size());
        lore.add("§7观战者: §7" + game.getSpectators().size());
        lore.add("");
        lore.add("§e点击观战此游戏");
        
        return createItem(material, "§6§l游戏 #" + game.getGameId(), lore.toArray(new String[0]));
    }
    
    /**
     * 创建物品
     */
    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore.length > 0) {
                List<String> loreList = new ArrayList<>();
                for (String line : lore) {
                    loreList.add(line);
                }
                meta.setLore(loreList);
            }
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * 处理点击
     */
    public void handleClick(int slot, ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return;
        }
        
        String name = meta.getDisplayName();
        
        // 返回按钮
        if (name.contains("返回")) {
            player.closeInventory();
            return;
        }
        
        // 游戏项
        if (name.contains("游戏 #")) {
            // 提取游戏ID
            String gameId = name.replace("§6§l游戏 #", "");
            
            // 观战游戏
            boolean success = plugin.getManhuntManager().spectateGame(player, gameId);
            
            if (success) {
                player.closeInventory();
                player.sendMessage("§a你现在正在观战游戏 #" + gameId);
            } else {
                player.sendMessage("§c无法观战此游戏！");
            }
        }
    }
    
    /**
     * 获取Inventory
     */
    public Inventory getInventory() {
        return inventory;
    }
}
