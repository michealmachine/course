package com.zhangziqi.online_course_mine.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 机构实体类
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "institutions")
public class Institution extends BaseEntity {

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
     * 状态（0-待审核，1-正常，2-禁用）
     */
    @Builder.Default
    private Integer status = 0;

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
} 