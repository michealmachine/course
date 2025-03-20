package com.zhangziqi.online_course_mine.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 用户收藏课程实体类
 */
@Getter
@Setter
@ToString(exclude = {"user", "course"})
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_favorites", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "course_id"}))
@EqualsAndHashCode(callSuper = true)
public class UserFavorite extends BaseEntity {

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
     * 收藏时间
     */
    @Builder.Default
    private LocalDateTime favoriteTime = LocalDateTime.now();
    
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
} 