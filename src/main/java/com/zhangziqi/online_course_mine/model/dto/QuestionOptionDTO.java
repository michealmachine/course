package com.zhangziqi.online_course_mine.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 题目选项数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionOptionDTO {
    
    /**
     * 选项ID（创建时为null，更新时可能需要提供）
     */
    private Long id;
    
    /**
     * 选项内容
     */
    @NotBlank(message = "选项内容不能为空")
    @Size(max = 1000, message = "选项内容长度不能超过1000个字符")
    private String content;
    
    /**
     * 是否为正确选项
     */
    @NotNull(message = "必须指定选项是否正确")
    private Boolean isCorrect;
    
    /**
     * 选项顺序
     */
    @NotNull(message = "选项顺序不能为空")
    private Integer orderIndex;
} 