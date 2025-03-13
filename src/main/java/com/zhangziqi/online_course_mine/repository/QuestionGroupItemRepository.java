package com.zhangziqi.online_course_mine.repository;

import com.zhangziqi.online_course_mine.model.entity.QuestionGroup;
import com.zhangziqi.online_course_mine.model.entity.QuestionGroupItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 题目组项仓库接口
 */
@Repository
public interface QuestionGroupItemRepository extends JpaRepository<QuestionGroupItem, Long> {
    
    /**
     * 根据题目组查询所有项目，按顺序排序
     */
    List<QuestionGroupItem> findByGroupOrderByOrderIndexAsc(QuestionGroup group);
    
    /**
     * 根据题目组ID查询所有项目，按顺序排序
     */
    @Query("SELECT i FROM QuestionGroupItem i WHERE i.group.id = :groupId ORDER BY i.orderIndex ASC")
    List<QuestionGroupItem> findByGroupIdOrderByOrderIndexAsc(@Param("groupId") Long groupId);
    
    /**
     * 根据题目组ID和题目ID查询项目
     */
    @Query("SELECT i FROM QuestionGroupItem i WHERE i.group.id = :groupId AND i.question.id = :questionId")
    Optional<QuestionGroupItem> findByGroupIdAndQuestionId(@Param("groupId") Long groupId, @Param("questionId") Long questionId);
    
    /**
     * 删除题目组的所有项目
     */
    @Modifying
    @Query("DELETE FROM QuestionGroupItem i WHERE i.group.id = :groupId")
    void deleteByGroupId(@Param("groupId") Long groupId);
    
    /**
     * 删除题目组中的指定题目
     */
    @Modifying
    @Query("DELETE FROM QuestionGroupItem i WHERE i.group.id = :groupId AND i.question.id = :questionId")
    void deleteByGroupIdAndQuestionId(@Param("groupId") Long groupId, @Param("questionId") Long questionId);
    
    /**
     * 统计题目组中的题目数量
     */
    @Query("SELECT COUNT(i) FROM QuestionGroupItem i WHERE i.group.id = :groupId")
    long countByGroupId(@Param("groupId") Long groupId);
    
    /**
     * 查询题目所在的所有题目组
     */
    @Query("SELECT i FROM QuestionGroupItem i WHERE i.question.id = :questionId")
    List<QuestionGroupItem> findByQuestionId(@Param("questionId") Long questionId);

    /**
     * 根据题目组ID查询所有题目项
     */
    @Query("SELECT gi FROM QuestionGroupItem gi WHERE gi.group.id = :groupId")
    List<QuestionGroupItem> findByGroupId(@Param("groupId") Long groupId);

    /**
     * 根据题目组ID查询所有题目项，按排序索引升序排列
     */
    @Query("SELECT gi FROM QuestionGroupItem gi WHERE gi.group.id = :groupId ORDER BY gi.orderIndex ASC")
    List<QuestionGroupItem> findByGroupIdOrderByOrderIndex(@Param("groupId") Long groupId);

    /**
     * 验证题目是否已存在于题目组中
     */
    @Query("SELECT COUNT(gi) > 0 FROM QuestionGroupItem gi WHERE gi.group.id = :groupId AND gi.question.id = :questionId")
    boolean existsByGroupIdAndQuestionId(@Param("groupId") Long groupId, @Param("questionId") Long questionId);

    /**
     * 查询题目被哪些题目组引用
     */
    @Query("SELECT gi.group.id FROM QuestionGroupItem gi WHERE gi.question.id = :questionId")
    List<Long> findGroupIdsByQuestionId(@Param("questionId") Long questionId);

    /**
     * 统计题目被引用的次数
     */
    @Query("SELECT COUNT(gi) FROM QuestionGroupItem gi WHERE gi.question.id = :questionId")
    long countByQuestionId(@Param("questionId") Long questionId);
} 