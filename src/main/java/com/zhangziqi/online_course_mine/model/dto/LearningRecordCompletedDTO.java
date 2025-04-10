package com.zhangziqi.online_course_mine.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 已完成学习记录DTO
 * 用于一次性记录已完成的学习活动，无需单独的开始和结束请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningRecordCompletedDTO {
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 章节ID
     */
    private Long chapterId;
    
    /**
     * 小节ID
     */
    private Long sectionId;
    
    /**
     * 活动类型
     * @see com.zhangziqi.online_course_mine.model.enums.LearningActivityType
     */
    private String activityType;
    
    /**
     * 持续时间（秒）
     */
    private Integer durationSeconds;
    
    /**
     * 上下文数据（JSON字符串）
     * 可选的额外信息
     */
    private String contextData;
} 