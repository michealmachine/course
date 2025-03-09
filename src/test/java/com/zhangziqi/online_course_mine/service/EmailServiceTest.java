package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.service.impl.EmailServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.context.ActiveProfiles;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.mockito.ArgumentCaptor;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.concurrent.TimeUnit;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 邮件服务测试
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        emailService.setVerificationCodePrefix("email:verification:");
        emailService.setVerificationCodeExpiration(5L);
        
        // 使用反射设置私有字段emailFrom
        try {
            Field emailFromField = EmailServiceImpl.class.getDeclaredField("emailFrom");
            emailFromField.setAccessible(true);
            emailFromField.set(emailService, "test@example.com");
        } catch (Exception e) {
            fail("无法设置emailFrom字段: " + e.getMessage());
        }
    }

    @Test
    void sendVerificationCodeShouldSendEmail() throws MessagingException {
        // 准备
        String email = "test@example.com";
        String code = "123456";
        String processedTemplate = "<html>验证码是: 123456</html>";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("email/verification-code"), any(Context.class))).thenReturn(processedTemplate);

        // 执行
        emailService.sendVerificationCode(email, code);

        // 验证
        verify(mailSender).createMimeMessage();
        verify(templateEngine).process(eq("email/verification-code"), any(Context.class));
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void generateVerificationCodeShouldReturnSixDigitCode() {
        // 执行
        String code = emailService.generateVerificationCode();

        // 验证
        assertNotNull(code);
        assertEquals(6, code.length());
        assertTrue(code.matches("^\\d{6}$"));
    }

    @Test
    void saveVerificationCodeShouldSaveToRedis() {
        // 准备
        String email = "test@example.com";
        String code = "123456";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // 执行
        emailService.saveVerificationCode(email, code);

        // 验证
        verify(valueOperations).set(
            eq("email:verification:test@example.com"),
            eq(code),
            eq(5L),
            eq(TimeUnit.MINUTES)
        );
    }

    @Test
    void validateVerificationCodeShouldReturnTrueWhenValid() {
        // 准备
        String email = "test@example.com";
        String code = "123456";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("email:verification:test@example.com")).thenReturn(code);

        // 执行
        boolean result = emailService.validateVerificationCode(email, code);

        // 验证
        assertTrue(result);
        verify(valueOperations).get("email:verification:test@example.com");
        verify(redisTemplate).delete("email:verification:test@example.com");
    }

    @Test
    void validateVerificationCodeShouldReturnFalseWhenInvalid() {
        // 准备
        String email = "test@example.com";
        String code = "123456";
        String wrongCode = "654321";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("email:verification:test@example.com")).thenReturn(wrongCode);

        // 执行
        boolean result = emailService.validateVerificationCode(email, code);

        // 验证
        assertFalse(result);
        verify(valueOperations).get("email:verification:test@example.com");
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void validateVerificationCodeShouldReturnFalseWhenExpired() {
        // 准备
        String email = "test@example.com";
        String code = "123456";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("email:verification:test@example.com")).thenReturn(null);

        // 执行
        boolean result = emailService.validateVerificationCode(email, code);

        // 验证
        assertFalse(result);
        verify(valueOperations).get("email:verification:test@example.com");
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    public void testSendEmailUpdateCode() throws MessagingException {
        // 准备
        String to = "test@example.com";
        String code = "123456";
        String emailContent = "<div>邮箱更新验证码：123456</div>";
        
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("email/email-update-code"), any(Context.class))).thenReturn(emailContent);
        
        // 执行
        emailService.sendEmailUpdateCode(to, code);
        
        // 验证
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(any(MimeMessage.class));
        verify(templateEngine, times(1)).process(eq("email/email-update-code"), any(Context.class));
        
        // 验证上下文参数
        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("email/email-update-code"), contextCaptor.capture());
        Context context = contextCaptor.getValue();
        assertNotNull(context);
    }
} 