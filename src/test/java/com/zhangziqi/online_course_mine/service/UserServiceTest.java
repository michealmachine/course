package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.model.dto.RegisterDTO;
import com.zhangziqi.online_course_mine.model.entity.Role;
import com.zhangziqi.online_course_mine.model.entity.User;
import com.zhangziqi.online_course_mine.model.enums.RoleEnum;
import com.zhangziqi.online_course_mine.repository.RoleRepository;
import com.zhangziqi.online_course_mine.repository.UserRepository;
import com.zhangziqi.online_course_mine.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 用户服务测试
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test") // 使用测试环境配置
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private RegisterDTO registerDTO;
    private User user;
    private Role userRole;

    @BeforeEach
    void setUp() {
        // 初始化注册DTO
        registerDTO = new RegisterDTO();
        registerDTO.setUsername("testuser");
        registerDTO.setPassword("password123");
        registerDTO.setEmail("test@example.com");
        registerDTO.setPhone("13812345678");

        // 初始化角色
        userRole = new Role();
        userRole.setName("普通用户");
        userRole.setCode(RoleEnum.USER.getCode());

        // 初始化用户
        user = User.builder()
                .username(registerDTO.getUsername())
                .password("encodedPassword")
                .email(registerDTO.getEmail())
                .phone(registerDTO.getPhone())
                .status(1)
                .roles(Collections.singleton(userRole))
                .build();
    }

    @Test
    void registerShouldSucceedWhenUserInfoValid() {
        // 准备
        when(userRepository.existsByUsername(registerDTO.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerDTO.getEmail())).thenReturn(false);
        when(userRepository.existsByPhone(registerDTO.getPhone())).thenReturn(false);
        when(roleRepository.findByCode(RoleEnum.USER.getCode())).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(registerDTO.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // 执行
        User result = userService.register(registerDTO);

        // 验证
        assertNotNull(result);
        assertEquals(registerDTO.getUsername(), result.getUsername());
        verify(userRepository).existsByUsername(registerDTO.getUsername());
        verify(userRepository).existsByEmail(registerDTO.getEmail());
        verify(userRepository).existsByPhone(registerDTO.getPhone());
        verify(roleRepository).findByCode(RoleEnum.USER.getCode());
        verify(passwordEncoder).encode(registerDTO.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerShouldThrowExceptionWhenUsernameExists() {
        // 准备
        when(userRepository.existsByUsername(registerDTO.getUsername())).thenReturn(true);

        // 执行并验证
        assertThrows(BusinessException.class, () -> userService.register(registerDTO));
        verify(userRepository).existsByUsername(registerDTO.getUsername());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerShouldThrowExceptionWhenEmailExists() {
        // 准备
        when(userRepository.existsByUsername(registerDTO.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerDTO.getEmail())).thenReturn(true);

        // 执行并验证
        assertThrows(BusinessException.class, () -> userService.register(registerDTO));
        verify(userRepository).existsByUsername(registerDTO.getUsername());
        verify(userRepository).existsByEmail(registerDTO.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerShouldThrowExceptionWhenPhoneExists() {
        // 准备
        when(userRepository.existsByUsername(registerDTO.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerDTO.getEmail())).thenReturn(false);
        when(userRepository.existsByPhone(registerDTO.getPhone())).thenReturn(true);

        // 执行并验证
        assertThrows(BusinessException.class, () -> userService.register(registerDTO));
        verify(userRepository).existsByUsername(registerDTO.getUsername());
        verify(userRepository).existsByEmail(registerDTO.getEmail());
        verify(userRepository).existsByPhone(registerDTO.getPhone());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserByUsernameShouldReturnUserWhenUserExists() {
        // 准备
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        // 执行
        User result = userService.getUserByUsername(user.getUsername());

        // 验证
        assertNotNull(result);
        assertEquals(user.getUsername(), result.getUsername());
        verify(userRepository).findByUsername(user.getUsername());
    }

    @Test
    void getUserByUsernameShouldThrowExceptionWhenUserNotExists() {
        // 准备
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // 执行并验证
        assertThrows(BusinessException.class, () -> userService.getUserByUsername("nonexistentuser"));
        verify(userRepository).findByUsername("nonexistentuser");
    }

    @Test
    void existsByUsernameShouldReturnTrueWhenUserExists() {
        // 准备
        when(userRepository.existsByUsername(user.getUsername())).thenReturn(true);

        // 执行
        boolean result = userService.existsByUsername(user.getUsername());

        // 验证
        assertTrue(result);
        verify(userRepository).existsByUsername(user.getUsername());
    }

    @Test
    void existsByEmailShouldReturnTrueWhenEmailExists() {
        // 准备
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);

        // 执行
        boolean result = userService.existsByEmail(user.getEmail());

        // 验证
        assertTrue(result);
        verify(userRepository).existsByEmail(user.getEmail());
    }

    @Test
    void existsByPhoneShouldReturnTrueWhenPhoneExists() {
        // 准备
        when(userRepository.existsByPhone(user.getPhone())).thenReturn(true);

        // 执行
        boolean result = userService.existsByPhone(user.getPhone());

        // 验证
        assertTrue(result);
        verify(userRepository).existsByPhone(user.getPhone());
    }

    @Test
    void updateLastLoginTimeShouldUpdateTimeWhenUserExists() {
        // 准备
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // 执行
        userService.updateLastLoginTime(user.getUsername());

        // 验证
        verify(userRepository).findByUsername(user.getUsername());
        verify(userRepository).save(any(User.class));
        assertNotNull(user.getLastLoginAt());
    }

    @Test
    void updateLastLoginTimeShouldThrowExceptionWhenUserNotExists() {
        // 准备
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // 执行并验证
        assertThrows(BusinessException.class, () -> userService.updateLastLoginTime("nonexistentuser"));
        verify(userRepository).findByUsername("nonexistentuser");
        verify(userRepository, never()).save(any(User.class));
    }
} 