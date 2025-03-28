package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.model.dto.InstitutionApplyDTO;
import com.zhangziqi.online_course_mine.model.dto.InstitutionApplicationQueryDTO;
import com.zhangziqi.online_course_mine.model.dto.InstitutionUpdateDTO;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import com.zhangziqi.online_course_mine.service.MinioService;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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

    @Mock
    private MinioService minioService;

    @InjectMocks
    private InstitutionServiceImpl institutionService;

    private InstitutionApplyDTO applyDTO;
    private InstitutionApplication application;
    private InstitutionApplicationQueryDTO queryDTO;
    private User user;
    private Institution institution;

    @BeforeEach
    void setUp() {
        institutionService = new InstitutionServiceImpl(
                institutionRepository,
                applicationRepository,
                userRepository,
                emailService,
                storageQuotaService,
                minioService
        );

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

    @Test
    void isInstitutionAdmin_WhenUserNotExists_ThrowsException() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(BusinessException.class, () -> 
                institutionService.isInstitutionAdmin("nonexistent", 1L));
    }
    
    @Test
    void isInstitutionAdmin_WhenUserNotAssociatedWithInstitution_ReturnsFalse() {
        // Arrange
        User differentInstitutionUser = User.builder()
                .id(2L)
                .username("user2")
                .email("user2@example.com")
                .institutionId(2L) // 不同的机构
                .build();
                
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(differentInstitutionUser));
        
        // Act
        boolean result = institutionService.isInstitutionAdmin("user2", 1L);
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    void isInstitutionAdmin_WhenUserEmailEmpty_ReturnsFalse() {
        // Arrange
        User userWithoutEmail = User.builder()
                .id(3L)
                .username("user3")
                .email(null) // 没有邮箱
                .institutionId(1L)
                .build();
                
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(userWithoutEmail));
        
        // Act
        boolean result = institutionService.isInstitutionAdmin("user3", 1L);
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    void isInstitutionAdmin_WhenInstitutionNotExists_ThrowsException() {
        // Arrange
        User userWithInstitution = User.builder()
                .id(1L)
                .username("admin")
                .email("admin@example.com")
                .institutionId(999L) // 确保用户关联到机构
                .build();
                
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(userWithInstitution));
        when(institutionRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(BusinessException.class, () -> 
                institutionService.isInstitutionAdmin("admin", 999L));
    }
    
    @Test
    void isInstitutionAdmin_WhenInstitutionContactEmailEmpty_ReturnsFalse() {
        // Arrange
        Institution institutionWithoutEmail = Institution.builder()
                .id(1L)
                .name("测试机构")
                .contactEmail(null) // 没有联系邮箱
                .build();
                
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(institutionRepository.findById(anyLong())).thenReturn(Optional.of(institutionWithoutEmail));
        
        // Act
        boolean result = institutionService.isInstitutionAdmin("admin", 1L);
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    void isInstitutionAdmin_WhenEmailsMatch_ReturnsTrue() {
        // Arrange
        User adminUser = User.builder()
                .id(4L)
                .username("admin")
                .email("admin@example.com") // 邮箱与机构联系邮箱相同
                .institutionId(1L)
                .build();
                
        Institution institutionWithEmail = Institution.builder()
                .id(1L)
                .name("测试机构")
                .contactEmail("admin@example.com") // 联系邮箱
                .build();
                
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(adminUser));
        when(institutionRepository.findById(anyLong())).thenReturn(Optional.of(institutionWithEmail));
        
        // Act
        boolean result = institutionService.isInstitutionAdmin("admin", 1L);
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    void isInstitutionAdmin_WhenEmailsDontMatch_ReturnsFalse() {
        // Arrange
        User normalUser = User.builder()
                .id(5L)
                .username("user")
                .email("user@example.com") // 邮箱与机构联系邮箱不同
                .institutionId(1L)
                .build();
                
        Institution institutionWithEmail = Institution.builder()
                .id(1L)
                .name("测试机构")
                .contactEmail("admin@example.com") // 联系邮箱
                .build();
                
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(normalUser));
        when(institutionRepository.findById(anyLong())).thenReturn(Optional.of(institutionWithEmail));
        
        // Act
        boolean result = institutionService.isInstitutionAdmin("user", 1L);
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    void getInstitutionDetail_Success() {
        // Arrange
        when(institutionRepository.findById(anyLong())).thenReturn(Optional.of(institution));
        
        // Act
        InstitutionVO result = institutionService.getInstitutionDetail(1L);
        
        // Assert
        assertNotNull(result);
        assertEquals(institution.getId(), result.getId());
        assertEquals(institution.getName(), result.getName());
        assertEquals(institution.getDescription(), result.getDescription());
    }
    
    @Test
    void getInstitutionDetail_NotFound() {
        // Arrange
        when(institutionRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(BusinessException.class, () -> institutionService.getInstitutionDetail(999L));
    }
    
    @Test
    void updateInstitution_Success() {
        // Arrange
        when(institutionRepository.findById(anyLong())).thenReturn(Optional.of(institution));
        when(institutionRepository.save(any(Institution.class))).thenReturn(institution);
        
        // 模拟用户是机构管理员
        User adminUser = User.builder()
                .id(1L)
                .username("admin")
                .email("admin@example.com")
                .institutionId(1L)
                .build();
        
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(adminUser));
        
        // 直接模拟isInstitutionAdmin方法返回true
        InstitutionServiceImpl spyService = spy(institutionService);
        doReturn(true).when(spyService).isInstitutionAdmin(anyString(), anyLong());
        
        // 创建更新DTO
        InstitutionUpdateDTO updateDTO = new InstitutionUpdateDTO();
        updateDTO.setName("更新后的机构名称");
        updateDTO.setDescription("更新后的描述");
        updateDTO.setContactPerson("李四");
        updateDTO.setContactPhone("13900139000");
        updateDTO.setAddress("北京市朝阳区");
        
        // Act
        InstitutionVO result = spyService.updateInstitution(1L, updateDTO, "admin");
        
        // Assert
        assertNotNull(result);
        
        // 验证机构更新
        ArgumentCaptor<Institution> institutionCaptor = ArgumentCaptor.forClass(Institution.class);
        verify(institutionRepository).save(institutionCaptor.capture());
        
        Institution savedInstitution = institutionCaptor.getValue();
        assertEquals(updateDTO.getName(), savedInstitution.getName());
        assertEquals(updateDTO.getDescription(), savedInstitution.getDescription());
        assertEquals(updateDTO.getContactPerson(), savedInstitution.getContactPerson());
        assertEquals(updateDTO.getContactPhone(), savedInstitution.getContactPhone());
        assertEquals(updateDTO.getAddress(), savedInstitution.getAddress());
    }
    
    @Test
    void updateInstitution_NotAdmin_ThrowsException() {
        // Arrange
        // 模拟用户邮箱与机构联系邮箱不匹配
        User normalUser = User.builder()
                .id(5L)
                .username("user")
                .email("user@example.com") 
                .institutionId(1L)
                .build();
                
        Institution institutionWithEmail = Institution.builder()
                .id(1L)
                .name("测试机构")
                .contactEmail("admin@example.com")
                .build();
        
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(normalUser));
        when(institutionRepository.findById(anyLong())).thenReturn(Optional.of(institutionWithEmail));
        
        InstitutionUpdateDTO updateDTO = new InstitutionUpdateDTO();
        updateDTO.setName("更新后的机构名称");
        
        // Act & Assert
        assertThrows(BusinessException.class, () -> 
                institutionService.updateInstitution(1L, updateDTO, "user"));
    }
    
    @Test
    void resetInstitutionRegisterCode_Success() {
        // Arrange
        // 模拟用户是机构管理员
        User adminUser = User.builder()
                .id(1L)
                .username("admin")
                .email("admin@example.com")
                .institutionId(1L)
                .build();
        
        Institution institutionWithEmail = Institution.builder()
                .id(1L)
                .name("测试机构")
                .contactEmail("admin@example.com")
                .registerCode("OLDCODE1")
                .build();
                
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(adminUser));
        when(institutionRepository.findById(anyLong())).thenReturn(Optional.of(institutionWithEmail));
        when(institutionRepository.save(any(Institution.class))).thenReturn(institutionWithEmail);
        when(institutionRepository.existsByRegisterCode(anyString())).thenReturn(false);
        
        // Act
        String newCode = institutionService.resetInstitutionRegisterCode(1L, "admin");
        
        // Assert
        assertNotNull(newCode);
        assertNotEquals("OLDCODE1", newCode);
        
        // 验证保存调用
        ArgumentCaptor<Institution> institutionCaptor = ArgumentCaptor.forClass(Institution.class);
        verify(institutionRepository).save(institutionCaptor.capture());
        
        Institution savedInstitution = institutionCaptor.getValue();
        assertEquals(newCode, savedInstitution.getRegisterCode());
    }
    
    @Test
    void resetInstitutionRegisterCode_NotAdmin_ThrowsException() {
        // Arrange
        // 模拟用户邮箱与机构联系邮箱不匹配
        User normalUser = User.builder()
                .id(5L)
                .username("user")
                .email("user@example.com") 
                .institutionId(1L)
                .build();
                
        Institution institutionWithEmail = Institution.builder()
                .id(1L)
                .name("测试机构")
                .contactEmail("admin@example.com")
                .build();
        
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(normalUser));
        when(institutionRepository.findById(anyLong())).thenReturn(Optional.of(institutionWithEmail));
        
        // Act & Assert
        assertThrows(BusinessException.class, () -> 
                institutionService.resetInstitutionRegisterCode(1L, "user"));
    }

    @Test
    void updateInstitutionLogo_Success() throws IOException {
        // Arrange
        // 模拟用户是机构管理员
        User adminUser = User.builder()
                .id(1L)
                .username("admin")
                .email("admin@example.com")
                .institutionId(1L)
                .build();
        
        Institution institutionWithEmail = Institution.builder()
                .id(1L)
                .name("测试机构")
                .contactEmail("admin@example.com")
                .logo("old-logo-url.jpg")
                .build();
                
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(adminUser));
        when(institutionRepository.findById(anyLong())).thenReturn(Optional.of(institutionWithEmail));
        when(institutionRepository.save(any(Institution.class))).thenReturn(institutionWithEmail);
        
        // 模拟文件上传
        MockMultipartFile logoFile = new MockMultipartFile(
            "logo", 
            "logo.jpg", 
            "image/jpeg", 
            "test image content".getBytes()
        );
        
        String newLogoUrl = "http://example.com/storage/institutions/1/logo.jpg";
        when(minioService.uploadFile(anyString(), any(InputStream.class), anyString())).thenReturn(newLogoUrl);
        
        // Act
        InstitutionVO result = institutionService.updateInstitutionLogo(1L, logoFile, "admin");
        
        // Assert
        assertNotNull(result);
        assertEquals(newLogoUrl, result.getLogo());
        
        // 验证存储服务调用
        verify(minioService).uploadFile(anyString(), any(InputStream.class), anyString());
        
        // 验证更新机构Logo
        ArgumentCaptor<Institution> institutionCaptor = ArgumentCaptor.forClass(Institution.class);
        verify(institutionRepository).save(institutionCaptor.capture());
        
        Institution savedInstitution = institutionCaptor.getValue();
        assertEquals(newLogoUrl, savedInstitution.getLogo());
    }
    
    @Test
    void updateInstitutionLogo_NotAdmin_ThrowsException() throws IOException {
        // Arrange
        // 模拟用户邮箱与机构联系邮箱不匹配
        User normalUser = User.builder()
                .id(5L)
                .username("user")
                .email("user@example.com") 
                .institutionId(1L)
                .build();
                
        Institution institutionWithEmail = Institution.builder()
                .id(1L)
                .name("测试机构")
                .contactEmail("admin@example.com")
                .build();
        
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(normalUser));
        when(institutionRepository.findById(anyLong())).thenReturn(Optional.of(institutionWithEmail));
        
        MockMultipartFile logoFile = new MockMultipartFile(
            "logo", 
            "logo.jpg", 
            "image/jpeg", 
            "test image content".getBytes()
        );
        
        // Act & Assert
        assertThrows(BusinessException.class, () -> 
                institutionService.updateInstitutionLogo(1L, logoFile, "user"));
    }
    
    @Test
    void updateInstitutionLogo_InvalidFileType_ThrowsException() throws IOException {
        // Arrange
        // 模拟用户是机构管理员
        User adminUser = User.builder()
                .id(1L)
                .username("admin")
                .email("admin@example.com")
                .institutionId(1L)
                .build();
        
        Institution institutionWithEmail = Institution.builder()
                .id(1L)
                .name("测试机构")
                .contactEmail("admin@example.com")
                .build();
                
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(adminUser));
        when(institutionRepository.findById(anyLong())).thenReturn(Optional.of(institutionWithEmail));
        
        // 创建非图片类型文件
        MockMultipartFile textFile = new MockMultipartFile(
            "logo", 
            "document.txt", 
            "text/plain", 
            "not an image".getBytes()
        );
        
        // Act & Assert
        assertThrows(BusinessException.class, () -> 
                institutionService.updateInstitutionLogo(1L, textFile, "admin"));
    }
} 