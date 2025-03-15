package com.zhangziqi.online_course_mine.service.impl;

import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.exception.ResourceNotFoundException;
import com.zhangziqi.online_course_mine.model.dto.section.*;
import com.zhangziqi.online_course_mine.model.entity.*;
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
    private final SectionResourceRepository sectionResourceRepository;
    private final SectionQuestionGroupRepository sectionQuestionGroupRepository;

    @Override
    @Transactional
    public Section createSection(SectionCreateDTO dto) {
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
                .build();
        
        return sectionRepository.save(section);
    }

    @Override
    @Transactional
    public Section updateSection(Long id, SectionCreateDTO dto) {
        // 获取小节
        Section section = getSectionById(id);
        
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
        
        return sectionRepository.save(section);
    }

    @Override
    @Transactional(readOnly = true)
    public Section getSectionById(Long id) {
        return sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("小节不存在，ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Section> getSectionsByChapter(Long chapterId) {
        // 验证章节是否存在
        chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("章节不存在，ID: " + chapterId));
        
        return sectionRepository.findByChapter_IdOrderByOrderIndexAsc(chapterId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Section> getSectionsByCourse(Long courseId) {
        // 验证课程是否存在
        courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在，ID: " + courseId));
        
        return sectionRepository.findByCourseIdOrderByChapterOrderIndexAndOrderIndexAsc(courseId);
    }

    @Override
    @Transactional
    public void deleteSection(Long id) {
        // 获取小节
        Section section = getSectionById(id);
        
        // 删除关联的资源
        sectionResourceRepository.deleteBySection_Id(section.getId());
        
        // 删除关联的题目组
        sectionQuestionGroupRepository.deleteBySectionId(section.getId());
        
        // 删除小节
        sectionRepository.delete(section);
    }

    @Override
    @Transactional
    public List<Section> reorderSections(Long chapterId, List<SectionOrderDTO> sectionOrders) {
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
        return sectionRepository.findByChapter_IdOrderByOrderIndexAsc(chapterId);
    }

    @Override
    @Transactional
    public SectionResource addSectionResource(SectionResourceDTO dto) {
        // 验证小节是否存在
        Section section = sectionRepository.findById(dto.getSectionId())
                .orElseThrow(() -> new ResourceNotFoundException("小节不存在，ID: " + dto.getSectionId()));
        
        // 验证媒体资源是否存在
        Media media = mediaRepository.findById(dto.getMediaId())
                .orElseThrow(() -> new ResourceNotFoundException("媒体资源不存在，ID: " + dto.getMediaId()));
        
        // 验证资源类型
        if (dto.getResourceType() == null) {
            throw new BusinessException(400, "资源类型不能为空");
        }
        
        // 如果没有指定排序索引，则放在最后
        if (dto.getOrderIndex() == null) {
            Integer maxOrderIndex = getMaxOrderIndexForSectionResource(dto.getSectionId());
            dto.setOrderIndex(maxOrderIndex != null ? maxOrderIndex + 1 : 0);
        }
        
        // 创建小节资源
        SectionResource sectionResource = SectionResource.builder()
                .section(section)
                .media(media)
                .resourceType(dto.getResourceType())
                .orderIndex(dto.getOrderIndex())
                .build();
        
        return sectionResourceRepository.save(sectionResource);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SectionResource> getSectionResources(Long sectionId) {
        // 验证小节是否存在
        sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("小节不存在，ID: " + sectionId));
        
        // 尝试查找按顺序排序的资源
        List<SectionResource> resources = sectionResourceRepository.findBySection_Id(sectionId);
        
        // 手动排序，如果repository不提供排序方法
        resources.sort((a, b) -> Integer.compare(a.getOrderIndex(), b.getOrderIndex()));
        
        return resources;
    }

    @Override
    @Transactional
    public void deleteSectionResource(Long resourceId) {
        // 验证小节资源是否存在
        SectionResource resource = sectionResourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("小节资源不存在，ID: " + resourceId));
        
        // 删除小节资源
        sectionResourceRepository.delete(resource);
    }

    @Override
    @Transactional
    public SectionQuestionGroup addSectionQuestionGroup(SectionQuestionGroupDTO dto) {
        // 验证小节是否存在
        Section section = sectionRepository.findById(dto.getSectionId())
                .orElseThrow(() -> new ResourceNotFoundException("小节不存在，ID: " + dto.getSectionId()));
        
        // 验证题目组是否存在
        QuestionGroup questionGroup = questionGroupRepository.findById(dto.getQuestionGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("题目组不存在，ID: " + dto.getQuestionGroupId()));
        
        // 如果没有指定排序索引，则放在最后
        if (dto.getOrderIndex() == null) {
            Integer maxOrderIndex = getMaxOrderIndexForSectionQuestionGroup(dto.getSectionId());
            dto.setOrderIndex(maxOrderIndex != null ? maxOrderIndex + 1 : 0);
        }
        
        // 设置默认值
        Boolean randomOrder = dto.getRandomOrder() != null ? dto.getRandomOrder() : false;
        Boolean orderByDifficulty = dto.getOrderByDifficulty() != null ? dto.getOrderByDifficulty() : false;
        Boolean showAnalysis = dto.getShowAnalysis() != null ? dto.getShowAnalysis() : true;
        
        // 创建小节题目组
        SectionQuestionGroup sectionQuestionGroup = SectionQuestionGroup.builder()
                .questionGroup(questionGroup)
                .sectionId(section.getId()) // 显式设置sectionId
                .orderIndex(dto.getOrderIndex())
                .randomOrder(randomOrder)
                .orderByDifficulty(orderByDifficulty)
                .showAnalysis(showAnalysis)
                .build();
        
        // 关联Section (需要检查是否需要在保存前设置)
        // sectionQuestionGroup.setSection(section);  
        
        return sectionQuestionGroupRepository.save(sectionQuestionGroup);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SectionQuestionGroup> getSectionQuestionGroups(Long sectionId) {
        // 验证小节是否存在
        sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("小节不存在，ID: " + sectionId));
        
        return sectionQuestionGroupRepository.findBySectionIdOrderByOrderIndexAsc(sectionId);
    }

    @Override
    @Transactional
    public SectionQuestionGroup updateSectionQuestionGroup(Long sectionId, Long questionGroupId, SectionQuestionGroupDTO dto) {
        // 验证小节题目组是否存在
        SectionQuestionGroup sectionQuestionGroup = sectionQuestionGroupRepository
                .findBySectionIdAndQuestionGroupId(sectionId, questionGroupId)
                .orElseThrow(() -> new ResourceNotFoundException("小节题目组不存在，小节ID: " + sectionId + "，题目组ID: " + questionGroupId));
        
        // 更新小节题目组
        if (dto.getOrderIndex() != null) {
            sectionQuestionGroup.setOrderIndex(dto.getOrderIndex());
        }
        
        if (dto.getRandomOrder() != null) {
            sectionQuestionGroup.setRandomOrder(dto.getRandomOrder());
        }
        
        if (dto.getOrderByDifficulty() != null) {
            sectionQuestionGroup.setOrderByDifficulty(dto.getOrderByDifficulty());
        }
        
        if (dto.getShowAnalysis() != null) {
            sectionQuestionGroup.setShowAnalysis(dto.getShowAnalysis());
        }
        
        return sectionQuestionGroupRepository.save(sectionQuestionGroup);
    }

    @Override
    @Transactional
    public void deleteSectionQuestionGroup(Long sectionId, Long questionGroupId) {
        // 验证小节题目组是否存在
        SectionQuestionGroup sectionQuestionGroup = sectionQuestionGroupRepository
                .findBySectionIdAndQuestionGroupId(sectionId, questionGroupId)
                .orElseThrow(() -> new ResourceNotFoundException("小节题目组不存在，小节ID: " + sectionId + "，题目组ID: " + questionGroupId));
        
        // 删除小节题目组
        sectionQuestionGroupRepository.delete(sectionQuestionGroup);
    }
    
    /**
     * 获取小节资源的最大排序索引
     * 
     * @param sectionId 小节ID
     * @return 最大排序索引
     */
    private Integer getMaxOrderIndexForSectionResource(Long sectionId) {
        List<SectionResource> resources = sectionResourceRepository.findBySection_Id(sectionId);
        if (resources.isEmpty()) {
            return null;
        }
        return resources.stream()
                .mapToInt(SectionResource::getOrderIndex)
                .max()
                .orElse(0);
    }
    
    /**
     * 获取小节题目组的最大排序索引
     * 
     * @param sectionId 小节ID
     * @return 最大排序索引
     */
    private Integer getMaxOrderIndexForSectionQuestionGroup(Long sectionId) {
        List<SectionQuestionGroup> questionGroups = sectionQuestionGroupRepository.findBySectionIdOrderByOrderIndexAsc(sectionId);
        if (questionGroups.isEmpty()) {
            return null;
        }
        return questionGroups.stream()
                .mapToInt(SectionQuestionGroup::getOrderIndex)
                .max()
                .orElse(0);
    }
} 