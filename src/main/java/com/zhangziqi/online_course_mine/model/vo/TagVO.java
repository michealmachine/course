package com.zhangziqi.online_course_mine.model.vo;

import com.zhangziqi.online_course_mine.model.entity.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 标签值对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagVO {

    /**
     * 标签ID
     */
    private Long id;

    /**
     * 标签名称
     */
    private String name;

    /**
     * 标签描述
     */
    private String description;

    /**
     * 使用次数
     */
    private Integer useCount;

    /**
     * 相关课程数量
     */
    private Integer courseCount;

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
    public static TagVO fromEntity(Tag tag) {
        if (tag == null) {
            return null;
        }
        
        return TagVO.builder()
                .id(tag.getId())
                .name(tag.getName())
                .useCount(tag.getUseCount())
                .createdAt(tag.getCreatedAt())
                .updatedAt(tag.getUpdatedAt())
                .build();
    }
} 