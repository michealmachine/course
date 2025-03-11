package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.exception.ResourceNotFoundException;
import com.zhangziqi.online_course_mine.model.entity.Institution;
import com.zhangziqi.online_course_mine.model.entity.Media;
import com.zhangziqi.online_course_mine.model.entity.StorageQuota;
import com.zhangziqi.online_course_mine.model.enums.QuotaType;
import com.zhangziqi.online_course_mine.model.vo.StorageQuotaVO;
import com.zhangziqi.online_course_mine.repository.InstitutionRepository;
import com.zhangziqi.online_course_mine.repository.MediaRepository;
import com.zhangziqi.online_course_mine.repository.StorageQuotaRepository;
import com.zhangziqi.online_course_mine.service.impl.StorageQuotaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.InOrder;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageQuotaServiceTest {

    @Mock
    private StorageQuotaRepository storageQuotaRepository;

    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private InstitutionRepository institutionRepository;

    @InjectMocks
    private StorageQuotaServiceImpl storageQuotaService;

    private Institution testInstitution;
    private StorageQuota testQuota;
    private static final Long INSTITUTION_ID = 1L;

    @BeforeEach
    void setUp() {
        testInstitution = Institution.builder()
                .id(INSTITUTION_ID)
                .name("测试机构")
                .status(1)
                .build();

        testQuota = new StorageQuota();
        testQuota.setId(1L);
        testQuota.setInstitution(testInstitution);
        testQuota.setType(QuotaType.VIDEO);
        testQuota.setTotalQuota(5L * 1024 * 1024 * 1024); // 5GB
        testQuota.setUsedQuota(1L * 1024 * 1024 * 1024);  // 1GB
        testQuota.setCreatedAt(LocalDateTime.now());
        testQuota.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void hasEnoughQuota_WhenInstitutionNotExists_ReturnsFalse() {
        when(institutionRepository.findById(INSTITUTION_ID)).thenReturn(Optional.empty());

        boolean result = storageQuotaService.hasEnoughQuota(INSTITUTION_ID, QuotaType.VIDEO, 1024L);

        assertFalse(result);
        verify(institutionRepository).findById(INSTITUTION_ID);
        verifyNoInteractions(storageQuotaRepository);
    }

    @Test
    void hasEnoughQuota_WhenQuotaNotExists_ReturnsTrue() {
        when(institutionRepository.findById(INSTITUTION_ID)).thenReturn(Optional.of(testInstitution));
        when(storageQuotaRepository.findByInstitutionAndType(testInstitution, QuotaType.VIDEO))
                .thenReturn(Optional.empty());

        boolean result = storageQuotaService.hasEnoughQuota(INSTITUTION_ID, QuotaType.VIDEO, 1024L);

        assertTrue(result);
    }

    @Test
    void hasEnoughQuota_WhenQuotaExpired_ReturnsFalse() {
        when(institutionRepository.findById(INSTITUTION_ID)).thenReturn(Optional.of(testInstitution));
        testQuota.setExpiresAt(LocalDateTime.now().minusDays(1));
        when(storageQuotaRepository.findByInstitutionAndType(testInstitution, QuotaType.VIDEO))
                .thenReturn(Optional.of(testQuota));

        boolean result = storageQuotaService.hasEnoughQuota(INSTITUTION_ID, QuotaType.VIDEO, 1024L);

        assertFalse(result);
    }

    @Test
    void hasEnoughQuota_WhenNotEnoughSpace_ReturnsFalse() {
        when(institutionRepository.findById(INSTITUTION_ID)).thenReturn(Optional.of(testInstitution));
        when(storageQuotaRepository.findByInstitutionAndType(testInstitution, QuotaType.VIDEO))
                .thenReturn(Optional.of(testQuota));

        // 尝试上传超过剩余配额的文件
        long remainingQuota = testQuota.getTotalQuota() - testQuota.getUsedQuota();
        boolean result = storageQuotaService.hasEnoughQuota(INSTITUTION_ID, QuotaType.VIDEO, remainingQuota + 1024L);

        assertFalse(result);
    }

    @Test
    void hasEnoughQuota_WhenEnoughSpace_ReturnsTrue() {
        when(institutionRepository.findById(INSTITUTION_ID)).thenReturn(Optional.of(testInstitution));
        when(storageQuotaRepository.findByInstitutionAndType(testInstitution, QuotaType.VIDEO))
                .thenReturn(Optional.of(testQuota));

        // 尝试上传小于剩余配额的文件
        long remainingQuota = testQuota.getTotalQuota() - testQuota.getUsedQuota();
        boolean result = storageQuotaService.hasEnoughQuota(INSTITUTION_ID, QuotaType.VIDEO, remainingQuota - 1024L);

        assertTrue(result);
    }

    @Test
    void getQuotaInfo_WhenInstitutionNotExists_ThrowsException() {
        when(institutionRepository.findById(INSTITUTION_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                storageQuotaService.getQuotaInfo(INSTITUTION_ID, QuotaType.VIDEO));
    }

    @Test
    void getQuotaInfo_WhenQuotaExists_ReturnsQuotaInfo() {
        when(institutionRepository.findById(INSTITUTION_ID)).thenReturn(Optional.of(testInstitution));
        when(storageQuotaRepository.findByInstitutionAndType(testInstitution, QuotaType.VIDEO))
                .thenReturn(Optional.of(testQuota));

        StorageQuotaVO result = storageQuotaService.getQuotaInfo(INSTITUTION_ID, QuotaType.VIDEO);

        assertNotNull(result);
        assertEquals(testQuota.getId(), result.getId());
        assertEquals(testQuota.getType().name(), result.getType());
        assertEquals(testQuota.getTotalQuota(), result.getTotalQuota());
        assertEquals(testQuota.getUsedQuota(), result.getUsedQuota());
        assertEquals(testQuota.getTotalQuota() - testQuota.getUsedQuota(), result.getAvailableQuota());
    }

    @Test
    void getAllQuotas_WhenInstitutionNotExists_ThrowsException() {
        when(institutionRepository.findById(INSTITUTION_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                storageQuotaService.getAllQuotas(INSTITUTION_ID));
    }

    @Test
    void getAllQuotas_WhenQuotasExist_ReturnsAllQuotas() {
        when(institutionRepository.findById(INSTITUTION_ID)).thenReturn(Optional.of(testInstitution));
        StorageQuota documentQuota = new StorageQuota();
        documentQuota.setId(2L);
        documentQuota.setInstitution(testInstitution);
        documentQuota.setType(QuotaType.DOCUMENT);
        documentQuota.setTotalQuota(2L * 1024 * 1024 * 1024);
        documentQuota.setUsedQuota(512L * 1024 * 1024);

        when(storageQuotaRepository.findByInstitution(testInstitution))
                .thenReturn(Arrays.asList(testQuota, documentQuota));

        List<StorageQuotaVO> results = storageQuotaService.getAllQuotas(INSTITUTION_ID);

        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(q -> q.getType().equals(QuotaType.VIDEO.name())));
        assertTrue(results.stream().anyMatch(q -> q.getType().equals(QuotaType.DOCUMENT.name())));
    }

    @Test
    void updateUsedQuota_WhenInstitutionNotExists_ThrowsException() {
        when(institutionRepository.findById(INSTITUTION_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                storageQuotaService.updateUsedQuota(INSTITUTION_ID, QuotaType.VIDEO, 1024L));
    }

    @Test
    void updateUsedQuota_WhenQuotaExists_UpdatesSuccessfully() {
        // Arrange
        when(institutionRepository.findById(INSTITUTION_ID)).thenReturn(Optional.of(testInstitution));
        
        // 设置VIDEO配额的初始值
        long initialVideoQuota = 1L * 1024 * 1024 * 1024; // 1GB
        testQuota.setUsedQuota(initialVideoQuota);
        when(storageQuotaRepository.findByInstitutionAndType(testInstitution, QuotaType.VIDEO))
                .thenReturn(Optional.of(testQuota));
        
        // 设置TOTAL配额的初始值
        long initialTotalQuota = 2L * 1024 * 1024 * 1024; // 2GB
        StorageQuota totalQuota = new StorageQuota();
        totalQuota.setId(3L);
        totalQuota.setInstitution(testInstitution);
        totalQuota.setType(QuotaType.TOTAL);
        totalQuota.setTotalQuota(10L * 1024 * 1024 * 1024); // 10GB
        totalQuota.setUsedQuota(initialTotalQuota);
        when(storageQuotaRepository.findByInstitutionAndType(testInstitution, QuotaType.TOTAL))
                .thenReturn(Optional.of(totalQuota));
        
        // Mock save方法以返回保存的对象
        when(storageQuotaRepository.save(any(StorageQuota.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // 使用ArgumentCaptor捕获save方法的参数
        ArgumentCaptor<StorageQuota> quotaCaptor = ArgumentCaptor.forClass(StorageQuota.class);

        // 更新配额使用量
        long sizeDelta = 1024L * 1024; // 1MB
        storageQuotaService.updateUsedQuota(INSTITUTION_ID, QuotaType.VIDEO, sizeDelta);

        // 验证调用次数
        verify(institutionRepository, times(2)).findById(INSTITUTION_ID);
        verify(storageQuotaRepository, times(1)).findByInstitutionAndType(testInstitution, QuotaType.VIDEO);
        verify(storageQuotaRepository, times(1)).findByInstitutionAndType(testInstitution, QuotaType.TOTAL);
        verify(storageQuotaRepository, times(2)).save(quotaCaptor.capture());
        
        // 获取所有捕获的参数
        List<StorageQuota> capturedQuotas = quotaCaptor.getAllValues();
        
        // 计算期望的新值
        long expectedVideoQuota = initialVideoQuota + sizeDelta;
        long expectedTotalQuota = initialTotalQuota + sizeDelta;
        
        // 验证两种配额类型都被正确更新
        boolean videoQuotaUpdated = false;
        boolean totalQuotaUpdated = false;
        
        for (StorageQuota quota : capturedQuotas) {
            if (quota.getType() == QuotaType.VIDEO) {
                System.out.printf("VIDEO配额 - 期望值: %d, 实际值: %d%n", expectedVideoQuota, quota.getUsedQuota());
                assertEquals(expectedVideoQuota, quota.getUsedQuota(), 
                    String.format("VIDEO配额更新错误 - 初始值: %d, 增加值: %d, 期望值: %d, 实际值: %d", 
                        initialVideoQuota, sizeDelta, expectedVideoQuota, quota.getUsedQuota()));
                videoQuotaUpdated = true;
            } else if (quota.getType() == QuotaType.TOTAL) {
                System.out.printf("TOTAL配额 - 期望值: %d, 实际值: %d%n", expectedTotalQuota, quota.getUsedQuota());
                assertEquals(expectedTotalQuota, quota.getUsedQuota(),
                    String.format("TOTAL配额更新错误 - 初始值: %d, 增加值: %d, 期望值: %d, 实际值: %d",
                        initialTotalQuota, sizeDelta, expectedTotalQuota, quota.getUsedQuota()));
                totalQuotaUpdated = true;
            }
        }
        
        assertTrue(videoQuotaUpdated, "VIDEO配额应该被更新");
        assertTrue(totalQuotaUpdated, "TOTAL配额应该被更新");
    }

    @Test
    void setQuota_WhenInstitutionNotExists_ThrowsException() {
        when(institutionRepository.findById(INSTITUTION_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                storageQuotaService.setQuota(INSTITUTION_ID, QuotaType.VIDEO, 1024L, null));
    }

    @Test
    void setQuota_WhenSettingNewQuota_CreatesSuccessfully() {
        when(institutionRepository.findById(INSTITUTION_ID)).thenReturn(Optional.of(testInstitution));
        when(storageQuotaRepository.findByInstitutionAndType(testInstitution, QuotaType.VIDEO))
                .thenReturn(Optional.empty());
        when(storageQuotaRepository.save(any(StorageQuota.class))).thenReturn(testQuota);

        LocalDateTime expiresAt = LocalDateTime.now().plusDays(30);
        long totalQuota = 10L * 1024 * 1024 * 1024; // 10GB

        storageQuotaService.setQuota(INSTITUTION_ID, QuotaType.VIDEO, totalQuota, expiresAt);

        verify(storageQuotaRepository).save(argThat(quota ->
                quota.getTotalQuota().equals(totalQuota) &&
                quota.getExpiresAt().equals(expiresAt)));
    }
} 