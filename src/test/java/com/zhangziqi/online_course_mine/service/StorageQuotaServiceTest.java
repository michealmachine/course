package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.exception.ResourceNotFoundException;
import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.model.entity.Institution;
import com.zhangziqi.online_course_mine.model.entity.StorageQuota;
import com.zhangziqi.online_course_mine.model.enums.QuotaType;
import com.zhangziqi.online_course_mine.model.vo.QuotaDistributionVO;
import com.zhangziqi.online_course_mine.model.vo.QuotaInfoVO;
import com.zhangziqi.online_course_mine.model.vo.QuotaStatsVO;
import com.zhangziqi.online_course_mine.model.vo.InstitutionQuotaStatsVO;
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
import java.util.Collections;
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

    @Test
    void increaseQuota_WhenInstitutionNotExists_ThrowsException() {
        when(institutionRepository.findById(INSTITUTION_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                storageQuotaService.increaseQuota(INSTITUTION_ID, QuotaType.VIDEO, 1024L));
    }
    
    @Test
    void increaseQuota_WithNegativeValue_ThrowsException() {
        assertThrows(BusinessException.class, () ->
                storageQuotaService.increaseQuota(INSTITUTION_ID, QuotaType.VIDEO, -1024L));
    }
    
    @Test
    void increaseQuota_WhenQuotaExists_IncreaseSuccessfully() {
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
        
        long additionalQuota = 1024L * 1024 * 1024; // 1GB
        storageQuotaService.increaseQuota(INSTITUTION_ID, QuotaType.VIDEO, additionalQuota);
        
        // 验证方法调用
        verify(institutionRepository, times(1)).findById(INSTITUTION_ID);
        verify(storageQuotaRepository, times(1)).findByInstitutionAndType(testInstitution, QuotaType.VIDEO);
        verify(storageQuotaRepository, times(1)).findByInstitutionAndType(testInstitution, QuotaType.TOTAL);
        
        // 使用ArgumentCaptor捕获保存的配额对象
        ArgumentCaptor<StorageQuota> quotaCaptor = ArgumentCaptor.forClass(StorageQuota.class);
        verify(storageQuotaRepository, times(2)).save(quotaCaptor.capture());
        
        // 获取所有被保存的配额对象
        List<StorageQuota> capturedQuotas = quotaCaptor.getAllValues();
        assertEquals(2, capturedQuotas.size());
        
        // 验证配额是否正确增加
        boolean foundVideo = false;
        boolean foundTotal = false;
        
        for (StorageQuota quota : capturedQuotas) {
            if (quota.getType() == QuotaType.VIDEO) {
                assertEquals(5L * 1024 * 1024 * 1024 + additionalQuota, quota.getTotalQuota());
                foundVideo = true;
            } else if (quota.getType() == QuotaType.TOTAL) {
                assertEquals(7L * 1024 * 1024 * 1024 + additionalQuota, quota.getTotalQuota());
                foundTotal = true;
            }
        }
        
        // 确保找到了两种类型的配额
        assertTrue(foundVideo, "未找到保存的视频配额");
        assertTrue(foundTotal, "未找到保存的总配额");
    }

    @Test
    void getQuotaStats_WhenInstitutionNotExists_ThrowsException() {
        when(institutionRepository.findById(INSTITUTION_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                storageQuotaService.getQuotaStats(INSTITUTION_ID));
    }

    @Test
    void getQuotaStats_WhenQuotasExist_ReturnsStatsCorrectly() {
        // 准备测试数据
        when(institutionRepository.findById(INSTITUTION_ID)).thenReturn(Optional.of(testInstitution));
        
        // 视频配额
        StorageQuota videoQuota = new StorageQuota();
        videoQuota.setId(1L);
        videoQuota.setInstitution(testInstitution);
        videoQuota.setType(QuotaType.VIDEO);
        videoQuota.setTotalQuota(5L * 1024 * 1024 * 1024); // 5GB
        videoQuota.setUsedQuota(2L * 1024 * 1024 * 1024);  // 2GB
        videoQuota.setUpdatedAt(LocalDateTime.now());
        
        // 文档配额
        StorageQuota docQuota = new StorageQuota();
        docQuota.setId(2L);
        docQuota.setInstitution(testInstitution);
        docQuota.setType(QuotaType.DOCUMENT);
        docQuota.setTotalQuota(3L * 1024 * 1024 * 1024); // 3GB
        docQuota.setUsedQuota(1L * 1024 * 1024 * 1024);  // 1GB
        docQuota.setUpdatedAt(LocalDateTime.now());
        
        // 总配额
        StorageQuota totalQuota = new StorageQuota();
        totalQuota.setId(3L);
        totalQuota.setInstitution(testInstitution);
        totalQuota.setType(QuotaType.TOTAL);
        totalQuota.setTotalQuota(8L * 1024 * 1024 * 1024); // 8GB
        totalQuota.setUsedQuota(3L * 1024 * 1024 * 1024);  // 3GB
        totalQuota.setUpdatedAt(LocalDateTime.now());
        
        when(storageQuotaRepository.findByInstitution(testInstitution))
                .thenReturn(Arrays.asList(videoQuota, docQuota, totalQuota));
        
        // 调用被测方法
        QuotaStatsVO result = storageQuotaService.getQuotaStats(INSTITUTION_ID);
        
        // 验证结果
        assertNotNull(result, "返回的统计信息不应为空");
        
        // 验证总配额信息
        assertNotNull(result.getTotalQuota(), "总配额信息不应为空");
        assertEquals(QuotaType.TOTAL.name(), result.getTotalQuota().getType(), "总配额类型不匹配");
        assertEquals(totalQuota.getTotalQuota(), result.getTotalQuota().getTotalQuota(), "总配额大小不匹配");
        assertEquals(totalQuota.getUsedQuota(), result.getTotalQuota().getUsedQuota(), "已使用总配额不匹配");
        
        // 验证类型配额
        assertNotNull(result.getTypeQuotas(), "类型配额列表不应为空");
        assertEquals(2, result.getTypeQuotas().size(), "类型配额数量不匹配");
        
        // 验证配额分布
        assertNotNull(result.getDistribution(), "配额分布不应为空");
        assertEquals(2, result.getDistribution().size(), "配额分布项数量不匹配");
        
        // 验证分布百分比
        double totalUsed = videoQuota.getUsedQuota() + docQuota.getUsedQuota();
        double videoPercentage = (double) videoQuota.getUsedQuota() / totalUsed * 100.0;
        double docPercentage = (double) docQuota.getUsedQuota() / totalUsed * 100.0;
        
        QuotaDistributionVO videoDistribution = result.getDistribution().stream()
                .filter(d -> d.getType().equals(QuotaType.VIDEO.name()))
                .findFirst()
                .orElse(null);
        
        QuotaDistributionVO docDistribution = result.getDistribution().stream()
                .filter(d -> d.getType().equals(QuotaType.DOCUMENT.name()))
                .findFirst()
                .orElse(null);
        
        assertNotNull(videoDistribution, "视频配额分布不应为空");
        assertNotNull(docDistribution, "文档配额分布不应为空");
        
        assertEquals(videoQuota.getUsedQuota(), videoDistribution.getUsedQuota(), "视频已用配额不匹配");
        assertEquals(docQuota.getUsedQuota(), docDistribution.getUsedQuota(), "文档已用配额不匹配");
        
        assertEquals(videoPercentage, videoDistribution.getPercentage(), 0.01, "视频配额百分比不匹配");
        assertEquals(docPercentage, docDistribution.getPercentage(), 0.01, "文档配额百分比不匹配");
    }

    @Test
    void getAllInstitutionsQuotaStats_WhenNoInstitutions_ReturnsEmptyStats() {
        // 模拟没有机构的情况
        when(institutionRepository.findByStatus(1)).thenReturn(Collections.emptyList());
        
        // 调用测试方法
        InstitutionQuotaStatsVO result = storageQuotaService.getAllInstitutionsQuotaStats();
        
        // 验证结果
        assertNotNull(result, "返回的统计信息不应为空");
        assertEquals(0, result.getTotalUsage().getInstitutionCount(), "机构数量应为0");
        assertEquals(0L, result.getTotalUsage().getTotalQuota(), "总配额应为0");
        assertEquals(0L, result.getTotalUsage().getUsedQuota(), "已用配额应为0");
        assertTrue(result.getInstitutions().isEmpty(), "机构列表应为空");
        assertTrue(result.getDistribution().isEmpty(), "分布数据应为空");
    }
    
    @Test
    void getAllInstitutionsQuotaStats_WithInstitutions_ReturnsCorrectStats() {
        // 准备测试数据
        Institution institution1 = new Institution();
        institution1.setId(1L);
        institution1.setName("测试机构1");
        institution1.setStatus(1);
        
        Institution institution2 = new Institution();
        institution2.setId(2L);
        institution2.setName("测试机构2");
        institution2.setStatus(1);
        
        List<Institution> institutions = Arrays.asList(institution1, institution2);
        
        // 模拟机构仓库返回
        when(institutionRepository.findByStatus(1)).thenReturn(institutions);
        
        // 准备第一个机构的配额数据
        StorageQuota videoQuota1 = new StorageQuota();
        videoQuota1.setId(11L);
        videoQuota1.setInstitution(institution1);
        videoQuota1.setType(QuotaType.VIDEO);
        videoQuota1.setTotalQuota(7L * 1024 * 1024 * 1024); // 7GB
        videoQuota1.setUsedQuota(3L * 1024 * 1024 * 1024);  // 3GB
        videoQuota1.setUpdatedAt(LocalDateTime.now());
        
        StorageQuota docQuota1 = new StorageQuota();
        docQuota1.setId(12L);
        docQuota1.setInstitution(institution1);
        docQuota1.setType(QuotaType.DOCUMENT);
        docQuota1.setTotalQuota(3L * 1024 * 1024 * 1024); // 3GB
        docQuota1.setUsedQuota(1L * 1024 * 1024 * 1024);  // 1GB
        docQuota1.setUpdatedAt(LocalDateTime.now());
        
        StorageQuota totalQuota1 = new StorageQuota();
        totalQuota1.setId(13L);
        totalQuota1.setInstitution(institution1);
        totalQuota1.setType(QuotaType.TOTAL);
        totalQuota1.setTotalQuota(10L * 1024 * 1024 * 1024); // 10GB
        totalQuota1.setUsedQuota(4L * 1024 * 1024 * 1024);   // 4GB
        totalQuota1.setUpdatedAt(LocalDateTime.now());
        
        // 准备第二个机构的配额数据
        StorageQuota videoQuota2 = new StorageQuota();
        videoQuota2.setId(21L);
        videoQuota2.setInstitution(institution2);
        videoQuota2.setType(QuotaType.VIDEO);
        videoQuota2.setTotalQuota(3L * 1024 * 1024 * 1024); // 3GB
        videoQuota2.setUsedQuota(1L * 1024 * 1024 * 1024);  // 1GB
        videoQuota2.setUpdatedAt(LocalDateTime.now());
        
        StorageQuota docQuota2 = new StorageQuota();
        docQuota2.setId(22L);
        docQuota2.setInstitution(institution2);
        docQuota2.setType(QuotaType.DOCUMENT);
        docQuota2.setTotalQuota(2L * 1024 * 1024 * 1024); // 2GB
        docQuota2.setUsedQuota(1L * 1024 * 1024 * 1024);  // 1GB
        docQuota2.setUpdatedAt(LocalDateTime.now());
        
        StorageQuota totalQuota2 = new StorageQuota();
        totalQuota2.setId(23L);
        totalQuota2.setInstitution(institution2);
        totalQuota2.setType(QuotaType.TOTAL);
        totalQuota2.setTotalQuota(5L * 1024 * 1024 * 1024); // 5GB
        totalQuota2.setUsedQuota(2L * 1024 * 1024 * 1024);  // 2GB
        totalQuota2.setUpdatedAt(LocalDateTime.now());
        
        // 模拟仓库方法行为
        when(institutionRepository.findById(1L)).thenReturn(Optional.of(institution1));
        when(institutionRepository.findById(2L)).thenReturn(Optional.of(institution2));
        
        // 模拟quotaRepository.findByInstitution方法的行为，这是getAllQuotas方法中使用的
        when(storageQuotaRepository.findByInstitution(institution1))
                .thenReturn(Arrays.asList(videoQuota1, docQuota1, totalQuota1));
        when(storageQuotaRepository.findByInstitution(institution2))
                .thenReturn(Arrays.asList(videoQuota2, docQuota2, totalQuota2));
        
        // 调用测试方法
        InstitutionQuotaStatsVO result = storageQuotaService.getAllInstitutionsQuotaStats();
        
        // 验证结果
        assertNotNull(result, "返回的统计信息不应为空");
        
        // 验证总体统计
        assertEquals(2, result.getTotalUsage().getInstitutionCount(), "机构数量不匹配");
        assertEquals(30L * 1024 * 1024 * 1024, result.getTotalUsage().getTotalQuota(), "总配额不匹配");
        assertEquals(12L * 1024 * 1024 * 1024, result.getTotalUsage().getUsedQuota(), "已用配额不匹配");
        assertEquals(18L * 1024 * 1024 * 1024, result.getTotalUsage().getAvailableQuota(), "可用配额不匹配");
        
        // 验证机构列表
        assertEquals(2, result.getInstitutions().size(), "机构列表数量不匹配");
        
        // 验证分布数据
        assertEquals(2, result.getDistribution().size(), "分布数据数量不匹配");
        
        // 验证第一个机构的分布百分比
        double expectedPercentage1 = (8.0 / 12.0) * 100.0; // 8GB占总使用量12GB的百分比
        
        InstitutionQuotaStatsVO.InstitutionQuotaDistributionVO distribution1 = result.getDistribution().stream()
                .filter(d -> d.getInstitutionId().equals(1L))
                .findFirst()
                .orElse(null);
                
        assertNotNull(distribution1, "第一个机构的分布数据不应为空");
        assertEquals(8L * 1024 * 1024 * 1024, distribution1.getUsedQuota(), "第一个机构已用配额不匹配");
        assertEquals(expectedPercentage1, distribution1.getPercentage(), 0.01, "第一个机构配额百分比不匹配");

        // 验证第二个机构的分布百分比
        double expectedPercentage2 = (4.0 / 12.0) * 100.0; // 4GB占总使用量12GB的百分比
        
        InstitutionQuotaStatsVO.InstitutionQuotaDistributionVO distribution2 = result.getDistribution().stream()
                .filter(d -> d.getInstitutionId().equals(2L))
                .findFirst()
                .orElse(null);
                
        assertNotNull(distribution2, "第二个机构的分布数据不应为空");
        assertEquals(4L * 1024 * 1024 * 1024, distribution2.getUsedQuota(), "第二个机构已用配额不匹配");
        assertEquals(expectedPercentage2, distribution2.getPercentage(), 0.01, "第二个机构配额百分比不匹配");
    }
} 