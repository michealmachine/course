package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.model.dto.RegisterDTO;
import com.zhangziqi.online_course_mine.model.entity.Role;
import com.zhangziqi.online_course_mine.model.entity.User;
import com.zhangziqi.online_course_mine.model.enums.RoleEnum;
import com.zhangziqi.online_course_mine.model.vo.*;
import com.zhangziqi.online_course_mine.repository.RoleRepository;
import com.zhangziqi.online_course_mine.repository.UserRepository;
import com.zhangziqi.online_course_mine.security.jwt.TokenBlacklistService;
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
import java.util.*;
import java.util.stream.Collectors;

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
    
    @Mock
    private TokenBlacklistService tokenBlacklistService;

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
        userRole.setId(1L);
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

    @Test
    void getUserStatsShouldReturnAllStatistics() {
        // 创建Spy以便于验证方法调用
        UserServiceImpl spyUserService = spy(userService);
        
        // 创建模拟数据
        UserRoleDistributionVO roleDistribution = new UserRoleDistributionVO();
        UserGrowthStatsVO growthStats = new UserGrowthStatsVO();
        UserStatusStatsVO statusStats = new UserStatusStatsVO();
        UserActivityStatsVO activityStats = new UserActivityStatsVO();
        
        // 配置spy的行为
        doReturn(roleDistribution).when(spyUserService).getUserRoleDistribution();
        doReturn(growthStats).when(spyUserService).getUserGrowthStats();
        doReturn(statusStats).when(spyUserService).getUserStatusStats();
        doReturn(activityStats).when(spyUserService).getUserActivityStats();
        
        // 执行测试
        UserStatsVO result = spyUserService.getUserStats();
        
        // 验证结果
        assertNotNull(result);
        assertSame(roleDistribution, result.getRoleDistribution());
        assertSame(growthStats, result.getGrowthStats());
        assertSame(statusStats, result.getStatusStats());
        assertSame(activityStats, result.getActivityStats());
        
        // 验证方法调用
        verify(spyUserService).getUserRoleDistribution();
        verify(spyUserService).getUserGrowthStats();
        verify(spyUserService).getUserStatusStats();
        verify(spyUserService).getUserActivityStats();
    }
    
    @Test
    void getUserRoleDistributionShouldCalculateCorrectPercentages() {
        // 准备测试数据
        Role adminRole = new Role();
        adminRole.setId(2L);
        adminRole.setName("管理员");
        adminRole.setCode("ADMIN");
        
        List<Role> roles = Arrays.asList(userRole, adminRole);
        
        // 配置mock行为
        when(userRepository.count()).thenReturn(100L);
        when(roleRepository.findAll()).thenReturn(roles);
        when(userRepository.countByRoleId(1L)).thenReturn(80L);
        when(userRepository.countByRoleId(2L)).thenReturn(20L);
        
        // 执行测试
        UserRoleDistributionVO result = userService.getUserRoleDistribution();
        
        // 验证结果
        assertNotNull(result);
        assertEquals(100L, result.getTotalUserCount());
        assertEquals(2, result.getRoleDistributions().size());
        
        // 获取并验证普通用户角色分布
        UserRoleDistributionVO.RoleDistribution userDist = result.getRoleDistributions().stream()
                .filter(r -> r.getRoleId().equals(1L))
                .findFirst()
                .orElse(null);
        assertNotNull(userDist);
        assertEquals(1L, userDist.getRoleId());
        assertEquals("普通用户", userDist.getRoleName());
        assertEquals("ROLE_USER", userDist.getRoleCode());
        assertEquals(80L, userDist.getUserCount());
        assertEquals(80.0, userDist.getPercentage());
        
        // 获取并验证管理员角色分布
        UserRoleDistributionVO.RoleDistribution adminDist = result.getRoleDistributions().stream()
                .filter(r -> r.getRoleId().equals(2L))
                .findFirst()
                .orElse(null);
        assertNotNull(adminDist);
        assertEquals(2L, adminDist.getRoleId());
        assertEquals("管理员", adminDist.getRoleName());
        assertEquals("ADMIN", adminDist.getRoleCode());
        assertEquals(20L, adminDist.getUserCount());
        assertEquals(20.0, adminDist.getPercentage());
        
        // 验证方法调用
        verify(userRepository).count();
        verify(roleRepository).findAll();
        verify(userRepository).countByRoleId(1L);
        verify(userRepository).countByRoleId(2L);
    }
    
    @Test
    void getUserGrowthStatsShouldCalculateCorrectGrowthRates() {
        // 准备测试数据
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
        LocalDateTime weekStart = now.toLocalDate().minusDays(now.getDayOfWeek().getValue() - 1).atStartOfDay();
        LocalDateTime monthStart = now.toLocalDate().withDayOfMonth(1).atStartOfDay();
        
        LocalDateTime yesterdayStart = todayStart.minusDays(1);
        LocalDateTime lastWeekStart = weekStart.minusWeeks(1);
        LocalDateTime lastMonthStart = monthStart.minusMonths(1);
        
        LocalDateTime thirtyDaysAgo = now.minusDays(30);
        
        List<Object[]> registrationData = Arrays.asList(
            new Object[]{"2023-10-01", 5L},
            new Object[]{"2023-10-02", 8L}
        );
        
        // 配置mock行为 - 使用any(LocalDateTime.class)来避免严格匹配问题
        when(userRepository.count()).thenReturn(200L);
        when(userRepository.countByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(10L, 30L, 50L, 5L, 20L, 40L);
        
        when(userRepository.countUserRegistrationsByDateRange(any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(registrationData);
        
        // 执行测试
        UserGrowthStatsVO result = userService.getUserGrowthStats();
        
        // 验证结果
        assertNotNull(result);
        assertEquals(200L, result.getTotalUserCount());
        assertEquals(10L, result.getTodayNewUsers());
        assertEquals(30L, result.getWeekNewUsers());
        assertEquals(50L, result.getMonthNewUsers());
        
        // 增长率计算：(当前 - 上一期) / 上一期 * 100
        assertEquals(100.0, result.getDailyGrowthRate()); // (10 - 5) / 5 * 100 = 100%
        assertEquals(50.0, result.getWeeklyGrowthRate()); // (30 - 20) / 20 * 100 = 50%
        assertEquals(25.0, result.getMonthlyGrowthRate()); // (50 - 40) / 40 * 100 = 25%
        
        // 验证每日注册数据
        assertEquals(2, result.getDailyRegistrations().size());
        assertEquals("2023-10-01", result.getDailyRegistrations().get(0).getDate());
        assertEquals(5L, result.getDailyRegistrations().get(0).getCount());
        assertEquals("2023-10-02", result.getDailyRegistrations().get(1).getDate());
        assertEquals(8L, result.getDailyRegistrations().get(1).getCount());
    }
    
    @Test
    void getUserStatusStatsShouldCalculateCorrectStatusPercentages() {
        // 配置mock行为
        when(userRepository.count()).thenReturn(100L);
        when(userRepository.countByStatus(1)).thenReturn(75L);
        when(userRepository.countByStatus(0)).thenReturn(25L);
        
        // 执行测试
        UserStatusStatsVO result = userService.getUserStatusStats();
        
        // 验证结果
        assertNotNull(result);
        assertEquals(100L, result.getTotalUserCount());
        assertEquals(75L, result.getActiveUserCount());
        assertEquals(25L, result.getDisabledUserCount());
        assertEquals(75.0, result.getActiveUserPercentage());
        assertEquals(25.0, result.getDisabledUserPercentage());
        
        // 验证方法调用
        verify(userRepository).count();
        verify(userRepository).countByStatus(1);
        verify(userRepository).countByStatus(0);
    }
    
    @Test
    void getUserActivityStatsShouldCalculateCorrectActivityMetrics() {
        // 准备测试数据
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneDayAgo = now.minusDays(1);
        LocalDateTime oneWeekAgo = now.minusWeeks(1);
        LocalDateTime oneMonthAgo = now.minusMonths(1);
        
        List<Object[]> activityData = Arrays.asList(
            new Object[]{"2023-10-01", 15L},
            new Object[]{"2023-10-02", 25L}
        );
        
        List<Object[]> hourlyData = Arrays.asList(
            new Object[]{9, 30L},
            new Object[]{14, 45L}
        );
        
        List<Object[]> weekdayData = Arrays.asList(
            new Object[]{1, 20L}, // 周日
            new Object[]{2, 35L}  // 周一
        );
        
        // 配置mock行为 - 使用any(LocalDateTime.class)来避免严格匹配问题
        when(userRepository.count()).thenReturn(200L);
        when(userRepository.countByLastLoginAtAfter(any(LocalDateTime.class))).thenReturn(150L, 50L, 100L);
        
        when(userRepository.countUserActivityByDateRange(any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(activityData);
        when(userRepository.countUserLoginByHourOfDay(any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(hourlyData);
        when(userRepository.countUserLoginByDayOfWeek(any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(weekdayData);
        
        // 执行测试
        UserActivityStatsVO result = userService.getUserActivityStats();
        
        // 验证结果
        assertNotNull(result);
        assertEquals(200L, result.getTotalUserCount());
        assertEquals(150L, result.getActiveUserCount());
        assertEquals(50L, result.getInactiveUserCount()); // 200 - 150 = 50
        assertEquals(50L, result.getTodayActiveUsers());
        assertEquals(100L, result.getWeekActiveUsers());
        assertEquals(150L, result.getMonthActiveUsers());
        assertEquals(75.0, result.getActiveUserPercentage()); // 150 / 200 * 100 = 75%
        
        // 验证每日活跃数据
        assertEquals(2, result.getDailyActiveUsers().size());
        assertEquals("2023-10-01", result.getDailyActiveUsers().get(0).getDate());
        assertEquals(15L, result.getDailyActiveUsers().get(0).getCount());
        assertEquals("2023-10-02", result.getDailyActiveUsers().get(1).getDate());
        assertEquals(25L, result.getDailyActiveUsers().get(1).getCount());
        
        // 验证小时分布
        Map<Integer, Long> hourlyDistribution = result.getHourlyActiveDistribution();
        assertEquals(2, hourlyDistribution.size());
        assertEquals(30L, hourlyDistribution.get(9));
        assertEquals(45L, hourlyDistribution.get(14));
        
        // 验证星期分布
        Map<Integer, Long> weekdayDistribution = result.getWeekdayActiveDistribution();
        assertEquals(2, weekdayDistribution.size());
        assertEquals(20L, weekdayDistribution.get(1));
        assertEquals(35L, weekdayDistribution.get(2));
    }
} 