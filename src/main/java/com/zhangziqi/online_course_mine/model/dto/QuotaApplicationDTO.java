package com.zhangziqi.online_course_mine.model.dto;

import com.zhangziqi.online_course_mine.model.enums.QuotaType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 存储配额申请DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "存储配额申请DTO")
public class QuotaApplicationDTO {
    
    @NotNull(message = "配额类型不能为空")
    @Schema(description = "配额类型", required = true)
    private QuotaType quotaType;
    
    @NotNull(message = "申请容量不能为空")
    @Min(value = 1, message = "申请容量必须大于0")
    @Schema(description = "申请容量（字节）", required = true)
    private Long requestedBytes;
    
    @NotNull(message = "申请原因不能为空")
    @Size(min = 10, max = 500, message = "申请原因长度必须在10-500字符之间")
    @Schema(description = "申请原因", required = true)
    private String reason;
} 