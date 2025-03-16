package com.zhangziqi.online_course_mine.model.vo;

import com.zhangziqi.online_course_mine.model.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 分类值对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
     * 分类图标
     */
    private String iconUrl;

    /**
     * 排序索引
     */
    private Integer orderIndex;

    /**
     * 课程数量
     */
    private Integer courseCount;

    /**
     * 子分类数量
     */
    private Integer childrenCount;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 从实体转换为VO
     */
    public static CategoryVO fromEntity(Category category) {
        if (category == null) {
            return null;
        }
        
        CategoryVO.CategoryVOBuilder builder = CategoryVO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .level(category.getLevel())
                .iconUrl(category.getIcon())
                .orderIndex(category.getOrderIndex())
                .courseCount(category.getCourses() != null ? category.getCourses().size() : 0)
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt());
        
        // 设置父分类信息
        if (category.getParent() != null) {
            builder.parentId(category.getParent().getId());
            builder.parentName(category.getParent().getName());
        }
        
        return builder.build();
    }
} 