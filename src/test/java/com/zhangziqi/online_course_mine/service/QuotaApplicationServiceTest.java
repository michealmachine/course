package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.model.converter.QuotaApplicationConverter;
import com.zhangziqi.online_course_mine.model.dto.QuotaApplicationDTO;
import com.zhangziqi.online_course_mine.model.entity.Institution;
import com.zhangziqi.online_course_mine.model.entity.QuotaApplication;
import com.zhangziqi.online_course_mine.model.entity.User;
import com.zhangziqi.online_course_mine.model.enums.QuotaType;
import com.zhangziqi.online_course_mine.model.vo.QuotaApplicationVO;
import com.zhangziqi.online_course_mine.repository.InstitutionRepository;
import com.zhangziqi.online_course_mine.repository.QuotaApplicationRepository;
import com.zhangziqi.online_course_mine.repository.UserRepository;
import com.zhangziqi.online_course_mine.service.impl.QuotaApplicationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class QuotaApplicationServiceTest {

    @Mock
    private QuotaApplicationRepository quotaApplicationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private InstitutionRepository institutionRepository;

    @Mock
    private InstitutionService institutionService;

    @Mock
    private StorageQuotaService storageQuotaService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private QuotaApplicationServiceImpl quotaApplicationService;

    private User user;
    private Institution institution;
    private QuotaApplication application;
    private QuotaApplicationDTO applicationDTO;

    @BeforeEach
    void setUp() {
        // 设置用户
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .institutionId(1L)
                .build();

        // 设置机构
        institution = Institution.builder()
                .id(1L)
                .name("测试机构")
                .contactEmail("admin@example.com")
                .build();

        // 设置配额申请
        application = QuotaApplication.builder()
                .id(1L)
                .applicationId("QA12345678")
                .institutionId(1L)
                .applicantId(1L)
                .quotaType(QuotaType.VIDEO)
                .requestedBytes(5L * 1024 * 1024 * 1024) // 5GB
                .reason("需要更多存储空间用于教学视频")
                .status(0) // 待审核
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // 设置申请DTO
        applicationDTO = new QuotaApplicationDTO();
        applicationDTO.setQuotaType(QuotaType.VIDEO);
        applicationDTO.setRequestedBytes(5L * 1024 * 1024 * 1024); // 5GB
        applicationDTO.setReason("需要更多存储空间用于教学视频");
    }

    @Test
    void applyQuota_Success() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(quotaApplicationRepository.save(any(QuotaApplication.class))).thenReturn(application);
        doNothing().when(emailService).sendVerificationCode(anyString(), anyString());

        // Act
        String applicationId = quotaApplicationService.applyQuota("testuser", applicationDTO);

        // Assert
        assertNotNull(applicationId);
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(quotaApplicationRepository, times(1)).save(any(QuotaApplication.class));
        verify(emailService, times(1)).sendVerificationCode(anyString(), anyString());
    }

    @Test
    void applyQuota_UserNotFound() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BusinessException.class, () -> quotaApplicationService.applyQuota("nonexistent", applicationDTO));
    }

    @Test
    void applyQuota_UserNotAssociatedWithInstitution() {
        // Arrange
        User userWithoutInstitution = User.builder()
                .id(2L)
                .username("noninstitution")
                .email("noninstitution@example.com")
                .institutionId(null)
                .build();

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(userWithoutInstitution));

        // Act & Assert
        assertThrows(BusinessException.class, () -> quotaApplicationService.applyQuota("noninstitution", applicationDTO));
    }

    @Test
    void getApplicationStatus_Success() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(quotaApplicationRepository.findByApplicationIdAndApplicantId(anyString(), anyLong()))
                .thenReturn(Optional.of(application));
        when(institutionRepository.findById(anyLong())).thenReturn(Optional.of(institution));

        // Act
        QuotaApplicationVO result = quotaApplicationService.getApplicationStatus("testuser", "QA12345678");

        // Assert
        assertNotNull(result);
        assertEquals(application.getId(), result.getId());
        assertEquals(application.getApplicationId(), result.getApplicationId());
        assertEquals(application.getQuotaType(), result.getQuotaType());
        assertEquals(application.getRequestedBytes(), result.getRequestedBytes());
    }

    @Test
    void getApplicationStatus_ApplicationNotFound() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(quotaApplicationRepository.findByApplicationIdAndApplicantId(anyString(), anyLong()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BusinessException.class, () -> 
                quotaApplicationService.getApplicationStatus("testuser", "QA99999999"));
    }

    @Test
    void getUserApplications_Success() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(quotaApplicationRepository.findByApplicantId(anyLong(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(application)));
        when(institutionRepository.findById(anyLong())).thenReturn(Optional.of(institution));

        // Act
        Page<QuotaApplicationVO> result = quotaApplicationService.getUserApplications("testuser", null, 1, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(application.getId(), result.getContent().get(0).getId());
    }

    @Test
    void getInstitutionApplications_Success() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(institutionService.isInstitutionAdmin(anyString(), anyLong())).thenReturn(true);
        when(quotaApplicationRepository.findByInstitutionId(anyLong(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(application)));
        when(institutionRepository.findById(anyLong())).thenReturn(Optional.of(institution));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        // Act
        Page<QuotaApplicationVO> result = quotaApplicationService.getInstitutionApplications("testuser", null, 1, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(application.getId(), result.getContent().get(0).getId());
    }

    @Test
    void getInstitutionApplications_NotAdmin() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(institutionService.isInstitutionAdmin(anyString(), anyLong())).thenReturn(false);

        // Act & Assert
        assertThrows(BusinessException.class, () -> 
                quotaApplicationService.getInstitutionApplications("testuser", null, 1, 10));
    }

    @Test
    void approveApplication_Success() {
        // Arrange
        User adminUser = User.builder()
                .id(2L)
                .username("admin")
                .email("admin@example.com")
                .build();

        QuotaApplication pendingApplication = QuotaApplication.builder()
                .id(1L)
                .applicationId("QA12345678")
                .institutionId(1L)
                .applicantId(1L)
                .quotaType(QuotaType.VIDEO)
                .requestedBytes(5L * 1024 * 1024 * 1024) // 5GB
                .reason("需要更多存储空间用于教学视频")
                .status(0) // 待审核
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(quotaApplicationRepository.findById(anyLong())).thenReturn(Optional.of(pendingApplication));
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(adminUser));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(institutionRepository.findById(anyLong())).thenReturn(Optional.of(institution));
        when(quotaApplicationRepository.save(any(QuotaApplication.class))).thenReturn(pendingApplication);
        doNothing().when(storageQuotaService).increaseQuota(anyLong(), any(QuotaType.class), anyLong());
        doNothing().when(emailService).sendApplicationApprovedEmail(anyString(), anyString(), anyString());

        // Act
        QuotaApplicationVO result = quotaApplicationService.approveApplication(1L, "admin");

        // Assert
        assertNotNull(result);
        
        // 验证申请状态更新
        ArgumentCaptor<QuotaApplication> applicationCaptor = ArgumentCaptor.forClass(QuotaApplication.class);
        verify(quotaApplicationRepository).save(applicationCaptor.capture());
        
        QuotaApplication capturedApplication = applicationCaptor.getValue();
        assertEquals(1, capturedApplication.getStatus()); // 状态改为已通过
        assertEquals(adminUser.getId(), capturedApplication.getReviewerId());
        assertNotNull(capturedApplication.getReviewedAt());
        
        // 验证配额增加
        verify(storageQuotaService).increaseQuota(
                eq(pendingApplication.getInstitutionId()),
                eq(pendingApplication.getQuotaType()),
                eq(pendingApplication.getRequestedBytes())
        );
        
        // 验证邮件发送
        verify(emailService).sendApplicationApprovedEmail(
                eq(user.getEmail()),
                anyString(),
                anyString()
        );
    }

    @Test
    void approveApplication_WrongStatus() {
        // Arrange
        QuotaApplication approvedApplication = QuotaApplication.builder()
                .id(1L)
                .applicationId("QA12345678")
                .institutionId(1L)
                .applicantId(1L)
                .quotaType(QuotaType.VIDEO)
                .requestedBytes(5L * 1024 * 1024 * 1024)
                .reason("需要更多存储空间用于教学视频")
                .status(1) // 已通过
                .reviewerId(2L)
                .reviewedAt(LocalDateTime.now().minusDays(1))
                .build();

        when(quotaApplicationRepository.findById(anyLong())).thenReturn(Optional.of(approvedApplication));

        // Act & Assert
        assertThrows(BusinessException.class, () -> quotaApplicationService.approveApplication(1L, "admin"));
    }

    @Test
    void rejectApplication_Success() {
        // Arrange
        User adminUser = User.builder()
                .id(2L)
                .username("admin")
                .email("admin@example.com")
                .build();

        QuotaApplication pendingApplication = QuotaApplication.builder()
                .id(1L)
                .applicationId("QA12345678")
                .institutionId(1L)
                .applicantId(1L)
                .quotaType(QuotaType.VIDEO)
                .requestedBytes(5L * 1024 * 1024 * 1024)
                .reason("需要更多存储空间用于教学视频")
                .status(0) // 待审核
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(quotaApplicationRepository.findById(anyLong())).thenReturn(Optional.of(pendingApplication));
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(adminUser));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(quotaApplicationRepository.save(any(QuotaApplication.class))).thenReturn(pendingApplication);
        doNothing().when(emailService).sendApplicationRejectedEmail(anyString(), anyString(), anyString());

        String rejectReason = "超出了机构年度配额限制";

        // Act
        quotaApplicationService.rejectApplication(1L, rejectReason, "admin");

        // Assert
        // 验证申请状态更新
        ArgumentCaptor<QuotaApplication> applicationCaptor = ArgumentCaptor.forClass(QuotaApplication.class);
        verify(quotaApplicationRepository).save(applicationCaptor.capture());
        
        QuotaApplication capturedApplication = applicationCaptor.getValue();
        assertEquals(2, capturedApplication.getStatus()); // 状态改为已拒绝
        assertEquals(adminUser.getId(), capturedApplication.getReviewerId());
        assertEquals(rejectReason, capturedApplication.getReviewComment());
        assertNotNull(capturedApplication.getReviewedAt());
        
        // 验证邮件发送
        verify(emailService).sendApplicationRejectedEmail(
                eq(user.getEmail()),
                anyString(),
                eq(rejectReason)
        );
    }
} 