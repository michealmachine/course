package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.model.dto.InstitutionRegisterDTO;
import com.zhangziqi.online_course_mine.model.entity.Institution;
import com.zhangziqi.online_course_mine.model.entity.Role;
import com.zhangziqi.online_course_mine.model.entity.User;
import com.zhangziqi.online_course_mine.model.enums.RoleEnum;
import com.zhangziqi.online_course_mine.repository.InstitutionRepository;
import com.zhangziqi.online_course_mine.repository.RoleRepository;
import com.zhangziqi.online_course_mine.repository.UserRepository;
import com.zhangziqi.online_course_mine.service.impl.InstitutionAuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class InstitutionAuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private InstitutionRepository institutionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private InstitutionAuthServiceImpl institutionAuthService;

    private InstitutionRegisterDTO registerDTO;
    private Institution institution;
    private Role institutionRole;

    @BeforeEach
    void setUp() {
        // 设置注册DTO
        registerDTO = new InstitutionRegisterDTO();
        registerDTO.setUsername("institution_user");
        registerDTO.setPassword("password123");
        registerDTO.setEmail("user@example.com");
        registerDTO.setPhone("13800138000");
        registerDTO.setInstitutionCode("ABC12345");

        // 设置机构
        institution = Institution.builder()
                .id(1L)
                .name("测试机构")
                .description("这是一个测试机构")
                .contactPerson("张三")
                .contactPhone("13800138000")
                .contactEmail("contact@example.com")
                .address("北京市海淀区")
                .registerCode("ABC12345")
                .status(1) // 正常状态
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // 设置机构角色
        institutionRole = Role.builder()
                .id(2L)
                .name("机构用户")
                .code(RoleEnum.INSTITUTION.getCode())
                .build();
    }

    @Test
    void registerWithInstitutionCode_Success() {
        // Arrange
        when(institutionRepository.findByRegisterCode(anyString())).thenReturn(Optional.of(institution));
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(roleRepository.findByCode(RoleEnum.INSTITUTION.getCode())).thenReturn(Optional.of(institutionRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        institutionAuthService.registerWithInstitutionCode(registerDTO);

        // Assert
        verify(institutionRepository).findByRegisterCode(registerDTO.getInstitutionCode());
        verify(userRepository).existsByUsername(registerDTO.getUsername());
        verify(userRepository).existsByEmail(registerDTO.getEmail());
        verify(roleRepository).findByCode(RoleEnum.INSTITUTION.getCode());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerWithInstitutionCode_InvalidCode() {
        // Arrange
        when(institutionRepository.findByRegisterCode(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BusinessException.class, () -> institutionAuthService.registerWithInstitutionCode(registerDTO));
    }

    @Test
    void registerWithInstitutionCode_InactiveInstitution() {
        // Arrange
        institution.setStatus(0); // 待审核状态
        when(institutionRepository.findByRegisterCode(anyString())).thenReturn(Optional.of(institution));

        // Act & Assert
        assertThrows(BusinessException.class, () -> institutionAuthService.registerWithInstitutionCode(registerDTO));
    }

    @Test
    void registerWithInstitutionCode_UsernameExists() {
        // Arrange
        when(institutionRepository.findByRegisterCode(anyString())).thenReturn(Optional.of(institution));
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(BusinessException.class, () -> institutionAuthService.registerWithInstitutionCode(registerDTO));
    }

    @Test
    void registerWithInstitutionCode_EmailExists() {
        // Arrange
        when(institutionRepository.findByRegisterCode(anyString())).thenReturn(Optional.of(institution));
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(BusinessException.class, () -> institutionAuthService.registerWithInstitutionCode(registerDTO));
    }

    @Test
    void registerWithInstitutionCode_RoleNotFound() {
        // Arrange
        when(institutionRepository.findByRegisterCode(anyString())).thenReturn(Optional.of(institution));
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByCode(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BusinessException.class, () -> institutionAuthService.registerWithInstitutionCode(registerDTO));
    }
} 