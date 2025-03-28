package com.zhangziqi.online_course_mine.service.impl;

import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.model.converter.QuotaApplicationConverter;
import com.zhangziqi.online_course_mine.model.dto.QuotaApplicationDTO;
import com.zhangziqi.online_course_mine.model.entity.Institution;
import com.zhangziqi.online_course_mine.model.entity.QuotaApplication;
import com.zhangziqi.online_course_mine.model.entity.User;
import com.zhangziqi.online_course_mine.model.vo.QuotaApplicationVO;
import com.zhangziqi.online_course_mine.repository.InstitutionRepository;
import com.zhangziqi.online_course_mine.repository.QuotaApplicationRepository;
import com.zhangziqi.online_course_mine.repository.UserRepository;
import com.zhangziqi.online_course_mine.service.EmailService;
import com.zhangziqi.online_course_mine.service.InstitutionService;
import com.zhangziqi.online_course_mine.service.QuotaApplicationService;
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
import java.util.HashMap;
import java.util.Map;

/**
 * 存储配额申请服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuotaApplicationServiceImpl implements QuotaApplicationService {
    
    private final QuotaApplicationRepository quotaApplicationRepository;
    private final UserRepository userRepository;
    private final InstitutionRepository institutionRepository;
    private final InstitutionService institutionService;
    private final StorageQuotaService storageQuotaService;
    private final EmailService emailService;
    
    @Override
    @Transactional
    public String applyQuota(String username, QuotaApplicationDTO dto) {
        log.info("申请增加存储配额: username={}, quotaType={}", username, dto.getQuotaType());
        
        // 查找用户
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        // 检查用户是否属于机构
        if (user.getInstitutionId() == null) {
            throw new BusinessException("只有机构用户可以申请存储配额");
        }
        
        // 生成申请ID
        String applicationId = generateApplicationId();
        
        // 创建申请记录
        QuotaApplication application = QuotaApplicationConverter.toEntity(dto, applicationId, user);
        quotaApplicationRepository.save(application);
        
        // 发送确认邮件给申请人
        emailService.sendVerificationCode(
                user.getEmail(),
                "存储配额申请（申请编号：" + applicationId + "）已提交，请等待管理员审核。"
        );
        
        // 发送通知邮件给管理员
        sendNotificationToAdmin(application);
        
        log.info("存储配额申请已创建: applicationId={}", applicationId);
        return applicationId;
    }
    
    /**
     * 发送通知邮件给管理员
     */
    private void sendNotificationToAdmin(QuotaApplication application) {
        // 这里可以实现发送通知给管理员的逻辑
        // 例如，查询所有管理员用户，并发送邮件
    }
    
    @Override
    public QuotaApplicationVO getApplicationStatus(String username, String applicationId) {
        log.info("查询申请状态: username={}, applicationId={}", username, applicationId);
        
        // 查找用户
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        // 查找申请
        QuotaApplication application = quotaApplicationRepository
                .findByApplicationIdAndApplicantId(applicationId, user.getId())
                .orElseThrow(() -> new BusinessException("申请不存在或不属于当前用户"));
        
        // 查找机构
        Institution institution = institutionRepository.findById(application.getInstitutionId())
                .orElse(null);
        
        // 查找审核人（如果已审核）
        User reviewer = null;
        if (application.getReviewerId() != null) {
            reviewer = userRepository.findById(application.getReviewerId()).orElse(null);
        }
        
        return QuotaApplicationConverter.toVO(application, institution, user, reviewer);
    }
    
    @Override
    public Page<QuotaApplicationVO> getUserApplications(String username, Integer status, int pageNum, int pageSize) {
        log.info("查询用户申请列表: username={}, status={}, page={}", username, status, pageNum);
        
        // 查找用户
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        Pageable pageable = PageRequest.of(
                pageNum - 1,
                pageSize,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        
        Page<QuotaApplication> page;
        if (status != null) {
            page = quotaApplicationRepository.findByApplicantIdAndStatus(user.getId(), status, pageable);
        } else {
            page = quotaApplicationRepository.findByApplicantId(user.getId(), pageable);
        }
        
        return page.map(application -> {
            Institution institution = institutionRepository.findById(application.getInstitutionId()).orElse(null);
            User reviewer = application.getReviewerId() != null ? 
                    userRepository.findById(application.getReviewerId()).orElse(null) : null;
            return QuotaApplicationConverter.toVO(application, institution, user, reviewer);
        });
    }
    
    @Override
    public Page<QuotaApplicationVO> getInstitutionApplications(String username, Integer status, int pageNum, int pageSize) {
        log.info("查询机构申请列表: username={}, status={}, page={}", username, status, pageNum);
        
        // 查找用户
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        // 检查用户是否属于机构
        if (user.getInstitutionId() == null) {
            throw new BusinessException("用户未关联机构");
        }
        
        // 检查用户是否为机构管理员
        if (!institutionService.isInstitutionAdmin(username, user.getInstitutionId())) {
            throw new BusinessException("只有机构管理员可以查看机构配额申请");
        }
        
        Pageable pageable = PageRequest.of(
                pageNum - 1,
                pageSize,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        
        Page<QuotaApplication> page;
        if (status != null) {
            page = quotaApplicationRepository.findByInstitutionIdAndStatus(user.getInstitutionId(), status, pageable);
        } else {
            page = quotaApplicationRepository.findByInstitutionId(user.getInstitutionId(), pageable);
        }
        
        Institution institution = institutionRepository.findById(user.getInstitutionId()).orElse(null);
        
        return page.map(application -> {
            User applicant = userRepository.findById(application.getApplicantId()).orElse(null);
            User reviewer = application.getReviewerId() != null ? 
                    userRepository.findById(application.getReviewerId()).orElse(null) : null;
            return QuotaApplicationConverter.toVO(application, institution, applicant, reviewer);
        });
    }
    
    @Override
    public Page<QuotaApplicationVO> getAllApplications(Integer status, int pageNum, int pageSize) {
        log.info("查询所有申请列表: status={}, page={}", status, pageNum);
        
        Pageable pageable = PageRequest.of(
                pageNum - 1,
                pageSize,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        
        Page<QuotaApplication> page;
        if (status != null) {
            page = quotaApplicationRepository.findByStatus(status, pageable);
        } else {
            page = quotaApplicationRepository.findAll(pageable);
        }
        
        return page.map(application -> {
            Institution institution = institutionRepository.findById(application.getInstitutionId()).orElse(null);
            User applicant = userRepository.findById(application.getApplicantId()).orElse(null);
            User reviewer = application.getReviewerId() != null ? 
                    userRepository.findById(application.getReviewerId()).orElse(null) : null;
            return QuotaApplicationConverter.toVO(application, institution, applicant, reviewer);
        });
    }
    
    @Override
    public QuotaApplicationVO getApplicationDetail(Long id) {
        log.info("查询申请详情: {}", id);
        
        QuotaApplication application = quotaApplicationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("申请不存在"));
        
        Institution institution = institutionRepository.findById(application.getInstitutionId()).orElse(null);
        User applicant = userRepository.findById(application.getApplicantId()).orElse(null);
        User reviewer = application.getReviewerId() != null ? 
                userRepository.findById(application.getReviewerId()).orElse(null) : null;
        
        return QuotaApplicationConverter.toVO(application, institution, applicant, reviewer);
    }
    
    @Override
    @Transactional
    public QuotaApplicationVO approveApplication(Long id, String reviewerUsername) {
        log.info("审核通过申请: id={}, reviewer={}", id, reviewerUsername);
        
        // 查找申请
        QuotaApplication application = quotaApplicationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("申请不存在"));
        
        if (application.getStatus() != 0) {
            throw new BusinessException("申请状态不是待审核");
        }
        
        // 查找审核人
        User reviewer = userRepository.findByUsername(reviewerUsername)
                .orElseThrow(() -> new BusinessException("审核人不存在"));
        
        // 查找机构
        Institution institution = institutionRepository.findById(application.getInstitutionId())
                .orElseThrow(() -> new BusinessException("机构不存在"));
        
        // 增加存储配额
        storageQuotaService.increaseQuota(
                application.getInstitutionId(),
                application.getQuotaType(),
                application.getRequestedBytes()
        );
        
        // 更新申请状态
        application.setStatus(1); // 已通过
        application.setReviewerId(reviewer.getId());
        application.setReviewedAt(LocalDateTime.now());
        quotaApplicationRepository.save(application);
        
        // 查找申请人
        User applicant = userRepository.findById(application.getApplicantId())
                .orElseThrow(() -> new BusinessException("申请人不存在"));
        
        // 发送通知邮件给申请人
        emailService.sendApplicationApprovedEmail(
                applicant.getEmail(),
                "存储配额申请已通过",
                "您的存储配额申请（申请编号：" + application.getApplicationId() + "）已通过审核。"
        );
        
        log.info("存储配额申请已通过: id={}, quotaType={}, requestedBytes={}",
                id, application.getQuotaType(), application.getRequestedBytes());
        
        return QuotaApplicationConverter.toVO(application, institution, applicant, reviewer);
    }
    
    @Override
    @Transactional
    public void rejectApplication(Long id, String reason, String reviewerUsername) {
        log.info("审核拒绝申请: id={}, reviewer={}, reason={}", id, reviewerUsername, reason);
        
        // 查找申请
        QuotaApplication application = quotaApplicationRepository.findById(id)
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
        quotaApplicationRepository.save(application);
        
        // 查找申请人
        User applicant = userRepository.findById(application.getApplicantId())
                .orElseThrow(() -> new BusinessException("申请人不存在"));
        
        // 发送通知邮件给申请人
        emailService.sendApplicationRejectedEmail(
                applicant.getEmail(),
                "存储配额申请（申请编号：" + application.getApplicationId() + 
                "）被拒绝",
                reason
        );
        
        log.info("存储配额申请已拒绝: id={}", id);
    }
    
    // 生成申请ID
    private String generateApplicationId() {
        return "QA" + System.currentTimeMillis() % 10000000 + 
               RandomStringUtils.randomNumeric(4);
    }
} 