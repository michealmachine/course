package com.zhangziqi.online_course_mine.model.dto.chapter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

/**
 * 章节排序数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChapterOrderDTO {
    
    /**
     * 章节ID
     */
    @NotNull(message = "章节ID不能为空")
    private Long id;
    
    /**
     * 排序索引
     */
    @NotNull(message = "排序索引不能为空")
    @Min(value = 0, message = "排序索引不能为负数")
    private Integer orderIndex;
} 