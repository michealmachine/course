package com.zhangziqi.online_course_mine.model.vo;

import com.zhangziqi.online_course_mine.model.entity.LearningRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 学习记录视图对象
 * 用于返回学习记录数据给前端
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningRecordVO {
    
    /**
     * 记录ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 课程标题
     */
    private String courseTitle;
    
    /**
     * 章节ID
     */
    private Long chapterId;
    
    /**
     * 章节标题
     */
    private String chapterTitle;
    
    /**
     * 小节ID
     */
    private Long sectionId;
    
    /**
     * 小节标题
     */
    private String sectionTitle;
    
    /**
     * 活动类型
     */
    private String activityType;
    
    /**
     * 活动类型描述
     */
    private String activityTypeDescription;
    
    /**
     * 活动开始时间
     */
    private LocalDateTime activityStartTime;
    
    /**
     * 活动结束时间
     */
    private LocalDateTime activityEndTime;
    
    /**
     * 持续时间（秒）
     */
    private Integer durationSeconds;
    
    /**
     * 上下文数据（JSON字符串）
     */
    private String contextData;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 从实体转换为VO
     */
    public static LearningRecordVO fromEntity(LearningRecord entity) {
        if (entity == null) {
            return null;
        }
        
        return LearningRecordVO.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .courseId(entity.getCourseId())
                .courseTitle(entity.getCourse() != null ? entity.getCourse().getTitle() : null)
                .chapterId(entity.getChapterId())
                .chapterTitle(entity.getChapter() != null ? entity.getChapter().getTitle() : null)
                .sectionId(entity.getSectionId())
                .sectionTitle(entity.getSection() != null ? entity.getSection().getTitle() : null)
                .activityType(entity.getActivityType())
                .activityTypeDescription(entity.getActivityTypeEnum() != null ? 
                        entity.getActivityTypeEnum().getDescription() : null)
                .activityStartTime(entity.getActivityStartTime())
                .activityEndTime(entity.getActivityEndTime())
                .durationSeconds(entity.getDurationSeconds())
                .contextData(entity.getContextData())
                .createdAt(entity.getCreatedAt())
                .build();
    }
} 