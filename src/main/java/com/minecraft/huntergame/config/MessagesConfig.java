package com.minecraft.huntergame.config;

import com.minecraft.huntergame.HunterGame;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * 消息配置类
 * 
 * @author YourName
 * @version 1.0.0
 */
public class MessagesConfig {
    
    private final HunterGame plugin;
    private FileConfiguration config;
    
    public MessagesConfig(HunterGame plugin) {
        this.plugin = plugin;
        load();
    }
    
    /**
     * 加载配置
     */
    public void load() {
        config = plugin.getConfigManager().getMessagesConfig();
        plugin.getLogger().info("消息配置已加载");
    }
    
    /**
     * 重载配置
     */
    public void reload() {
        // 重新从ConfigManager获取最新的配置引用
        config = plugin.getConfigManager().getMessagesConfig();
        plugin.getLogger().info("消息配置已重载");
    }
    
    /**
     * 获取消息前缀
     */
    public String getPrefix() {
        return ChatColor.translateAlternateColorCodes('&',
            config.getString("prefix", "&6[HunterGame] &r"));
    }
    
    /**
     * 获取消息（带前缀）
     */
    public String getMessage(String path) {
        String message = config.getString(path, path);
        return ChatColor.translateAlternateColorCodes('&', getPrefix() + message);
    }
    
    /**
     * 获取消息（不带前缀）
     */
    public String getMessageRaw(String path) {
        String message = config.getString(path, path);
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    /**
     * 获取消息并替换占位符
     */
    public String getMessage(String path, String... replacements) {
        String message = getMessage(path);
        
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }
        
        return message;
    }
}
