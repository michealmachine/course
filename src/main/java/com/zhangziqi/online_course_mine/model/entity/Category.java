package com.zhangziqi.online_course_mine.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * 课程分类实体类
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "categories")
@EqualsAndHashCode(callSuper = true)
public class Category extends BaseEntity {

    /**
     * 分类名称
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 分类编码（唯一标识）
     */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /**
     * 分类描述
     */
    @Column(length = 500)
    private String description;

    /**
     * 父分类
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    /**
     * 子分类
     */
    @OneToMany(mappedBy = "parent")
    @Builder.Default
    private List<Category> children = new ArrayList<>();

    /**
     * 层级（1-一级分类，2-二级分类，以此类推）
     */
    private Integer level;

    /**
     * 排序索引
     */
    private Integer orderIndex;

    /**
     * 是否启用
     */
    @Builder.Default
    private Boolean enabled = true;

    /**
     * 分类图标
     */
    @Column(length = 255)
    private String icon;

    /**
     * 关联的课程
     */
    @OneToMany(mappedBy = "category")
    @Builder.Default
    private List<Course> courses = new ArrayList<>();
    
    /**
     * 获取分类的完整路径
     * @return 分类路径（如：计算机科学/编程语言/Java）
     */
    @Transient
    public String getFullPath() {
        if (parent == null) {
            return name;
        }
        return parent.getFullPath() + "/" + name;
    }
} 