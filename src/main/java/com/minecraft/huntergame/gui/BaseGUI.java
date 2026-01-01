package com.minecraft.huntergame.gui;

import com.minecraft.huntergame.HunterGame;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * GUI基类
 * 提供GUI的基础功能和生命周期管理
 * 
 * @author YourName
 * @version 1.0.0
 */
public abstract class BaseGUI {
    
    protected final HunterGame plugin;
    protected final Player player;
    protected Inventory inventory;
    protected boolean autoRefresh;
    protected int refreshInterval; // tick
    protected int taskId = -1;
    
    public BaseGUI(HunterGame plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.autoRefresh = false;
        this.refreshInterval = 20; // 默认1秒
    }
    
    /**
     * 获取GUI标题
     */
    protected abstract String getTitle();
    
    /**
     * 获取GUI大小（必须是9的倍数）
     */
    protected abstract int getSize();
    
    /**
     * 构建GUI内容
     */
    protected abstract void buildContent();
    
    /**
     * 处理点击事件
     * 
     * @param slot 点击的槽位
     * @param item 点击的物品
     * @return 是否取消事件
     */
    public abstract boolean handleClick(int slot, ItemStack item);
    
    /**
     * 打开GUI
     */
    public void open() {
        // 创建inventory
        inventory = Bukkit.createInventory(null, getSize(), getTitle());
        
        // 构建内容
        buildContent();
        
        // 打开给玩家
        player.openInventory(inventory);
        
        // 注册到管理器
        plugin.getGUIManager().registerGUI(player, this);
        
        // 启动自动刷新
        if (autoRefresh) {
            startAutoRefresh();
        }
        
        // 调用打开回调
        onOpen();
    }
    
    /**
     * 关闭GUI
     */
    public void close() {
        // 停止自动刷新
        stopAutoRefresh();
        
        // 关闭inventory
        player.closeInventory();
        
        // 从管理器移除
        plugin.getGUIManager().unregisterGUI(player);
        
        // 调用关闭回调
        onClose();
    }
    
    /**
     * 刷新GUI内容
     */
    public void refresh() {
        if (inventory == null) {
            return;
        }
        
        // 清空内容
        inventory.clear();
        
        // 重新构建
        buildContent();
        
        // 更新玩家视图
        player.updateInventory();
        
        // 调用刷新回调
        onRefresh();
    }
    
    /**
     * 启用自动刷新
     */
    public void enableAutoRefresh(int interval) {
        this.autoRefresh = true;
        this.refreshInterval = interval;
        
        if (inventory != null) {
            startAutoRefresh();
        }
    }
    
    /**
     * 禁用自动刷新
     */
    public void disableAutoRefresh() {
        this.autoRefresh = false;
        stopAutoRefresh();
    }
    
    /**
     * 启动自动刷新任务
     */
    private void startAutoRefresh() {
        if (taskId != -1) {
            return;
        }
        
        taskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(
            plugin,
            this::refresh,
            refreshInterval,
            refreshInterval
        );
    }
    
    /**
     * 停止自动刷新任务
     */
    private void stopAutoRefresh() {
        if (taskId != -1) {
            plugin.getServer().getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }
    
    /**
     * 设置物品到指定槽位
     */
    protected void setItem(int slot, ItemStack item) {
        if (inventory != null && slot >= 0 && slot < inventory.getSize()) {
            inventory.setItem(slot, item);
        }
    }
    
    /**
     * 填充边框
     */
    protected void fillBorder(ItemStack borderItem) {
        int size = inventory.getSize();
        int rows = size / 9;
        
        // 填充第一行和最后一行
        for (int i = 0; i < 9; i++) {
            setItem(i, borderItem);
            setItem(size - 9 + i, borderItem);
        }
        
        // 填充左右两列
        for (int row = 1; row < rows - 1; row++) {
            setItem(row * 9, borderItem);
            setItem(row * 9 + 8, borderItem);
        }
    }
    
    /**
     * 填充整个GUI
     */
    protected void fill(ItemStack item) {
        for (int i = 0; i < inventory.getSize(); i++) {
            setItem(i, item);
        }
    }
    
    // ==================== 生命周期回调 ====================
    
    /**
     * GUI打开时调用
     */
    protected void onOpen() {
        // 子类可以重写
    }
    
    /**
     * GUI关闭时调用
     */
    protected void onClose() {
        // 子类可以重写
    }
    
    /**
     * GUI刷新时调用
     */
    protected void onRefresh() {
        // 子类可以重写
    }
    
    // ==================== Getter方法 ====================
    
    public Inventory getInventory() {
        return inventory;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public boolean isAutoRefresh() {
        return autoRefresh;
    }
}
