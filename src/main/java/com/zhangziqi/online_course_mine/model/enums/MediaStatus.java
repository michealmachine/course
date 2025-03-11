package com.zhangziqi.online_course_mine.model.enums;

/**
 * 媒体状态枚举
 */
public enum MediaStatus {
    /**
     * 上传中
     */
    UPLOADING,
    
    /**
     * 处理中（如转码）
     */
    PROCESSING,
    
    /**
     * 已完成
     */
    COMPLETED,
    
    /**
     * 失败
     */
    FAILED
} 