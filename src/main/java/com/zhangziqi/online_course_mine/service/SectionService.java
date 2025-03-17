package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.model.dto.section.*;
import com.zhangziqi.online_course_mine.model.vo.SectionVO;

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
    SectionVO createSection(SectionCreateDTO dto);

    /**
     * 更新小节
     *
     * @param id 小节ID
     * @param dto 小节更新DTO
     * @return 更新后的小节
     */
    SectionVO updateSection(Long id, SectionCreateDTO dto);

    /**
     * 获取小节详情
     *
     * @param id 小节ID
     * @return 小节
     */
    SectionVO getSectionById(Long id);

    /**
     * 获取章节下的小节列表
     *
     * @param chapterId 章节ID
     * @return 小节列表
     */
    List<SectionVO> getSectionsByChapter(Long chapterId);

    /**
     * 获取课程下的所有小节
     *
     * @param courseId 课程ID
     * @return 小节列表
     */
    List<SectionVO> getSectionsByCourse(Long courseId);

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
    List<SectionVO> reorderSections(Long chapterId, List<SectionOrderDTO> sectionOrders);

    /**
     * 设置小节媒体资源（直接关联）
     *
     * @param sectionId 小节ID
     * @param mediaId 媒体资源ID
     * @param resourceType 资源类型
     * @return 更新后的小节
     */
    SectionVO setMediaResource(Long sectionId, Long mediaId, String resourceType);

    /**
     * 移除小节媒体资源（直接关联）
     *
     * @param sectionId 小节ID
     * @return 更新后的小节
     */
    SectionVO removeMediaResource(Long sectionId);

    /**
     * 设置小节题目组（直接关联）
     *
     * @param sectionId 小节ID
     * @param questionGroupId 题目组ID
     * @param dto 题目组配置DTO
     * @return 更新后的小节
     */
    SectionVO setQuestionGroup(Long sectionId, Long questionGroupId, SectionQuestionGroupConfigDTO dto);

    /**
     * 移除小节题目组（直接关联）
     *
     * @param sectionId 小节ID
     * @return 更新后的小节
     */
    SectionVO removeQuestionGroup(Long sectionId);

} 