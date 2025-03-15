package com.zhangziqi.online_course_mine.model.dto.section;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;

/**
 * 小节创建数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionCreateDTO {
    
    /**
     * 小节标题
     */
    @NotBlank(message = "小节标题不能为空")
    @Size(max = 200, message = "小节标题长度不能超过200个字符")
    private String title;
    
    /**
     * 小节描述
     */
    @Size(max = 1000, message = "小节描述长度不能超过1000个字符")
    private String description;
    
    /**
     * 所属章节ID
     */
    @NotNull(message = "章节ID不能为空")
    private Long chapterId;
    
    /**
     * 排序索引
     */
    @Min(value = 0, message = "排序索引不能为负数")
    private Integer orderIndex;
    
    /**
     * 内容类型 (video, document, audio, text, image, mixed)
     */
    @NotBlank(message = "内容类型不能为空")
    private String contentType;
} 