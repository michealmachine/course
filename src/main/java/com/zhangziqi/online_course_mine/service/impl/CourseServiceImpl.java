package com.zhangziqi.online_course_mine.service.impl;

import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.exception.ResourceNotFoundException;
import com.zhangziqi.online_course_mine.model.dto.course.*;
import com.zhangziqi.online_course_mine.model.entity.Category;
import com.zhangziqi.online_course_mine.model.entity.Course;
import com.zhangziqi.online_course_mine.model.entity.Institution;
import com.zhangziqi.online_course_mine.model.entity.Tag;
import com.zhangziqi.online_course_mine.model.enums.CourseStatus;
import com.zhangziqi.online_course_mine.model.enums.CourseVersion;
import com.zhangziqi.online_course_mine.model.enums.CoursePaymentType;
import com.zhangziqi.online_course_mine.model.vo.PreviewUrlVO;
import com.zhangziqi.online_course_mine.repository.CategoryRepository;
import com.zhangziqi.online_course_mine.repository.CourseRepository;
import com.zhangziqi.online_course_mine.repository.InstitutionRepository;
import com.zhangziqi.online_course_mine.repository.TagRepository;
import com.zhangziqi.online_course_mine.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 课程服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {
    
    private final CourseRepository courseRepository;
    private final InstitutionRepository institutionRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final StringRedisTemplate redisTemplate;
    
    // 预览URL有效期（分钟）
    private static final long PREVIEW_URL_EXPIRATION_MINUTES = 60;
    // 预览token有效期（分钟）
    private static final long PREVIEW_TOKEN_EXPIRATION_MINUTES = 1440; // 24小时
    // Redis中存储预览token的键前缀
    private static final String PREVIEW_TOKEN_KEY_PREFIX = "course:preview:";
    
    @Override
    @Transactional
    public Course createCourse(CourseCreateDTO dto, Long creatorId) {
        // 验证机构是否存在
        Institution institution = institutionRepository.findById(dto.getInstitutionId())
                .orElseThrow(() -> new ResourceNotFoundException("机构不存在，ID: " + dto.getInstitutionId()));
        
        // 验证分类是否存在（如果指定了分类）
        Category category = null;
        if (dto.getCategoryId() != null) {
            category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("分类不存在，ID: " + dto.getCategoryId()));
        }
        
        // 验证标签是否存在（如果指定了标签）
        Set<Tag> tags = new HashSet<>();
        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            tags = dto.getTagIds().stream()
                    .map(tagId -> tagRepository.findById(tagId)
                            .orElseThrow(() -> new ResourceNotFoundException("标签不存在，ID: " + tagId)))
                    .collect(Collectors.toSet());
        }
        
        // 设置付费类型
        Integer paymentType = dto.getPaymentType();
        if (paymentType == null) {
            paymentType = CoursePaymentType.FREE.getValue();
        }
        
        // 如果是付费课程，验证价格
        if (paymentType.equals(CoursePaymentType.PAID.getValue())) {
            if (dto.getPrice() == null || dto.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException(400, "付费课程必须设置价格且价格必须大于0");
            }
        }
        
        // 创建课程
        Course course = Course.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .institution(institution)
                .creatorId(creatorId)
                .status(CourseStatus.DRAFT.getValue())
                .isPublishedVersion(false)
                .versionType(CourseVersion.DRAFT.getValue())
                .category(category)
                .tags(tags)
                .paymentType(paymentType)
                .price(dto.getPrice())
                .discountPrice(dto.getDiscountPrice())
                .difficulty(dto.getDifficulty())
                .targetAudience(dto.getTargetAudience())
                .learningObjectives(dto.getLearningObjectives())
                .totalLessons(0)
                .totalDuration(0)
                .build();
        
        return courseRepository.save(course);
    }
    
    @Override
    @Transactional
    public Course updateCourse(Long id, CourseCreateDTO dto) {
        // 获取课程
        Course course = getCourseById(id);
        
        // 检查课程状态，只有草稿或已拒绝状态的课程才能更新
        if (!course.getStatusEnum().equals(CourseStatus.DRAFT) && 
            !course.getStatusEnum().equals(CourseStatus.REJECTED)) {
            throw new BusinessException(400, "只有草稿或已拒绝状态的课程才能更新");
        }
        
        // 验证机构是否存在
        Institution institution = institutionRepository.findById(dto.getInstitutionId())
                .orElseThrow(() -> new ResourceNotFoundException("机构不存在，ID: " + dto.getInstitutionId()));
        
        // 验证分类是否存在（如果指定了分类）
        Category category = null;
        if (dto.getCategoryId() != null) {
            category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("分类不存在，ID: " + dto.getCategoryId()));
        }
        
        // 验证标签是否存在（如果指定了标签）
        Set<Tag> tags = new HashSet<>();
        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            tags = dto.getTagIds().stream()
                    .map(tagId -> tagRepository.findById(tagId)
                            .orElseThrow(() -> new ResourceNotFoundException("标签不存在，ID: " + tagId)))
                    .collect(Collectors.toSet());
        }
        
        // 设置付费类型
        Integer paymentType = dto.getPaymentType();
        if (paymentType == null) {
            paymentType = CoursePaymentType.FREE.getValue();
        }
        
        // 如果是付费课程，验证价格
        if (paymentType.equals(CoursePaymentType.PAID.getValue())) {
            if (dto.getPrice() == null || dto.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException(400, "付费课程必须设置价格且价格必须大于0");
            }
        }
        
        // 更新课程
        course.setTitle(dto.getTitle());
        course.setDescription(dto.getDescription());
        course.setInstitution(institution);
        course.setCategory(category);
        course.setTags(tags);
        course.setPaymentType(paymentType);
        course.setPrice(dto.getPrice());
        course.setDiscountPrice(dto.getDiscountPrice());
        course.setDifficulty(dto.getDifficulty());
        course.setTargetAudience(dto.getTargetAudience());
        course.setLearningObjectives(dto.getLearningObjectives());
        
        // 如果是已拒绝状态，更新后设置为草稿状态
        if (course.getStatusEnum().equals(CourseStatus.REJECTED)) {
            course.setStatusEnum(CourseStatus.DRAFT);
        }
        
        return courseRepository.save(course);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在，ID: " + id));
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Course> getCoursesByInstitution(Long institutionId, Pageable pageable) {
        // 获取机构
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("机构不存在，ID: " + institutionId));
        
        return courseRepository.findByInstitution(institution, pageable);
    }
    
    @Override
    @Transactional
    public void deleteCourse(Long id) {
        Course course = getCourseById(id);
        
        // 只有草稿状态的课程才能删除
        if (!course.getStatusEnum().equals(CourseStatus.DRAFT)) {
            throw new BusinessException(400, "只有草稿状态的课程才能删除");
        }
        
        courseRepository.delete(course);
    }
    
    @Override
    @Transactional
    public Course updateCourseCover(Long id, String coverImageUrl) {
        Course course = getCourseById(id);
        
        // 检查课程状态，只有草稿或已拒绝状态的课程才能更新封面
        if (!course.getStatusEnum().equals(CourseStatus.DRAFT) && 
            !course.getStatusEnum().equals(CourseStatus.REJECTED)) {
            throw new BusinessException(400, "只有草稿或已拒绝状态的课程才能更新封面");
        }
        
        course.setCoverImage(coverImageUrl);
        
        return courseRepository.save(course);
    }
    
    @Override
    @Transactional
    public Course submitForReview(Long id) {
        Course course = getCourseById(id);
        
        // 检查课程状态，只有草稿状态的课程才能提交审核
        if (!course.getStatusEnum().equals(CourseStatus.DRAFT)) {
            throw new BusinessException(400, "只有草稿状态的课程才能提交审核");
        }
        
        // 检查课程是否有章节
        if (course.getChapters() == null || course.getChapters().isEmpty()) {
            throw new BusinessException(400, "课程必须至少有一个章节才能提交审核");
        }
        
        // 更新课程状态为待审核
        course.setStatusEnum(CourseStatus.PENDING_REVIEW);
        course.setVersionTypeEnum(CourseVersion.REVIEW);
        
        return courseRepository.save(course);
    }
    
    @Override
    @Transactional
    public Course startReview(Long id, Long reviewerId) {
        Course course = getCourseById(id);
        
        // 检查课程状态，只有待审核状态的课程才能开始审核
        if (!course.getStatusEnum().equals(CourseStatus.PENDING_REVIEW)) {
            throw new BusinessException(400, "只有待审核状态的课程才能开始审核");
        }
        
        // 更新课程状态为审核中
        course.setStatusEnum(CourseStatus.REVIEWING);
        course.setReviewerId(reviewerId);
        
        return courseRepository.save(course);
    }
    
    @Override
    @Transactional
    public Course approveCourse(Long id, String comment, Long reviewerId) {
        Course course = getCourseById(id);
        
        // 检查课程状态，只有审核中状态的课程才能审核通过
        if (!course.getStatusEnum().equals(CourseStatus.REVIEWING)) {
            throw new BusinessException(400, "只有审核中状态的课程才能审核通过");
        }
        
        // 检查审核人是否匹配
        if (!reviewerId.equals(course.getReviewerId())) {
            throw new BusinessException(400, "只有分配的审核人才能审核课程");
        }
        
        // 更新课程状态为已发布
        course.setStatusEnum(CourseStatus.PUBLISHED);
        course.setReviewComment(comment);
        course.setReviewedAt(LocalDateTime.now());
        course.setVersionTypeEnum(CourseVersion.PUBLISHED);
        
        // 如果是首次发布，创建一个已发布版本
        if (course.getPublishedVersionId() == null) {
            // 保存新发布的课程，获取ID
            Course savedCourse = courseRepository.save(course);
            
            // 创建已发布版本
            Course publishedVersion = new Course();
            BeanUtils.copyProperties(savedCourse, publishedVersion, "id", "dataVersion");
            publishedVersion.setId(null); // 确保新对象没有ID
            publishedVersion.setIsPublishedVersion(true);
            publishedVersion.setPublishedVersionId(savedCourse.getId());
            
            // 保存已发布版本
            Course savedPublishedVersion = courseRepository.save(publishedVersion);
            
            // 更新原课程的已发布版本ID
            savedCourse.setPublishedVersionId(savedPublishedVersion.getId());
            return courseRepository.save(savedCourse);
        } else {
            // 如果已有发布版本，更新发布版本
            Optional<Course> publishedVersionOpt = courseRepository.findById(course.getPublishedVersionId());
            
            if (publishedVersionOpt.isPresent()) {
                Course publishedVersion = publishedVersionOpt.get();
                BeanUtils.copyProperties(course, publishedVersion, "id", "dataVersion", 
                        "isPublishedVersion", "publishedVersionId");
                courseRepository.save(publishedVersion);
            }
            
            return courseRepository.save(course);
        }
    }
    
    @Override
    @Transactional
    public Course rejectCourse(Long id, String reason, Long reviewerId) {
        Course course = getCourseById(id);
        
        // 检查课程状态，只有审核中状态的课程才能被拒绝
        if (!course.getStatusEnum().equals(CourseStatus.REVIEWING)) {
            throw new BusinessException(400, "只有审核中状态的课程才能被拒绝");
        }
        
        // 检查审核人是否匹配
        if (!reviewerId.equals(course.getReviewerId())) {
            throw new BusinessException(400, "只有分配的审核人才能审核课程");
        }
        
        // 更新课程状态为已拒绝
        course.setStatusEnum(CourseStatus.REJECTED);
        course.setReviewComment(reason);
        course.setReviewedAt(LocalDateTime.now());
        course.setVersionTypeEnum(CourseVersion.DRAFT);
        
        return courseRepository.save(course);
    }
    
    @Override
    @Transactional
    public Course unpublishCourse(Long id) {
        Course course = getCourseById(id);
        
        // 检查课程状态，只有已发布状态的课程才能下线
        if (!course.getStatusEnum().equals(CourseStatus.PUBLISHED)) {
            throw new BusinessException(400, "只有已发布状态的课程才能下线");
        }
        
        // 更新课程状态为已下线
        course.setStatusEnum(CourseStatus.UNPUBLISHED);
        
        return courseRepository.save(course);
    }
    
    @Override
    @Transactional
    public Course republishCourse(Long id) {
        Course course = getCourseById(id);
        
        // 检查课程状态，只有已下线状态的课程才能重新上线
        if (!course.getStatusEnum().equals(CourseStatus.UNPUBLISHED)) {
            throw new BusinessException(400, "只有已下线状态的课程才能重新上线");
        }
        
        // 更新课程状态为已发布
        course.setStatusEnum(CourseStatus.PUBLISHED);
        
        return courseRepository.save(course);
    }
    
    @Override
    @Transactional
    public Course reEditRejectedCourse(Long id) {
        Course course = getCourseById(id);
        
        // 检查课程状态，只有已拒绝状态的课程才能重新编辑
        if (!course.getStatusEnum().equals(CourseStatus.REJECTED)) {
            throw new BusinessException(400, "只有已拒绝状态的课程才能重新编辑");
        }
        
        // 更新课程状态为草稿
        course.setStatusEnum(CourseStatus.DRAFT);
        
        return courseRepository.save(course);
    }
    
    @Override
    public PreviewUrlVO generatePreviewUrl(Long id) {
        Course course = getCourseById(id);
        
        // 生成随机预览token
        String previewToken = UUID.randomUUID().toString();
        
        // 构建预览URL
        String previewUrl = "/api/courses/preview/" + previewToken;
        
        // 计算过期时间
        LocalDateTime expireTime = LocalDateTime.now().plusMinutes(PREVIEW_URL_EXPIRATION_MINUTES);
        
        // 在Redis中存储预览token和课程ID的映射，设置过期时间
        redisTemplate.opsForValue().set(
                PREVIEW_TOKEN_KEY_PREFIX + previewToken, 
                course.getId().toString(),
                PREVIEW_TOKEN_EXPIRATION_MINUTES,
                TimeUnit.MINUTES
        );
        
        log.info("生成课程预览URL - 课程ID: {}, token: {}, 过期时间: {}", course.getId(), previewToken, expireTime);
        
        return PreviewUrlVO.builder()
                .url(previewUrl)
                .expireTime(expireTime)
                .courseId(course.getId())
                .courseTitle(course.getTitle())
                .build();
    }
    
    @Override
    public Course getCourseByPreviewToken(String token) {
        // 从Redis中获取token对应的课程ID
        String courseIdStr = redisTemplate.opsForValue().get(PREVIEW_TOKEN_KEY_PREFIX + token);
        
        if (courseIdStr == null) {
            log.warn("预览token不存在或已过期: {}", token);
            throw new BusinessException(403, "预览链接不存在或已过期");
        }
        
        try {
            Long courseId = Long.parseLong(courseIdStr);
            log.info("通过预览token获取课程 - token: {}, 课程ID: {}", token, courseId);
            return getCourseById(courseId);
        } catch (NumberFormatException e) {
            log.error("Redis中存储的课程ID格式错误: {}", courseIdStr, e);
            throw new BusinessException(500, "系统错误，无法获取预览课程");
        }
    }
    
    @Override
    @Transactional
    public Course updatePaymentSettings(Long id, Integer paymentType, BigDecimal price, BigDecimal discountPrice) {
        Course course = getCourseById(id);
        
        // 检查课程状态，只有草稿或已拒绝状态的课程才能更新支付设置
        if (!course.getStatusEnum().equals(CourseStatus.DRAFT) && 
            !course.getStatusEnum().equals(CourseStatus.REJECTED)) {
            throw new BusinessException(400, "只有草稿或已拒绝状态的课程才能更新支付设置");
        }
        
        // 设置付费类型
        if (paymentType == null) {
            paymentType = CoursePaymentType.FREE.getValue();
        }
        
        // 如果是付费课程，验证价格
        if (paymentType.equals(CoursePaymentType.PAID.getValue())) {
            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException(400, "付费课程必须设置价格且价格必须大于0");
            }
        }
        
        // 更新支付设置
        course.setPaymentType(paymentType);
        course.setPrice(price);
        course.setDiscountPrice(discountPrice);
        
        return courseRepository.save(course);
    }
} 