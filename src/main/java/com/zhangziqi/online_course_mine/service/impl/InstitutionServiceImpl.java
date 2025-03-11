package com.zhangziqi.online_course_mine.service.impl;

import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.model.converter.InstitutionApplicationConverter;
import com.zhangziqi.online_course_mine.model.converter.InstitutionConverter;
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
import com.zhangziqi.online_course_mine.service.EmailService;
import com.zhangziqi.online_course_mine.service.InstitutionService;
import com.zhangziqi.online_course_mine.service.StorageQuotaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 机构服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InstitutionServiceImpl implements InstitutionService {
    
    private final InstitutionRepository institutionRepository;
    private final InstitutionApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final StorageQuotaService storageQuotaService;
    
    @Override
    @Transactional
    public String applyInstitution(InstitutionApplyDTO applyDTO) {
        log.info("申请创建机构: {}", applyDTO.getName());
        
        // 生成申请ID
        String applicationId = generateApplicationId();
        
        // 创建申请记录
        InstitutionApplication application = InstitutionApplication.builder()
                .applicationId(applicationId)
                .name(applyDTO.getName())
                .logo(applyDTO.getLogo())
                .description(applyDTO.getDescription())
                .contactPerson(applyDTO.getContactPerson())
                .contactPhone(applyDTO.getContactPhone())
                .contactEmail(applyDTO.getContactEmail())
                .address(applyDTO.getAddress())
                .status(0) // 待审核
                .build();
        
        applicationRepository.save(application);
        
        // 发送确认邮件
        emailService.sendApplicationConfirmationEmail(
                applyDTO.getContactEmail(),
                applicationId,
                applyDTO.getName()
        );
        
        return applicationId;
    }
    
    @Override
    public InstitutionApplicationVO getApplicationStatus(String applicationId, String email) {
        log.info("查询申请状态: applicationId={}, email={}", applicationId, email);
        
        InstitutionApplication application = applicationRepository
                .findByApplicationIdAndContactEmail(applicationId, email)
                .orElseThrow(() -> new BusinessException("申请不存在或邮箱不匹配"));
        
        return InstitutionApplicationConverter.toVO(application);
    }
    
    @Override
    public Page<InstitutionApplicationVO> getApplications(InstitutionApplicationQueryDTO queryDTO) {
        log.info("分页查询机构申请: {}", queryDTO);
        
        Pageable pageable = PageRequest.of(
                queryDTO.getPageNum() - 1,
                queryDTO.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        
        Page<InstitutionApplication> page;
        if (queryDTO.getStatus() != null) {
            page = applicationRepository.findByStatus(queryDTO.getStatus(), pageable);
        } else {
            page = applicationRepository.findAll(pageable);
        }
        
        return page.map(InstitutionApplicationConverter::toVO);
    }
    
    @Override
    public InstitutionApplicationVO getApplicationDetail(Long id) {
        log.info("查询申请详情: {}", id);
        
        InstitutionApplication application = applicationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("申请不存在"));
        
        return InstitutionApplicationConverter.toVO(application);
    }
    
    @Override
    @Transactional
    public InstitutionVO approveApplication(Long id, String reviewerUsername) {
        log.info("审核通过申请: id={}, reviewer={}", id, reviewerUsername);
        
        // 查找申请
        InstitutionApplication application = applicationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("申请不存在"));
        
        if (application.getStatus() != 0) {
            throw new BusinessException("申请状态不是待审核");
        }
        
        // 查找审核人
        User reviewer = userRepository.findByUsername(reviewerUsername)
                .orElseThrow(() -> new BusinessException("审核人不存在"));
        
        // 生成唯一注册码
        String registerCode = generateInstitutionCode();
        
        // 创建机构
        Institution institution = Institution.builder()
                .name(application.getName())
                .logo(application.getLogo())
                .description(application.getDescription())
                .contactPerson(application.getContactPerson())
                .contactPhone(application.getContactPhone())
                .contactEmail(application.getContactEmail())
                .address(application.getAddress())
                .registerCode(registerCode)
                .status(1) // 正常状态
                .build();
        
        institution = institutionRepository.save(institution);
        
        // 初始化存储配额
        initializeStorageQuotas(institution.getId());
        
        // 更新申请状态
        application.setStatus(1); // 已通过
        application.setReviewerId(reviewer.getId());
        application.setReviewedAt(LocalDateTime.now());
        application.setInstitutionId(institution.getId());
        applicationRepository.save(application);
        
        // 发送通知邮件
        emailService.sendApplicationApprovedEmail(
                application.getContactEmail(),
                institution.getName(),
                registerCode
        );
        
        return InstitutionConverter.toVO(institution);
    }
    
    /**
     * 初始化机构的存储配额
     *
     * @param institutionId 机构ID
     */
    private void initializeStorageQuotas(Long institutionId) {
        // 设置视频配额（5GB）
        storageQuotaService.setQuota(
                institutionId,
                QuotaType.VIDEO,
                5L * 1024 * 1024 * 1024,
                null
        );
        
        // 设置文档配额（2GB）
        storageQuotaService.setQuota(
                institutionId,
                QuotaType.DOCUMENT,
                2L * 1024 * 1024 * 1024,
                null
        );
        
        // 设置总配额（10GB）
        storageQuotaService.setQuota(
                institutionId,
                QuotaType.TOTAL,
                10L * 1024 * 1024 * 1024,
                null
        );
        
        log.info("已为机构{}初始化存储配额", institutionId);
    }
    
    @Override
    @Transactional
    public void rejectApplication(Long id, String reason, String reviewerUsername) {
        log.info("审核拒绝申请: id={}, reviewer={}, reason={}", id, reviewerUsername, reason);
        
        // 查找申请
        InstitutionApplication application = applicationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("申请不存在"));
        
        if (application.getStatus() != 0) {
            throw new BusinessException("申请状态不是待审核");
        }
        
        // 查找审核人
        User reviewer = userRepository.findByUsername(reviewerUsername)
                .orElseThrow(() -> new BusinessException("审核人不存在"));
        
        // 更新申请状态
        application.setStatus(2); // 已拒绝
        application.setReviewerId(reviewer.getId());
        application.setReviewedAt(LocalDateTime.now());
        application.setReviewComment(reason);
        applicationRepository.save(application);
        
        // 发送通知邮件
        emailService.sendApplicationRejectedEmail(
                application.getContactEmail(),
                application.getName(),
                reason
        );
    }
    
    @Override
    public String getInstitutionRegisterCode(String username) {
        log.info("获取机构注册码: {}", username);
        
        // 查找用户
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        if (user.getInstitutionId() == null) {
            throw new BusinessException("用户未关联机构");
        }
        
        // 查找机构
        Institution institution = institutionRepository.findById(user.getInstitutionId())
                .orElseThrow(() -> new BusinessException("机构不存在"));
        
        return institution.getRegisterCode();
    }
    
    // 生成唯一的机构注册码
    private String generateInstitutionCode() {
        String code;
        boolean exists;
        
        do {
            // 生成8位随机字母数字组合
            code = RandomStringUtils.randomAlphanumeric(8).toUpperCase();
            exists = institutionRepository.existsByRegisterCode(code);
        } while (exists);
        
        return code;
    }
    
    // 生成申请ID
    private String generateApplicationId() {
        return "APP" + System.currentTimeMillis() % 10000000 + 
               RandomStringUtils.randomNumeric(4);
    }
} 