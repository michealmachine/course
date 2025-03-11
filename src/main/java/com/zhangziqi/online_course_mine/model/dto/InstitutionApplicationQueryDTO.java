package com.zhangziqi.online_course_mine.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 机构申请查询DTO
 */
@Data
@Schema(description = "机构申请查询参数")
public class InstitutionApplicationQueryDTO {

    @Schema(description = "申请ID", example = "APP123456")
    private String applicationId;
    
    @Schema(description = "机构名称", example = "示例教育机构")
    private String name;
    
    @Schema(description = "联系人", example = "张三")
    private String contactPerson;
    
    @Schema(description = "联系邮箱", example = "contact@example.com")
    private String contactEmail;
    
    @Schema(description = "状态：0-待审核，1-已通过，2-已拒绝", example = "0")
    private Integer status;
    
    @Schema(description = "页码", defaultValue = "1")
    private Integer pageNum = 1;
    
    @Schema(description = "每页条数", defaultValue = "10")
    private Integer pageSize = 10;
} 