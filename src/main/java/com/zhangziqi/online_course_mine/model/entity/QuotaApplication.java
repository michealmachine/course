package com.zhangziqi.online_course_mine.model.entity;

import com.zhangziqi.online_course_mine.model.enums.QuotaType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 存储配额申请实体
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "quota_applications")
public class QuotaApplication {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String applicationId;
    
    @Column(nullable = false)
    private Long institutionId;
    
    @Column(nullable = false)
    private Long applicantId;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private QuotaType quotaType;
    
    @Column(nullable = false)
    private Long requestedBytes;
    
    @Column(nullable = false, length = 500)
    private String reason;
    
    @Column(nullable = false)
    private Integer status; // 0-待审核, 1-已通过, 2-已拒绝
    
    private Long reviewerId;
    
    private LocalDateTime reviewedAt;
    
    @Column(length = 500)
    private String reviewComment;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
} 