package com.minecraft.huntergame.util;

/**
 * 时间格式化工具类
 * 
 * @author YourName
 * @version 1.0.0
 */
public class TimeUtil {
    
    /**
     * 格式化时间（秒转换为"Xh Ym Zs"格式）
     * 
     * @param seconds 秒数
     * @return 格式化后的时间字符串
     */
    public static String formatTime(long seconds) {
        if (seconds < 0) {
            return "0s";
        }
        
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        
        StringBuilder result = new StringBuilder();
        
        if (hours > 0) {
            result.append(hours).append("h ");
        }
        
        if (minutes > 0) {
            result.append(minutes).append("m ");
        }
        
        if (secs > 0 || result.length() == 0) {
            result.append(secs).append("s");
        }
        
        return result.toString().trim();
    }
    
    /**
     * 格式化时间（秒转换为"HH:MM:SS"格式）
     * 
     * @param seconds 秒数
     * @return 格式化后的时间字符串
     */
    public static String formatTimeColon(long seconds) {
        if (seconds < 0) {
            return "00:00:00";
        }
        
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        
        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, secs);
        } else {
            return String.format("%02d:%02d", minutes, secs);
        }
    }
    
    /**
     * 格式化时间（中文格式）
     * 
     * @param seconds 秒数
     * @return 格式化后的时间字符串
     */
    public static String formatTimeChinese(long seconds) {
        if (seconds < 0) {
            return "0秒";
        }
        
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        
        StringBuilder result = new StringBuilder();
        
        if (hours > 0) {
            result.append(hours).append("小时");
        }
        
        if (minutes > 0) {
            result.append(minutes).append("分");
        }
        
        if (secs > 0 || result.length() == 0) {
            result.append(secs).append("秒");
        }
        
        return result.toString();
    }
}
