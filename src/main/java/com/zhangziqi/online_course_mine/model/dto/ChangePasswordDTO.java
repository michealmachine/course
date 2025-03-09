package com.zhangziqi.online_course_mine.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 密码修改DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "密码修改请求")
public class ChangePasswordDTO {

    @NotBlank(message = "旧密码不能为空")
    @Schema(description = "旧密码", example = "oldPassword123")
    private String oldPassword;
    
    @NotBlank(message = "新密码不能为空")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,20}$", 
            message = "密码必须包含大小写字母和数字，长度为8-20位")
    @Schema(description = "新密码", example = "newPassword123")
    private String newPassword;
    
    @NotBlank(message = "确认密码不能为空")
    @Schema(description = "确认密码", example = "newPassword123")
    private String confirmPassword;
} 