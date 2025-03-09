package com.zhangziqi.online_course_mine.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 邮箱更新DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "邮箱更新请求")
public class EmailUpdateDTO {

    @NotBlank(message = "新邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "新邮箱", example = "newemail@example.com")
    private String newEmail;
    
    @NotBlank(message = "验证码不能为空")
    @Schema(description = "邮箱验证码", example = "123456")
    private String emailCode;
    
    @NotBlank(message = "当前密码不能为空")
    @Schema(description = "当前密码", example = "password123")
    private String password;
} 