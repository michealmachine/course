package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.config.security.JwtConfig;
import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.model.dto.JwtTokenDTO;
import com.zhangziqi.online_course_mine.model.dto.LoginDTO;
import com.zhangziqi.online_course_mine.model.dto.RefreshTokenDTO;
import com.zhangziqi.online_course_mine.model.dto.RegisterDTO;
import com.zhangziqi.online_course_mine.model.entity.User;
import com.zhangziqi.online_course_mine.repository.UserRepository;
import com.zhangziqi.online_course_mine.security.jwt.JwtTokenProvider;
import com.zhangziqi.online_course_mine.security.jwt.TokenBlacklistService;
import com.zhangziqi.online_course_mine.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.BDDMockito.given;

/**
 * 认证服务测试
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test") // 使用测试环境配置
public class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private UserService userService;

    @Mock
    private CaptchaService captchaService;

    @Mock
    private EmailService emailService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private JwtConfig jwtConfig;

    @Mock
    private Authentication authentication;
    
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterDTO registerDTO;
    private LoginDTO loginDTO;
    private RefreshTokenDTO refreshTokenDTO;
    private JwtTokenDTO jwtTokenDTO;
    private User testUser;

    @BeforeEach
    void setUp() {
        // 初始化注册DTO
        registerDTO = new RegisterDTO();
        registerDTO.setUsername("testuser");
        registerDTO.setPassword("password123");
        registerDTO.setEmail("test@example.com");
        registerDTO.setCaptchaKey("captcha-key");
        registerDTO.setCaptchaCode("1234");
        registerDTO.setEmailCode("123456");

        // 初始化登录DTO
        loginDTO = new LoginDTO();
        loginDTO.setUsername("testuser");
        loginDTO.setPassword("password123");
        loginDTO.setCaptchaKey("captcha-key");
        loginDTO.setCaptchaCode("1234");

        // 初始化刷新令牌DTO
        refreshTokenDTO = new RefreshTokenDTO();
        refreshTokenDTO.setRefreshToken("refresh-token");

        // 初始化JWT令牌
        jwtTokenDTO = JwtTokenDTO.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .tokenType("Bearer")
                .expiresIn(3600000)
                .build();
                
        // 初始化测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
    }

    @Test
    void registerShouldSucceedWhenAllValidationsPass() {
        // 模拟验证通过
        given(emailService.validateVerificationCode(registerDTO.getEmail(), registerDTO.getEmailCode())).willReturn(true);
        
        // 模拟用户注册
        given(userService.register(registerDTO)).willReturn(new User());
        
        // 执行注册
        authService.register(registerDTO);
        
        // 验证调用
        verify(emailService).validateVerificationCode(registerDTO.getEmail(), registerDTO.getEmailCode());
        verify(userService).register(registerDTO);
    }
    
    @Test
    void registerShouldThrowExceptionWhenEmailCodeInvalid() {
        // 模拟邮箱验证码验证失败
        given(emailService.validateVerificationCode(registerDTO.getEmail(), registerDTO.getEmailCode())).willReturn(false);
        
        // 执行注册并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.register(registerDTO);
        });
        
        // 验证异常消息
        assertEquals("邮箱验证码错误或已过期", exception.getMessage());
        
        // 验证调用
        verify(emailService).validateVerificationCode(registerDTO.getEmail(), registerDTO.getEmailCode());
        verify(userService, never()).register(registerDTO);
    }

    @Test
    void loginShouldSucceedWhenCredentialsValid() {
        // 准备
        when(captchaService.validateCaptcha(loginDTO.getCaptchaKey(), loginDTO.getCaptchaCode())).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(tokenProvider.createToken(authentication)).thenReturn(jwtTokenDTO);

        // 执行
        JwtTokenDTO result = authService.login(loginDTO);

        // 验证
        assertNotNull(result);
        assertEquals(jwtTokenDTO.getAccessToken(), result.getAccessToken());
        assertEquals(jwtTokenDTO.getRefreshToken(), result.getRefreshToken());
        verify(captchaService).validateCaptcha(loginDTO.getCaptchaKey(), loginDTO.getCaptchaCode());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService).updateLastLoginTime(loginDTO.getUsername());
        verify(tokenProvider).createToken(authentication);
    }

    @Test
    void loginShouldThrowExceptionWhenCaptchaInvalid() {
        // 准备
        when(captchaService.validateCaptcha(loginDTO.getCaptchaKey(), loginDTO.getCaptchaCode())).thenReturn(false);

        // 执行并验证
        assertThrows(BusinessException.class, () -> authService.login(loginDTO));
        verify(captchaService).validateCaptcha(loginDTO.getCaptchaKey(), loginDTO.getCaptchaCode());
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void refreshTokenShouldSucceedWhenTokenValid() {
        // 准备
        when(tokenProvider.validateToken(refreshTokenDTO.getRefreshToken())).thenReturn(true);
        when(tokenBlacklistService.isBlacklisted(refreshTokenDTO.getRefreshToken())).thenReturn(false);
        when(tokenProvider.refreshToken(refreshTokenDTO.getRefreshToken())).thenReturn(jwtTokenDTO);

        // 执行
        JwtTokenDTO result = authService.refreshToken(refreshTokenDTO);

        // 验证
        assertNotNull(result);
        assertEquals(jwtTokenDTO.getAccessToken(), result.getAccessToken());
        assertEquals(jwtTokenDTO.getRefreshToken(), result.getRefreshToken());
        verify(tokenProvider).validateToken(refreshTokenDTO.getRefreshToken());
        verify(tokenBlacklistService).isBlacklisted(refreshTokenDTO.getRefreshToken());
        verify(tokenProvider).refreshToken(refreshTokenDTO.getRefreshToken());
    }

    @Test
    void refreshTokenShouldThrowExceptionWhenTokenInvalid() {
        // 准备
        when(tokenProvider.validateToken(refreshTokenDTO.getRefreshToken())).thenReturn(false);

        // 执行并验证
        assertThrows(BusinessException.class, () -> authService.refreshToken(refreshTokenDTO));
        verify(tokenProvider).validateToken(refreshTokenDTO.getRefreshToken());
        verify(tokenBlacklistService, never()).isBlacklisted(any());
    }

    @Test
    void refreshTokenShouldThrowExceptionWhenTokenBlacklisted() {
        // 准备
        when(tokenProvider.validateToken(refreshTokenDTO.getRefreshToken())).thenReturn(true);
        when(tokenBlacklistService.isBlacklisted(refreshTokenDTO.getRefreshToken())).thenReturn(true);

        // 执行并验证
        assertThrows(BusinessException.class, () -> authService.refreshToken(refreshTokenDTO));
        verify(tokenProvider).validateToken(refreshTokenDTO.getRefreshToken());
        verify(tokenBlacklistService).isBlacklisted(refreshTokenDTO.getRefreshToken());
        verify(tokenProvider, never()).refreshToken(any());
    }
    
    @Test
    void logoutShouldAddTokenToBlacklist() {
        // 准备
        String token = "test-token";
        when(tokenProvider.getUsernameFromToken(token)).thenReturn("testuser");
        when(jwtConfig.getAccessTokenExpiration()).thenReturn(3600000L);

        // 执行
        authService.logout(token);

        // 验证
        verify(tokenProvider).getUsernameFromToken(token);
        verify(tokenBlacklistService).addToBlacklist(token, jwtConfig.getAccessTokenExpiration());
    }

    @Test
    void logoutShouldDoNothingWhenTokenEmpty() {
        // 执行
        authService.logout("");

        // 验证
        verify(tokenProvider, never()).getUsernameFromToken(any());
        verify(tokenBlacklistService, never()).addToBlacklist(any(), anyLong());
    }
} 