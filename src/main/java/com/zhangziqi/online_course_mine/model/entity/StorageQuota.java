package com.zhangziqi.online_course_mine.model.entity;

import com.zhangziqi.online_course_mine.model.enums.QuotaType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 存储配额实体
 */
@Entity
@Table(name = "storage_quota")
@Data
@EqualsAndHashCode(callSuper = false)
public class StorageQuota {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 配额类型
     */
    @Enumerated(EnumType.STRING)
    private QuotaType type;
    
    /**
     * 总配额(字节)
     */
    private Long totalQuota;
    
    /**
     * 已使用配额(字节)
     */
    private Long usedQuota;
    
    /**
     * 所属机构
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;
    
    /**
     * 创建时间
     */
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    /**
     * 过期时间(可选)
     */
    private LocalDateTime expiresAt;

    /**
     * 获取机构ID
     */
    public Long getInstitutionId() {
        return institution != null ? institution.getId() : null;
    }

    /**
     * 设置机构ID
     */
    public void setInstitutionId(Long institutionId) {
        if (institutionId != null) {
            this.institution = Institution.builder().id(institutionId).build();
        } else {
            this.institution = null;
        }
    }
} 