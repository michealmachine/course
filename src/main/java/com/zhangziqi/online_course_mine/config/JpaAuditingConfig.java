package com.zhangziqi.online_course_mine.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

/**
 * JPA审计配置
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {

    /**
     * 当前操作用户
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.of("System");
    }
} 