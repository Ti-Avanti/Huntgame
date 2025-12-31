package com.minecraft.huntergame.manager;

import com.minecraft.huntergame.HunterGame;
import com.minecraft.huntergame.game.GameState;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 游戏状态管理器
 * 负责管理竞技场的状态转换
 * 
 * @author YourName
 * @version 1.0.0
 */
public class GameStateManager {
    
    private final HunterGame plugin;
    private final String arenaName;
    private GameState currentState;
    
    // 状态变化监听器
    private final Map<GameState, Consumer<GameState>> stateListeners;
    
    public GameStateManager(HunterGame plugin, String arenaName) {
        this.plugin = plugin;
        this.arenaName = arenaName;
        this.currentState = GameState.WAITING;
        this.stateListeners = new HashMap<>();
    }
    
    /**
     * 获取当前状态
     */
    public GameState getCurrentState() {
        return currentState;
    }
    
    /**
     * 设置状态（不验证转换合法性）
     * 仅用于初始化
     */
    public void setState(GameState state) {
        this.currentState = state;
    }
    
    /**
     * 转换到新状态
     * 
     * @param newState 新状态
     * @return 是否成功转换
     */
    public boolean transitionTo(GameState newState) {
        return transitionTo(newState, true);
    }
    
    /**
     * 转换到新状态
     * 
     * @param newState 新状态
     * @param validate 是否验证转换合法性
     * @return 是否成功转换
     */
    public boolean transitionTo(GameState newState, boolean validate) {
        if (newState == null) {
            plugin.getLogger().warning("[" + arenaName + "] 尝试转换到null状态");
            return false;
        }
        
        // 如果已经是目标状态，不需要转换
        if (currentState == newState) {
            return true;
        }
        
        // 验证转换合法性
        if (validate && !currentState.canTransitionTo(newState)) {
            plugin.getLogger().warning("[" + arenaName + "] 非法状态转换: " + 
                currentState + " -> " + newState);
            return false;
        }
        
        GameState oldState = currentState;
        currentState = newState;
        
        plugin.getLogger().info("[" + arenaName + "] 状态转换: " + 
            oldState + " -> " + newState);
        
        // 触发状态变化事件
        notifyStateChange(oldState, newState);
        
        return true;
    }
    
    /**
     * 注册状态监听器
     * 
     * @param state 要监听的状态
     * @param listener 监听器（接收旧状态作为参数）
     */
    public void registerListener(GameState state, Consumer<GameState> listener) {
        stateListeners.put(state, listener);
    }
    
    /**
     * 移除状态监听器
     * 
     * @param state 状态
     */
    public void unregisterListener(GameState state) {
        stateListeners.remove(state);
    }
    
    /**
     * 通知状态变化
     */
    private void notifyStateChange(GameState oldState, GameState newState) {
        Consumer<GameState> listener = stateListeners.get(newState);
        if (listener != null) {
            try {
                listener.accept(oldState);
            } catch (Exception ex) {
                plugin.getLogger().severe("[" + arenaName + "] 状态监听器执行失败: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * 检查是否可以加入
     */
    public boolean isJoinable() {
        return currentState.isJoinable();
    }
    
    /**
     * 检查是否正在游戏中
     */
    public boolean isPlaying() {
        return currentState.isPlaying();
    }
    
    /**
     * 检查是否正在运行
     */
    public boolean isRunning() {
        return currentState.isRunning();
    }
    
    /**
     * 检查是否已结束
     */
    public boolean isEnded() {
        return currentState.isEnded();
    }
    
    /**
     * 检查是否可用
     */
    public boolean isAvailable() {
        return currentState.isAvailable();
    }
    
    /**
     * 重置状态到等待
     */
    public void reset() {
        transitionTo(GameState.WAITING, false);
    }
}
