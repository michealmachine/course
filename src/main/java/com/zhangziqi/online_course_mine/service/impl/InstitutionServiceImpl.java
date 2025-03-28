package com.zhangziqi.online_course_mine.service.impl;

import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.model.converter.InstitutionApplicationConverter;
import com.zhangziqi.online_course_mine.model.converter.InstitutionConverter;
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
import com.zhangziqi.online_course_mine.service.EmailService;
import com.zhangziqi.online_course_mine.service.InstitutionService;
import com.zhangziqi.online_course_mine.service.MinioService;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.UUID;

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
    private final MinioService minioService;
    
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
    
    @Override
    @Transactional(readOnly = true)
    public boolean isInstitutionAdmin(String username, Long institutionId) {
        log.debug("检查用户是否为机构管理员: username={}, institutionId={}", username, institutionId);
        
        // 查找用户
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        // 检查用户是否属于该机构
        if (user.getInstitutionId() == null || !user.getInstitutionId().equals(institutionId)) {
            log.debug("用户不属于该机构: username={}, userInstitutionId={}, requestedInstitutionId={}",
                    username, user.getInstitutionId(), institutionId);
            return false;
        }
        
        // 获取用户邮箱
        String userEmail = user.getEmail();
        if (userEmail == null || userEmail.isEmpty()) {
            log.debug("用户邮箱为空: username={}", username);
            return false;
        }
        
        // 查找机构
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new BusinessException("机构不存在"));
        
        // 获取机构联系邮箱
        String institutionEmail = institution.getContactEmail();
        if (institutionEmail == null || institutionEmail.isEmpty()) {
            log.debug("机构联系邮箱为空: institutionId={}", institutionId);
            return false;
        }
        
        // 比较邮箱是否匹配
        boolean isAdmin = userEmail.equalsIgnoreCase(institutionEmail);
        log.debug("用户邮箱与机构联系邮箱比较结果: username={}, isAdmin={}, userEmail={}, institutionEmail={}",
                username, isAdmin, userEmail, institutionEmail);
        
        return isAdmin;
    }
    
    /**
     * 获取机构详情
     *
     * @param institutionId 机构ID
     * @return 机构信息
     */
    @Override
    @Transactional(readOnly = true)
    public InstitutionVO getInstitutionDetail(Long institutionId) {
        log.info("获取机构详情: {}", institutionId);
        
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new BusinessException("机构不存在"));
        
        return InstitutionConverter.toVO(institution);
    }
    
    /**
     * 获取机构详情，可控制是否返回注册码
     *
     * @param institutionId 机构ID
     * @param username 用户名
     * @return 机构信息
     */
    @Override
    @Transactional(readOnly = true)
    public InstitutionVO getInstitutionDetail(Long institutionId, String username) {
        log.info("获取机构详情: institutionId={}, username={}", institutionId, username);
        
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new BusinessException("机构不存在"));
        
        // 查找用户
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        // 判断用户是否属于该机构
        boolean belongsToInstitution = user.getInstitutionId() != null && 
                user.getInstitutionId().equals(institutionId);
        
        // 只有属于该机构的用户才能看到注册码
        return InstitutionConverter.toVO(institution, belongsToInstitution);
    }
    
    /**
     * 更新机构信息
     *
     * @param institutionId 机构ID
     * @param updateDTO 更新信息DTO
     * @param username 当前操作用户名
     * @return 更新后的机构信息
     */
    @Override
    @Transactional
    public InstitutionVO updateInstitution(Long institutionId, InstitutionUpdateDTO updateDTO, String username) {
        log.info("更新机构信息: institutionId={}, username={}", institutionId, username);
        
        // 检查用户是否为机构管理员
        if (!isInstitutionAdmin(username, institutionId)) {
            throw new BusinessException("权限不足，只有机构管理员可以更新机构信息");
        }
        
        // 查找机构
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new BusinessException("机构不存在"));
        
        // 更新机构信息
        institution.setName(updateDTO.getName());
        institution.setDescription(updateDTO.getDescription());
        institution.setContactPerson(updateDTO.getContactPerson());
        institution.setContactPhone(updateDTO.getContactPhone());
        institution.setAddress(updateDTO.getAddress());
        
        // 保存更新后的机构信息
        institution = institutionRepository.save(institution);
        log.info("机构信息更新成功: {}", institutionId);
        
        return InstitutionConverter.toVO(institution);
    }
    
    /**
     * 更新机构Logo
     *
     * @param institutionId 机构ID
     * @param file Logo文件
     * @param username 当前操作用户名
     * @return 更新后的机构信息
     */
    @Override
    @Transactional
    public InstitutionVO updateInstitutionLogo(Long institutionId, MultipartFile file, String username) 
            throws IOException {
        log.info("更新机构Logo: institutionId={}, username={}", institutionId, username);
        
        // 检查用户是否为机构管理员
        if (!isInstitutionAdmin(username, institutionId)) {
            throw new BusinessException("权限不足，只有机构管理员可以更新机构Logo");
        }
        
        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(400, "只支持上传图片文件");
        }
        
        // 检查文件大小（最大2MB）
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new BusinessException(400, "文件大小不能超过2MB");
        }
        
        // 查找机构
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new BusinessException("机构不存在"));
        
        // 生成唯一的对象名
        String objectName = "institutions/logos/" + institutionId + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();
        
        // 上传到MinIO
        String logoUrl = minioService.uploadFile(objectName, file.getInputStream(), file.getContentType());
        
        // 如果机构已有Logo，删除旧Logo
        if (institution.getLogo() != null && !institution.getLogo().isEmpty()) {
            try {
                // 从URL中提取对象名
                String oldLogoUrl = institution.getLogo();
                URI uri = new URI(oldLogoUrl);
                String path = uri.getPath();
                // 移除开头的'/'和桶名
                String oldObjectName = path.replaceFirst("^/[^/]+/", "");
                
                // 删除旧文件
                minioService.deleteFile(oldObjectName);
                log.info("已删除旧的机构Logo: {}", oldObjectName);
            } catch (Exception e) {
                log.warn("删除旧Logo文件失败: {}", e.getMessage());
                // 继续执行，不影响更新新Logo
            }
        }
        
        // 更新机构Logo
        institution.setLogo(logoUrl);
        institution = institutionRepository.save(institution);
        log.info("机构Logo更新成功: {}", institutionId);
        
        return InstitutionConverter.toVO(institution);
    }
    
    /**
     * 重置机构注册码
     *
     * @param institutionId 机构ID
     * @param username 当前操作用户名
     * @return 新的注册码
     */
    @Override
    @Transactional
    public String resetInstitutionRegisterCode(Long institutionId, String username) {
        log.info("重置机构注册码: institutionId={}, username={}", institutionId, username);
        
        // 检查用户是否为机构管理员
        if (!isInstitutionAdmin(username, institutionId)) {
            throw new BusinessException("权限不足，只有机构管理员可以重置注册码");
        }
        
        // 查找机构
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new BusinessException("机构不存在"));
        
        // 生成新的注册码
        String newRegisterCode = generateInstitutionCode();
        
        // 更新机构注册码
        institution.setRegisterCode(newRegisterCode);
        institutionRepository.save(institution);
        log.info("机构注册码重置成功: {}", institutionId);
        
        return newRegisterCode;
    }
} 