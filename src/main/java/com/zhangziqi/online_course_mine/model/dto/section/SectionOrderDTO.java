package com.zhangziqi.online_course_mine.model.dto.section;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

/**
 * 小节排序数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionOrderDTO {
    
    /**
     * 小节ID
     */
    @NotNull(message = "小节ID不能为空")
    private Long id;
    
    /**
     * 排序索引
     */
    @NotNull(message = "排序索引不能为空")
    @Min(value = 0, message = "排序索引不能为负数")
    private Integer orderIndex;
} 