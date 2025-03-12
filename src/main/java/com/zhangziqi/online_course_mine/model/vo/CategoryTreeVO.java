package com.zhangziqi.online_course_mine.model.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 分类树形结构展示对象
 */
@Data
public class CategoryTreeVO {

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
     * 子分类
     */
    private List<CategoryTreeVO> children = new ArrayList<>();

    /**
     * 完整路径
     */
    private String fullPath;
} 