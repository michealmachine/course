package com.zhangziqi.online_course_mine.repository;

import com.zhangziqi.online_course_mine.model.entity.Institution;
import com.zhangziqi.online_course_mine.model.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 题目Repository
 */
@Repository
public interface QuestionRepository extends JpaRepository<Question, Long>, JpaSpecificationExecutor<Question> {
    
    /**
     * 根据机构查找题目（分页）
     */
    Page<Question> findByInstitution(Institution institution, Pageable pageable);
    
    /**
     * 根据机构和创建者ID查找题目（分页）
     */
    Page<Question> findByInstitutionAndCreatorId(Institution institution, Long creatorId, Pageable pageable);
    
    /**
     * 根据机构和题目类型查找题目（分页）
     */
    Page<Question> findByInstitutionAndType(Institution institution, Integer type, Pageable pageable);
    
    /**
     * 根据机构ID和题目ID查找题目
     */
    @Query("SELECT q FROM Question q WHERE q.id = :questionId AND q.institution.id = :institutionId")
    Optional<Question> findByIdAndInstitutionId(@Param("questionId") Long questionId, @Param("institutionId") Long institutionId);
    
    /**
     * 根据难度级别查找题目（分页）
     */
    Page<Question> findByInstitutionAndDifficulty(Institution institution, Integer difficulty, Pageable pageable);
    
    /**
     * 根据关键词搜索题目（分页）
     */
    @Query("SELECT q FROM Question q WHERE q.institution = :institution AND (LOWER(q.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(q.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Question> searchByKeyword(@Param("institution") Institution institution, @Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 随机获取指定数量的题目
     */
    @Query(value = "SELECT * FROM questions WHERE institution_id = :institutionId AND type = :type ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<Question> findRandomQuestions(@Param("institutionId") Long institutionId, @Param("type") Integer type, @Param("limit") int limit);
    
    /**
     * 根据标签ID列表查询包含所有这些标签的题目ID
     * 使用COUNT(DISTINCT m.tag_id) = :tagCount确保题目包含所有指定的标签
     */
    @Query(value = "SELECT q.id FROM questions q " +
            "JOIN question_tag_mappings m ON q.id = m.question_id " +
            "WHERE m.tag_id IN :tagIds " +
            "GROUP BY q.id " +
            "HAVING COUNT(DISTINCT m.tag_id) = :tagCount", nativeQuery = true)
    List<Long> findQuestionIdsByTagIds(@Param("tagIds") List<Long> tagIds, @Param("tagCount") Long tagCount);
    
    /**
     * 根据ID列表和机构查询题目（分页）
     */
    Page<Question> findByIdInAndInstitution(List<Long> ids, Institution institution, Pageable pageable);
} 