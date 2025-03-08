package com.zhangziqi.online_course_mine.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 权限数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "权限数据")
public class PermissionDTO {

    @Schema(description = "权限ID")
    private Long id;

    @NotBlank(message = "权限名称不能为空")
    @Size(min = 2, max = 50, message = "权限名称长度为2-50个字符")
    @Schema(description = "权限名称", example = "用户查询")
    private String name;

    @NotBlank(message = "权限编码不能为空")
    @Size(min = 4, max = 50, message = "权限编码长度为4-50个字符")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "权限编码只能包含大写字母、数字和下划线")
    @Schema(description = "权限编码", example = "USER_QUERY")
    private String code;

    @Schema(description = "权限描述")
    private String description;

    @Schema(description = "资源URL")
    private String url;

    @Schema(description = "HTTP方法", example = "GET")
    private String method;
} 