package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.model.dto.RegisterDTO;
import com.zhangziqi.online_course_mine.model.entity.Role;
import com.zhangziqi.online_course_mine.model.entity.User;
import com.zhangziqi.online_course_mine.model.enums.RoleEnum;
import com.zhangziqi.online_course_mine.model.vo.UserVO;
import com.zhangziqi.online_course_mine.repository.RoleRepository;
import com.zhangziqi.online_course_mine.repository.UserRepository;
import com.zhangziqi.online_course_mine.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
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
    
    @Mock
    private EmailService emailService;

    @Mock
    private MinioService minioService;

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

    @Test
    void getCurrentUserShouldReturnUserInfoWhenUserExists() {
        // 准备
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        
        // 执行
        UserVO result = userService.getCurrentUser(user.getUsername());
        
        // 验证
        assertNotNull(result);
        assertEquals(user.getUsername(), result.getUsername());
        assertEquals(user.getEmail(), result.getEmail());
        assertEquals(user.getPhone(), result.getPhone());
        verify(userRepository).findByUsername(user.getUsername());
    }
    
    @Test
    void updateCurrentUserProfileShouldUpdateUserInfoWhenValid() {
        // 准备
        String nickname = "新昵称";
        String phone = "13900001111";
        
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(userRepository.existsByPhone(phone)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        // 执行
        UserVO result = userService.updateCurrentUserProfile(user.getUsername(), nickname, phone);
        
        // 验证
        assertNotNull(result);
        assertEquals(nickname, user.getNickname());
        assertEquals(phone, user.getPhone());
        verify(userRepository).findByUsername(user.getUsername());
        verify(userRepository).existsByPhone(phone);
        verify(userRepository).save(user);
    }
    
    @Test
    void updateCurrentUserProfileShouldThrowExceptionWhenPhoneExists() {
        // 准备
        String nickname = "新昵称";
        String phone = "13900001111";
        
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(userRepository.existsByPhone(phone)).thenReturn(true);
        
        // 执行与验证
        assertThrows(BusinessException.class, () -> 
            userService.updateCurrentUserProfile(user.getUsername(), nickname, phone)
        );
        
        verify(userRepository).findByUsername(user.getUsername());
        verify(userRepository).existsByPhone(phone);
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void changePasswordShouldSucceedWhenOldPasswordCorrect() {
        // 准备
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";
        
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(oldPassword, user.getPassword())).thenReturn(true);
        when(passwordEncoder.matches(newPassword, user.getPassword())).thenReturn(false);
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");
        
        // 执行
        boolean result = userService.changePassword(user.getUsername(), oldPassword, newPassword);
        
        // 验证
        assertTrue(result);
        assertEquals("encodedNewPassword", user.getPassword());
        verify(userRepository).findByUsername(user.getUsername());
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(user);
    }
    
    @Test
    void changePasswordShouldThrowExceptionWhenOldPasswordIncorrect() {
        // 准备
        String oldPassword = "wrongPassword";
        String newPassword = "newPassword";
        
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(oldPassword, user.getPassword())).thenReturn(false);
        
        // 执行与验证
        assertThrows(BusinessException.class, () -> 
            userService.changePassword(user.getUsername(), oldPassword, newPassword)
        );
        
        verify(userRepository).findByUsername(user.getUsername());
        verify(passwordEncoder, never()).encode(any(String.class));
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void changePasswordShouldThrowExceptionWhenNewPasswordSameAsOld() {
        // 准备
        String oldPassword = "oldPassword";
        String newPassword = "oldPassword"; // 新密码与旧密码相同
        
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(oldPassword, user.getPassword())).thenReturn(true);
        when(passwordEncoder.matches(newPassword, user.getPassword())).thenReturn(true);
        
        // 执行与验证
        assertThrows(BusinessException.class, () -> 
            userService.changePassword(user.getUsername(), oldPassword, newPassword)
        );
        
        verify(userRepository).findByUsername(user.getUsername());
        verify(passwordEncoder, never()).encode(any(String.class));
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void updateAvatarShouldUpdateAvatarUrlWhenUserExists() {
        // 准备
        String avatarUrl = "https://example.com/avatar.jpg";
        
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        // 执行
        UserVO result = userService.updateAvatar(user.getUsername(), avatarUrl);
        
        // 验证
        assertNotNull(result);
        assertEquals(avatarUrl, user.getAvatar());
        verify(userRepository).findByUsername(user.getUsername());
        verify(userRepository).save(user);
    }
    
    @Test
    void updateEmailShouldUpdateEmailWhenValid() {
        // 准备
        String newEmail = "newemail@example.com";
        String emailCode = "123456";
        String password = "password123";
        
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, user.getPassword())).thenReturn(true);
        when(userRepository.existsByEmail(newEmail)).thenReturn(false);
        when(emailService.validateVerificationCode(newEmail, emailCode)).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        // 执行
        UserVO result = userService.updateEmail(user.getUsername(), newEmail, emailCode, password);
        
        // 验证
        assertNotNull(result);
        assertEquals(newEmail, user.getEmail());
        verify(userRepository).findByUsername(user.getUsername());
        verify(passwordEncoder).matches(password, user.getPassword());
        verify(userRepository).existsByEmail(newEmail);
        verify(emailService).validateVerificationCode(newEmail, emailCode);
        verify(userRepository).save(user);
    }
    
    @Test
    void updateEmailShouldThrowExceptionWhenPasswordIncorrect() {
        // 准备
        String newEmail = "newemail@example.com";
        String emailCode = "123456";
        String password = "wrongPassword";
        
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, user.getPassword())).thenReturn(false);
        
        // 执行与验证
        assertThrows(BusinessException.class, () -> 
            userService.updateEmail(user.getUsername(), newEmail, emailCode, password)
        );
        
        verify(userRepository).findByUsername(user.getUsername());
        verify(passwordEncoder).matches(password, user.getPassword());
        verify(userRepository, never()).existsByEmail(any());
        verify(emailService, never()).validateVerificationCode(any(), any());
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void updateEmailShouldThrowExceptionWhenEmailExists() {
        // 准备
        String newEmail = "newemail@example.com";
        String emailCode = "123456";
        String password = "password123";
        
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, user.getPassword())).thenReturn(true);
        when(userRepository.existsByEmail(newEmail)).thenReturn(true);
        
        // 执行与验证
        assertThrows(BusinessException.class, () -> 
            userService.updateEmail(user.getUsername(), newEmail, emailCode, password)
        );
        
        verify(userRepository).findByUsername(user.getUsername());
        verify(passwordEncoder).matches(password, user.getPassword());
        verify(userRepository).existsByEmail(newEmail);
        verify(emailService, never()).validateVerificationCode(any(), any());
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void updateEmailShouldThrowExceptionWhenEmailCodeInvalid() {
        // 准备
        String newEmail = "newemail@example.com";
        String emailCode = "123456";
        String password = "password123";
        
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, user.getPassword())).thenReturn(true);
        when(userRepository.existsByEmail(newEmail)).thenReturn(false);
        when(emailService.validateVerificationCode(newEmail, emailCode)).thenReturn(false);
        
        // 执行与验证
        assertThrows(BusinessException.class, () -> 
            userService.updateEmail(user.getUsername(), newEmail, emailCode, password)
        );
        
        verify(userRepository).findByUsername(user.getUsername());
        verify(passwordEncoder).matches(password, user.getPassword());
        verify(userRepository).existsByEmail(newEmail);
        verify(emailService).validateVerificationCode(newEmail, emailCode);
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void getBasicUserInfoShouldReturnBasicInfoWhenUserExists() {
        // 准备
        Long userId = 1L;
        user.setId(userId);
        user.setNickname("测试用户");
        user.setAvatar("https://example.com/avatar.jpg");
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        
        // 执行
        UserVO result = userService.getBasicUserInfo(userId);
        
        // 验证
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals(user.getUsername(), result.getUsername());
        assertEquals(user.getNickname(), result.getNickname());
        assertEquals(user.getAvatar(), result.getAvatar());
        verify(userRepository).findById(userId);
    }
    
    @Test
    void getBasicUserInfoShouldThrowExceptionWhenUserNotExists() {
        // 准备
        Long userId = 1L;
        
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        // 执行与验证
        assertThrows(BusinessException.class, () -> 
            userService.getBasicUserInfo(userId)
        );
        
        verify(userRepository).findById(userId);
    }

    @Test
    void uploadAndUpdateAvatarShouldSucceedWhenFileValid() throws IOException {
        // 准备
        String avatarUrl = "http://localhost:8999/media/avatars/testuser/uuid-test.jpg";
        String objectName = "avatars/testuser/uuid-test.jpg";
        
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getContentType()).thenReturn("image/jpeg");
        when(mockFile.getSize()).thenReturn(1024L); // 1KB
        when(mockFile.getOriginalFilename()).thenReturn("test.jpg");
        
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(minioService.uploadFile(contains("avatars/" + user.getUsername()), any(), eq("image/jpeg"))).thenReturn(avatarUrl);
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        // 执行
        Map<String, String> result = userService.uploadAndUpdateAvatar(user.getUsername(), mockFile);
        
        // 验证
        assertNotNull(result);
        assertTrue(result.containsKey("avatarUrl"));
        assertEquals(avatarUrl, result.get("avatarUrl"));
        verify(userRepository).findByUsername(user.getUsername());
        verify(minioService).uploadFile(anyString(), any(), anyString());
        verify(userRepository).save(user);
    }
    
    @Test
    void uploadAndUpdateAvatarShouldThrowExceptionWhenFileTypeInvalid() {
        // 准备
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getContentType()).thenReturn("application/pdf");
        
        // 执行和验证
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.uploadAndUpdateAvatar(user.getUsername(), mockFile);
        });
        
        assertEquals(400, exception.getCode());
        assertTrue(exception.getMessage().contains("只支持上传图片文件"));
        verify(minioService, never()).uploadFile(anyString(), any(), anyString());
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void uploadAndUpdateAvatarShouldThrowExceptionWhenFileSizeTooLarge() {
        // 准备
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getContentType()).thenReturn("image/jpeg");
        when(mockFile.getSize()).thenReturn(3 * 1024 * 1024L); // 3MB
        
        // 执行和验证
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.uploadAndUpdateAvatar(user.getUsername(), mockFile);
        });
        
        assertEquals(400, exception.getCode());
        assertTrue(exception.getMessage().contains("文件大小不能超过2MB"));
        verify(minioService, never()).uploadFile(anyString(), any(), anyString());
        verify(userRepository, never()).save(any(User.class));
    }
} 