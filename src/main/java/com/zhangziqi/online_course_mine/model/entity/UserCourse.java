package com.zhangziqi.online_course_mine.model.entity;

import com.zhangziqi.online_course_mine.model.enums.UserCourseStatus;
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
 * 用户课程关联实体类
 */
@Getter
@Setter
@ToString(exclude = {"user", "course", "order"})
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_courses", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "course_id"}))
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class UserCourse extends BaseEntity {
    
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
     * 购买时间
     */
    @Column(nullable = false)
    private LocalDateTime purchasedAt;
    
    /**
     * 关联订单
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;
    
    /**
     * 过期时间（如果有）
     */
    private LocalDateTime expireAt;
    
    /**
     * 学习进度（百分比）
     */
    @Builder.Default
    private Integer progress = 0;
    
    /**
     * 状态：0-正常，1-已过期，2-已退款
     */
    @Builder.Default
    @Column(nullable = false)
    private Integer status = UserCourseStatus.NORMAL.getValue();
    
    /**
     * 乐观锁版本字段
     */
    @Version
    private Integer version;
    
    /**
     * 最后学习时间
     */
    private LocalDateTime lastLearnAt;
    
    /**
     * 学习时长（秒）
     */
    @Builder.Default
    private Integer learnDuration = 0;
    
    /**
     * 当前学习章节ID
     */
    private Long currentChapterId;
    
    /**
     * 当前学习小节ID
     */
    private Long currentSectionId;
    
    /**
     * 当前小节学习进度（百分比）
     */
    @Builder.Default
    private Integer currentSectionProgress = 0;
    
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
     * 获取订单ID
     */
    public Long getOrderId() {
        return order != null ? order.getId() : null;
    }
    
    /**
     * 获取状态枚举
     */
    @Transient
    public UserCourseStatus getStatusEnum() {
        return UserCourseStatus.valueOf(this.status);
    }
    
    /**
     * 设置状态枚举
     */
    public void setStatusEnum(UserCourseStatus status) {
        this.status = status != null ? status.getValue() : null;
    }

    /**
     * 自定义equals方法，只比较ID
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserCourse that = (UserCourse) o;
        return getId() != null && getId().equals(that.getId());
    }

    /**
     * 自定义hashCode方法，只使用ID
     */
    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }
} 