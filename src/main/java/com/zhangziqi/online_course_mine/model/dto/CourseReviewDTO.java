package com.zhangziqi.online_course_mine.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 课程审核DTO
 * 用于提交审核意见
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "课程审核DTO")
public class CourseReviewDTO {
    
    /**
     * 审核意见或拒绝原因
     */
    @NotBlank(message = "审核意见不能为空")
    @Size(max = 500, message = "审核意见最多500个字符")
    @Schema(description = "审核意见或拒绝原因", example = "内容质量不符合要求，需要进一步完善")
    private String reason;
    
    /**
     * 审核评分（可选）
     */
    @Schema(description = "审核评分（1-5）", example = "4")
    private Integer score;
} 