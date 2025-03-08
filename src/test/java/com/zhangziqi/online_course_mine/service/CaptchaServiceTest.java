package com.zhangziqi.online_course_mine.service;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.zhangziqi.online_course_mine.service.impl.CaptchaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.awt.image.BufferedImage;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 验证码服务测试
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test") // 使用测试环境配置
public class CaptchaServiceTest {

    @Mock
    private DefaultKaptcha captchaProducer;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private CaptchaServiceImpl captchaService;

    @BeforeEach
    void setUp() {
        // 只在需要使用到的测试方法中进行模拟，避免不必要的模拟
        // when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        
        // 设置私有常量字段的值
        ReflectionTestUtils.setField(captchaService, "CAPTCHA_PREFIX", "captcha:");
        ReflectionTestUtils.setField(captchaService, "CAPTCHA_EXPIRATION", 300L); // 5分钟
    }

    @Test
    void generateCaptchaShouldReturnImageAndSaveToRedis() {
        // 准备
        String captchaKey = "test-key";
        String captchaText = "1234";
        BufferedImage mockImage = new BufferedImage(150, 50, BufferedImage.TYPE_INT_RGB);
        
        // 在这个测试中才需要模拟opsForValue
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(captchaProducer.createText()).thenReturn(captchaText);
        when(captchaProducer.createImage(captchaText)).thenReturn(mockImage);
        // 使用when-thenReturn代替doNothing
        // doNothing().when(valueOperations).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));

        // 执行
        BufferedImage result = captchaService.generateCaptcha(captchaKey);

        // 验证
        assertNotNull(result);
        assertEquals(mockImage, result);
        verify(captchaProducer).createText();
        verify(captchaProducer).createImage(captchaText);
        verify(valueOperations).set(eq("captcha:" + captchaKey), eq(captchaText), eq(300L), eq(TimeUnit.SECONDS));
    }

    @Test
    void validateCaptchaShouldReturnTrueWhenCaptchaValid() {
        // 准备
        String captchaKey = "test-key";
        String captchaCode = "1234";
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("captcha:" + captchaKey)).thenReturn(captchaCode);
        // 对于返回值的方法，使用when-thenReturn而不是doNothing
        when(redisTemplate.delete(anyString())).thenReturn(Boolean.TRUE);

        // 执行
        boolean result = captchaService.validateCaptcha(captchaKey, captchaCode);

        // 验证
        assertTrue(result);
        verify(valueOperations).get("captcha:" + captchaKey);
        verify(redisTemplate).delete("captcha:" + captchaKey);
    }

    @Test
    void validateCaptchaShouldReturnFalseWhenCaptchaInvalid() {
        // 准备
        String captchaKey = "test-key";
        String captchaCode = "1234";
        String storedCaptchaCode = "5678";
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("captcha:" + captchaKey)).thenReturn(storedCaptchaCode);
        // 对于返回值的方法，使用when-thenReturn而不是doNothing
        when(redisTemplate.delete(anyString())).thenReturn(Boolean.TRUE);

        // 执行
        boolean result = captchaService.validateCaptcha(captchaKey, captchaCode);

        // 验证
        assertFalse(result);
        verify(valueOperations).get("captcha:" + captchaKey);
        verify(redisTemplate).delete("captcha:" + captchaKey);
    }

    @Test
    void validateCaptchaShouldReturnFalseWhenCaptchaExpired() {
        // 准备
        String captchaKey = "test-key";
        String captchaCode = "1234";
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("captcha:" + captchaKey)).thenReturn(null);

        // 执行
        boolean result = captchaService.validateCaptcha(captchaKey, captchaCode);

        // 验证
        assertFalse(result);
        verify(valueOperations).get("captcha:" + captchaKey);
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void validateCaptchaShouldReturnFalseWhenKeyOrCodeEmpty() {
        // 执行 - 空key
        boolean result1 = captchaService.validateCaptcha("", "1234");
        
        // 执行 - 空code
        boolean result2 = captchaService.validateCaptcha("test-key", "");
        
        // 执行 - 都为空
        boolean result3 = captchaService.validateCaptcha("", "");

        // 验证
        assertFalse(result1);
        assertFalse(result2);
        assertFalse(result3);
        verify(valueOperations, never()).get(anyString());
        verify(redisTemplate, never()).delete(anyString());
    }
} 