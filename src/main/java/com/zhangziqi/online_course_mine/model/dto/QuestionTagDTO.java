package com.zhangziqi.online_course_mine.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 题目标签数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionTagDTO {
    
    /**
     * 标签ID（创建时为null，更新时必须提供）
     */
    private Long id;
    
    /**
     * 所属机构ID
     */
    @NotNull(message = "机构ID不能为空")
    private Long institutionId;
    
    /**
     * 标签名称
     */
    @NotBlank(message = "标签名称不能为空")
    @Size(max = 50, message = "标签名称长度不能超过50个字符")
    private String name;
} 