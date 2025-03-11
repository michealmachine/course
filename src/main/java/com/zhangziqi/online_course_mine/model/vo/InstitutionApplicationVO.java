package com.zhangziqi.online_course_mine.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 机构申请VO
 */
@Data
@Schema(description = "机构申请信息")
public class InstitutionApplicationVO {
    
    @Schema(description = "ID")
    private Long id;
    
    @Schema(description = "申请ID")
    private String applicationId;
    
    @Schema(description = "机构名称")
    private String name;
    
    @Schema(description = "机构Logo")
    private String logo;
    
    @Schema(description = "机构描述")
    private String description;
    
    @Schema(description = "联系人")
    private String contactPerson;
    
    @Schema(description = "联系电话")
    private String contactPhone;
    
    @Schema(description = "联系邮箱")
    private String contactEmail;
    
    @Schema(description = "地址")
    private String address;
    
    @Schema(description = "状态：0-待审核，1-已通过，2-已拒绝")
    private Integer status;
    
    @Schema(description = "审核结果备注")
    private String reviewComment;
    
    @Schema(description = "审核人ID")
    private Long reviewerId;
    
    @Schema(description = "审核时间")
    private LocalDateTime reviewedAt;
    
    @Schema(description = "关联的机构ID")
    private Long institutionId;
    
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
} 