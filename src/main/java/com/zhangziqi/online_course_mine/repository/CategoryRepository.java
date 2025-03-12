package com.zhangziqi.online_course_mine.repository;

import com.zhangziqi.online_course_mine.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 课程分类Repository
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {
    
    /**
     * 根据编码查找分类
     * 
     * @param code 分类编码
     * @return 分类
     */
    Optional<Category> findByCode(String code);
    
    /**
     * 根据名称查找分类
     * 
     * @param name 分类名称
     * @return 分类
     */
    Optional<Category> findByName(String name);
    
    /**
     * 查找所有根分类（无父分类的分类）
     * 
     * @return 根分类列表
     */
    List<Category> findByParentIsNull();
    
    /**
     * 根据父分类ID查找子分类
     * 
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    List<Category> findByParentId(Long parentId);
    
    /**
     * 根据层级查找分类
     * 
     * @param level 层级
     * @return 分类列表
     */
    List<Category> findByLevel(Integer level);
    
    /**
     * 检查分类下是否有课程
     * 
     * @param categoryId 分类ID
     * @return 课程数量
     */
    @Query("SELECT COUNT(c) FROM Course c WHERE c.category.id = :categoryId")
    long countCoursesByCategoryId(@Param("categoryId") Long categoryId);
    
    /**
     * 根据父分类ID统计子分类数量
     * 
     * @param parentId 父分类ID
     * @return 子分类数量
     */
    long countByParentId(Long parentId);
} 