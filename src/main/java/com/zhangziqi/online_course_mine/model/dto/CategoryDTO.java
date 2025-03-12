package com.zhangziqi.online_course_mine.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 分类数据传输对象
 */
@Data
public class CategoryDTO {

    /**
     * 分类ID（更新时使用）
     */
    private Long id;

    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 100, message = "分类名称不能超过100个字符")
    private String name;

    /**
     * 分类编码
     */
    @NotBlank(message = "分类编码不能为空")
    @Size(max = 50, message = "分类编码不能超过50个字符")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "分类编码只能包含字母、数字、下划线和连字符")
    private String code;

    /**
     * 分类描述
     */
    @Size(max = 500, message = "分类描述不能超过500个字符")
    private String description;

    /**
     * 父分类ID
     */
    private Long parentId;

    /**
     * 排序索引
     */
    private Integer orderIndex;

    /**
     * 是否启用
     */
    private Boolean enabled = true;

    /**
     * 分类图标
     */
    @Size(max = 255, message = "图标路径不能超过255个字符")
    private String icon;
} 