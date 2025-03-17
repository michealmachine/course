package com.zhangziqi.online_course_mine.service.impl;

import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.exception.ResourceNotFoundException;
import com.zhangziqi.online_course_mine.model.dto.chapter.ChapterCreateDTO;
import com.zhangziqi.online_course_mine.model.dto.chapter.ChapterOrderDTO;
import com.zhangziqi.online_course_mine.model.entity.Chapter;
import com.zhangziqi.online_course_mine.model.entity.Course;
import com.zhangziqi.online_course_mine.model.enums.ChapterAccessType;
import com.zhangziqi.online_course_mine.model.enums.CoursePaymentType;
import com.zhangziqi.online_course_mine.model.vo.ChapterVO;
import com.zhangziqi.online_course_mine.repository.ChapterRepository;
import com.zhangziqi.online_course_mine.repository.CourseRepository;
import com.zhangziqi.online_course_mine.service.ChapterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 章节服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChapterServiceImpl implements ChapterService {

    private final ChapterRepository chapterRepository;
    private final CourseRepository courseRepository;

    @Override
    @Transactional
    public ChapterVO createChapter(ChapterCreateDTO dto) {
        // 验证课程是否存在
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在，ID: " + dto.getCourseId()));
        
        // 如果没有指定排序索引，则放在最后
        if (dto.getOrderIndex() == null) {
            Integer maxOrderIndex = chapterRepository.findMaxOrderIndexByCourse_Id(dto.getCourseId());
            dto.setOrderIndex(maxOrderIndex != null ? maxOrderIndex + 1 : 0);
        }
        
        // 设置访问类型
        Integer accessType = dto.getAccessType();
        if (course.getPaymentType().equals(CoursePaymentType.FREE.getValue())) {
            // 如果课程是免费的，章节必须是免费的
            accessType = ChapterAccessType.FREE_TRIAL.getValue();
        } else {
            // 如果课程是付费的，默认章节为付费，但允许设置为免费试看
            if (accessType == null) {
                accessType = ChapterAccessType.PAID_ONLY.getValue();
            } else if (!accessType.equals(ChapterAccessType.FREE_TRIAL.getValue()) && 
                       !accessType.equals(ChapterAccessType.PAID_ONLY.getValue())) {
                throw new BusinessException(400, "无效的访问类型");
            }
        }
        
        // 创建章节
        Chapter chapter = Chapter.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .course(course)
                .orderIndex(dto.getOrderIndex())
                .accessType(accessType)
                .estimatedMinutes(dto.getEstimatedMinutes())
                .build();
        
        Chapter savedChapter = chapterRepository.save(chapter);
        
        // 更新课程的章节总数和总时长
        updateCourseTotalLessonsAndDuration(course);
        
        // 转换为VO并返回
        return ChapterVO.fromEntity(savedChapter);
    }

    @Override
    @Transactional
    public ChapterVO updateChapter(Long id, ChapterCreateDTO dto) {
        // 获取章节
        Chapter chapter = findChapterById(id);
        
        // 验证课程是否存在，且课程ID是否一致
        Course course;
        if (!chapter.getCourse().getId().equals(dto.getCourseId())) {
            course = courseRepository.findById(dto.getCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException("课程不存在，ID: " + dto.getCourseId()));
            chapter.setCourse(course);
        } else {
            course = chapter.getCourse();
        }
        
        // 更新章节信息
        chapter.setTitle(dto.getTitle());
        chapter.setDescription(dto.getDescription());
        chapter.setOrderIndex(dto.getOrderIndex());
        
        // 更新访问类型
        if (course.getPaymentType().equals(CoursePaymentType.FREE.getValue())) {
            // 如果课程是免费的，章节必须是免费的
            chapter.setAccessType(ChapterAccessType.FREE_TRIAL.getValue());
        } else if (dto.getAccessType() != null) {
            // 如果课程是付费的，验证访问类型的有效性
            if (!dto.getAccessType().equals(ChapterAccessType.FREE_TRIAL.getValue()) && 
                !dto.getAccessType().equals(ChapterAccessType.PAID_ONLY.getValue())) {
                throw new BusinessException(400, "无效的访问类型");
            }
            chapter.setAccessType(dto.getAccessType());
        }
        
        // 更新学习时长估计（如果提供）
        if (dto.getEstimatedMinutes() != null) {
            chapter.setEstimatedMinutes(dto.getEstimatedMinutes());
        }
        
        Chapter updatedChapter = chapterRepository.save(chapter);
        
        // 更新课程的总时长
        updateCourseTotalLessonsAndDuration(chapter.getCourse());
        
        // 转换为VO并返回
        return ChapterVO.fromEntity(updatedChapter);
    }

    @Override
    @Transactional(readOnly = true)
    public ChapterVO getChapterById(Long id) {
        Chapter chapter = findChapterById(id);
        return ChapterVO.fromEntityWithSections(chapter);
    }
    
    /**
     * 查找章节实体（内部使用）
     */
    private Chapter findChapterById(Long id) {
        return chapterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("章节不存在，ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChapterVO> getChaptersByCourse(Long courseId) {
        // 验证课程是否存在
        courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在，ID: " + courseId));
        
        List<Chapter> chapters = chapterRepository.findByCourse_IdOrderByOrderIndexAsc(courseId);
        
        // 转换为VO并返回
        return chapters.stream()
                .map(ChapterVO::fromEntityWithSections)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteChapter(Long id) {
        // 获取章节
        Chapter chapter = findChapterById(id);
        Course course = chapter.getCourse();
        
        // 删除章节
        chapterRepository.delete(chapter);
        
        // 更新课程的章节总数和总时长
        updateCourseTotalLessonsAndDuration(course);
    }

    @Override
    @Transactional
    public ChapterVO updateAccessType(Long id, Integer accessType) {
        // 获取章节
        Chapter chapter = findChapterById(id);
        
        // 验证访问类型
        if (accessType == null) {
            throw new BusinessException(400, "访问类型不能为空");
        }
        
        // 检查课程的付费类型
        if (chapter.getCourse().getPaymentType().equals(CoursePaymentType.FREE.getValue())) {
            throw new BusinessException(400, "免费课程的章节不能修改访问类型");
        }
        
        // 检查访问类型是否有效
        if (!accessType.equals(ChapterAccessType.FREE_TRIAL.getValue()) && 
            !accessType.equals(ChapterAccessType.PAID_ONLY.getValue())) {
            throw new BusinessException(400, "无效的访问类型");
        }
        
        // 更新访问类型
        chapter.setAccessType(accessType);
        
        Chapter updatedChapter = chapterRepository.save(chapter);
        
        // 转换为VO并返回
        return ChapterVO.fromEntity(updatedChapter);
    }

    @Override
    @Transactional
    public List<ChapterVO> reorderChapters(Long courseId, List<ChapterOrderDTO> chapterOrders) {
        // 验证课程是否存在
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在，ID: " + courseId));
        
        // 获取当前所有章节
        List<Chapter> existingChapters = chapterRepository.findByCourse_IdOrderByOrderIndexAsc(courseId);
        Map<Long, Chapter> chapterMap = existingChapters.stream()
                .collect(Collectors.toMap(Chapter::getId, Function.identity()));
        
        // 验证所有提供的章节ID是否都属于该课程
        for (ChapterOrderDTO orderDTO : chapterOrders) {
            if (!chapterMap.containsKey(orderDTO.getId())) {
                throw new BusinessException(400, "章节不属于该课程，章节ID: " + orderDTO.getId());
            }
        }
        
        // 更新章节顺序
        for (ChapterOrderDTO orderDTO : chapterOrders) {
            Chapter chapter = chapterMap.get(orderDTO.getId());
            chapter.setOrderIndex(orderDTO.getOrderIndex());
            chapterRepository.save(chapter);
        }
        
        // 获取更新后的章节列表，按orderIndex排序
        List<Chapter> updatedChapters = chapterRepository.findByCourse_IdOrderByOrderIndexAsc(courseId);
        
        // 转换为VO并返回
        return updatedChapters.stream()
                .map(ChapterVO::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * 更新课程的章节总数和总时长
     * 
     * @param course 课程
     */
    private void updateCourseTotalLessonsAndDuration(Course course) {
        List<Chapter> chapters = chapterRepository.findByCourse_IdOrderByOrderIndexAsc(course.getId());
        
        // 计算课程的章节总数
        course.setTotalLessons(chapters.size());
        
        // 计算课程的总时长（分钟）
        int totalDuration = chapters.stream()
                .filter(chapter -> chapter.getEstimatedMinutes() != null)
                .mapToInt(Chapter::getEstimatedMinutes)
                .sum();
        course.setTotalDuration(totalDuration);
        
        courseRepository.save(course);
    }
} 