package com.zhangziqi.online_course_mine.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 权限实体类
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "permissions")
@EqualsAndHashCode(callSuper = true)
public class Permission extends BaseEntity {

    /**
     * 权限名称
     */
    @Column(nullable = false, length = 50)
    private String name;

    /**
     * 权限编码
     */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /**
     * 权限描述
     */
    @Column(length = 255)
    private String description;

    /**
     * 资源路径
     */
    @Column(length = 255)
    private String url;

    /**
     * HTTP方法
     */
    @Column(length = 10)
    private String method;
} 