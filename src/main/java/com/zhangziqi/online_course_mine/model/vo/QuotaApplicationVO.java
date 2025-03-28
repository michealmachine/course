package com.zhangziqi.online_course_mine.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zhangziqi.online_course_mine.model.enums.QuotaType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 存储配额申请VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "存储配额申请VO")
public class QuotaApplicationVO {
    
    @Schema(description = "申请ID")
    private Long id;
    
    @Schema(description = "申请编号")
    private String applicationId;
    
    @Schema(description = "机构ID")
    private Long institutionId;
    
    @Schema(description = "机构名称")
    private String institutionName;
    
    @Schema(description = "申请人ID")
    private Long applicantId;
    
    @Schema(description = "申请人用户名")
    private String applicantUsername;
    
    @Schema(description = "配额类型")
    private QuotaType quotaType;
    
    @Schema(description = "申请容量（字节）")
    private Long requestedBytes;
    
    @Schema(description = "申请原因")
    private String reason;
    
    @Schema(description = "申请状态: 0-待审核, 1-已通过, 2-已拒绝")
    private Integer status;
    
    @Schema(description = "审核人ID")
    private Long reviewerId;
    
    @Schema(description = "审核人用户名")
    private String reviewerUsername;
    
    @Schema(description = "审核时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reviewedAt;
    
    @Schema(description = "审核意见")
    private String reviewComment;
    
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
} 