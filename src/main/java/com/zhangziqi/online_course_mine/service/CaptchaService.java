package com.zhangziqi.online_course_mine.service;

import java.awt.image.BufferedImage;

/**
 * 验证码服务接口
 */
public interface CaptchaService {

    /**
     * 生成验证码
     *
     * @param captchaKey 验证码标识
     * @return 验证码图片
     */
    BufferedImage generateCaptcha(String captchaKey);

    /**
     * 验证验证码
     *
     * @param captchaKey   验证码标识
     * @param captchaCode 验证码
     * @return 是否验证成功
     */
    boolean validateCaptcha(String captchaKey, String captchaCode);
} 