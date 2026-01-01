package com.minecraft.huntergame.game;

/**
 * 游戏结束原因枚举
 * 
 * @author YourName
 * @version 1.0.0
 */
public enum GameEndReason {
    
    /** 逃亡者胜利 - 击败末影龙 */
    RUNNERS_WIN_DRAGON,
    
    /** 猎人胜利 - 击杀所有逃亡者 */
    HUNTERS_WIN_KILL,
    
    /** 游戏超时 */
    TIMEOUT,
    
    /** 所有逃亡者离开 */
    RUNNERS_LEFT,
    
    /** 所有猎人离开 */
    HUNTERS_LEFT,
    
    /** 游戏被取消 */
    CANCELLED,
    
    /** 管理员强制结束 */
    ADMIN_STOP,
    
    /** 服务器关闭 */
    SERVER_SHUTDOWN,
    
    /** 未知原因 */
    UNKNOWN;
    
    /**
     * 获取结束原因的显示文本
     */
    public String getDisplayName() {
        switch (this) {
            case RUNNERS_WIN_DRAGON:
                return "§a逃亡者击败末影龙获胜！";
            case HUNTERS_WIN_KILL:
                return "§c猎人击杀所有逃亡者获胜！";
            case TIMEOUT:
                return "§e游戏超时";
            case RUNNERS_LEFT:
                return "§c所有逃亡者已离开";
            case HUNTERS_LEFT:
                return "§c所有猎人已离开";
            case CANCELLED:
                return "§c游戏已取消";
            case ADMIN_STOP:
                return "§c管理员强制结束";
            case SERVER_SHUTDOWN:
                return "§c服务器关闭";
            default:
                return "§7未知原因";
        }
    }
    
    /**
     * 是否是正常结束（有胜者）
     */
    public boolean isNormalEnd() {
        return this == RUNNERS_WIN_DRAGON || this == HUNTERS_WIN_KILL;
    }
    
    /**
     * 是否是逃亡者胜利
     */
    public boolean isRunnersWin() {
        return this == RUNNERS_WIN_DRAGON || this == HUNTERS_LEFT;
    }
    
    /**
     * 是否是猎人胜利
     */
    public boolean isHuntersWin() {
        return this == HUNTERS_WIN_KILL || this == RUNNERS_LEFT;
    }
}
