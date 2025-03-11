package com.zhangziqi.online_course_mine.security.jwt;

import com.zhangziqi.online_course_mine.config.security.JwtConfig;
import com.zhangziqi.online_course_mine.model.dto.JwtTokenDTO;
import com.zhangziqi.online_course_mine.model.entity.Role;
import com.zhangziqi.online_course_mine.model.entity.User;
import com.zhangziqi.online_course_mine.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtTokenProviderTest {

    @Mock
    private UserRepository userRepository;

    @Spy
    private JwtConfig jwtConfig;

    @InjectMocks
    private JwtTokenProvider tokenProvider;

    private Authentication authentication;
    private User testUser;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        // 设置JWT配置
        ReflectionTestUtils.setField(jwtConfig, "secret", "testSecretKeyWithMinimum32Characters1234567890");
        ReflectionTestUtils.setField(jwtConfig, "accessTokenExpiration", 3600000L);
        ReflectionTestUtils.setField(jwtConfig, "refreshTokenExpiration", 86400000L);

        // 创建测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        // 创建角色
        adminRole = new Role();
        adminRole.setId(1L);
        adminRole.setName("管理员");
        adminRole.setCode("ROLE_ADMIN");

        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);
        testUser.setRoles(roles);

        // 创建认证对象
        Set<SimpleGrantedAuthority> authorities = Collections.singleton(
                new SimpleGrantedAuthority("ROLE_ADMIN"));
        authentication = new UsernamePasswordAuthenticationToken(
                "testuser", "password", authorities);
    }

    @Test
    void createTokenShouldGenerateValidTokens() {
        // 执行
        JwtTokenDTO result = tokenProvider.createToken(authentication);

        // 验证
        assertNotNull(result);
        assertNotNull(result.getAccessToken());
        assertNotNull(result.getRefreshToken());
        assertEquals("Bearer", result.getTokenType());
        assertEquals(jwtConfig.getAccessTokenExpiration(), result.getExpiresIn());

        // 验证令牌有效性
        assertTrue(tokenProvider.validateToken(result.getAccessToken()));
        assertTrue(tokenProvider.validateToken(result.getRefreshToken()));

        // 验证用户名
        assertEquals("testuser", tokenProvider.getUsernameFromToken(result.getAccessToken()));
        assertEquals("testuser", tokenProvider.getUsernameFromToken(result.getRefreshToken()));
    }

    @Test
    void refreshTokenShouldCreateNewAccessTokenWithUserRoles() {
        // 准备
        JwtTokenDTO originalTokens = tokenProvider.createToken(authentication);
        String refreshToken = originalTokens.getRefreshToken();

        // 执行
        JwtTokenDTO result = tokenProvider.refreshToken(refreshToken);

        // 验证
        assertNotNull(result);
        assertNotNull(result.getAccessToken());
        assertEquals(refreshToken, result.getRefreshToken());
        
        // 验证从新访问令牌中获取的认证包含正确的角色
        Authentication newAuth = tokenProvider.getAuthentication(result.getAccessToken());
        assertTrue(newAuth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
                
        // 不再验证用户仓库调用，因为现在直接从令牌中提取角色信息
    }

    @Test
    void refreshTokenShouldThrowExceptionWhenUserNotFound() {
        // 重命名测试方法，因为新逻辑不再从数据库获取用户
        // 转为测试令牌解析异常
        
        // 准备
        String invalidRefreshToken = "invalid.token.string";
        
        // 执行和验证 - 应该抛出JWT解析异常
        assertThrows(io.jsonwebtoken.JwtException.class, () -> {
            tokenProvider.refreshToken(invalidRefreshToken);
        });
        
        // 不再验证用户仓库调用
    }

    @Test
    void validateTokenShouldReturnTrueForValidToken() {
        // 准备
        JwtTokenDTO tokens = tokenProvider.createToken(authentication);
        
        // 执行和验证
        assertTrue(tokenProvider.validateToken(tokens.getAccessToken()));
    }

    @Test
    void validateTokenShouldReturnFalseForInvalidToken() {
        // 执行和验证
        assertFalse(tokenProvider.validateToken("invalid-token"));
    }

    @Test
    void getAuthenticationShouldReturnValidAuthentication() {
        // 准备
        JwtTokenDTO tokens = tokenProvider.createToken(authentication);
        
        // 执行
        Authentication result = tokenProvider.getAuthentication(tokens.getAccessToken());
        
        // 验证
        assertNotNull(result);
        assertEquals("testuser", result.getName());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void getUsernameFromTokenShouldReturnCorrectUsername() {
        // 准备
        JwtTokenDTO tokens = tokenProvider.createToken(authentication);
        
        // 执行和验证
        assertEquals("testuser", tokenProvider.getUsernameFromToken(tokens.getAccessToken()));
    }
} 