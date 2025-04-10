package com.zhangziqi.online_course_mine.model.entity;

import com.zhangziqi.online_course_mine.model.enums.LearningActivityType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 学习记录实体类
 * 用于记录用户的学习活动详情
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "learning_records")
@EqualsAndHashCode(callSuper = true)
public class LearningRecord extends BaseEntity {
    
    /**
     * 用户
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    /**
     * 课程
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    
    /**
     * 章节
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id")
    private Chapter chapter;
    
    /**
     * 小节
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private Section section;
    
    /**
     * 活动类型
     * @see LearningActivityType
     */
    @Column(name = "activity_type", length = 20, nullable = false)
    private String activityType;
    
    /**
     * 活动开始时间
     */
    @Column(name = "activity_start_time", nullable = false)
    private LocalDateTime activityStartTime;
    
    /**
     * 活动结束时间
     * 为null表示活动仍在进行中
     */
    @Column(name = "activity_end_time")
    private LocalDateTime activityEndTime;
    
    /**
     * 持续时间（秒）
     */
    @Column(name = "duration_seconds")
    private Integer durationSeconds;
    
    /**
     * 上下文数据（JSON字符串）
     * 用于存储与活动相关的额外信息
     */
    @Column(name = "context_data", columnDefinition = "TEXT")
    private String contextData;
    
    /**
     * 获取用户ID
     */
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }
    
    /**
     * 获取课程ID
     */
    public Long getCourseId() {
        return course != null ? course.getId() : null;
    }
    
    /**
     * 获取章节ID
     */
    public Long getChapterId() {
        return chapter != null ? chapter.getId() : null;
    }
    
    /**
     * 获取小节ID
     */
    public Long getSectionId() {
        return section != null ? section.getId() : null;
    }
    
    /**
     * 获取活动类型枚举
     */
    @Transient
    public LearningActivityType getActivityTypeEnum() {
        return LearningActivityType.getByCode(this.activityType);
    }
    
    /**
     * 设置活动类型枚举
     */
    public void setActivityTypeEnum(LearningActivityType activityType) {
        this.activityType = activityType != null ? activityType.getCode() : null;
    }
} 