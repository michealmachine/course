package com.zhangziqi.online_course_mine.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 机构查询参数DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "机构查询参数")
public class InstitutionQueryDTO {
    
    /**
     * 机构名称（模糊查询）
     */
    @Schema(description = "机构名称（模糊查询）")
    private String name;
    
    /**
     * 机构状态（0-禁用，1-正常）
     */
    @Schema(description = "机构状态（0-禁用，1-正常）")
    private Integer status;
    
    /**
     * 页码
     */
    @Schema(description = "页码", defaultValue = "0")
    @Builder.Default
    private Integer page = 0;
    
    /**
     * 每页大小
     */
    @Schema(description = "每页大小", defaultValue = "10")
    @Builder.Default
    private Integer size = 10;
}
