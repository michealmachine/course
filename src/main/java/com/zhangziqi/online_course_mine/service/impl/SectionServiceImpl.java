package com.zhangziqi.online_course_mine.service.impl;

import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.exception.ResourceNotFoundException;
import com.zhangziqi.online_course_mine.model.dto.section.*;
import com.zhangziqi.online_course_mine.model.entity.*;
import com.zhangziqi.online_course_mine.model.vo.SectionVO;
import com.zhangziqi.online_course_mine.repository.*;
import com.zhangziqi.online_course_mine.service.SectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 小节服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SectionServiceImpl implements SectionService {

    private final SectionRepository sectionRepository;
    private final ChapterRepository chapterRepository;
    private final CourseRepository courseRepository;
    private final MediaRepository mediaRepository;
    private final QuestionGroupRepository questionGroupRepository;

    @Override
    @Transactional
    public SectionVO createSection(SectionCreateDTO dto) {
        // 验证章节是否存在
        Chapter chapter = chapterRepository.findById(dto.getChapterId())
                .orElseThrow(() -> new ResourceNotFoundException("章节不存在，ID: " + dto.getChapterId()));
        
        // 如果没有指定排序索引，则放在最后
        if (dto.getOrderIndex() == null) {
            Integer maxOrderIndex = sectionRepository.findMaxOrderIndexByChapter_Id(dto.getChapterId());
            dto.setOrderIndex(maxOrderIndex != null ? maxOrderIndex + 1 : 0);
        }
        
        // 创建小节
        Section section = Section.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .chapter(chapter)
                .orderIndex(dto.getOrderIndex())
                .contentType(dto.getContentType())
                .resourceTypeDiscriminator("NONE") // 设置默认资源类型鉴别器
                .build();
        
        Section savedSection = sectionRepository.save(section);
        
        // 转换为VO并返回
        return SectionVO.fromEntity(savedSection);
    }

    @Override
    @Transactional
    public SectionVO updateSection(Long id, SectionCreateDTO dto) {
        // 获取小节
        Section section = findSectionById(id);
        
        // 验证章节是否存在，且章节ID是否一致
        if (!section.getChapter().getId().equals(dto.getChapterId())) {
            Chapter chapter = chapterRepository.findById(dto.getChapterId())
                    .orElseThrow(() -> new ResourceNotFoundException("章节不存在，ID: " + dto.getChapterId()));
            section.setChapter(chapter);
        }
        
        // 更新小节信息
        section.setTitle(dto.getTitle());
        section.setDescription(dto.getDescription());
        section.setOrderIndex(dto.getOrderIndex());
        section.setContentType(dto.getContentType());
        
        Section updatedSection = sectionRepository.save(section);
        
        // 转换为VO并返回
        return SectionVO.fromEntity(updatedSection);
    }

    @Override
    @Transactional(readOnly = true)
    public SectionVO getSectionById(Long id) {
        log.info("获取小节信息, sectionId: {}", id);
        
        // 验证小节是否存在
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("小节不存在, sectionId: {}", id);
                    return new ResourceNotFoundException("小节不存在，ID: " + id);
                });
        
        log.info("成功获取小节信息: {}", section.getTitle());
        
        // 转换为VO
        return SectionVO.fromEntity(section);
    }
    
    /**
     * 查找小节实体（内部使用）
     */
    private Section findSectionById(Long id) {
        return sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("小节不存在，ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SectionVO> getSectionsByChapter(Long chapterId) {
        // 验证章节是否存在
        chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("章节不存在，ID: " + chapterId));
        
        List<Section> sections = sectionRepository.findByChapter_IdOrderByOrderIndexAsc(chapterId);
        
        // 转换为VO并返回
        return sections.stream()
                .map(SectionVO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SectionVO> getSectionsByCourse(Long courseId) {
        // 验证课程是否存在
        courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在，ID: " + courseId));
        
        List<Section> sections = sectionRepository.findByCourseIdOrderByChapterOrderIndexAndOrderIndexAsc(courseId);
        
        // 转换为VO并返回
        return sections.stream()
                .map(SectionVO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteSection(Long id) {
        // 验证小节是否存在
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("小节不存在，ID: " + id));
        
        // 删除小节
        sectionRepository.delete(section);
    }

    @Override
    @Transactional
    public List<SectionVO> reorderSections(Long chapterId, List<SectionOrderDTO> sectionOrders) {
        // 验证章节是否存在
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("章节不存在，ID: " + chapterId));
        
        // 获取当前所有小节
        List<Section> existingSections = sectionRepository.findByChapter_IdOrderByOrderIndexAsc(chapterId);
        Map<Long, Section> sectionMap = existingSections.stream()
                .collect(Collectors.toMap(Section::getId, Function.identity()));
        
        // 验证所有提供的小节ID是否都属于该章节
        for (SectionOrderDTO orderDTO : sectionOrders) {
            if (!sectionMap.containsKey(orderDTO.getId())) {
                throw new BusinessException(400, "小节不属于该章节，小节ID: " + orderDTO.getId());
            }
        }
        
        // 更新小节顺序
        for (SectionOrderDTO orderDTO : sectionOrders) {
            Section section = sectionMap.get(orderDTO.getId());
            section.setOrderIndex(orderDTO.getOrderIndex());
            sectionRepository.save(section);
        }
        
        // 获取更新后的小节列表，按orderIndex排序
        List<Section> updatedSections = sectionRepository.findByChapter_IdOrderByOrderIndexAsc(chapterId);
        
        // 转换为VO并返回
        return updatedSections.stream()
                .map(SectionVO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SectionVO setMediaResource(Long sectionId, Long mediaId, String resourceType) {
        // 验证小节是否存在
        Section section = findSectionById(sectionId);
        
        // 验证媒体资源是否存在
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new ResourceNotFoundException("媒体资源不存在，ID: " + mediaId));
        
        // 验证资源类型
        if (resourceType == null) {
            throw new BusinessException(400, "资源类型不能为空");
        }
        
        // 如果当前小节已经有题目组，则先移除
        if ("QUESTION_GROUP".equals(section.getResourceTypeDiscriminator())) {
            section.setQuestionGroup(null);
        }
        
        // 设置媒体资源
        section.setMedia(media);
        section.setMediaResourceType(resourceType);
        section.setResourceTypeDiscriminator("MEDIA");
        
        Section updatedSection = sectionRepository.save(section);
        
        // 转换为VO并返回
        return SectionVO.fromEntity(updatedSection);
    }

    @Override
    @Transactional
    public SectionVO removeMediaResource(Long sectionId) {
        // 验证小节是否存在
        Section section = findSectionById(sectionId);
        
        // 检查小节是否有媒体资源
        if (!"MEDIA".equals(section.getResourceTypeDiscriminator())) {
            throw new BusinessException(400, "小节没有关联媒体资源");
        }
        
        // 移除媒体资源
        section.setMedia(null);
        section.setMediaResourceType(null);
        section.setResourceTypeDiscriminator("NONE");
        
        Section updatedSection = sectionRepository.save(section);
        
        // 转换为VO并返回
        return SectionVO.fromEntity(updatedSection);
    }

    @Override
    @Transactional
    public SectionVO setQuestionGroup(Long sectionId, Long questionGroupId, SectionQuestionGroupConfigDTO dto) {
        // 验证小节是否存在
        Section section = findSectionById(sectionId);
        
        // 验证题目组是否存在
        QuestionGroup questionGroup = questionGroupRepository.findById(questionGroupId)
                .orElseThrow(() -> new ResourceNotFoundException("题目组不存在，ID: " + questionGroupId));
        
        // 如果当前小节已经有媒体资源，则先移除
        if ("MEDIA".equals(section.getResourceTypeDiscriminator())) {
            section.setMedia(null);
            section.setMediaResourceType(null);
        }
        
        // 设置题目组和相关配置
        section.setQuestionGroup(questionGroup);
        section.setResourceTypeDiscriminator("QUESTION_GROUP");
        
        // 设置题目组配置
        Boolean randomOrder = dto != null && dto.getRandomOrder() != null ? dto.getRandomOrder() : false;
        Boolean orderByDifficulty = dto != null && dto.getOrderByDifficulty() != null ? dto.getOrderByDifficulty() : false;
        Boolean showAnalysis = dto != null && dto.getShowAnalysis() != null ? dto.getShowAnalysis() : true;
        
        section.setRandomOrder(randomOrder);
        section.setOrderByDifficulty(orderByDifficulty);
        section.setShowAnalysis(showAnalysis);
        
        Section updatedSection = sectionRepository.save(section);
        
        // 转换为VO并返回
        return SectionVO.fromEntity(updatedSection);
    }

    @Override
    @Transactional
    public SectionVO removeQuestionGroup(Long sectionId) {
        // 验证小节是否存在
        Section section = findSectionById(sectionId);
        
        // 检查小节是否有题目组
        if (!"QUESTION_GROUP".equals(section.getResourceTypeDiscriminator())) {
            throw new BusinessException(400, "小节没有关联题目组");
        }
        
        // 移除题目组
        section.setQuestionGroup(null);
        section.setRandomOrder(false);
        section.setOrderByDifficulty(false);
        section.setShowAnalysis(true);
        section.setResourceTypeDiscriminator("NONE");
        
        Section updatedSection = sectionRepository.save(section);
        
        // 转换为VO并返回
        return SectionVO.fromEntity(updatedSection);
    }
} 