package com.minecraft.huntergame.integration;

import com.minecraft.huntergame.HunterGame;
import org.bukkit.Bukkit;

/**
 * 集成管理器
 * 负责管理所有第三方插件集成
 * 
 * @author YourName
 * @version 1.0.0
 */
public class IntegrationManager {
    
    private final HunterGame plugin;
    private VaultIntegration vaultIntegration;
    private PlaceholderAPIIntegration placeholderAPIIntegration;
    private MultiverseIntegration multiverseIntegration;
    
    public IntegrationManager(HunterGame plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 注册所有集成
     */
    public void registerAll() {
        try {
            // Vault 集成
            if (isPluginEnabled("Vault")) {
                try {
                    vaultIntegration = new VaultIntegration(plugin);
                    if (vaultIntegration.isEnabled()) {
                        plugin.getLogger().info("已集成 Vault 经济系统");
                    } else {
                        plugin.getLogger().warning("Vault 插件已安装但未找到经济系统提供者");
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Vault 集成初始化失败: " + e.getMessage());
                }
            } else {
                plugin.getLogger().info("未检测到 Vault 插件，经济功能将被禁用");
            }
            
            // PlaceholderAPI 集成
            if (isPluginEnabled("PlaceholderAPI")) {
                try {
                    placeholderAPIIntegration = new PlaceholderAPIIntegration(plugin);
                    if (placeholderAPIIntegration.register()) {
                        plugin.getLogger().info("已集成 PlaceholderAPI，提供 24 个变量支持");
                    } else {
                        plugin.getLogger().warning("PlaceholderAPI 注册失败");
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("PlaceholderAPI 集成初始化失败: " + e.getMessage());
                }
            } else {
                plugin.getLogger().info("未检测到 PlaceholderAPI 插件，变量功能将被禁用");
            }
            
            // Multiverse-Core 集成
            if (plugin.getManhuntConfig().useMultiverse()) {
                if (isPluginEnabled("Multiverse-Core")) {
                    try {
                        multiverseIntegration = new MultiverseIntegration(plugin);
                        if (multiverseIntegration.initialize()) {
                            plugin.getLogger().info("已集成 Multiverse-Core，支持独立游戏世界管理");
                        } else {
                            plugin.getLogger().warning("Multiverse-Core 初始化失败");
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("Multiverse-Core 集成初始化失败: " + e.getMessage());
                    }
                } else {
                    plugin.getLogger().warning("配置启用了 Multiverse 模式，但未检测到 Multiverse-Core 插件");
                    plugin.getLogger().warning("请安装 Multiverse-Core 或在配置中禁用 use-multiverse");
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("集成管理器初始化时发生严重错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 注销所有集成
     */
    public void unregisterAll() {
        // 注销 PlaceholderAPI
        if (placeholderAPIIntegration != null) {
            placeholderAPIIntegration.unregister();
            plugin.getLogger().info("已注销 PlaceholderAPI 集成");
        }
    }
    
    /**
     * 检查插件是否启用
     */
    private boolean isPluginEnabled(String pluginName) {
        return Bukkit.getPluginManager().isPluginEnabled(pluginName);
    }
    
    /**
     * 获取 Vault 集成
     */
    public VaultIntegration getVaultIntegration() {
        return vaultIntegration;
    }
    
    /**
     * 获取 PlaceholderAPI 集成
     */
    public PlaceholderAPIIntegration getPlaceholderAPIIntegration() {
        return placeholderAPIIntegration;
    }
    
    /**
     * 获取 Multiverse 集成
     */
    public MultiverseIntegration getMultiverseIntegration() {
        return multiverseIntegration;
    }
    
    /**
     * 检查 Vault 是否可用
     */
    public boolean isVaultEnabled() {
        return vaultIntegration != null && vaultIntegration.isEnabled();
    }
    
    /**
     * 检查 PlaceholderAPI 是否可用
     */
    public boolean isPlaceholderAPIEnabled() {
        return placeholderAPIIntegration != null;
    }
    
    /**
     * 检查 Multiverse 是否可用
     */
    public boolean isMultiverseEnabled() {
        return multiverseIntegration != null && multiverseIntegration.isEnabled();
    }
}
