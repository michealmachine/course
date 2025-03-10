package com.zhangziqi.online_course_mine.security;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 测试环境专用的UserDetailsService实现
 * 用于集成测试中加载测试用户
 */
@Service("userDetailsService")
@Profile("test")
@Primary
public class TestUserDetailsService implements UserDetailsService {

    /**
     * 根据用户名加载用户
     *
     * @param username 用户名
     * @return 用户详情
     * @throws UsernameNotFoundException 用户名不存在
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 为集成测试创建固定的测试用户
        if ("admin_test".equals(username)) {
            List<SimpleGrantedAuthority> authorities = Arrays.asList(
                "ROLE_ADMIN", "ADMIN", "TEST_READ"
            ).stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
            
            return new User(
                "admin_test", 
                "$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG", // "password"的BCrypt加密值
                true, true, true, true,
                authorities
            );
        } else if ("user_test".equals(username)) {
            List<SimpleGrantedAuthority> authorities = Arrays.asList(
                "ROLE_USER", "USER"
            ).stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
            
            return new User(
                "user_test", 
                "$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG", // "password"的BCrypt加密值
                true, true, true, true,
                authorities
            );
        }
        
        throw new UsernameNotFoundException("用户不存在: " + username);
    }
} 