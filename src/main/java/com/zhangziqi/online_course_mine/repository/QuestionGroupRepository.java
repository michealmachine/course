package com.zhangziqi.online_course_mine.repository;

import com.zhangziqi.online_course_mine.model.entity.Institution;
import com.zhangziqi.online_course_mine.model.entity.QuestionGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 题目组数据访问层
 */
@Repository
public interface QuestionGroupRepository extends JpaRepository<QuestionGroup, Long> {

    /**
     * 根据机构查询所有题目组，支持分页
     */
    Page<QuestionGroup> findByInstitution(Institution institution, Pageable pageable);

    /**
     * 根据ID和机构ID查询题目组
     */
    @Query("SELECT g FROM QuestionGroup g WHERE g.id = :id AND g.institution.id = :institutionId")
    Optional<QuestionGroup> findByIdAndInstitutionId(@Param("id") Long id, @Param("institutionId") Long institutionId);

    /**
     * 根据机构ID查询所有题目组
     */
    @Query("SELECT g FROM QuestionGroup g WHERE g.institution.id = :institutionId ORDER BY g.name ASC")
    List<QuestionGroup> findAllByInstitutionId(@Param("institutionId") Long institutionId);

    /**
     * 关键词搜索题目组
     */
    @Query("SELECT g FROM QuestionGroup g WHERE g.institution = :institution AND (LOWER(g.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(g.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<QuestionGroup> searchByKeyword(@Param("institution") Institution institution, @Param("keyword") String keyword, Pageable pageable);

    /**
     * 根据ID列表和机构ID查询题目组
     */
    @Query("SELECT g FROM QuestionGroup g WHERE g.id IN :ids AND g.institution.id = :institutionId ORDER BY g.name ASC")
    List<QuestionGroup> findByIdInAndInstitutionId(@Param("ids") List<Long> ids, @Param("institutionId") Long institutionId);

    /**
     * 批量查询题目组的题目数量
     */
    @Query("SELECT g.id, COUNT(gi.id) FROM QuestionGroup g LEFT JOIN QuestionGroupItem gi ON g.id = gi.group.id WHERE g.id IN :groupIds GROUP BY g.id")
    List<Object[]> countQuestionsByGroupIds(@Param("groupIds") List<Long> groupIds);
} 