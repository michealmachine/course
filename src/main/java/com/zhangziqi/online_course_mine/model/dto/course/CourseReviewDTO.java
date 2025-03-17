package com.zhangziqi.online_course_mine.model.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 课程审核DTO
 * 用于审核员提交审核结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "课程审核DTO")
public class CourseReviewDTO {
    
    /**
     * 审核意见/拒绝原因
     */
    @NotBlank(message = "审核意见不能为空")
    @Size(max = 500, message = "审核意见不能超过500个字符")
    @Schema(description = "审核意见/拒绝原因", required = true, example = "内容质量不符合要求")
    private String reason;
} 