package com.zhangziqi.online_course_mine.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 学习记录开始DTO
 * 用于接收开始一个学习活动的请求数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningRecordStartDTO {
    
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
     * 上下文数据（JSON字符串）
     * 可选的额外信息
     */
    private String contextData;
} 