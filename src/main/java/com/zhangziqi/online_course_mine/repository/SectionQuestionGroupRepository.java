package com.zhangziqi.online_course_mine.repository;

import com.zhangziqi.online_course_mine.model.entity.SectionQuestionGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 章节与题目组关联数据访问层
 */
@Repository
public interface SectionQuestionGroupRepository extends JpaRepository<SectionQuestionGroup, Long> {

    /**
     * 根据章节ID获取所有题目组ID
     */
    @Query("SELECT sg.questionGroup.id FROM SectionQuestionGroup sg WHERE sg.sectionId = :sectionId")
    List<Long> findGroupIdsBySectionId(@Param("sectionId") Long sectionId);

    /**
     * 根据题目组ID获取所有章节ID
     */
    @Query("SELECT sg.sectionId FROM SectionQuestionGroup sg WHERE sg.questionGroup.id = :groupId")
    List<Long> findSectionIdsByGroupId(@Param("groupId") Long groupId);

    /**
     * 删除题目组的所有关联
     */
    @Modifying
    @Query("DELETE FROM SectionQuestionGroup sg WHERE sg.questionGroup.id = :groupId")
    void deleteByGroupId(@Param("groupId") Long groupId);

    /**
     * 删除章节的所有关联
     */
    @Modifying
    @Query("DELETE FROM SectionQuestionGroup sg WHERE sg.sectionId = :sectionId")
    void deleteBySectionId(@Param("sectionId") Long sectionId);

    /**
     * 删除特定的章节和题目组关联
     */
    @Modifying
    @Query("DELETE FROM SectionQuestionGroup sg WHERE sg.questionGroup.id = :groupId AND sg.sectionId = :sectionId")
    void deleteByGroupIdAndSectionId(@Param("groupId") Long groupId, @Param("sectionId") Long sectionId);

    /**
     * 检查章节和题目组关联是否存在
     */
    @Query("SELECT COUNT(sg) > 0 FROM SectionQuestionGroup sg WHERE sg.questionGroup.id = :groupId AND sg.sectionId = :sectionId")
    boolean existsByGroupIdAndSectionId(@Param("groupId") Long groupId, @Param("sectionId") Long sectionId);

    /**
     * 根据小节ID查询所有关联的题目组，按顺序排序
     */
    @Query("SELECT sg FROM SectionQuestionGroup sg WHERE sg.sectionId = :sectionId ORDER BY sg.orderIndex ASC")
    List<SectionQuestionGroup> findBySectionIdOrderByOrderIndexAsc(@Param("sectionId") Long sectionId);
    
    /**
     * 根据小节ID和题目组ID查询关联
     */
    @Query("SELECT sg FROM SectionQuestionGroup sg WHERE sg.sectionId = :sectionId AND sg.questionGroup.id = :groupId")
    Optional<SectionQuestionGroup> findBySectionIdAndQuestionGroupId(@Param("sectionId") Long sectionId, @Param("groupId") Long groupId);
    
    /**
     * 统计题目组关联的小节数量
     */
    @Query("SELECT COUNT(sg) FROM SectionQuestionGroup sg WHERE sg.questionGroup.id = :groupId")
    long countSectionsByQuestionGroupId(@Param("groupId") Long groupId);
    
    /**
     * 根据题目组ID查询关联的所有小节
     */
    @Query("SELECT sg FROM SectionQuestionGroup sg WHERE sg.questionGroup.id = :groupId")
    List<SectionQuestionGroup> findByQuestionGroupId(@Param("groupId") Long groupId);
} 