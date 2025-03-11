package com.zhangziqi.online_course_mine.model.dto.media;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分片完成通知DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartCompletionDTO {
    
    @NotNull(message = "分片编号不能为空")
    @Min(value = 1, message = "分片编号必须大于0")
    private Integer partNumber;  // 分片编号
    
    @NotBlank(message = "ETag不能为空")
    private String eTag;         // 分片ETag
} 