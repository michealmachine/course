package com.zhangziqi.online_course_mine.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 机构申请实体类
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "institution_applications")
public class InstitutionApplication extends BaseEntity {

    /**
     * 申请ID（业务编号）
     */
    @Column(unique = true, length = 20)
    private String applicationId;
    
    /**
     * 机构名称
     */
    @Column(nullable = false, length = 100)
    private String name;
    
    /**
     * 机构Logo
     */
    private String logo;
    
    /**
     * 机构描述
     */
    @Column(length = 500)
    private String description;
    
    /**
     * 联系人
     */
    @Column(length = 50)
    private String contactPerson;
    
    /**
     * 联系电话
     */
    @Column(length = 20)
    private String contactPhone;
    
    /**
     * 联系邮箱
     */
    private String contactEmail;
    
    /**
     * 地址
     */
    @Column(length = 255)
    private String address;
    
    /**
     * 状态（0-待审核，1-已通过，2-已拒绝）
     */
    @Builder.Default
    private Integer status = 0;
    
    /**
     * 审核结果备注
     */
    @Column(length = 500)
    private String reviewComment;
    
    /**
     * 审核人ID
     */
    private Long reviewerId;
    
    /**
     * 审核时间
     */
    private LocalDateTime reviewedAt;
    
    /**
     * 关联的机构ID（审核通过后）
     */
    private Long institutionId;
} 