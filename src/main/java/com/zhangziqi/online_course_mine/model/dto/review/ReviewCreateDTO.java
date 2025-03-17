package com.zhangziqi.online_course_mine.model.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建课程评论DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCreateDTO {
    
    /**
     * 课程ID
     */
    @NotNull(message = "课程ID不能为空")
    private Long courseId;
    
    /**
     * 评分 (1-5)
     */
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最小为1")
    @Max(value = 5, message = "评分最大为5")
    private Integer rating;
    
    /**
     * 评价内容
     */
    @Size(max = 1000, message = "评价内容不能超过1000个字符")
    private String content;
} 