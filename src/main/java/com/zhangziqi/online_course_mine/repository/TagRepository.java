package com.zhangziqi.online_course_mine.repository;

import com.zhangziqi.online_course_mine.model.entity.Tag;
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
 * 标签Repository
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, Long>, JpaSpecificationExecutor<Tag> {

    /**
     * 根据名称查找标签
     *
     * @param name 标签名称
     * @return 标签
     */
    Optional<Tag> findByName(String name);

    /**
     * 检查标签名称是否存在
     *
     * @param name 标签名称
     * @return 是否存在
     */
    boolean existsByName(String name);

    /**
     * 根据名称模糊查询标签
     *
     * @param keyword 关键词
     * @return 标签列表
     */
    List<Tag> findByNameContaining(String keyword);

    /**
     * 根据使用次数排序查找热门标签
     *
     * @param pageable 分页参数
     * @return 标签分页
     */
    Page<Tag> findByOrderByUseCountDesc(Pageable pageable);

    /**
     * 查询最热门的标签
     *
     * @param pageable 分页参数
     * @return 标签列表
     */
    List<Tag> findTopByOrderByUseCountDesc(Pageable pageable);

    /**
     * 统计标签关联的已发布课程数量
     *
     * @param tagId 标签ID
     * @return 已发布课程数量
     */
    @Query("SELECT COUNT(c) FROM Course c JOIN c.tags t WHERE t.id = :tagId AND c.status = 4 AND c.isPublishedVersion = true")
    long countPublishedCoursesByTagId(@Param("tagId") Long tagId);

    /**
     * 查询使用次数大于指定值的标签
     *
     * @param count 使用次数
     * @return 标签列表
     */
    List<Tag> findByUseCountGreaterThan(Integer count);
}