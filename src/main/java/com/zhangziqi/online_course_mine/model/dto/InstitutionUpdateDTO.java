package com.zhangziqi.online_course_mine.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 机构信息更新DTO
 */
@Data
@Schema(description = "机构信息更新参数")
public class InstitutionUpdateDTO {
    
    @NotBlank(message = "机构名称不能为空")
    @Schema(description = "机构名称", example = "示例教育机构")
    private String name;
    
    @Size(max = 500, message = "机构描述最多500字")
    @Schema(description = "机构描述", example = "专注于提供高质量的在线课程...")
    private String description;
    
    @NotBlank(message = "联系人不能为空")
    @Schema(description = "联系人", example = "张三")
    private String contactPerson;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "联系电话", example = "13800138000")
    private String contactPhone;
    
    @Schema(description = "地址", example = "北京市朝阳区xxx街道xxx号")
    private String address;
} 