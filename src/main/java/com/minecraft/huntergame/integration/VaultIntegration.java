package com.minecraft.huntergame.integration;

import com.minecraft.huntergame.HunterGame;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Vault经济系统集成
 * 负责与Vault插件交互，处理经济相关操作
 * 
 * @author YourName
 * @version 1.0.0
 */
public class VaultIntegration {
    
    private final HunterGame plugin;
    private Economy economy;
    private boolean enabled;
    
    public VaultIntegration(HunterGame plugin) {
        this.plugin = plugin;
        this.enabled = false;
        
        // 尝试连接Vault
        setupEconomy();
    }
    
    /**
     * 设置经济系统
     */
    private void setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().info("未检测到Vault插件，经济系统功能将被禁用");
            return;
        }
        
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager()
            .getRegistration(Economy.class);
        
        if (rsp == null) {
            plugin.getLogger().warning("Vault已安装但未找到经济插件，经济系统功能将被禁用");
            return;
        }
        
        economy = rsp.getProvider();
        enabled = true;
        plugin.getLogger().info("Vault经济系统已连接: " + economy.getName());
    }
    
    /**
     * 检查经济系统是否可用
     */
    public boolean isEnabled() {
        return enabled && economy != null;
    }
    
    /**
     * 获取Economy实例
     */
    public Economy getEconomy() {
        return economy;
    }
    
    /**
     * 给予玩家金币
     * 
     * @param player 玩家
     * @param amount 金额
     * @return 是否成功
     */
    public boolean giveMoney(OfflinePlayer player, double amount) {
        if (!isEnabled()) {
            return false;
        }
        
        try {
            economy.depositPlayer(player, amount);
            return true;
        } catch (Exception ex) {
            plugin.getLogger().severe("给予玩家 " + player.getName() + " 金币失败: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }
    
    /**
     * 扣除玩家金币
     * 
     * @param player 玩家
     * @param amount 金额
     * @return 是否成功
     */
    public boolean takeMoney(OfflinePlayer player, double amount) {
        if (!isEnabled()) {
            return false;
        }
        
        try {
            if (!economy.has(player, amount)) {
                return false;
            }
            
            economy.withdrawPlayer(player, amount);
            return true;
        } catch (Exception ex) {
            plugin.getLogger().severe("扣除玩家 " + player.getName() + " 金币失败: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }
    
    /**
     * 获取玩家余额
     * 
     * @param player 玩家
     * @return 余额
     */
    public double getBalance(OfflinePlayer player) {
        if (!isEnabled()) {
            return 0.0;
        }
        
        try {
            return economy.getBalance(player);
        } catch (Exception ex) {
            plugin.getLogger().severe("获取玩家 " + player.getName() + " 余额失败: " + ex.getMessage());
            ex.printStackTrace();
            return 0.0;
        }
    }
    
    /**
     * 检查玩家是否有足够的金币
     * 
     * @param player 玩家
     * @param amount 金额
     * @return 是否有足够金币
     */
    public boolean has(OfflinePlayer player, double amount) {
        if (!isEnabled()) {
            return false;
        }
        
        try {
            return economy.has(player, amount);
        } catch (Exception ex) {
            plugin.getLogger().severe("检查玩家 " + player.getName() + " 余额失败: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }
    
    /**
     * 格式化金额
     * 
     * @param amount 金额
     * @return 格式化后的字符串
     */
    public String format(double amount) {
        if (!isEnabled()) {
            return String.valueOf(amount);
        }
        
        try {
            return economy.format(amount);
        } catch (Exception ex) {
            return String.valueOf(amount);
        }
    }
}
