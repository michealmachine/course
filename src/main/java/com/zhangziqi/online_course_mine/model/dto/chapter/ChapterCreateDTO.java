package com.zhangziqi.online_course_mine.model.dto.chapter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;

/**
 * 章节创建数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChapterCreateDTO {
    
    /**
     * 章节标题
     */
    @NotBlank(message = "章节标题不能为空")
    @Size(max = 200, message = "章节标题长度不能超过200个字符")
    private String title;
    
    /**
     * 章节描述
     */
    @Size(max = 1000, message = "章节描述长度不能超过1000个字符")
    private String description;
    
    /**
     * 所属课程ID
     */
    @NotNull(message = "课程ID不能为空")
    private Long courseId;
    
    /**
     * 排序索引
     */
    @Min(value = 0, message = "排序索引不能为负数")
    private Integer orderIndex;
    
    /**
     * 访问类型 (0-免费试学, 1-付费访问)
     */
    private Integer accessType;
    
    /**
     * 学习时长估计(分钟)
     */
    @Min(value = 0, message = "学习时长不能为负数")
    private Integer estimatedMinutes;
} 