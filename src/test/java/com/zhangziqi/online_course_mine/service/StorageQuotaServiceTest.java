package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.exception.ResourceNotFoundException;
import com.zhangziqi.online_course_mine.model.entity.Institution;
import com.zhangziqi.online_course_mine.model.entity.StorageQuota;
import com.zhangziqi.online_course_mine.model.enums.QuotaType;
import com.zhangziqi.online_course_mine.model.vo.QuotaInfoVO;
import com.zhangziqi.online_course_mine.repository.InstitutionRepository;
import com.zhangziqi.online_course_mine.repository.StorageQuotaRepository;
import com.zhangziqi.online_course_mine.service.impl.StorageQuotaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    void hasEnoughQuota_WhenInstitutionNotExists_ThrowsException() {
        when(institutionRepository.findById(INSTITUTION_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                storageQuotaService.hasEnoughQuota(INSTITUTION_ID, QuotaType.VIDEO, 1024L));
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
                storageQuotaService.getQuotaInfo(INSTITUTION_ID));
    }

    @Test
    void getQuotaInfo_WhenQuotaExists_ReturnsQuotaInfo() {
        when(institutionRepository.findById(INSTITUTION_ID)).thenReturn(Optional.of(testInstitution));
        when(storageQuotaRepository.findByInstitution(testInstitution))
                .thenReturn(Arrays.asList(testQuota));

        QuotaInfoVO result = storageQuotaService.getQuotaInfo(INSTITUTION_ID);

        assertNotNull(result);
        assertEquals(QuotaType.TOTAL.name(), result.getType());
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

        List<QuotaInfoVO> results = storageQuotaService.getAllQuotas(INSTITUTION_ID);

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
        
        // 视频配额
        StorageQuota videoQuota = new StorageQuota();
        videoQuota.setId(1L);
        videoQuota.setInstitution(testInstitution);
        videoQuota.setType(QuotaType.VIDEO);
        videoQuota.setTotalQuota(5L * 1024 * 1024 * 1024); // 5GB
        videoQuota.setUsedQuota(1L * 1024 * 1024 * 1024);  // 1GB
        
        // 总配额
        StorageQuota totalQuota = new StorageQuota();
        totalQuota.setId(3L);
        totalQuota.setInstitution(testInstitution);
        totalQuota.setType(QuotaType.TOTAL);
        totalQuota.setTotalQuota(7L * 1024 * 1024 * 1024); // 7GB
        totalQuota.setUsedQuota(1L * 1024 * 1024 * 1024);  // 1GB
        
        when(storageQuotaRepository.findByInstitutionAndType(testInstitution, QuotaType.VIDEO))
                .thenReturn(Optional.of(videoQuota));
        when(storageQuotaRepository.findByInstitutionAndType(testInstitution, QuotaType.TOTAL))
                .thenReturn(Optional.of(totalQuota));
        
        // 直接返回参数，不做任何修改
        when(storageQuotaRepository.save(any(StorageQuota.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        long sizeDelta = 1024L * 1024; // 1MB
        storageQuotaService.updateUsedQuota(INSTITUTION_ID, QuotaType.VIDEO, sizeDelta);
        
        // 验证方法调用
        verify(institutionRepository, times(2)).findById(INSTITUTION_ID);
        verify(storageQuotaRepository, times(1)).findByInstitutionAndType(testInstitution, QuotaType.VIDEO);
        verify(storageQuotaRepository, times(1)).findByInstitutionAndType(testInstitution, QuotaType.TOTAL);
        
        // 使用ArgumentCaptor捕获保存的配额对象
        ArgumentCaptor<StorageQuota> quotaCaptor = ArgumentCaptor.forClass(StorageQuota.class);
        verify(storageQuotaRepository, times(2)).save(quotaCaptor.capture());
        
        // 获取所有被保存的配额对象
        List<StorageQuota> capturedQuotas = quotaCaptor.getAllValues();
        assertEquals(2, capturedQuotas.size());
        
        // 直接校验捕获的参数值，而不是依赖被保存后的对象状态
        boolean foundVideo = false;
        boolean foundTotal = false;
        
        for (StorageQuota quota : capturedQuotas) {
            if (quota.getType() == QuotaType.VIDEO) {
                assertEquals(1L * 1024 * 1024 * 1024 + sizeDelta, quota.getUsedQuota());
                foundVideo = true;
            } else if (quota.getType() == QuotaType.TOTAL) {
                assertEquals(1L * 1024 * 1024 * 1024 + sizeDelta, quota.getUsedQuota());
                foundTotal = true;
            }
        }
        
        // 确保找到了两种类型的配额
        assertTrue(foundVideo, "未找到保存的视频配额");
        assertTrue(foundTotal, "未找到保存的总配额");
    }

    @Test
    void setQuota_WhenInstitutionNotExists_ThrowsException() {
        when(institutionRepository.findById(INSTITUTION_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                storageQuotaService.setQuota(INSTITUTION_ID, QuotaType.VIDEO, 1024L, null));
    }

    @Test
    void testSetQuota_WhenSettingNewQuota_CreatesSuccessfully() {
        when(institutionRepository.findById(INSTITUTION_ID)).thenReturn(Optional.of(testInstitution));
        when(storageQuotaRepository.findByInstitutionAndType(testInstitution, QuotaType.VIDEO))
                .thenReturn(Optional.empty());
        when(storageQuotaRepository.save(any(StorageQuota.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        long totalQuota = 5L * 1024 * 1024 * 1024; // 5GB
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(30);
        
        storageQuotaService.setQuota(INSTITUTION_ID, QuotaType.VIDEO, totalQuota, expiresAt);

        ArgumentCaptor<StorageQuota> quotaCaptor = ArgumentCaptor.forClass(StorageQuota.class);
        verify(storageQuotaRepository).save(quotaCaptor.capture());

        StorageQuota savedQuota = quotaCaptor.getValue();
        assertEquals(QuotaType.VIDEO, savedQuota.getType());
        assertEquals(totalQuota, savedQuota.getTotalQuota());
        assertEquals(0L, savedQuota.getUsedQuota());
        assertEquals(expiresAt, savedQuota.getExpiresAt());
        assertTrue(savedQuota.getEnabled());
    }
} 