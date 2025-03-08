package com.zhangziqi.online_course_mine.config;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * 验证码配置
 */
@Configuration
public class KaptchaConfig {

    /**
     * 图形验证码生成器
     */
    @Bean
    public DefaultKaptcha captchaProducer() {
        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        Properties properties = new Properties();
        // 图片宽度
        properties.setProperty("kaptcha.image.width", "150");
        // 图片高度
        properties.setProperty("kaptcha.image.height", "50");
        // 字体大小
        properties.setProperty("kaptcha.textproducer.font.size", "38");
        // 字体颜色
        properties.setProperty("kaptcha.textproducer.font.color", "0,0,0");
        // 字体
        properties.setProperty("kaptcha.textproducer.font.names", "Arial,Courier");
        // 字符间距
        properties.setProperty("kaptcha.textproducer.char.space", "5");
        // 验证码长度
        properties.setProperty("kaptcha.textproducer.char.length", "4");
        // 干扰线颜色
        properties.setProperty("kaptcha.noise.color", "blue");
        // 图片边框
        properties.setProperty("kaptcha.border", "no");
        // 背景颜色渐变，开始颜色
        properties.setProperty("kaptcha.background.clear.from", "white");
        // 背景颜色渐变，结束颜色
        properties.setProperty("kaptcha.background.clear.to", "white");

        Config config = new Config(properties);
        defaultKaptcha.setConfig(config);
        return defaultKaptcha;
    }
} 