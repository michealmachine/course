package com.zhangziqi.online_course_mine.config;

import com.zhangziqi.online_course_mine.config.security.JwtConfig;
import com.zhangziqi.online_course_mine.security.jwt.JwtAuthenticationFilter;
import com.zhangziqi.online_course_mine.security.jwt.JwtTokenProvider;
import com.zhangziqi.online_course_mine.security.jwt.TokenBlacklistService;
import org.mockito.ArgumentMatchers;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 测试环境安全配置
 * 用于集成测试，提供尽可能接近生产环境的配置
 */
@TestConfiguration
@EnableWebSecurity
@Profile("test") // 确保只在测试环境下激活
public class TestSecurityConfig {

    @Bean
    @Primary
    public JwtTokenProvider jwtTokenProvider() {
        JwtTokenProvider mockProvider = mock(JwtTokenProvider.class);
        // 可以在这里添加一些基本的行为
        when(mockProvider.validateToken(ArgumentMatchers.anyString())).thenReturn(true);
        return mockProvider;
    }

    @Bean
    @Primary
    public TokenBlacklistService tokenBlacklistService() {
        TokenBlacklistService mockService = mock(TokenBlacklistService.class);
        // 可以在这里添加一些基本的行为
        when(mockService.isBlacklisted(ArgumentMatchers.anyString())).thenReturn(false);
        return mockService;
    }

    @Bean
    @Primary
    public JwtConfig jwtConfig() {
        JwtConfig config = new JwtConfig();
        config.setSecret("test-secret-key-for-testing-purposes-only");
        config.setAccessTokenExpiration(3600000L); // 1小时
        config.setRefreshTokenExpiration(86400000L); // 24小时
        return config;
    }

    @Bean
    @Primary
    public AuthenticationManager authenticationManager() {
        return mock(AuthenticationManager.class);
    }

    @Bean
    @Primary
    public UserDetailsService userDetailsService() {
        // 不再使用mock实现，而是返回一个实际能查询到测试用户的UserDetailsService
        return username -> {
            // 为集成测试创建一个简单的UserDetails实现
            if ("admin_test".equals(username)) {
                return org.springframework.security.core.userdetails.User.builder()
                    .username("admin_test")
                    .password("$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG") // "password"的加密值
                    .roles("ADMIN")
                    .authorities("ROLE_ADMIN", "TEST_READ")
                    .build();
            } else if ("user_test".equals(username)) {
                return org.springframework.security.core.userdetails.User.builder()
                    .username("user_test")
                    .password("$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG") // "password"的加密值
                    .roles("USER")
                    .build();
            }
            throw new org.springframework.security.core.userdetails.UsernameNotFoundException("用户不存在: " + username);
        };
    }

    @Bean
    @Primary
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenProvider tokenProvider, TokenBlacklistService blacklistService) {
        return new JwtAuthenticationFilter(tokenProvider, blacklistService);
    }

    @Bean
    @Primary
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll() // 开放认证相关接口
                .anyRequest().authenticated() // 其他接口需要认证
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
} 