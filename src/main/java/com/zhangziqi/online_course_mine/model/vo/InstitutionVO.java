package com.zhangziqi.online_course_mine.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 机构VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "机构信息")
public class InstitutionVO {

    @Schema(description = "ID")
    private Long id;
    
    @Schema(description = "机构名称")
    private String name;
    
    @Schema(description = "机构Logo")
    private String logo;
    
    @Schema(description = "机构描述")
    private String description;
    
    @Schema(description = "状态：0-待审核，1-正常，2-禁用")
    private Integer status;
    
    @Schema(description = "联系人")
    private String contactPerson;
    
    @Schema(description = "联系电话")
    private String contactPhone;
    
    @Schema(description = "联系邮箱")
    private String contactEmail;
    
    @Schema(description = "地址")
    private String address;
    
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
} 