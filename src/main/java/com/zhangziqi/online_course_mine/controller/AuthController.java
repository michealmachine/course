package com.zhangziqi.online_course_mine.controller;

import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.model.dto.*;
import com.zhangziqi.online_course_mine.model.vo.Result;
import com.zhangziqi.online_course_mine.service.AuthService;
import com.zhangziqi.online_course_mine.service.CaptchaService;
import com.zhangziqi.online_course_mine.service.EmailService;
import com.zhangziqi.online_course_mine.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.UUID;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "认证接口", description = "包括注册、登录、刷新令牌等接口")
public class AuthController {

    private final AuthService authService;
    private final CaptchaService captchaService;
    private final EmailService emailService;
    private final UserService userService;

    /**
     * 获取验证码key
     */
    @GetMapping("/captcha/key")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "获取验证码key",
        description = "获取验证码key，用于后续获取验证码图片"
    )
    public Result<String> getCaptchaKey() {
        return Result.success(UUID.randomUUID().toString());
    }

    /**
     * 获取验证码图片
     */
    @GetMapping("/captcha/image/{key}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "获取验证码图片",
        description = "根据验证码key获取对应的验证码图片"
    )
    public void getCaptchaImage(
        @Parameter(description = "验证码key")
        @PathVariable String key,
        HttpServletResponse response
    ) throws IOException {
        BufferedImage image = captchaService.generateCaptcha(key);

        response.setContentType(MediaType.IMAGE_JPEG_VALUE);
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store, no-cache, must-revalidate");
        response.setHeader(HttpHeaders.PRAGMA, "no-cache");
        response.setDateHeader(HttpHeaders.EXPIRES, 0);

        ImageIO.write(image, "jpg", response.getOutputStream());
    }

    /**
     * 发送邮箱验证码
     *
     * @param emailVerificationDTO 邮箱验证码请求
     * @return 结果
     */
    @PostMapping("/email-verification-code")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "发送邮箱验证码", description = "发送邮箱验证码（用于用户注册）")
    public Result<Void> sendEmailVerificationCode(@Valid @RequestBody EmailVerificationDTO emailVerificationDTO) {
        // 验证图形验证码
        if (!captchaService.validateCaptcha(emailVerificationDTO.getCaptchaKey(), emailVerificationDTO.getCaptchaCode())) {
            return Result.fail("验证码错误");
        }

        // 生成邮箱验证码
        String code = emailService.generateVerificationCode();
        // 发送验证码
        emailService.sendVerificationCode(emailVerificationDTO.getEmail(), code);
        // 保存验证码到Redis
        emailService.saveVerificationCode(emailVerificationDTO.getEmail(), code);

        return Result.success();
    }

    /**
     * 发送邮箱更新验证码
     *
     * @param emailVerificationDTO 邮箱验证码请求
     * @return 结果
     */
    @PostMapping("/email-update-code")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "发送邮箱更新验证码", description = "发送邮箱更新验证码（用于更换邮箱）")
    public Result<Void> sendEmailUpdateCode(@Valid @RequestBody EmailVerificationDTO emailVerificationDTO) {
        // 验证图形验证码
        if (!captchaService.validateCaptcha(emailVerificationDTO.getCaptchaKey(), emailVerificationDTO.getCaptchaCode())) {
            return Result.fail("验证码错误");
        }

        // 生成邮箱验证码
        String code = emailService.generateVerificationCode();
        // 发送更新验证码邮件
        emailService.sendEmailUpdateCode(emailVerificationDTO.getEmail(), code);
        // 保存验证码到Redis
        emailService.saveVerificationCode(emailVerificationDTO.getEmail(), code);

        return Result.success();
    }

    /**
     * 发送密码重置验证码
     *
     * @param emailVerificationDTO 邮箱验证码请求
     * @return 结果
     */
    @PostMapping("/password-reset-code")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "发送密码重置验证码", description = "发送密码重置验证码（用于忘记密码）")
    public Result<Void> sendPasswordResetCode(@Valid @RequestBody EmailVerificationDTO emailVerificationDTO) {
        // 验证图形验证码
        if (!captchaService.validateCaptcha(emailVerificationDTO.getCaptchaKey(), emailVerificationDTO.getCaptchaCode())) {
            return Result.fail("验证码错误");
        }

        // 检查邮箱是否存在
        if (!userService.existsByEmail(emailVerificationDTO.getEmail())) {
            return Result.fail("该邮箱未注册");
        }

        // 生成邮箱验证码
        String code = emailService.generateVerificationCode();
        // 发送验证码
        emailService.sendVerificationCode(emailVerificationDTO.getEmail(), code);
        // 保存验证码到Redis
        emailService.saveVerificationCode(emailVerificationDTO.getEmail(), code);

        return Result.success();
    }

    /**
     * 用户注册
     *
     * @param registerDTO 注册请求
     * @return 结果
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "用户注册", description = "注册新用户")
    public Result<Void> register(@Valid @RequestBody RegisterDTO registerDTO) {
        authService.register(registerDTO);
        return Result.success();
    }

    /**
     * 用户登录
     *
     * @param loginDTO 登录请求
     * @return JWT令牌
     */
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "用户登录", description = "用户登录获取JWT令牌")
    public Result<JwtTokenDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
        JwtTokenDTO jwtTokenDTO = authService.login(loginDTO);
        return Result.success(jwtTokenDTO);
    }

    /**
     * 刷新令牌
     *
     * @param refreshTokenDTO 刷新令牌请求
     * @return JWT令牌
     */
    @PostMapping("/refresh-token")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "刷新令牌", description = "刷新JWT令牌")
    public Result<JwtTokenDTO> refreshToken(@Valid @RequestBody RefreshTokenDTO refreshTokenDTO) {
        JwtTokenDTO jwtTokenDTO = authService.refreshToken(refreshTokenDTO);
        return Result.success(jwtTokenDTO);
    }

    /**
     * 注销
     *
     * @param request 请求
     * @return 结果
     */
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "注销", description = "用户注销")
    public Result<Void> logout(HttpServletRequest request) {
        // 从请求头中获取JWT令牌
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            authService.logout(token);
        }
        return Result.success();
    }

    /**
     * 重置密码
     *
     * @param passwordResetDTO 密码重置请求
     * @return 结果
     */
    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "重置密码", description = "通过邮箱验证码重置密码")
    public Result<Void> resetPassword(@Valid @RequestBody PasswordResetDTO passwordResetDTO) {
        log.info("重置密码: email={}", passwordResetDTO.getEmail());

        try {
            boolean result = userService.resetPassword(passwordResetDTO.getEmail(), passwordResetDTO.getEmailCode());
            if (result) {
                return Result.success();
            } else {
                return Result.fail("密码重置失败");
            }
        } catch (BusinessException e) {
            log.warn("密码重置失败: {}", e.getMessage());
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            log.error("密码重置异常", e);
            return Result.fail("系统异常，请稍后重试");
        }
    }
}