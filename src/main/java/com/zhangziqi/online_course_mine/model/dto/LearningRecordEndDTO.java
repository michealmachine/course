package com.zhangziqi.online_course_mine.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 学习记录结束DTO
 * 用于接收结束一个学习活动的请求数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningRecordEndDTO {
    
    /**
     * 上下文数据（JSON字符串）
     * 可选的额外信息，例如学习进度、完成情况等
     */
    private String contextData;
} 