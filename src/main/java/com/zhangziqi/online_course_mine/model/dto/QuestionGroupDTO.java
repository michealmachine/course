package com.zhangziqi.online_course_mine.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 题目组数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionGroupDTO {
    
    /**
     * 题目组ID（创建时为null，更新时必须提供）
     */
    private Long id;
    
    /**
     * 所属机构ID
     */
    @NotNull(message = "机构ID不能为空")
    private Long institutionId;
    
    /**
     * 题目组名称
     */
    @NotBlank(message = "题目组名称不能为空")
    @Size(max = 100, message = "题目组名称长度不能超过100个字符")
    private String name;
    
    /**
     * 题目组描述
     */
    @Size(max = 500, message = "题目组描述长度不能超过500个字符")
    private String description;
} 