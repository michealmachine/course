package com.zhangziqi.online_course_mine.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({MinioConfig.class, S3Config.class})
public class StorageConfigProperties {
    // 此类仅用于启用ConfigurationProperties注解的绑定
} 