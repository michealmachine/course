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
import com.zhangziqi.online_course_mine.service.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.io.IOException;
import java.util.UUID;

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
    private final MinioService minioService;
    
    // 预览URL有效期（分钟）
    private static final long PREVIEW_URL_EXPIRATION_MINUTES = 60;
    // 预览token有效期（分钟）
    private static final long PREVIEW_TOKEN_EXPIRATION_MINUTES = 1440; // 24小时
    // Redis中存储预览token的键前缀
    private static final String PREVIEW_TOKEN_KEY_PREFIX = "course:preview:";
    
    @Override
    @Transactional
    public Course createCourse(CourseCreateDTO dto, Long creatorId, Long institutionId) {
        // 验证机构是否存在
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("机构不存在，ID: " + institutionId));
        
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
            
            // 增加标签使用次数
            tags.forEach(tag -> {
                tag.incrementUseCount();
                tagRepository.save(tag);
            });
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
                .tags(tags.isEmpty() ? null : tags)
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
    public Course updateCourse(Long id, CourseCreateDTO dto, Long institutionId) {
        // 获取课程
        Course course = getCourseById(id);
        
        // 检查课程状态，只有草稿或已拒绝状态的课程才能更新
        if (!course.getStatusEnum().equals(CourseStatus.DRAFT) && 
            !course.getStatusEnum().equals(CourseStatus.REJECTED)) {
            throw new BusinessException(400, "只有草稿或已拒绝状态的课程才能更新");
        }
        
        // 验证机构是否存在
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("机构不存在，ID: " + institutionId));
        
        // 验证分类是否存在（如果指定了分类）
        Category category = null;
        if (dto.getCategoryId() != null) {
            category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("分类不存在，ID: " + dto.getCategoryId()));
        }
        
        // 保存旧标签用于计数更新
        Set<Tag> oldTags = new HashSet<>(course.getTags());
        
        // 验证标签是否存在（如果指定了标签）
        Set<Tag> newTags;
        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            newTags = dto.getTagIds().stream()
                    .map(tagId -> tagRepository.findById(tagId)
                            .orElseThrow(() -> new ResourceNotFoundException("标签不存在，ID: " + tagId)))
                    .collect(Collectors.toSet());
            
            // 处理标签使用计数
            // 1. 减少不再使用的旧标签的计数
            oldTags.forEach(oldTag -> {
                if (!newTags.contains(oldTag)) {
                    oldTag.decrementUseCount();
                    tagRepository.save(oldTag);
                }
            });
            
            // 2. 增加新增标签的计数
            newTags.forEach(newTag -> {
                if (!oldTags.contains(newTag)) {
                    newTag.incrementUseCount();
                    tagRepository.save(newTag);
                }
            });
        } else {
            newTags = new HashSet<>();
            // 如果新标签列表为空，减少所有旧标签的计数
            oldTags.forEach(oldTag -> {
                oldTag.decrementUseCount();
                tagRepository.save(oldTag);
            });
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
        course.setTags(newTags);
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
        
        // 减少标签使用计数
        if (course.getTags() != null && !course.getTags().isEmpty()) {
            course.getTags().forEach(tag -> {
                tag.decrementUseCount();
                tagRepository.save(tag);
            });
        }
        
        courseRepository.delete(course);
    }
    
    @Override
    @Transactional
    public Course updateCourseCover(Long id, MultipartFile file) throws IOException {
        Course course = getCourseById(id);
        
        // 检查课程状态，只有草稿或已拒绝状态的课程才能更新封面
        if (!course.getStatusEnum().equals(CourseStatus.DRAFT) && 
            !course.getStatusEnum().equals(CourseStatus.REJECTED)) {
            throw new BusinessException(400, "只有草稿或已拒绝状态的课程才能更新封面");
        }
        
        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(400, "只支持上传图片文件");
        }
        
        // 检查文件大小（最大5MB）
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BusinessException(400, "文件大小不能超过5MB");
        }
        
        // 生成唯一的对象名
        String objectName = "course-covers/" + course.getId() + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();
        
        // 上传到MinIO
        String coverImageUrl = minioService.uploadFile(objectName, file.getInputStream(), file.getContentType());
        
        // 获取旧封面URL
        String oldCoverUrl = course.getCoverImage();
        
        // 更新课程封面
        course.setCoverImage(coverImageUrl);
        Course updatedCourse = courseRepository.save(course);
        
        // 尝试删除旧封面
        if (oldCoverUrl != null && !oldCoverUrl.isEmpty()) {
            try {
                // 从URL中提取对象名
                String oldObjectName = extractObjectNameFromUrl(oldCoverUrl);
                if (oldObjectName != null) {
                    boolean deleted = minioService.deleteFile(oldObjectName);
                    if (deleted) {
                        log.info("删除旧封面成功: {}", oldObjectName);
                    } else {
                        log.warn("删除旧封面失败: {}", oldObjectName);
                    }
                }
            } catch (Exception e) {
                log.error("删除旧封面出错: {}", e.getMessage(), e);
                // 继续执行，不影响封面更新
            }
        }
        
        return updatedCourse;
    }
    
    /**
     * 从URL中提取对象名
     * 例如：http://localhost:8999/media/course-covers/123/uuid-filename.jpg
     * 提取为：course-covers/123/uuid-filename.jpg
     */
    private String extractObjectNameFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        
        try {
            // 查找桶名在URL中的位置
            String bucketName = "media"; // MinIO配置中的桶名
            int bucketIndex = url.indexOf("/" + bucketName + "/");
            
            if (bucketIndex != -1) {
                // +桶名长度+2，是为了跳过"/桶名/"
                return url.substring(bucketIndex + bucketName.length() + 2);
            }
            
            // 如果使用特殊格式，尝试直接从路径中提取
            String[] parts = url.split("/");
            if (parts.length >= 3) {
                // 假设格式为：course-covers/123/uuid-filename.jpg
                return String.join("/", parts[parts.length - 3], parts[parts.length - 2], parts[parts.length - 1]);
            }
            
            log.warn("无法从URL中提取对象名: {}", url);
            return null;
        } catch (Exception e) {
            log.error("提取对象名出错: {}", e.getMessage());
            return null;
        }
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