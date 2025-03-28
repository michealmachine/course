package com.zhangziqi.online_course_mine.service.impl;

import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.exception.ResourceNotFoundException;
import com.zhangziqi.online_course_mine.model.entity.Institution;
import com.zhangziqi.online_course_mine.model.entity.StorageQuota;
import com.zhangziqi.online_course_mine.model.enums.QuotaType;
import com.zhangziqi.online_course_mine.model.vo.QuotaInfoVO;
import com.zhangziqi.online_course_mine.repository.InstitutionRepository;
import com.zhangziqi.online_course_mine.repository.StorageQuotaRepository;
import com.zhangziqi.online_course_mine.service.StorageQuotaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 存储配额服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StorageQuotaServiceImpl implements StorageQuotaService {
    
    private final StorageQuotaRepository quotaRepository;
    private final InstitutionRepository institutionRepository;
    
    @Override
    @Transactional(readOnly = true)
    public boolean hasEnoughQuota(Long institutionId, QuotaType quotaType, Long requiredSize) {
        StorageQuota quota = findQuotaByType(institutionId, quotaType);
        
        // 如果剩余配额大于等于所需大小，则有足够配额
        return (quota.getTotalQuota() - quota.getUsedQuota()) >= requiredSize;
    }
    
    @Override
    @Transactional
    public void updateUsedQuota(Long institutionId, QuotaType type, Long sizeDelta) {
        // 验证机构存在
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("机构不存在，ID: " + institutionId));
        
        StorageQuota quota = quotaRepository.findByInstitutionAndType(institution, type)
                .orElseGet(() -> {
                    List<StorageQuota> quotas = initializeQuotas(institution);
                    return quotas.stream()
                            .filter(q -> q.getType() == type)
                            .findFirst()
                            .orElseThrow(() -> new BusinessException(500, "初始化配额失败"));
                });

        // 计算新的已用配额
        long newUsedQuota = quota.getUsedQuota() + sizeDelta;

        // 已用配额不能小于0
        if (newUsedQuota < 0) {
            newUsedQuota = 0;
        }

        // 已用配额不能超过总配额
        if (newUsedQuota > quota.getTotalQuota()) {
            throw new BusinessException(400, "存储配额不足，无法分配空间");
        }

        // 更新已用配额
        quota.setUsedQuota(newUsedQuota);

        // 保存配额
        quotaRepository.save(quota);
        
        // 如果更新的是特定类型，也要更新TOTAL类型
        if (type != QuotaType.TOTAL) {
            updateUsedQuota(institutionId, QuotaType.TOTAL, sizeDelta);
        }
        
        log.info("更新机构{}的{}配额，变化: {}字节，当前已用: {}字节", 
                institutionId, type, sizeDelta, quota.getUsedQuota());
    }
    
    @Override
    @Transactional(readOnly = true)
    public QuotaInfoVO getQuotaInfo(Long institutionId) {
        // 获取所有配额信息
        List<QuotaInfoVO> allQuotas = getAllQuotas(institutionId);
        
        // 计算总配额和已用总配额
        long totalQuota = 0;
        long usedQuota = 0;
        
        for (QuotaInfoVO info : allQuotas) {
            totalQuota += info.getTotalQuota();
            usedQuota += info.getUsedQuota();
        }
        
        // 构建总配额信息
        return QuotaInfoVO.builder()
                .type(QuotaType.TOTAL.name())
                .typeName("总配额")
                .totalQuota(totalQuota)
                .usedQuota(usedQuota)
                .lastUpdatedTime(LocalDateTime.now())
                .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<QuotaInfoVO> getAllQuotas(Long institutionId) {
        // 验证机构是否存在
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("机构不存在，ID: " + institutionId));
        
        // 获取机构的所有配额
        List<StorageQuota> quotas = quotaRepository.findByInstitution(institution);
        
        // 如果配额列表为空，则初始化配额
        if (quotas.isEmpty()) {
            quotas = initializeQuotas(institution);
        }
        
        // 转换为VO
        return quotas.stream()
                .map(this::mapToQuotaInfoVO)
                .collect(Collectors.toList());
    }
    
    /**
     * 按类型查找配额
     * 
     * @param institutionId 机构ID
     * @param quotaType 配额类型
     * @return 配额
     */
    private StorageQuota findQuotaByType(Long institutionId, QuotaType quotaType) {
        // 验证机构是否存在
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("机构不存在，ID: " + institutionId));
        
        // 查找指定类型的配额
        StorageQuota quota = quotaRepository.findByInstitutionAndType(institution, quotaType)
                .orElse(null);
        
        // 如果配额不存在，则初始化配额
        if (quota == null) {
            List<StorageQuota> quotas = initializeQuotas(institution);
            quota = quotas.stream()
                    .filter(q -> q.getType() == quotaType)
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(500, "初始化配额失败"));
        }
        
        return quota;
    }
    
    /**
     * 初始化机构的配额
     * 
     * @param institution 机构
     * @return 初始化的配额列表
     */
    private List<StorageQuota> initializeQuotas(Institution institution) {
        List<StorageQuota> quotas = new ArrayList<>();
        
        // 初始化各种类型的配额
        // 视频配额：默认10GB
        StorageQuota videoQuota = new StorageQuota();
        videoQuota.setInstitution(institution);
        videoQuota.setType(QuotaType.VIDEO);
        videoQuota.setTotalQuota(10L * 1024 * 1024 * 1024); // 10GB
        videoQuota.setUsedQuota(0L);
        quotas.add(videoQuota);
        
        // 文档配额：默认5GB
        StorageQuota docQuota = new StorageQuota();
        docQuota.setInstitution(institution);
        docQuota.setType(QuotaType.DOCUMENT);
        docQuota.setTotalQuota(5L * 1024 * 1024 * 1024); // 5GB
        docQuota.setUsedQuota(0L);
        quotas.add(docQuota);
        
        // 总配额：默认等于各类型配额之和
        StorageQuota totalQuota = new StorageQuota();
        totalQuota.setInstitution(institution);
        totalQuota.setType(QuotaType.TOTAL);
        totalQuota.setTotalQuota(videoQuota.getTotalQuota() + docQuota.getTotalQuota());
        totalQuota.setUsedQuota(0L);
        quotas.add(totalQuota);
        
        // 保存所有配额
        List<StorageQuota> savedQuotas = quotaRepository.saveAll(quotas);
        log.info("初始化配额成功: {}", savedQuotas);
        for (StorageQuota quota : savedQuotas) {
            log.info("保存的配额: ID={}, 类型={}, 总配额={}, 已用配额={}", 
                    quota.getId(), quota.getType(), quota.getTotalQuota(), quota.getUsedQuota());
        }
        return savedQuotas;
    }
    
    /**
     * 将StorageQuota实体转换为VO
     * 
     * @param quota 配额实体
     * @return 配额VO
     */
    private QuotaInfoVO mapToQuotaInfoVO(StorageQuota quota) {
        return QuotaInfoVO.builder()
                .type(quota.getType().name())
                .typeName(getQuotaTypeName(quota.getType()))
                .totalQuota(quota.getTotalQuota())
                .usedQuota(quota.getUsedQuota())
                .lastUpdatedTime(quota.getUpdatedAt())
                .build();
    }
    
    /**
     * 获取配额类型的中文名称
     * 
     * @param type 配额类型
     * @return 类型名称
     */
    private String getQuotaTypeName(QuotaType type) {
        switch (type) {
            case VIDEO:
                return "视频配额";
            case DOCUMENT:
                return "文档配额";
            case TOTAL:
                return "总配额";
            default:
                return "未知配额";
        }
    }
    
    @Override
    @Transactional
    public void setQuota(Long institutionId, QuotaType type, Long totalQuota, LocalDateTime expiresAt) {
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("机构不存在，ID: " + institutionId));

        StorageQuota quota = quotaRepository.findByInstitutionAndType(institution, type)
                .orElse(new StorageQuota());

        quota.setInstitution(institution);
        quota.setType(type);
        quota.setTotalQuota(totalQuota);
        quota.setUsedQuota(quota.getUsedQuota() == null ? 0L : quota.getUsedQuota()); // 初始化usedQuota为0
        quota.setExpiresAt(expiresAt);
        quota.setEnabled(true); // 设置为启用状态

        quotaRepository.save(quota);
        log.info("设置配额: 机构ID: {}, 类型: {}, 总配额: {}", institutionId, type, totalQuota);
    }

    /**
     * 增加存储配额
     *
     * @param institutionId 机构ID
     * @param type 配额类型
     * @param additionalQuota 增加的配额大小(字节)
     */
    @Override
    @Transactional
    public void increaseQuota(Long institutionId, QuotaType type, Long additionalQuota) {
        log.info("增加机构 {} 的 {} 配额: {} 字节", institutionId, type, additionalQuota);
        
        if (additionalQuota <= 0) {
            throw new BusinessException("增加的配额必须大于0");
        }
        
        // 验证机构存在
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("机构不存在，ID: " + institutionId));
        
        // 获取当前配额
        StorageQuota quota = quotaRepository.findByInstitutionAndType(institution, type)
                .orElseGet(() -> {
                    List<StorageQuota> quotas = initializeQuotas(institution);
                    return quotas.stream()
                            .filter(q -> q.getType() == type)
                            .findFirst()
                            .orElseThrow(() -> new BusinessException(500, "初始化配额失败"));
                });
        
        // 增加配额
        long newTotalQuota = quota.getTotalQuota() + additionalQuota;
        quota.setTotalQuota(newTotalQuota);
        quotaRepository.save(quota);
        
        // 如果更新的是特定类型，也要更新TOTAL类型
        if (type != QuotaType.TOTAL) {
            // 获取总配额
            StorageQuota totalQuota = quotaRepository.findByInstitutionAndType(institution, QuotaType.TOTAL)
                    .orElseThrow(() -> new BusinessException(500, "无法找到总配额信息"));
            
            // 增加总配额
            totalQuota.setTotalQuota(totalQuota.getTotalQuota() + additionalQuota);
            quotaRepository.save(totalQuota);
        }
        
        log.info("配额增加成功，机构 {} 的 {} 配额当前为: {} 字节", 
                institutionId, type, quota.getTotalQuota());
    }
} 