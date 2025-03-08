package com.zhangziqi.online_course_mine.service.impl;

import com.zhangziqi.online_course_mine.config.security.JwtConfig;
import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.model.dto.JwtTokenDTO;
import com.zhangziqi.online_course_mine.model.dto.LoginDTO;
import com.zhangziqi.online_course_mine.model.dto.RefreshTokenDTO;
import com.zhangziqi.online_course_mine.model.dto.RegisterDTO;
import com.zhangziqi.online_course_mine.security.jwt.JwtTokenProvider;
import com.zhangziqi.online_course_mine.security.jwt.TokenBlacklistService;
import com.zhangziqi.online_course_mine.service.AuthService;
import com.zhangziqi.online_course_mine.service.CaptchaService;
import com.zhangziqi.online_course_mine.service.EmailService;
import com.zhangziqi.online_course_mine.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;
    private final CaptchaService captchaService;
    private final EmailService emailService;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtConfig jwtConfig;

    /**
     * 用户注册
     *
     * @param registerDTO 注册请求
     */
    @Override
    @Transactional
    public void register(RegisterDTO registerDTO) {
        // 验证邮箱验证码
        if (!emailService.validateVerificationCode(registerDTO.getEmail(), registerDTO.getEmailCode())) {
            throw new BusinessException("邮箱验证码错误或已过期");
        }

        // 注册用户
        userService.register(registerDTO);
        log.info("用户注册成功: {}", registerDTO.getUsername());
    }

    /**
     * 用户登录
     *
     * @param loginDTO 登录请求
     * @return JWT令牌
     */
    @Override
    @Transactional
    public JwtTokenDTO login(LoginDTO loginDTO) {
        // 验证验证码
        if (!captchaService.validateCaptcha(loginDTO.getCaptchaKey(), loginDTO.getCaptchaCode())) {
            throw new BusinessException("验证码错误");
        }

        // 认证用户
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 更新最后登录时间
        userService.updateLastLoginTime(loginDTO.getUsername());

        // 生成JWT令牌
        JwtTokenDTO jwtTokenDTO = tokenProvider.createToken(authentication);
        log.info("用户登录成功: {}", loginDTO.getUsername());
        return jwtTokenDTO;
    }

    /**
     * 刷新令牌
     *
     * @param refreshTokenDTO 刷新令牌请求
     * @return 新的JWT令牌
     */
    @Override
    @Transactional
    public JwtTokenDTO refreshToken(RefreshTokenDTO refreshTokenDTO) {
        String refreshToken = refreshTokenDTO.getRefreshToken();

        // 验证刷新令牌
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new BusinessException("刷新令牌无效或已过期");
        }

        // 检查刷新令牌是否在黑名单中
        if (tokenBlacklistService.isBlacklisted(refreshToken)) {
            throw new BusinessException("刷新令牌已被注销");
        }

        // 刷新令牌
        JwtTokenDTO jwtTokenDTO = tokenProvider.refreshToken(refreshToken);
        log.info("刷新令牌成功: {}", tokenProvider.getUsernameFromToken(refreshToken));
        return jwtTokenDTO;
    }

    /**
     * 注销
     *
     * @param token 访问令牌
     */
    @Override
    public void logout(String token) {
        if (token == null || token.isEmpty()) {
            return;
        }

        // 从令牌中提取用户名
        String username = tokenProvider.getUsernameFromToken(token);

        // 将令牌加入黑名单
        tokenBlacklistService.addToBlacklist(token, jwtConfig.getAccessTokenExpiration());
        log.info("用户注销成功: {}", username);
    }
} 