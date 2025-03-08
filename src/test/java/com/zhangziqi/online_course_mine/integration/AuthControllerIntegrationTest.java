package com.zhangziqi.online_course_mine.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhangziqi.online_course_mine.config.TestSecurityConfig;
import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.model.dto.*;
import com.zhangziqi.online_course_mine.service.AuthService;
import com.zhangziqi.online_course_mine.service.CaptchaService;
import com.zhangziqi.online_course_mine.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 认证控制器集成测试
 * 使用实际的安全过滤器链，但模拟了业务服务组件
 */
@SpringBootTest(
    // 可以在这里指定特定的配置属性
    properties = {
        "spring.main.allow-bean-definition-overriding=true"
    }
)
@AutoConfigureMockMvc
@ActiveProfiles("test") // 使用测试环境配置
@Import(TestSecurityConfig.class)
public class AuthControllerIntegrationTest {

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

    @Test
    public void testGetCaptcha() throws Exception {
        // 模拟验证码服务
        BufferedImage mockImage = new BufferedImage(150, 50, BufferedImage.TYPE_INT_RGB);
        given(captchaService.generateCaptcha(any())).willReturn(mockImage);

        // 1. 获取验证码key
        String result = mockMvc.perform(get("/api/auth/captcha/key"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isString())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 从响应中提取验证码key
        String captchaKey = new ObjectMapper().readTree(result).get("data").asText();

        // 2. 使用key获取验证码图片
        mockMvc.perform(get("/api/auth/captcha/image/{key}", captchaKey))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store, no-cache, must-revalidate"));

        verify(captchaService, times(1)).generateCaptcha(eq(captchaKey));
    }

    @Test
    public void testCompleteRegistrationFlow() throws Exception {
        // 1. 获取图形验证码key
        String result = mockMvc.perform(get("/api/auth/captcha/key"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String captchaKey = new ObjectMapper().readTree(result).get("data").asText();

        // 2. 获取验证码图片
        BufferedImage mockImage = new BufferedImage(150, 50, BufferedImage.TYPE_INT_RGB);
        given(captchaService.generateCaptcha(any())).willReturn(mockImage);

        mockMvc.perform(get("/api/auth/captcha/image/{key}", captchaKey))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE));

        // 3. 发送邮箱验证码
        EmailVerificationDTO emailVerificationDTO = new EmailVerificationDTO();
        emailVerificationDTO.setEmail("test@example.com");
        emailVerificationDTO.setCaptchaKey(captchaKey);
        emailVerificationDTO.setCaptchaCode("1234");

        given(captchaService.validateCaptcha(eq(captchaKey), eq("1234"))).willReturn(true);
        given(emailService.generateVerificationCode()).willReturn("123456");
        doNothing().when(emailService).sendVerificationCode(anyString(), anyString());
        doNothing().when(emailService).saveVerificationCode(anyString(), anyString());

        mockMvc.perform(post("/api/auth/email-verification-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emailVerificationDTO)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 4. 完成注册
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername("testuser");
        registerDTO.setPassword("password123");
        registerDTO.setEmail("test@example.com");
        registerDTO.setPhone("13812345678");
        registerDTO.setCaptchaKey(captchaKey);
        registerDTO.setCaptchaCode("1234");
        registerDTO.setEmailCode("123456");

        // 注册时会先调用 emailService.validateVerificationCode，然后调用 userService
        doAnswer(invocation -> {
            RegisterDTO dto = invocation.getArgument(0);
            // 这会触发 emailService.validateVerificationCode 的调用
            emailService.validateVerificationCode(dto.getEmail(), dto.getEmailCode());
            return null;
        }).when(authService).register(any(RegisterDTO.class));
        
        given(emailService.validateVerificationCode(eq("test@example.com"), eq("123456"))).willReturn(true);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(200));

        // 验证所有服务调用
        verify(captchaService).validateCaptcha(eq(captchaKey), eq("1234")); // 只在发送邮箱验证码时验证
        verify(emailService).generateVerificationCode();
        verify(emailService).sendVerificationCode(eq("test@example.com"), eq("123456"));
        verify(emailService).saveVerificationCode(eq("test@example.com"), eq("123456"));
        verify(emailService).validateVerificationCode(eq("test@example.com"), eq("123456"));
        verify(authService).register(any(RegisterDTO.class));
    }

    @Test
    public void testRegistrationWithInvalidEmailCode() throws Exception {
        // 准备注册数据
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername("testuser");
        registerDTO.setPassword("password123");
        registerDTO.setEmail("test@example.com");
        registerDTO.setCaptchaKey("test-key");
        registerDTO.setCaptchaCode("1234");
        registerDTO.setEmailCode("123456");

        // 模拟邮箱验证码验证失败
        given(emailService.validateVerificationCode(eq("test@example.com"), eq("123456"))).willReturn(false);
        doThrow(new BusinessException("邮箱验证码错误或已过期"))
            .when(authService).register(any(RegisterDTO.class));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("邮箱验证码错误或已过期"));

        verify(emailService, never()).validateVerificationCode(eq("test@example.com"), eq("123456"));
        verify(authService).register(any(RegisterDTO.class));
    }

    @Test
    public void testSendEmailVerificationCodeWithInvalidCaptcha() throws Exception {
        // 准备邮箱验证码请求数据
        EmailVerificationDTO emailVerificationDTO = new EmailVerificationDTO();
        emailVerificationDTO.setEmail("test@example.com");
        emailVerificationDTO.setCaptchaKey("test-key");
        emailVerificationDTO.setCaptchaCode("1234");

        // 模拟图形验证码验证失败
        given(captchaService.validateCaptcha("test-key", "1234")).willReturn(false);

        mockMvc.perform(post("/api/auth/email-verification-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emailVerificationDTO)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("验证码错误"));

        verify(captchaService).validateCaptcha(eq("test-key"), eq("1234"));
        verify(emailService, never()).generateVerificationCode();
        verify(emailService, never()).sendVerificationCode(any(), any());
        verify(emailService, never()).saveVerificationCode(any(), any());
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
                .andDo(MockMvcResultHandlers.print())
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
                .andDo(MockMvcResultHandlers.print())
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
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("操作成功"));

        verify(authService, times(1)).logout(eq("test-token"));
    }
} 