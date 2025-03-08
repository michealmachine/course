package com.zhangziqi.online_course_mine.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 邮箱验证码请求DTO
 */
@Data
@Schema(description = "邮箱验证码请求")
public class EmailVerificationDTO {

    /**
     * 邮箱
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;

    /**
     * 验证码Key
     */
    @NotBlank(message = "验证码Key不能为空")
    @Schema(description = "验证码Key", example = "123456")
    private String captchaKey;
    
    /**
     * 验证码
     */
    @NotBlank(message = "验证码不能为空")
    @Schema(description = "验证码", example = "1234")
    private String captchaCode;
} 