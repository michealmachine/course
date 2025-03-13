package com.zhangziqi.online_course_mine.repository;

import com.zhangziqi.online_course_mine.model.entity.Institution;
import com.zhangziqi.online_course_mine.model.entity.QuestionTag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 题目标签数据访问层
 */
@Repository
public interface QuestionTagRepository extends JpaRepository<QuestionTag, Long> {

    /**
     * 根据机构和标签名称查询标签
     */
    Optional<QuestionTag> findByInstitutionAndName(Institution institution, String name);

    /**
     * 根据ID和机构ID查询标签
     */
    @Query("SELECT t FROM QuestionTag t WHERE t.id = :id AND t.institution.id = :institutionId")
    Optional<QuestionTag> findByIdAndInstitutionId(@Param("id") Long id, @Param("institutionId") Long institutionId);

    /**
     * 根据机构查询所有标签，支持分页
     */
    Page<QuestionTag> findByInstitution(Institution institution, Pageable pageable);

    /**
     * 根据机构ID查询所有标签
     */
    @Query("SELECT t FROM QuestionTag t WHERE t.institution.id = :institutionId ORDER BY t.name ASC")
    List<QuestionTag> findAllByInstitutionId(@Param("institutionId") Long institutionId);

    /**
     * 关键词搜索标签
     */
    @Query("SELECT t FROM QuestionTag t WHERE t.institution = :institution AND LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<QuestionTag> searchByKeyword(@Param("institution") Institution institution, @Param("keyword") String keyword, Pageable pageable);

    /**
     * 根据题目ID查询关联的标签
     */
    @Query("SELECT t FROM QuestionTag t JOIN QuestionTagMapping m ON t.id = m.tag.id WHERE m.question.id = :questionId ORDER BY t.name ASC")
    List<QuestionTag> findByQuestionId(@Param("questionId") Long questionId);
} 