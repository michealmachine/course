package com.zhangziqi.online_course_mine.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
@EqualsAndHashCode(callSuper = true)
public class Order extends BaseEntity {
    
    /**
     * 订单号（支付宝交易创建时生成）
     */
    @Column(nullable = false, unique = true, length = 64)
    private String orderNo;
    
    /**
     * 订单标题
     */
    @Column(nullable = false, length = 200)
    private String title;
    
    /**
     * 订单描述
     */
    @Column(length = 500)
    private String description;
    
    /**
     * 订单金额
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    /**
     * 购买用户
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
     * 机构（冗余字段，方便查询）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;
    
    /**
     * 支付宝交易号（支付成功后回填）
     */
    @Column(length = 64)
    private String tradeNo;
    
    /**
     * 支付时间
     */
    private LocalDateTime paidAt;
    
    /**
     * 订单状态：
     * 0-待支付，1-已支付，2-已关闭，3-申请退款，4-已退款，5-退款失败
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer status = 0;
    
    /**
     * 退款时间
     */
    private LocalDateTime refundedAt;
    
    /**
     * 退款金额
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal refundAmount;
    
    /**
     * 退款原因
     */
    @Column(length = 500)
    private String refundReason;
    
    /**
     * 退款交易号
     */
    @Column(length = 64)
    private String refundTradeNo;
    
    /**
     * 关联的用户课程记录
     */
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserCourse userCourse;
    
    /**
     * 支付链接（临时存储，不持久化到数据库）
     */
    @Transient
    private String payUrl;
    
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
     * 获取机构ID
     */
    public Long getInstitutionId() {
        return institution != null ? institution.getId() : null;
    }
} 