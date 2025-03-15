package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.model.dto.chapter.*;
import com.zhangziqi.online_course_mine.model.entity.Chapter;

import java.util.List;

/**
 * 章节服务接口
 */
public interface ChapterService {

    /**
     * 创建章节
     *
     * @param dto 章节创建DTO
     * @return 创建的章节
     */
    Chapter createChapter(ChapterCreateDTO dto);

    /**
     * 更新章节
     *
     * @param id 章节ID
     * @param dto 章节更新DTO
     * @return 更新后的章节
     */
    Chapter updateChapter(Long id, ChapterCreateDTO dto);

    /**
     * 获取章节详情
     *
     * @param id 章节ID
     * @return 章节
     */
    Chapter getChapterById(Long id);

    /**
     * 获取课程下的章节列表
     *
     * @param courseId 课程ID
     * @return 章节列表
     */
    List<Chapter> getChaptersByCourse(Long courseId);

    /**
     * 删除章节
     *
     * @param id 章节ID
     */
    void deleteChapter(Long id);

    /**
     * 更新章节访问类型
     *
     * @param id 章节ID
     * @param accessType 访问类型
     * @return 更新后的章节
     */
    Chapter updateAccessType(Long id, Integer accessType);

    /**
     * 调整章节顺序
     *
     * @param courseId 课程ID
     * @param chapterOrders 章节顺序列表，包含ID和顺序
     * @return 更新后的章节列表
     */
    List<Chapter> reorderChapters(Long courseId, List<ChapterOrderDTO> chapterOrders);
} 