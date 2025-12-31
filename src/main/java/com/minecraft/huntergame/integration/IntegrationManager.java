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
    
    public IntegrationManager(HunterGame plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 注册所有集成
     */
    public void registerAll() {
        // Vault 集成
        if (isPluginEnabled("Vault")) {
            vaultIntegration = new VaultIntegration(plugin);
            if (vaultIntegration.isEnabled()) {
                plugin.getLogger().info("已集成 Vault 经济系统");
            } else {
                plugin.getLogger().warning("Vault 插件已安装但未找到经济系统提供者");
            }
        } else {
            plugin.getLogger().info("未检测到 Vault 插件，经济功能将被禁用");
        }
        
        // PlaceholderAPI 集成
        if (isPluginEnabled("PlaceholderAPI")) {
            placeholderAPIIntegration = new PlaceholderAPIIntegration(plugin);
            if (placeholderAPIIntegration.register()) {
                plugin.getLogger().info("已集成 PlaceholderAPI，提供 24 个变量支持");
            } else {
                plugin.getLogger().warning("PlaceholderAPI 注册失败");
            }
        } else {
            plugin.getLogger().info("未检测到 PlaceholderAPI 插件，变量功能将被禁用");
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
}
