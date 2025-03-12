package com.zhangziqi.online_course_mine.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分类展示对象
 */
@Data
public class CategoryVO {

    /**
     * 分类ID
     */
    private Long id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 分类编码
     */
    private String code;

    /**
     * 分类描述
     */
    private String description;

    /**
     * 父分类ID
     */
    private Long parentId;

    /**
     * 父分类名称
     */
    private String parentName;

    /**
     * 层级
     */
    private Integer level;

    /**
     * 排序索引
     */
    private Integer orderIndex;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 分类图标
     */
    private String icon;

    /**
     * 课程数量
     */
    private Long courseCount;

    /**
     * 子分类数量
     */
    private Long childrenCount;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
} 