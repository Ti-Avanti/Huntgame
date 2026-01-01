package com.minecraft.huntergame.gui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * GUI工具类
 * 提供创建GUI物品的便捷方法
 * 
 * @author YourName
 * @version 1.0.0
 */
public class GUIUtils {
    
    /**
     * 创建物品
     */
    public static ItemStack createItem(Material material, String name) {
        return createItem(material, 1, name, null);
    }
    
    /**
     * 创建物品（带数量）
     */
    public static ItemStack createItem(Material material, int amount, String name) {
        return createItem(material, amount, name, null);
    }
    
    /**
     * 创建物品（带描述）
     */
    public static ItemStack createItem(Material material, String name, String... lore) {
        return createItem(material, 1, name, lore != null ? Arrays.asList(lore) : null);
    }
    
    /**
     * 创建物品（完整版本）
     */
    public static ItemStack createItem(Material material, int amount, String name, List<String> lore) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            if (name != null) {
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            }
            
            if (lore != null && !lore.isEmpty()) {
                List<String> coloredLore = new ArrayList<>();
                for (String line : lore) {
                    coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
                }
                meta.setLore(coloredLore);
            }
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * 创建物品（带List描述）
     */
    public static ItemStack createItem(Material material, String name, List<String> lore) {
        return createItem(material, 1, name, lore);
    }
    
    /**
     * 创建发光物品
     */
    public static ItemStack createGlowingItem(Material material, String name, String... lore) {
        ItemStack item = createItem(material, name, lore);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            // 使用UNBREAKING代替已过时的DURABILITY
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * 创建玩家头颅
     */
    public static ItemStack createPlayerHead(String playerName, String displayName, String... lore) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        
        if (meta != null) {
            meta.setOwner(playerName);
            
            if (displayName != null) {
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
            }
            
            if (lore != null && lore.length > 0) {
                List<String> coloredLore = new ArrayList<>();
                for (String line : lore) {
                    coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
                }
                meta.setLore(coloredLore);
            }
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * 创建边框物品
     */
    public static ItemStack createBorderItem() {
        return createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
    }
    
    /**
     * 创建返回按钮
     */
    public static ItemStack createBackButton() {
        return createItem(
            Material.ARROW,
            "&c返回",
            "&7点击返回上一页"
        );
    }
    
    /**
     * 创建关闭按钮
     */
    public static ItemStack createCloseButton() {
        return createItem(
            Material.BARRIER,
            "&c关闭",
            "&7点击关闭界面"
        );
    }
    
    /**
     * 创建确认按钮
     */
    public static ItemStack createConfirmButton() {
        return createGlowingItem(
            Material.LIME_WOOL,
            "&a确认",
            "&7点击确认操作"
        );
    }
    
    /**
     * 创建取消按钮
     */
    public static ItemStack createCancelButton() {
        return createItem(
            Material.RED_WOOL,
            "&c取消",
            "&7点击取消操作"
        );
    }
    
    /**
     * 创建信息物品
     */
    public static ItemStack createInfoItem(Material material, String title, String... info) {
        return createItem(material, title, info);
    }
    
    /**
     * 创建空物品（占位符）
     */
    public static ItemStack createPlaceholder() {
        return createItem(Material.AIR, "");
    }
    
    /**
     * 添加描述行
     */
    public static List<String> addLore(List<String> lore, String... lines) {
        if (lore == null) {
            lore = new ArrayList<>();
        }
        lore.addAll(Arrays.asList(lines));
        return lore;
    }
    
    /**
     * 创建分隔线
     */
    public static String createSeparator() {
        return "&7&m                    ";
    }
    
    /**
     * 格式化数字
     */
    public static String formatNumber(int number) {
        if (number >= 1000000) {
            return String.format("%.1fM", number / 1000000.0);
        } else if (number >= 1000) {
            return String.format("%.1fK", number / 1000.0);
        }
        return String.valueOf(number);
    }
    
    /**
     * 格式化时间（秒）
     */
    public static String formatTime(int seconds) {
        if (seconds < 60) {
            return seconds + "秒";
        } else if (seconds < 3600) {
            int minutes = seconds / 60;
            int secs = seconds % 60;
            return minutes + "分" + (secs > 0 ? secs + "秒" : "");
        } else {
            int hours = seconds / 3600;
            int minutes = (seconds % 3600) / 60;
            return hours + "小时" + (minutes > 0 ? minutes + "分" : "");
        }
    }
    
    /**
     * 获取状态颜色
     */
    public static String getStateColor(String state) {
        switch (state.toUpperCase()) {
            case "WAITING":
                return "&e";
            case "PREPARING":
                return "&6";
            case "PLAYING":
                return "&a";
            case "ENDING":
                return "&c";
            default:
                return "&7";
        }
    }
    
    /**
     * 获取角色颜色
     */
    public static String getRoleColor(String role) {
        switch (role.toUpperCase()) {
            case "RUNNER":
                return "&b";
            case "HUNTER":
                return "&c";
            case "SPECTATOR":
                return "&7";
            default:
                return "&f";
        }
    }
}
