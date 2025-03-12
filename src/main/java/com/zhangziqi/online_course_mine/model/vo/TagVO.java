package com.zhangziqi.online_course_mine.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签展示对象
 */
@Data
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
} 