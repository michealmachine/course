package com.zhangziqi.online_course_mine.utils;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 格式化工具类
 */
public class FormatUtil {
    
    private static final DecimalFormat df = new DecimalFormat("#.##");
    
    /**
     * 格式化文件大小
     *
     * @param bytes 文件大小（字节）
     * @return 格式化后的字符串
     */
    public static String formatFileSize(Long bytes) {
        if (bytes == null || bytes <= 0) {
            return "0 B";
        }
        
        // 单位转换
        final String[] units = {"B", "KB", "MB", "GB", "TB", "PB", "EB"};
        int unitIndex = (int) (Math.log10(bytes) / Math.log10(1024));
        unitIndex = Math.min(unitIndex, units.length - 1);
        
        double size = bytes / Math.pow(1024, unitIndex);
        return df.format(size) + " " + units[unitIndex];
    }
    
    /**
     * 格式化日期时间
     *
     * @param dateTime 日期时间
     * @return 格式化后的字符串 (yyyy-MM-dd HH:mm:ss)
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateTime.format(formatter);
    }
    
    /**
     * 格式化百分比
     *
     * @param value 百分比值（0-1）
     * @return 格式化后的字符串（如 85.5%）
     */
    public static String formatPercentage(double value) {
        return df.format(value * 100) + "%";
    }
} 