package com.zhangziqi.online_course_mine.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 机构用户注册DTO
 */
@Data
@Schema(description = "机构用户注册参数")
public class InstitutionRegisterDTO {
    
    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 20, message = "用户名长度为4-20位")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    @Schema(description = "用户名", example = "institution_user")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度为6-20位")
    @Schema(description = "密码", example = "password123")
    private String password;
    
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", example = "user@example.com")
    private String email;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "13800138000")
    private String phone;
    
    @NotBlank(message = "机构注册码不能为空")
    @Schema(description = "机构注册码", example = "ABC12345")
    private String institutionCode;
    
    @NotBlank(message = "验证码Key不能为空")
    @Schema(description = "验证码Key", example = "7d8f3e")
    private String captchaKey;
    
    @NotBlank(message = "验证码不能为空")
    @Schema(description = "验证码", example = "A2B3")
    private String captchaCode;
    
    @NotBlank(message = "邮箱验证码不能为空")
    @Size(min = 6, max = 6, message = "邮箱验证码长度必须为6位")
    @Pattern(regexp = "^\\d{6}$", message = "邮箱验证码必须为6位数字")
    @Schema(description = "邮箱验证码", example = "123456")
    private String emailCode;
} 