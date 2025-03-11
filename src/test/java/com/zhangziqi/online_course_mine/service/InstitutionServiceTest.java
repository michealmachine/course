package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.model.dto.InstitutionApplyDTO;
import com.zhangziqi.online_course_mine.model.dto.InstitutionApplicationQueryDTO;
import com.zhangziqi.online_course_mine.model.entity.Institution;
import com.zhangziqi.online_course_mine.model.entity.InstitutionApplication;
import com.zhangziqi.online_course_mine.model.entity.User;
import com.zhangziqi.online_course_mine.model.enums.QuotaType;
import com.zhangziqi.online_course_mine.model.vo.InstitutionApplicationVO;
import com.zhangziqi.online_course_mine.model.vo.InstitutionVO;
import com.zhangziqi.online_course_mine.repository.InstitutionApplicationRepository;
import com.zhangziqi.online_course_mine.repository.InstitutionRepository;
import com.zhangziqi.online_course_mine.repository.UserRepository;
import com.zhangziqi.online_course_mine.service.impl.InstitutionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class InstitutionServiceTest {

    @Mock
    private InstitutionRepository institutionRepository;

    @Mock
    private InstitutionApplicationRepository applicationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;
    
    @Mock
    private StorageQuotaService storageQuotaService;

    @InjectMocks
    private InstitutionServiceImpl institutionService;

    private InstitutionApplyDTO applyDTO;
    private InstitutionApplication application;
    private InstitutionApplicationQueryDTO queryDTO;
    private User user;
    private Institution institution;

    @BeforeEach
    void setUp() {
        // 设置申请DTO
        applyDTO = new InstitutionApplyDTO();
        applyDTO.setName("测试机构");
        applyDTO.setDescription("这是一个测试机构");
        applyDTO.setContactPerson("张三");
        applyDTO.setContactPhone("13800138000");
        applyDTO.setContactEmail("test@example.com");
        applyDTO.setAddress("北京市海淀区");

        // 设置申请实体
        application = InstitutionApplication.builder()
                .id(1L)
                .applicationId("APP12345678")
                .name("测试机构")
                .description("这是一个测试机构")
                .contactPerson("张三")
                .contactPhone("13800138000")
                .contactEmail("test@example.com")
                .address("北京市海淀区")
                .status(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // 设置查询DTO
        queryDTO = new InstitutionApplicationQueryDTO();
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(10);
        queryDTO.setStatus(0);

        // 设置用户
        user = User.builder()
                .id(1L)
                .username("admin")
                .password("password")
                .email("admin@example.com")
                .build();

        // 设置机构
        institution = Institution.builder()
                .id(1L)
                .name("测试机构")
                .description("这是一个测试机构")
                .contactPerson("张三")
                .contactPhone("13800138000")
                .contactEmail("test@example.com")
                .address("北京市海淀区")
                .registerCode("ABC12345")
                .status(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void applyInstitution_Success() {
        // Arrange
        when(applicationRepository.save(any(InstitutionApplication.class))).thenReturn(application);
        doNothing().when(emailService).sendApplicationConfirmationEmail(anyString(), anyString(), anyString());

        // Act
        String applicationId = institutionService.applyInstitution(applyDTO);

        // Assert
        assertNotNull(applicationId);
        verify(applicationRepository, times(1)).save(any(InstitutionApplication.class));
        verify(emailService, times(1)).sendApplicationConfirmationEmail(anyString(), anyString(), anyString());
    }

    @Test
    void getApplicationStatus_Success() {
        // Arrange
        when(applicationRepository.findByApplicationIdAndContactEmail(anyString(), anyString()))
                .thenReturn(Optional.of(application));

        // Act
        InstitutionApplicationVO result = institutionService.getApplicationStatus("APP12345678", "test@example.com");

        // Assert
        assertNotNull(result);
        assertEquals(application.getId(), result.getId());
        assertEquals(application.getApplicationId(), result.getApplicationId());
        assertEquals(application.getName(), result.getName());
    }

    @Test
    void getApplicationStatus_NotFound() {
        // Arrange
        when(applicationRepository.findByApplicationIdAndContactEmail(anyString(), anyString()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BusinessException.class, () ->
                institutionService.getApplicationStatus("APP12345678", "test@example.com"));
    }

    @Test
    void getApplications_Success() {
        // Arrange
        Page<InstitutionApplication> page = new PageImpl<>(Collections.singletonList(application));
        when(applicationRepository.findByStatus(anyInt(), any(Pageable.class))).thenReturn(page);

        // Act
        Page<InstitutionApplicationVO> result = institutionService.getApplications(queryDTO);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getApplicationDetail_Success() {
        // Arrange
        when(applicationRepository.findById(anyLong())).thenReturn(Optional.of(application));

        // Act
        InstitutionApplicationVO result = institutionService.getApplicationDetail(1L);

        // Assert
        assertNotNull(result);
        assertEquals(application.getId(), result.getId());
        assertEquals(application.getName(), result.getName());
    }

    @Test
    void getApplicationDetail_NotFound() {
        // Arrange
        when(applicationRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BusinessException.class, () -> institutionService.getApplicationDetail(1L));
    }

    @Test
    void approveApplication_Success() {
        // Arrange
        when(applicationRepository.findById(anyLong())).thenReturn(Optional.of(application));
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(institutionRepository.save(any(Institution.class))).thenReturn(institution);
        when(applicationRepository.save(any(InstitutionApplication.class))).thenReturn(application);
        doNothing().when(emailService).sendApplicationApprovedEmail(anyString(), anyString(), anyString());
        doNothing().when(storageQuotaService).setQuota(anyLong(), any(QuotaType.class), anyLong(), any());

        // Act
        InstitutionVO result = institutionService.approveApplication(1L, "admin");

        // Assert
        assertNotNull(result);
        assertEquals(institution.getId(), result.getId());
        assertEquals(institution.getName(), result.getName());
        
        // 验证存储配额初始化
        verify(storageQuotaService, times(1)).setQuota(
                eq(institution.getId()),
                eq(QuotaType.VIDEO),
                eq(5L * 1024 * 1024 * 1024),
                isNull()
        );
        verify(storageQuotaService, times(1)).setQuota(
                eq(institution.getId()),
                eq(QuotaType.DOCUMENT),
                eq(2L * 1024 * 1024 * 1024),
                isNull()
        );
        verify(storageQuotaService, times(1)).setQuota(
                eq(institution.getId()),
                eq(QuotaType.TOTAL),
                eq(10L * 1024 * 1024 * 1024),
                isNull()
        );
        
        verify(emailService, times(1)).sendApplicationApprovedEmail(anyString(), anyString(), anyString());
    }

    @Test
    void approveApplication_ApplicationNotFound() {
        // Arrange
        when(applicationRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BusinessException.class, () -> institutionService.approveApplication(1L, "admin"));
    }

    @Test
    void approveApplication_WrongStatus() {
        // Arrange
        application.setStatus(1); // 已通过状态
        when(applicationRepository.findById(anyLong())).thenReturn(Optional.of(application));

        // Act & Assert
        assertThrows(BusinessException.class, () -> institutionService.approveApplication(1L, "admin"));
    }

    @Test
    void rejectApplication_Success() {
        // Arrange
        when(applicationRepository.findById(anyLong())).thenReturn(Optional.of(application));
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(applicationRepository.save(any(InstitutionApplication.class))).thenReturn(application);
        doNothing().when(emailService).sendApplicationRejectedEmail(anyString(), anyString(), anyString());

        // Act
        institutionService.rejectApplication(1L, "原因", "admin");

        // Assert
        verify(applicationRepository, times(1)).save(any(InstitutionApplication.class));
        verify(emailService, times(1)).sendApplicationRejectedEmail(anyString(), anyString(), anyString());
    }

    @Test
    void getInstitutionRegisterCode_Success() {
        // Arrange
        user.setInstitutionId(1L);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(institutionRepository.findById(anyLong())).thenReturn(Optional.of(institution));

        // Act
        String result = institutionService.getInstitutionRegisterCode("admin");

        // Assert
        assertEquals(institution.getRegisterCode(), result);
    }

    @Test
    void getInstitutionRegisterCode_UserNotAssociatedWithInstitution() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));

        // Act & Assert
        assertThrows(BusinessException.class, () -> institutionService.getInstitutionRegisterCode("admin"));
    }

    @Test
    void approveApplication_ShouldRollbackOnQuotaError() {
        // Arrange
        when(applicationRepository.findById(anyLong())).thenReturn(Optional.of(application));
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(institutionRepository.save(any(Institution.class))).thenReturn(institution);
        doThrow(new RuntimeException("配额设置失败")).when(storageQuotaService)
                .setQuota(anyLong(), any(QuotaType.class), anyLong(), any());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> institutionService.approveApplication(1L, "admin"));
        
        // 验证事务回滚
        verify(emailService, never()).sendApplicationApprovedEmail(anyString(), anyString(), anyString());
    }
} 