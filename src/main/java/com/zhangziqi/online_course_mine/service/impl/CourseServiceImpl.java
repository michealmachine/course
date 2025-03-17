package com.zhangziqi.online_course_mine.service.impl;

import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.exception.ResourceNotFoundException;
import com.zhangziqi.online_course_mine.model.dto.course.*;
import com.zhangziqi.online_course_mine.model.entity.*;
import com.zhangziqi.online_course_mine.model.enums.CourseStatus;
import com.zhangziqi.online_course_mine.model.enums.CourseVersion;
import com.zhangziqi.online_course_mine.model.enums.CoursePaymentType;
import com.zhangziqi.online_course_mine.model.enums.ChapterAccessType;
import com.zhangziqi.online_course_mine.model.vo.CourseVO;
import com.zhangziqi.online_course_mine.model.vo.PreviewUrlVO;
import com.zhangziqi.online_course_mine.model.vo.CourseStructureVO;
import com.zhangziqi.online_course_mine.repository.CategoryRepository;
import com.zhangziqi.online_course_mine.repository.CourseRepository;
import com.zhangziqi.online_course_mine.repository.InstitutionRepository;
import com.zhangziqi.online_course_mine.repository.TagRepository;
import com.zhangziqi.online_course_mine.service.CourseService;
import com.zhangziqi.online_course_mine.service.MinioService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
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
    public CourseVO createCourse(CourseCreateDTO dto, Long creatorId, Long institutionId) {
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
        
        Course savedCourse = courseRepository.save(course);
        
        // 转换为VO并返回
        return CourseVO.fromEntity(savedCourse);
    }
    
    @Override
    @Transactional
    public CourseVO updateCourse(Long id, CourseCreateDTO dto, Long institutionId) {
        // 获取课程
        Course course = findCourseById(id);
        
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
        Set<Tag> oldTags = course.getTags() != null ? new HashSet<>(course.getTags()) : new HashSet<>();
        
        // 验证标签是否存在（如果指定了标签）
        Set<Tag> newTags;
        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            newTags = dto.getTagIds().stream()
                    .map(tagId -> tagRepository.findById(tagId)
                            .orElseThrow(() -> new ResourceNotFoundException("标签不存在，ID: " + tagId)))
                    .collect(Collectors.toSet());
            
            // 处理标签使用计数
            // 1. 减少不再使用的旧标签的计数 - 修改为使用ID比较
            Set<Long> newTagIds = newTags.stream().map(Tag::getId).collect(Collectors.toSet());
            oldTags.forEach(oldTag -> {
                if (oldTag != null && oldTag.getId() != null && !newTagIds.contains(oldTag.getId())) {
                    oldTag.decrementUseCount();
                    tagRepository.save(oldTag);
                }
            });
            
            // 2. 增加新增标签的计数 - 修改为使用ID比较
            Set<Long> oldTagIds = oldTags.stream()
                    .filter(tag -> tag != null && tag.getId() != null)
                    .map(Tag::getId)
                    .collect(Collectors.toSet());
            newTags.forEach(newTag -> {
                if (newTag != null && newTag.getId() != null && !oldTagIds.contains(newTag.getId())) {
                    newTag.incrementUseCount();
                    tagRepository.save(newTag);
                }
            });
        } else {
            newTags = new HashSet<>();
            // 如果新标签列表为空，减少所有旧标签的计数
            oldTags.forEach(oldTag -> {
                if (oldTag != null) {
                    oldTag.decrementUseCount();
                    tagRepository.save(oldTag);
                }
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
        course.setTags(newTags.isEmpty() ? null : newTags);
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
        
        Course updatedCourse = courseRepository.save(course);
        
        // 转换为VO并返回
        return CourseVO.fromEntity(updatedCourse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public CourseVO getCourseById(Long id) {
        Course course = findCourseById(id);
        return CourseVO.fromEntity(course);
    }
    
    @Override
    @Transactional(readOnly = true)
    public CourseStructureVO getCourseStructure(Long id) {
        // 获取课程实体（带关联对象）
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在，ID：" + id));
        
        // 使用CourseStructureVO的工厂方法创建结构对象
        return CourseStructureVO.fromEntity(course);
    }
    
    /**
     * 查找课程实体（内部使用）
     */
    private Course findCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在，ID：" + id));
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<CourseVO> getCoursesByInstitution(Long institutionId, Pageable pageable) {
        // 验证机构是否存在
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("机构不存在，ID: " + institutionId));
        
        // 只返回工作区版本的课程（非发布版本）
        Page<Course> coursePage = courseRepository.findByInstitutionAndIsPublishedVersion(institution, false, pageable);
        
        // 转换为VO列表
        List<CourseVO> courseVOs = coursePage.getContent().stream()
                .map(CourseVO::fromEntity)
                .collect(Collectors.toList());
        
        // 创建新的VO分页对象
        return new PageImpl<>(courseVOs, pageable, coursePage.getTotalElements());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<CourseVO> getWorkspaceCoursesByInstitution(Long institutionId, Pageable pageable) {
        // 直接调用更新后的getCoursesByInstitution方法，它现在只返回工作区版本
        return getCoursesByInstitution(institutionId, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<CourseVO> getPublishedCoursesByInstitution(Long institutionId, Pageable pageable) {
        // 验证机构是否存在
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("机构不存在，ID: " + institutionId));
        
        // 只返回发布版本的课程
        Page<Course> coursePage = courseRepository.findByInstitutionAndIsPublishedVersion(institution, true, pageable);
        
        // 转换为VO列表
        List<CourseVO> courseVOs = coursePage.getContent().stream()
                .map(CourseVO::fromEntity)
                .collect(Collectors.toList());
        
        // 创建新的VO分页对象
        return new PageImpl<>(courseVOs, pageable, coursePage.getTotalElements());
    }
    
    @Override
    @Transactional
    public void deleteCourse(Long id) {
        // 获取课程
        Course course = findCourseById(id);
        
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
    public CourseVO updateCourseCover(Long id, MultipartFile file) throws IOException {
        // 获取课程
        Course course = findCourseById(id);
        
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
        
        // 转换为VO并返回
        return CourseVO.fromEntity(updatedCourse);
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
    public CourseVO submitForReview(Long id) {
        // 获取课程
        Course course = findCourseById(id);
        
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
        
        Course updatedCourse = courseRepository.save(course);
        
        // 转换为VO并返回
        return CourseVO.fromEntity(updatedCourse);
    }
    
    @Override
    @Transactional
    public CourseVO startReview(Long id, Long reviewerId) {
        // 获取课程
        Course course = findCourseById(id);
        
        // 检查课程状态，只有待审核状态的课程才能开始审核
        if (!course.getStatusEnum().equals(CourseStatus.PENDING_REVIEW)) {
            throw new BusinessException(400, "只有待审核状态的课程才能开始审核");
        }
        
        // 更新课程状态为审核中
        course.setStatusEnum(CourseStatus.REVIEWING);
        course.setReviewerId(reviewerId);
        
        Course updatedCourse = courseRepository.save(course);
        
        // 转换为VO并返回
        return CourseVO.fromEntity(updatedCourse);
    }
    
    @Override
    @Transactional
    public CourseVO approveCourse(Long id, String comment, Long reviewerId) {
        // 获取课程
        Course course = findCourseById(id);
        
        // 检查课程状态，只有审核中状态的课程才能审核通过
        if (!course.getStatusEnum().equals(CourseStatus.REVIEWING)) {
            throw new BusinessException(400, "只有审核中状态的课程才能审核通过");
        }
        
        // 检查审核人是否匹配
        if (!reviewerId.equals(course.getReviewerId())) {
            throw new BusinessException(400, "只有分配的审核人才能审核课程");
        }
        
        // 更新课程状态为草稿（工作区版本）
        course.setStatusEnum(CourseStatus.DRAFT); // 修改为草稿而不是已发布
        course.setReviewComment(comment);
        course.setReviewedAt(LocalDateTime.now());
        course.setVersionTypeEnum(CourseVersion.DRAFT); // 工作区版本类型也变为草稿
        
        // 如果是首次发布，创建一个已发布版本
        if (course.getPublishedVersionId() == null) {
            // 保存工作区版本课程，获取ID
            Course savedCourse = courseRepository.save(course);
            
            // 创建已发布版本
            Course publishedVersion = new Course();
            // 只复制基本属性，排除集合和关联实体
            BeanUtils.copyProperties(savedCourse, publishedVersion, 
                "id", "dataVersion", "chapters", "tags", "category", "institution",
                "isPublishedVersion", "publishedVersionId", "status", "versionType",
                "studentCount", "averageRating", "ratingCount", "favoriteUsers");
            publishedVersion.setId(null); // 确保新对象没有ID
            publishedVersion.setIsPublishedVersion(true); // 标记为发布版本
            publishedVersion.setPublishedVersionId(savedCourse.getId());
            publishedVersion.setStatusEnum(CourseStatus.PUBLISHED); // 发布版本状态设为已发布
            publishedVersion.setVersionTypeEnum(CourseVersion.PUBLISHED); // 发布版本的版本类型为已发布
            
            // 设置关联的分类（只复制引用）
            if (savedCourse.getCategory() != null) {
                publishedVersion.setCategory(savedCourse.getCategory());
            }
            
            // 设置关联的机构（只复制引用）
            if (savedCourse.getInstitution() != null) {
                publishedVersion.setInstitution(savedCourse.getInstitution());
            }
            
            // 复制标签关联（不复制标签实体）
            if (savedCourse.getTags() != null && !savedCourse.getTags().isEmpty()) {
                publishedVersion.setTags(new HashSet<>(savedCourse.getTags()));
            }
            
            // 深度复制章节和小节
            if (savedCourse.getChapters() != null && !savedCourse.getChapters().isEmpty()) {
                List<Chapter> copiedChapters = new ArrayList<>();
                
                for (Chapter originalChapter : savedCourse.getChapters()) {
                    // 创建新章节对象
                    Chapter copiedChapter = new Chapter();
                    // 复制基本属性（排除集合和关联实体）
                    BeanUtils.copyProperties(originalChapter, copiedChapter, 
                        "id", "course", "sections", "dataVersion");
                    copiedChapter.setCourse(publishedVersion);
                    
                    // 深度复制小节
                    if (originalChapter.getSections() != null && !originalChapter.getSections().isEmpty()) {
                        List<Section> copiedSections = new ArrayList<>();
                        
                        for (Section originalSection : originalChapter.getSections()) {
                            // 创建新小节对象
                            Section copiedSection = new Section();
                            // 复制基本属性（排除关联实体）
                            BeanUtils.copyProperties(originalSection, copiedSection, 
                                "id", "chapter", "media", "questionGroup", "dataVersion");
                            copiedSection.setChapter(copiedChapter);
                            
                            // 设置媒体资源和题库引用（不复制这些实体）
                            if (originalSection.getMediaId() != null) {
                                copiedSection.setMediaId(originalSection.getMediaId());
                            }
                            
                            if (originalSection.getQuestionGroupId() != null) {
                                copiedSection.setQuestionGroupId(originalSection.getQuestionGroupId());
                            }
                            
                            copiedSections.add(copiedSection);
                        }
                        
                        copiedChapter.setSections(copiedSections);
                    }
                    
                    copiedChapters.add(copiedChapter);
                }
                
                publishedVersion.setChapters(copiedChapters);
            }
            
            // 保存已发布版本
            Course savedPublishedVersion = courseRepository.save(publishedVersion);
            
            // 更新原课程的已发布版本ID
            savedCourse.setPublishedVersionId(savedPublishedVersion.getId());
            Course updatedCourse = courseRepository.save(savedCourse);
            
            // 转换为VO并返回
            return CourseVO.fromEntity(updatedCourse);
        } else {
            // 如果已有发布版本，更新发布版本
            Optional<Course> publishedVersionOpt = courseRepository.findById(course.getPublishedVersionId());
            
            if (publishedVersionOpt.isPresent()) {
                Course publishedVersion = publishedVersionOpt.get();
                // 只更新基本属性，不更新集合或关联实体
                BeanUtils.copyProperties(course, publishedVersion, 
                    "id", "dataVersion", "chapters", "tags", "category", "institution",
                    "isPublishedVersion", "publishedVersionId", "status", "versionType",
                    "studentCount", "averageRating", "ratingCount", "favoriteUsers");
                
                // 确保发布版本的状态为已发布
                publishedVersion.setStatusEnum(CourseStatus.PUBLISHED);
                publishedVersion.setVersionTypeEnum(CourseVersion.PUBLISHED);
                publishedVersion.setReviewComment(comment);
                publishedVersion.setReviewedAt(LocalDateTime.now());
                
                // 设置关联的分类和机构（只复制引用）
                if (course.getCategory() != null) {
                    publishedVersion.setCategory(course.getCategory());
                }
                
                if (course.getInstitution() != null) {
                    publishedVersion.setInstitution(course.getInstitution());
                }
                
                // 复制标签关联（不复制标签实体）
                if (course.getTags() != null) {
                    publishedVersion.setTags(new HashSet<>(course.getTags()));
                } else {
                    publishedVersion.setTags(new HashSet<>());
                }
                
                // 清空现有章节 - 依赖JPA级联删除
                // 由于配置了cascade=CascadeType.ALL和orphanRemoval=true，
                // 清空集合会自动删除数据库中的章节和关联的小节
                if (publishedVersion.getChapters() != null) {
                    publishedVersion.getChapters().clear();
                } else {
                    publishedVersion.setChapters(new ArrayList<>());
                }
                
                // 持久化变更，确保级联删除执行
                courseRepository.saveAndFlush(publishedVersion);
                
                // 深度复制章节和小节
                if (course.getChapters() != null && !course.getChapters().isEmpty()) {
                    for (Chapter originalChapter : course.getChapters()) {
                        // 创建新章节对象
                        Chapter copiedChapter = new Chapter();
                        // 复制基本属性（排除集合和关联实体）
                        BeanUtils.copyProperties(originalChapter, copiedChapter, 
                            "id", "course", "sections", "dataVersion");
                        copiedChapter.setCourse(publishedVersion);
                        
                        // 为小节集合初始化
                        copiedChapter.setSections(new ArrayList<>());
                        
                        // 深度复制小节
                        if (originalChapter.getSections() != null && !originalChapter.getSections().isEmpty()) {
                            for (Section originalSection : originalChapter.getSections()) {
                                // 创建新小节对象
                                Section copiedSection = new Section();
                                // 复制基本属性（排除关联实体）
                                BeanUtils.copyProperties(originalSection, copiedSection, 
                                    "id", "chapter", "media", "questionGroup", "dataVersion");
                                copiedSection.setChapter(copiedChapter);
                                
                                // 设置媒体资源和题库引用（不复制这些实体）
                                if (originalSection.getMediaId() != null) {
                                    copiedSection.setMediaId(originalSection.getMediaId());
                                }
                                
                                if (originalSection.getQuestionGroupId() != null) {
                                    copiedSection.setQuestionGroupId(originalSection.getQuestionGroupId());
                                }
                                
                                copiedChapter.getSections().add(copiedSection);
                            }
                        }
                        
                        publishedVersion.getChapters().add(copiedChapter);
                    }
                }
                
                courseRepository.save(publishedVersion);
            }
            
            Course updatedCourse = courseRepository.save(course);
            
            // 转换为VO并返回
            return CourseVO.fromEntity(updatedCourse);
        }
    }
    
    @Override
    @Transactional
    public CourseVO rejectCourse(Long id, String reason, Long reviewerId) {
        // 获取课程
        Course course = findCourseById(id);
        
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
        
        Course updatedCourse = courseRepository.save(course);
        
        // 转换为VO并返回
        return CourseVO.fromEntity(updatedCourse);
    }
    
    @Override
    @Transactional
    public CourseVO unpublishCourse(Long id) {
        // 获取课程
        Course course = findCourseById(id);
        
        // 检查课程状态，只有已发布状态的课程才能下线
        if (!course.getStatusEnum().equals(CourseStatus.PUBLISHED)) {
            throw new BusinessException(400, "只有已发布状态的课程才能下线");
        }
        
        // 更新课程状态为已下线
        course.setStatusEnum(CourseStatus.UNPUBLISHED);
        
        Course updatedCourse = courseRepository.save(course);
        
        // 转换为VO并返回
        return CourseVO.fromEntity(updatedCourse);
    }
    
    @Override
    @Transactional
    public CourseVO republishCourse(Long id) {
        // 获取课程
        Course course = findCourseById(id);
        
        // 检查课程状态，只有已下线状态的课程才能重新上线
        if (!course.getStatusEnum().equals(CourseStatus.UNPUBLISHED)) {
            throw new BusinessException(400, "只有已下线状态的课程才能重新上线");
        }
        
        // 更新课程状态为已发布
        course.setStatusEnum(CourseStatus.PUBLISHED);
        
        Course updatedCourse = courseRepository.save(course);
        
        // 转换为VO并返回
        return CourseVO.fromEntity(updatedCourse);
    }
    
    @Override
    @Transactional
    public CourseVO reEditRejectedCourse(Long id) {
        // 获取课程
        Course course = findCourseById(id);
        
        // 检查课程状态，只有已拒绝状态的课程才能重新编辑
        if (!course.getStatusEnum().equals(CourseStatus.REJECTED)) {
            throw new BusinessException(400, "只有已拒绝状态的课程才能重新编辑");
        }
        
        // 更新课程状态为草稿
        course.setStatusEnum(CourseStatus.DRAFT);
        
        Course updatedCourse = courseRepository.save(course);
        
        // 转换为VO并返回
        return CourseVO.fromEntity(updatedCourse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public PreviewUrlVO generatePreviewUrl(Long id) {
        // 获取课程
        Course course = findCourseById(id);
        
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
    public CourseVO getCourseByPreviewToken(String token) {
        // 从Redis中获取token对应的课程ID
        String courseIdStr = redisTemplate.opsForValue().get(PREVIEW_TOKEN_KEY_PREFIX + token);
        
        if (courseIdStr == null) {
            log.warn("预览token不存在或已过期: {}", token);
            throw new BusinessException(403, "预览链接不存在或已过期");
        }
        
        try {
            Long courseId = Long.parseLong(courseIdStr);
            log.info("通过预览token获取课程 - token: {}, 课程ID: {}", token, courseId);
            Course course = findCourseById(courseId);
            
            // 转换为VO并返回
            return CourseVO.fromEntity(course);
        } catch (NumberFormatException e) {
            log.error("Redis中存储的课程ID格式错误: {}", courseIdStr, e);
            throw new BusinessException(500, "系统错误，无法获取预览课程");
        }
    }
    
    @Override
    public CourseStructureVO getCourseStructureByPreviewToken(String token) {
        // 从Redis中获取token对应的课程ID
        String courseIdStr = redisTemplate.opsForValue().get(PREVIEW_TOKEN_KEY_PREFIX + token);
        
        if (courseIdStr == null) {
            log.warn("预览token不存在或已过期: {}", token);
            throw new BusinessException(403, "预览链接不存在或已过期");
        }
        
        try {
            Long courseId = Long.parseLong(courseIdStr);
            log.info("通过预览token获取课程结构 - token: {}, 课程ID: {}", token, courseId);
            
            // 直接调用获取课程结构的方法
            return getCourseStructure(courseId);
        } catch (NumberFormatException e) {
            log.error("Redis中存储的课程ID格式错误: {}", courseIdStr, e);
            throw new BusinessException(500, "系统错误，无法获取预览课程");
        }
    }
    
    @Override
    @Transactional
    public CourseVO updatePaymentSettings(Long id, Integer paymentType, BigDecimal price, BigDecimal discountPrice) {
        // 获取课程
        Course course = findCourseById(id);
        
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
        
        // 如果课程变更为免费，则更新所有章节为免费访问
        if (paymentType.equals(CoursePaymentType.FREE.getValue())) {
            if (course.getChapters() != null && !course.getChapters().isEmpty()) {
                for (Chapter chapter : course.getChapters()) {
                    chapter.setAccessType(ChapterAccessType.FREE_TRIAL.getValue());
                }
            }
        }
        
        Course updatedCourse = courseRepository.save(course);
        
        // 转换为VO并返回
        return CourseVO.fromEntity(updatedCourse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<CourseVO> getCoursesByStatus(Integer status, Pageable pageable) {
        // 获取指定状态的课程列表
        Page<Course> coursePage = courseRepository.findByStatus(status, pageable);
        
        // 转换为VO
        return coursePage.map(CourseVO::fromEntity);
    }
    
    @Override
    public Page<CourseVO> getCoursesByStatusAndReviewer(Integer status, Long reviewerId, Pageable pageable) {
        Specification<Course> spec = (root, query, cb) -> {
            return cb.and(
                cb.equal(root.get("status"), status),
                cb.equal(root.get("reviewerId"), reviewerId)
            );
        };
        
        Page<Course> coursePage = courseRepository.findAll(spec, pageable);
        return coursePage.map(CourseVO::fromEntity);
    }
    
    @Override
    @Transactional(readOnly = true)
    public CourseVO getPublishedVersionByWorkspaceId(Long workspaceId) {
        Optional<Course> publishedVersionOpt = courseRepository.findPublishedVersionByWorkspaceId(workspaceId);
        return publishedVersionOpt.map(CourseVO::fromEntity).orElse(null);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<CourseVO> searchCourses(CourseSearchDTO searchDTO, Pageable pageable) {
        log.info("搜索课程，参数: {}", searchDTO);
        
        Specification<Course> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 只搜索已发布状态的课程
            predicates.add(cb.equal(root.get("status"), CourseStatus.PUBLISHED.getValue()));
            predicates.add(cb.equal(root.get("isPublishedVersion"), true));
            
            // 关键字搜索（标题和描述）
            if (searchDTO.getKeyword() != null && !searchDTO.getKeyword().isEmpty()) {
                String likePattern = "%" + searchDTO.getKeyword() + "%";
                predicates.add(cb.or(
                    cb.like(root.get("title"), likePattern),
                    cb.like(root.get("description"), likePattern)
                ));
            }
            
            // 分类筛选
            if (searchDTO.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), searchDTO.getCategoryId()));
            }
            
            // 标签筛选
            if (searchDTO.getTagIds() != null && !searchDTO.getTagIds().isEmpty()) {
                // 创建一个子查询，找到同时包含所有指定标签的课程
                Join<Course, Tag> tagJoin = root.join("tags", JoinType.INNER);
                predicates.add(tagJoin.get("id").in(searchDTO.getTagIds()));
            }
            
            // 难度筛选
            if (searchDTO.getDifficulty() != null) {
                predicates.add(cb.equal(root.get("difficulty"), searchDTO.getDifficulty()));
            }
            
            // 价格范围筛选
            if (searchDTO.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), searchDTO.getMinPrice()));
            }
            if (searchDTO.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), searchDTO.getMaxPrice()));
            }
            
            // 付费类型筛选
            if (searchDTO.getPaymentType() != null) {
                predicates.add(cb.equal(root.get("paymentType"), searchDTO.getPaymentType()));
            }
            
            // 机构筛选
            if (searchDTO.getInstitutionId() != null) {
                predicates.add(cb.equal(root.get("institution").get("id"), searchDTO.getInstitutionId()));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        Page<Course> coursePage = courseRepository.findAll(spec, pageable);
        
        log.info("搜索结果: 共{}条记录", coursePage.getTotalElements());
        
        // 转换为VO
        return coursePage.map(CourseVO::fromEntity);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CourseVO> getHotCourses(int limit) {
        log.info("获取热门课程，数量限制: {}", limit);
        
        Pageable pageable = PageRequest.of(0, limit);
        List<Course> courses = courseRepository.findHotCourses(
            CourseStatus.PUBLISHED.getValue(),
            true,
            pageable
        );
        
        log.info("获取到{}门热门课程", courses.size());
        
        return courses.stream()
            .map(CourseVO::fromEntity)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CourseVO> getLatestCourses(int limit) {
        log.info("获取最新课程，数量限制: {}", limit);
        
        Pageable pageable = PageRequest.of(0, limit);
        List<Course> courses = courseRepository.findLatestCourses(
            CourseStatus.PUBLISHED.getValue(),
            true,
            pageable
        );
        
        log.info("获取到{}门最新课程", courses.size());
        
        return courses.stream()
            .map(CourseVO::fromEntity)
            .collect(Collectors.toList());
    }
    
    /**
     * 获取课程的公开预览结构（免费课程或付费课程的试学部分）
     * @param id 课程ID
     * @param isUserEnrolled 用户是否已购买/注册该课程
     * @return 课程结构VO，对于未购买的付费课程，只返回免费试学章节的内容
     */
    @Override
    @Transactional(readOnly = true)
    public CourseStructureVO getPublicCourseStructure(Long id, boolean isUserEnrolled) {
        // 获取课程实体（带关联对象）
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在，ID：" + id));
        
        // 检查课程是否已发布
        if (!course.getIsPublishedVersion() || !course.getStatusEnum().equals(CourseStatus.PUBLISHED)) {
            throw new BusinessException(403, "该课程尚未发布，无法预览");
        }
        
        // 创建基础的课程结构VO
        CourseStructureVO structureVO = CourseStructureVO.fromEntity(course);
        
        // 如果是免费课程或用户已注册/购买课程，直接返回完整结构
        if (CoursePaymentType.FREE.getValue().equals(course.getPaymentType()) || isUserEnrolled) {
            return structureVO;
        }
        
        // 对于付费课程且用户未注册/购买，处理章节和小节的展示
        structureVO.getChapters().forEach(chapterVO -> {
            if (ChapterAccessType.PAID_ONLY.getValue().equals(chapterVO.getAccessType())) {
                // 对于付费章节，保留小节但清除敏感资源信息
                chapterVO.getSections().forEach(sectionVO -> {
                    // 清除媒体资源ID
                    sectionVO.setMediaId(null);
                    // 清除题目组ID
                    sectionVO.setQuestionGroupId(null);
                    // 清除资源类型鉴别器，避免前端误判有资源
                    sectionVO.setResourceTypeDiscriminator("NONE");
                    // 其他可能的资源相关字段也需要清除
                    sectionVO.setMediaResourceType(null);
                    
                    // 添加付费标记或提示信息（可选）
                    if (sectionVO.getDescription() == null) {
                        sectionVO.setDescription("这是付费内容，购买课程后可查看");
                    } else if (!sectionVO.getDescription().contains("付费内容")) {
                        sectionVO.setDescription(sectionVO.getDescription() + " (付费内容，购买后可查看)");
                    }
                });
            }
            // 免费试学章节保持不变
        });
        
        return structureVO;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CourseVO> getTopRatedCourses(int limit) {
        log.info("获取高评分课程，数量限制: {}", limit);
        
        Pageable pageable = PageRequest.of(0, limit);
        List<Course> courses = courseRepository.findTopRatedCourses(
            CourseStatus.PUBLISHED.getValue(),
            true,
            5, // 最小评分数量
            pageable
        );
        
        log.info("获取到{}门高评分课程", courses.size());
        
        return courses.stream()
            .map(CourseVO::fromEntity)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void incrementStudentCount(Long courseId) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new ResourceNotFoundException("课程不存在，ID: " + courseId));
        
        // 只能更新发布版本的统计数据
        if (!Boolean.TRUE.equals(course.getIsPublishedVersion())) {
            throw new BusinessException(400, "只能更新发布版本的课程统计数据");
        }
        
        // 初始化或增加学习人数
        if (course.getStudentCount() == null) {
            course.setStudentCount(1);
        } else {
            course.setStudentCount(course.getStudentCount() + 1);
        }
        
        courseRepository.save(course);
        log.info("课程{}学习人数增加，当前学习人数: {}", courseId, course.getStudentCount());
    }

    @Override
    @Transactional
    public void updateCourseRating(Long courseId, Integer newRating) {
        if (newRating == null || newRating < 1 || newRating > 5) {
            throw new BusinessException(400, "评分必须在1-5之间");
        }
        
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new ResourceNotFoundException("课程不存在，ID: " + courseId));
        
        // 只能更新发布版本的统计数据
        if (!Boolean.TRUE.equals(course.getIsPublishedVersion())) {
            throw new BusinessException(400, "只能更新发布版本的课程统计数据");
        }
        
        Float currentAvg = course.getAverageRating();
        Integer currentCount = course.getRatingCount();
        
        if (currentAvg == null || currentCount == null || currentCount == 0) {
            course.setAverageRating(newRating.floatValue());
            course.setRatingCount(1);
        } else {
            // 使用加权平均公式计算新的平均评分
            Float newAvg = (currentAvg * currentCount + newRating) / (currentCount + 1);
            course.setAverageRating(newAvg);
            course.setRatingCount(currentCount + 1);
        }
        
        courseRepository.save(course);
        log.info("课程{}评分更新，当前评分: {}，评分人数: {}", 
             courseId, course.getAverageRating(), course.getRatingCount());
    }
} 