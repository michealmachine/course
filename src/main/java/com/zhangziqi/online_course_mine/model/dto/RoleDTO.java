package com.zhangziqi.online_course_mine.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * 角色数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "角色数据")
public class RoleDTO {

    @Schema(description = "角色ID")
    private Long id;

    @NotBlank(message = "角色名称不能为空")
    @Size(min = 2, max = 50, message = "角色名称长度为2-50个字符")
    @Schema(description = "角色名称", example = "系统管理员")
    private String name;

    @NotBlank(message = "角色编码不能为空")
    @Size(min = 4, max = 50, message = "角色编码长度为4-50个字符")
    @Pattern(regexp = "^ROLE_[A-Z0-9_]+$", message = "角色编码必须以ROLE_开头，且只能包含大写字母、数字和下划线")
    @Schema(description = "角色编码", example = "ROLE_ADMIN")
    private String code;

    @Schema(description = "角色描述")
    private String description;
    
    @Schema(description = "权限ID列表")
    private Set<Long> permissionIds;
} 