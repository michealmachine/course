package com.zhangziqi.online_course_mine.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 密码重置请求DTO
 */
@Data
@Schema(description = "密码重置请求")
public class PasswordResetDTO {

    /**
     * 邮箱
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", example = "user@example.com")
    private String email;

    /**
     * 邮箱验证码
     */
    @NotBlank(message = "邮箱验证码不能为空")
    @Schema(description = "邮箱验证码", example = "123456")
    private String emailCode;
}
