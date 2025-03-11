package com.zhangziqi.online_course_mine.controller;

import com.zhangziqi.online_course_mine.model.dto.InstitutionRegisterDTO;
import com.zhangziqi.online_course_mine.model.vo.Result;
import com.zhangziqi.online_course_mine.service.CaptchaService;
import com.zhangziqi.online_course_mine.service.EmailService;
import com.zhangziqi.online_course_mine.service.InstitutionAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * 机构用户注册控制器
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/institution")
@Tag(name = "机构用户认证", description = "机构用户注册相关接口")
public class InstitutionAuthController {
    
    private final InstitutionAuthService institutionAuthService;
    private final CaptchaService captchaService;
    private final EmailService emailService;
    
    /**
     * 机构用户注册
     *
     * @param registerDTO 注册参数
     * @return 结果
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "机构用户注册", description = "使用机构注册码注册机构用户")
    public Result<Void> registerInstitutionMember(@Valid @RequestBody InstitutionRegisterDTO registerDTO) {
        log.info("收到机构用户注册请求: username={}, email={}, institutionCode={}, captchaKey={}", 
                registerDTO.getUsername(), registerDTO.getEmail(), 
                registerDTO.getInstitutionCode(), registerDTO.getCaptchaKey());
        
        // 验证图形验证码
        boolean captchaValid = captchaService.validateCaptcha(registerDTO.getCaptchaKey(), registerDTO.getCaptchaCode());
        log.info("图形验证码验证结果: {}, key={}, code={}", captchaValid, registerDTO.getCaptchaKey(), registerDTO.getCaptchaCode());
        if (!captchaValid) {
            log.warn("图形验证码验证失败: key={}, code={}", registerDTO.getCaptchaKey(), registerDTO.getCaptchaCode());
            return Result.fail("验证码错误");
        }
        
        // 验证邮箱验证码
        boolean emailCodeValid = emailService.validateVerificationCode(registerDTO.getEmail(), registerDTO.getEmailCode());
        log.info("邮箱验证码验证结果: {}, email={}, code={}", emailCodeValid, registerDTO.getEmail(), registerDTO.getEmailCode());
        if (!emailCodeValid) {
            log.warn("邮箱验证码验证失败: email={}, code={}", registerDTO.getEmail(), registerDTO.getEmailCode());
            return Result.fail("邮箱验证码错误或已过期");
        }
        
        log.info("开始处理机构用户注册: username={}, institutionCode={}", registerDTO.getUsername(), registerDTO.getInstitutionCode());
        try {
            institutionAuthService.registerWithInstitutionCode(registerDTO);
            log.info("机构用户注册成功: username={}", registerDTO.getUsername());
            return Result.success();
        } catch (Exception e) {
            log.error("机构用户注册失败: username={}, 原因: {}", registerDTO.getUsername(), e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }
} 