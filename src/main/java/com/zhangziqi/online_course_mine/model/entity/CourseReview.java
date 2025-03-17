package com.zhangziqi.online_course_mine.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 课程评论实体类
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "course_reviews")
@EqualsAndHashCode(callSuper = true)
public class CourseReview extends BaseEntity {
    
    /**
     * 关联的课程
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    
    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    /**
     * 评分 (1-5)
     */
    @Column(nullable = false)
    private Integer rating;
    
    /**
     * 评价内容
     */
    @Column(length = 1000)
    private String content;
    
    /**
     * 点赞数
     */
    @Builder.Default
    private Integer likeCount = 0;
    
    /**
     * 乐观锁版本字段
     */
    @Version
    private Integer dataVersion;
    
    /**
     * 获取课程ID
     */
    @Transient
    public Long getCourseId() {
        return course != null ? course.getId() : null;
    }
} 