package com.zhangziqi.online_course_mine.service.impl;

import com.zhangziqi.online_course_mine.exception.ResourceNotFoundException;
import com.zhangziqi.online_course_mine.model.entity.Institution;
import com.zhangziqi.online_course_mine.model.entity.StorageQuota;
import com.zhangziqi.online_course_mine.model.enums.QuotaType;
import com.zhangziqi.online_course_mine.model.vo.StorageQuotaVO;
import com.zhangziqi.online_course_mine.repository.InstitutionRepository;
import com.zhangziqi.online_course_mine.repository.MediaRepository;
import com.zhangziqi.online_course_mine.repository.StorageQuotaRepository;
import com.zhangziqi.online_course_mine.service.StorageQuotaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 存储配额服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StorageQuotaServiceImpl implements StorageQuotaService {
    
    private final StorageQuotaRepository storageQuotaRepository;
    private final MediaRepository mediaRepository;
    private final InstitutionRepository institutionRepository;
    
    @Override
    @Transactional(readOnly = true)
    public boolean hasEnoughQuota(Long institutionId, QuotaType type, Long fileSize) {
        // 检查机构是否存在
        Institution institution = institutionRepository.findById(institutionId)
                .orElse(null);
        if (institution == null) {
            log.warn("机构不存在，ID: {}", institutionId);
            return false;
        }
        
        Optional<StorageQuota> quotaOpt = storageQuotaRepository.findByInstitutionAndType(institution, type);
        
        if (quotaOpt.isEmpty()) {
            // 如果未设置配额，默认创建
            return true;
        }
        
        StorageQuota quota = quotaOpt.get();
        
        // 检查配额是否过期
        if (quota.getExpiresAt() != null && quota.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("机构{}的{}配额已过期", institutionId, type);
            return false;
        }
        
        // 检查是否有足够配额
        Long availableQuota = quota.getTotalQuota() - quota.getUsedQuota();
        boolean hasEnough = availableQuota >= fileSize;
        
        if (!hasEnough) {
            log.warn("机构{}的{}配额不足，需要{}字节，可用{}字节", 
                    institutionId, type, fileSize, availableQuota);
        }
        
        return hasEnough;
    }
    
    @Override
    @Transactional(readOnly = true)
    public StorageQuotaVO getQuotaInfo(Long institutionId, QuotaType type) {
        // 验证机构存在
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("机构不存在，ID: " + institutionId));
        
        StorageQuota quota = storageQuotaRepository.findByInstitutionAndType(institution, type)
                .orElseGet(() -> createDefaultQuota(institution, type));
                
        return mapToVO(quota);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<StorageQuotaVO> getAllQuotas(Long institutionId) {
        // 验证机构存在
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("机构不存在，ID: " + institutionId));
        
        List<StorageQuota> quotas = storageQuotaRepository.findByInstitution(institution);
        
        // 如果没有任何配额记录，创建默认配额
        if (quotas.isEmpty()) {
            quotas.add(createDefaultQuota(institution, QuotaType.VIDEO));
            quotas.add(createDefaultQuota(institution, QuotaType.DOCUMENT));
            quotas.add(createDefaultQuota(institution, QuotaType.TOTAL));
        }
        
        return quotas.stream().map(this::mapToVO).collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void updateUsedQuota(Long institutionId, QuotaType type, Long sizeDelta) {
        // 验证机构存在
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("机构不存在，ID: " + institutionId));
        
        StorageQuota quota = storageQuotaRepository.findByInstitutionAndType(institution, type)
                .orElseGet(() -> createDefaultQuota(institution, type));
                
        quota.setUsedQuota(Math.max(0, quota.getUsedQuota() + sizeDelta));
        quota.setUpdatedAt(LocalDateTime.now());
        
        storageQuotaRepository.save(quota);
        
        // 如果更新的是特定类型，也要更新TOTAL类型
        if (type != QuotaType.TOTAL) {
            updateUsedQuota(institutionId, QuotaType.TOTAL, sizeDelta);
        }
        
        log.info("更新机构{}的{}配额，变化: {}字节，当前已用: {}字节", 
                institutionId, type, sizeDelta, quota.getUsedQuota());
    }
    
    @Override
    @Transactional
    public void setQuota(Long institutionId, QuotaType type, Long totalQuota, LocalDateTime expiresAt) {
        // 验证机构存在
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("机构不存在，ID: " + institutionId));
        
        StorageQuota quota = storageQuotaRepository.findByInstitutionAndType(institution, type)
                .orElseGet(() -> {
                    StorageQuota newQuota = new StorageQuota();
                    newQuota.setInstitution(institution);
                    newQuota.setType(type);
                    newQuota.setUsedQuota(0L);
                    newQuota.setCreatedAt(LocalDateTime.now());
                    return newQuota;
                });
                
        quota.setTotalQuota(totalQuota);
        quota.setExpiresAt(expiresAt);
        quota.setUpdatedAt(LocalDateTime.now());
        
        storageQuotaRepository.save(quota);
        
        log.info("设置机构{}的{}配额为{}字节，过期时间: {}", 
                institutionId, type, totalQuota, expiresAt);
    }
    
    /**
     * 创建默认配额
     */
    private StorageQuota createDefaultQuota(Institution institution, QuotaType type) {
        StorageQuota quota = new StorageQuota();
        quota.setInstitution(institution);
        quota.setType(type);
        quota.setTotalQuota(getDefaultQuotaSize(type));
        quota.setUsedQuota(calculateUsedQuota(institution.getId(), type));
        quota.setCreatedAt(LocalDateTime.now());
        quota.setUpdatedAt(LocalDateTime.now());
        
        return storageQuotaRepository.save(quota);
    }
    
    /**
     * 获取默认配额大小
     */
    private Long getDefaultQuotaSize(QuotaType type) {
        // 可以从配置读取，这里使用硬编码的默认值
        switch (type) {
            case VIDEO:
                return 5L * 1024 * 1024 * 1024; // 5GB
            case DOCUMENT:
                return 2L * 1024 * 1024 * 1024; // 2GB
            case TOTAL:
                return 10L * 1024 * 1024 * 1024; // 10GB
            default:
                return 0L;
        }
    }
    
    /**
     * 计算已使用的配额
     */
    private Long calculateUsedQuota(Long institutionId, QuotaType type) {
        // TODO: 按媒体类型计算，目前简化处理
        if (type == QuotaType.TOTAL) {
            Long sum = mediaRepository.sumSizeByInstitutionId(institutionId);
            return sum != null ? sum : 0L;
        }
        
        // 特定类型的计算将在后续实现
        return 0L;
    }
    
    /**
     * 实体转VO
     */
    private StorageQuotaVO mapToVO(StorageQuota quota) {
        StorageQuotaVO vo = new StorageQuotaVO();
        vo.setId(quota.getId());
        vo.setType(quota.getType().name());
        vo.setTotalQuota(quota.getTotalQuota());
        vo.setUsedQuota(quota.getUsedQuota());
        vo.setAvailableQuota(quota.getTotalQuota() - quota.getUsedQuota());
        vo.setUsagePercentage(calculatePercentage(quota.getUsedQuota(), quota.getTotalQuota()));
        vo.setExpiresAt(quota.getExpiresAt());
        vo.setInstitutionId(quota.getInstitution().getId());
        return vo;
    }
    
    /**
     * 计算使用百分比
     */
    private double calculatePercentage(Long used, Long total) {
        if (total == null || total == 0 || used == null) {
            return 0.0;
        }
        return (double) used / total * 100.0;
    }
} 