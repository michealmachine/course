package com.zhangziqi.online_course_mine.config.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    /**
     * 密钥
     */
    private String secret;

    /**
     * 访问令牌过期时间（毫秒）
     */
    private long accessTokenExpiration;

    /**
     * 刷新令牌过期时间（毫秒）
     */
    private long refreshTokenExpiration;
} 