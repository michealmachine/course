package com.zhangziqi.online_course_mine.service.impl;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.zhangziqi.online_course_mine.service.CaptchaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.awt.image.BufferedImage;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaptchaServiceImpl implements CaptchaService {

    private final DefaultKaptcha captchaProducer;
    private final RedisTemplate<String, Object> redisTemplate;
    private static String CAPTCHA_PREFIX = "captcha:";
    private static long CAPTCHA_EXPIRATION = 5 * 60; // 5分钟

    /**
     * 生成验证码
     *
     * @param captchaKey 验证码标识
     * @return 验证码图片
     */
    @Override
    public BufferedImage generateCaptcha(String captchaKey) {
        // 生成验证码文本
        String captchaText = captchaProducer.createText();
        log.debug("生成验证码: {} -> {}", captchaKey, captchaText);

        // 保存验证码到Redis
        String redisKey = CAPTCHA_PREFIX + captchaKey;
        redisTemplate.opsForValue().set(redisKey, captchaText, CAPTCHA_EXPIRATION, TimeUnit.SECONDS);

        // 生成验证码图片
        return captchaProducer.createImage(captchaText);
    }

    /**
     * 验证验证码
     *
     * @param captchaKey   验证码标识
     * @param captchaCode 验证码
     * @return 是否验证成功
     */
    @Override
    public boolean validateCaptcha(String captchaKey, String captchaCode) {
        if (!StringUtils.hasText(captchaKey) || !StringUtils.hasText(captchaCode)) {
            return false;
        }

        // 从Redis获取验证码
        String redisKey = CAPTCHA_PREFIX + captchaKey;
        Object value = redisTemplate.opsForValue().get(redisKey);
        
        if (value == null) {
            log.debug("验证码不存在或已过期: {}", captchaKey);
            return false;
        }

        // 验证后删除验证码
        redisTemplate.delete(redisKey);

        // 忽略大小写比较
        boolean result = captchaCode.equalsIgnoreCase(value.toString());
        log.debug("验证码校验: {} -> {} vs {}, 结果: {}", captchaKey, captchaCode, value, result);
        return result;
    }
} 