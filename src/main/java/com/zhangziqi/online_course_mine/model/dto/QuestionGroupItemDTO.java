package com.zhangziqi.online_course_mine.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 题目组项数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionGroupItemDTO {
    
    /**
     * 题目组项ID（创建时为null，更新时可能需要提供）
     */
    private Long id;
    
    /**
     * 题目组ID
     */
    @NotNull(message = "题目组ID不能为空")
    private Long groupId;
    
    /**
     * 题目ID
     */
    @NotNull(message = "题目ID不能为空")
    private Long questionId;
    
    /**
     * 在组中的顺序
     */
    @NotNull(message = "顺序不能为空")
    @Min(value = 0, message = "顺序必须大于等于0")
    private Integer orderIndex;
    
    /**
     * 难度级别（可覆盖题目原始难度）
     */
    private Integer difficulty;
    
    /**
     * 分值（可覆盖题目原始分值）
     */
    private Integer score;
} 