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
        return mock(UserDetailsService.class);
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