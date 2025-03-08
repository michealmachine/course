package com.zhangziqi.online_course_mine.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhangziqi.online_course_mine.OnlineCourseMineApplication;
import com.zhangziqi.online_course_mine.model.dto.*;
import com.zhangziqi.online_course_mine.security.jwt.JwtTokenProvider;
import com.zhangziqi.online_course_mine.security.jwt.TokenBlacklistService;
import com.zhangziqi.online_course_mine.service.AuthService;
import com.zhangziqi.online_course_mine.service.CaptchaService;
import com.zhangziqi.online_course_mine.service.EmailService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.awt.image.BufferedImage;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 认证控制器测试
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // 禁用Spring Security过滤器
@ActiveProfiles("test") // 使用测试环境配置
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private CaptchaService captchaService;

    @MockBean
    private EmailService emailService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;
    
    @MockBean
    private TokenBlacklistService tokenBlacklistService;

    @Test
    public void testGetCaptchaKey() throws Exception {
        mockMvc.perform(get("/api/auth/captcha/key"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isString());
    }

    @Test
    public void testGetCaptchaImage() throws Exception {
        // 模拟验证码服务
        BufferedImage mockImage = new BufferedImage(150, 50, BufferedImage.TYPE_INT_RGB);
        given(captchaService.generateCaptcha("test-key")).willReturn(mockImage);

        // 执行请求
        mockMvc.perform(get("/api/auth/captcha/image/{key}", "test-key"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store, no-cache, must-revalidate"));

        verify(captchaService).generateCaptcha("test-key");
    }

    @Test
    public void testSendEmailVerificationCode() throws Exception {
        // 创建邮箱验证码请求
        EmailVerificationDTO emailVerificationDTO = new EmailVerificationDTO();
        emailVerificationDTO.setEmail("test@example.com");
        emailVerificationDTO.setCaptchaKey("test-key");
        emailVerificationDTO.setCaptchaCode("1234");

        // 模拟验证码验证通过
        when(captchaService.validateCaptcha(emailVerificationDTO.getCaptchaKey(), emailVerificationDTO.getCaptchaCode()))
                .thenReturn(true);

        // 模拟生成验证码
        when(emailService.generateVerificationCode()).thenReturn("123456");

        // 执行请求
        mockMvc.perform(post("/api/auth/email-verification-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emailVerificationDTO)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("操作成功"));

        verify(captchaService).validateCaptcha(emailVerificationDTO.getCaptchaKey(), emailVerificationDTO.getCaptchaCode());
        verify(emailService).generateVerificationCode();
        verify(emailService).sendVerificationCode(eq(emailVerificationDTO.getEmail()), any());
        verify(emailService).saveVerificationCode(eq(emailVerificationDTO.getEmail()), any());
    }

    @Test
    public void testSendEmailVerificationCodeWithInvalidCaptcha() throws Exception {
        // 创建邮箱验证码请求
        EmailVerificationDTO emailVerificationDTO = new EmailVerificationDTO();
        emailVerificationDTO.setEmail("test@example.com");
        emailVerificationDTO.setCaptchaKey("test-key");
        emailVerificationDTO.setCaptchaCode("1234");

        // 模拟验证码验证失败
        when(captchaService.validateCaptcha(emailVerificationDTO.getCaptchaKey(), emailVerificationDTO.getCaptchaCode()))
                .thenReturn(false);

        // 执行请求
        mockMvc.perform(post("/api/auth/email-verification-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emailVerificationDTO)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("验证码错误"));

        verify(captchaService).validateCaptcha(emailVerificationDTO.getCaptchaKey(), emailVerificationDTO.getCaptchaCode());
        verify(emailService, never()).generateVerificationCode();
        verify(emailService, never()).sendVerificationCode(any(), any());
        verify(emailService, never()).saveVerificationCode(any(), any());
    }

    @Test
    public void testRegister() throws Exception {
        // 创建注册请求
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername("testuser");
        registerDTO.setPassword("password123");
        registerDTO.setEmail("test@example.com");
        registerDTO.setPhone("13812345678");
        registerDTO.setCaptchaKey("test-key");
        registerDTO.setCaptchaCode("1234");
        registerDTO.setEmailCode("123456");

        // 模拟服务
        doNothing().when(authService).register(any(RegisterDTO.class));

        // 执行请求
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("操作成功"));

        verify(authService, times(1)).register(any(RegisterDTO.class));
    }

    @Test
    public void testLogin() throws Exception {
        // 创建登录请求
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("testuser");
        loginDTO.setPassword("password123");
        loginDTO.setCaptchaKey("test-key");
        loginDTO.setCaptchaCode("1234");

        // 模拟JWT令牌
        JwtTokenDTO jwtTokenDTO = JwtTokenDTO.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .expiresIn(3600000)
                .build();

        // 模拟服务
        given(authService.login(any(LoginDTO.class))).willReturn(jwtTokenDTO);

        // 执行请求
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andDo(MockMvcResultHandlers.print()) // 打印结果
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.data.expiresIn").value(3600000));

        verify(authService, times(1)).login(any(LoginDTO.class));
    }

    @Test
    public void testRefreshToken() throws Exception {
        // 创建刷新令牌请求
        RefreshTokenDTO refreshTokenDTO = new RefreshTokenDTO();
        refreshTokenDTO.setRefreshToken("refresh-token");

        // 模拟JWT令牌
        JwtTokenDTO jwtTokenDTO = JwtTokenDTO.builder()
                .accessToken("new-access-token")
                .refreshToken("refresh-token")
                .expiresIn(3600000)
                .build();

        // 模拟服务
        given(authService.refreshToken(any(RefreshTokenDTO.class))).willReturn(jwtTokenDTO);

        // 执行请求
        mockMvc.perform(post("/api/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenDTO)))
                .andDo(MockMvcResultHandlers.print()) // 打印结果
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.data.expiresIn").value(3600000));

        verify(authService, times(1)).refreshToken(any(RefreshTokenDTO.class));
    }

    @Test
    public void testLogout() throws Exception {
        // 模拟服务
        doNothing().when(authService).logout(anyString());

        // 执行请求
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer test-token"))
                .andDo(MockMvcResultHandlers.print()) // 打印结果
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("操作成功"));

        verify(authService, times(1)).logout(eq("test-token"));
    }
} 