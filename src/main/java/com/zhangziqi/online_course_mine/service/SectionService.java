package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.model.dto.section.*;
import com.zhangziqi.online_course_mine.model.entity.Section;
import com.zhangziqi.online_course_mine.model.entity.SectionResource;
import com.zhangziqi.online_course_mine.model.entity.SectionQuestionGroup;

import java.util.List;

/**
 * 小节服务接口
 */
public interface SectionService {

    /**
     * 创建小节
     *
     * @param dto 小节创建DTO
     * @return 创建的小节
     */
    Section createSection(SectionCreateDTO dto);

    /**
     * 更新小节
     *
     * @param id 小节ID
     * @param dto 小节更新DTO
     * @return 更新后的小节
     */
    Section updateSection(Long id, SectionCreateDTO dto);

    /**
     * 获取小节详情
     *
     * @param id 小节ID
     * @return 小节
     */
    Section getSectionById(Long id);

    /**
     * 获取章节下的小节列表
     *
     * @param chapterId 章节ID
     * @return 小节列表
     */
    List<Section> getSectionsByChapter(Long chapterId);

    /**
     * 获取课程下的所有小节
     *
     * @param courseId 课程ID
     * @return 小节列表
     */
    List<Section> getSectionsByCourse(Long courseId);

    /**
     * 删除小节
     *
     * @param id 小节ID
     */
    void deleteSection(Long id);

    /**
     * 调整小节顺序
     *
     * @param chapterId 章节ID
     * @param sectionOrders 小节顺序列表，包含ID和顺序
     * @return 更新后的小节列表
     */
    List<Section> reorderSections(Long chapterId, List<SectionOrderDTO> sectionOrders);

    /**
     * 添加小节资源
     *
     * @param dto 小节资源DTO
     * @return 创建的小节资源
     */
    SectionResource addSectionResource(SectionResourceDTO dto);

    /**
     * 获取小节资源列表
     *
     * @param sectionId 小节ID
     * @return 小节资源列表
     */
    List<SectionResource> getSectionResources(Long sectionId);

    /**
     * 删除小节资源
     *
     * @param resourceId 资源ID
     */
    void deleteSectionResource(Long resourceId);

    /**
     * 添加小节题目组
     *
     * @param dto 小节题目组DTO
     * @return 创建的小节题目组
     */
    SectionQuestionGroup addSectionQuestionGroup(SectionQuestionGroupDTO dto);

    /**
     * 获取小节题目组列表
     *
     * @param sectionId 小节ID
     * @return 小节题目组列表
     */
    List<SectionQuestionGroup> getSectionQuestionGroups(Long sectionId);

    /**
     * 更新小节题目组设置
     *
     * @param sectionId 小节ID
     * @param questionGroupId 题目组ID
     * @param dto 小节题目组DTO
     * @return 更新后的小节题目组
     */
    SectionQuestionGroup updateSectionQuestionGroup(Long sectionId, Long questionGroupId, SectionQuestionGroupDTO dto);

    /**
     * 删除小节题目组
     *
     * @param sectionId 小节ID
     * @param questionGroupId 题目组ID
     */
    void deleteSectionQuestionGroup(Long sectionId, Long questionGroupId);
} 