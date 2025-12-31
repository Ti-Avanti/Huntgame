package com.minecraft.huntergame.config;

import com.minecraft.huntergame.HunterGame;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 语言管理类
 * 负责加载和管理多语言消息
 * 
 * @author YourName
 * @version 1.0.0
 */
public class LanguageManager {
    
    private final HunterGame plugin;
    private final Map<String, FileConfiguration> languages;
    private String defaultLanguage;
    
    public LanguageManager(HunterGame plugin) {
        this.plugin = plugin;
        this.languages = new HashMap<>();
        this.defaultLanguage = "zh_CN";
        load();
    }
    
    /**
     * 加载所有语言文件
     */
    public void load() {
        // 创建语言文件夹
        File langFolder = new File(plugin.getDataFolder(), "languages");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }
        
        // 加载默认语言文件
        loadLanguage("zh_CN");
        loadLanguage("en_US");
        
        plugin.getLogger().info("语言文件已加载，默认语言: " + defaultLanguage);
    }
    
    /**
     * 加载指定语言文件
     */
    private void loadLanguage(String language) {
        File langFile = new File(plugin.getDataFolder() + "/languages", language + ".yml");
        
        // 如果文件不存在，从资源中复制
        if (!langFile.exists()) {
            saveResource("languages/" + language + ".yml");
        }
        
        // 加载配置
        FileConfiguration config = YamlConfiguration.loadConfiguration(langFile);
        
        // 加载默认配置（用于补全缺失的键）
        InputStream defStream = plugin.getResource("languages/" + language + ".yml");
        if (defStream != null) {
            FileConfiguration defConfig = YamlConfiguration.loadConfiguration(
                new InputStreamReader(defStream, StandardCharsets.UTF_8)
            );
            config.setDefaults(defConfig);
        }
        
        languages.put(language, config);
        plugin.getLogger().info("已加载语言: " + language);
    }
    
    /**
     * 从资源中保存文件
     */
    private void saveResource(String resourcePath) {
        File file = new File(plugin.getDataFolder(), resourcePath);
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                plugin.saveResource(resourcePath, false);
            } catch (Exception ex) {
                plugin.getLogger().warning("无法保存资源文件: " + resourcePath);
            }
        }
    }
    
    /**
     * 重载所有语言文件
     */
    public void reload() {
        languages.clear();
        load();
    }
    
    /**
     * 设置默认语言
     */
    public void setDefaultLanguage(String language) {
        if (languages.containsKey(language)) {
            this.defaultLanguage = language;
        } else {
            plugin.getLogger().warning("语言不存在: " + language);
        }
    }
    
    /**
     * 获取默认语言
     */
    public String getDefaultLanguage() {
        return defaultLanguage;
    }
    
    /**
     * 获取消息（使用默认语言）
     */
    public String getMessage(String key) {
        return getMessage(defaultLanguage, key);
    }
    
    /**
     * 获取消息（指定语言）
     */
    public String getMessage(String language, String key) {
        FileConfiguration config = languages.get(language);
        
        // 如果语言不存在，使用默认语言
        if (config == null) {
            config = languages.get(defaultLanguage);
        }
        
        // 如果默认语言也不存在，返回键名
        if (config == null) {
            return key;
        }
        
        String message = config.getString(key);
        
        // 如果消息不存在，尝试从默认语言获取
        if (message == null && !language.equals(defaultLanguage)) {
            FileConfiguration defConfig = languages.get(defaultLanguage);
            if (defConfig != null) {
                message = defConfig.getString(key);
            }
        }
        
        // 如果还是不存在，返回键名
        if (message == null) {
            return key;
        }
        
        // 翻译颜色代码
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    /**
     * 获取消息并替换占位符
     */
    public String getMessage(String key, Object... replacements) {
        return getMessage(defaultLanguage, key, replacements);
    }
    
    /**
     * 获取消息并替换占位符（指定语言）
     */
    public String getMessage(String language, String key, Object... replacements) {
        String message = getMessage(language, key);
        
        // 替换占位符
        for (int i = 0; i < replacements.length; i++) {
            message = message.replace("{" + i + "}", String.valueOf(replacements[i]));
        }
        
        return message;
    }
    
    /**
     * 发送消息给玩家
     */
    public void sendMessage(Player player, String key) {
        player.sendMessage(getMessage(key));
    }
    
    /**
     * 发送消息给玩家并替换占位符
     */
    public void sendMessage(Player player, String key, Object... replacements) {
        player.sendMessage(getMessage(key, replacements));
    }
    
    /**
     * 发送消息给命令发送者（支持控制台）
     */
    public void sendMessage(org.bukkit.command.CommandSender sender, String key) {
        sender.sendMessage(getMessage(key));
    }
    
    /**
     * 发送消息给命令发送者并替换占位符（支持控制台）
     */
    public void sendMessage(org.bukkit.command.CommandSender sender, String key, Object... replacements) {
        sender.sendMessage(getMessage(key, replacements));
    }
    
    /**
     * 获取玩家的语言（可以根据玩家的客户端语言）
     */
    public String getPlayerLanguage(Player player) {
        // TODO: 可以根据玩家的客户端语言或玩家数据返回对应语言
        // 目前返回默认语言
        return defaultLanguage;
    }
    
    /**
     * 发送消息给玩家（使用玩家的语言）
     */
    public void sendPlayerMessage(Player player, String key) {
        String language = getPlayerLanguage(player);
        player.sendMessage(getMessage(language, key));
    }
    
    /**
     * 发送消息给玩家并替换占位符（使用玩家的语言）
     */
    public void sendPlayerMessage(Player player, String key, Object... replacements) {
        String language = getPlayerLanguage(player);
        player.sendMessage(getMessage(language, key, replacements));
    }
}
