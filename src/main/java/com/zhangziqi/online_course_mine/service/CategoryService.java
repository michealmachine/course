package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.model.dto.CategoryDTO;
import com.zhangziqi.online_course_mine.model.entity.Category;
import com.zhangziqi.online_course_mine.model.vo.CategoryTreeVO;
import com.zhangziqi.online_course_mine.model.vo.CategoryVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 分类服务接口
 */
public interface CategoryService {
    
    /**
     * 创建分类
     * 
     * @param categoryDTO 分类信息
     * @return 创建的分类ID
     */
    Long createCategory(CategoryDTO categoryDTO);
    
    /**
     * 更新分类
     * 
     * @param id 分类ID
     * @param categoryDTO 分类信息
     * @return 是否更新成功
     */
    boolean updateCategory(Long id, CategoryDTO categoryDTO);
    
    /**
     * 删除分类
     * 
     * @param id 分类ID
     * @return 是否删除成功
     */
    boolean deleteCategory(Long id);
    
    /**
     * 获取分类详情
     * 
     * @param id 分类ID
     * @return 分类详情
     */
    CategoryVO getCategory(Long id);
    
    /**
     * 根据编码获取分类
     * 
     * @param code 分类编码
     * @return 分类详情
     */
    CategoryVO getCategoryByCode(String code);
    
    /**
     * 分页查询分类
     * 
     * @param keyword 关键词
     * @param pageable 分页参数
     * @return 分类分页结果
     */
    Page<CategoryVO> listCategories(String keyword, Pageable pageable);
    
    /**
     * 获取所有根分类
     * 
     * @return 根分类列表
     */
    List<CategoryVO> listRootCategories();
    
    /**
     * 获取子分类
     * 
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    List<CategoryVO> listChildCategories(Long parentId);
    
    /**
     * 获取分类树
     * 
     * @return 分类树
     */
    List<CategoryTreeVO> getCategoryTree();
    
    /**
     * 校验分类编码是否可用
     * 
     * @param code 分类编码
     * @param excludeId 排除的ID（更新时使用）
     * @return 编码是否可用
     */
    boolean isCodeAvailable(String code, Long excludeId);
    
    /**
     * 启用或禁用分类
     * 
     * @param id 分类ID
     * @param enabled 是否启用
     * @return 是否操作成功
     */
    boolean updateCategoryStatus(Long id, boolean enabled);
    
    /**
     * 更新分类排序
     * 
     * @param id 分类ID
     * @param orderIndex 排序索引
     * @return 是否操作成功
     */
    boolean updateCategoryOrder(Long id, Integer orderIndex);
    
    /**
     * 获取分类实体
     * 
     * @param id 分类ID
     * @return 分类实体
     */
    Category getCategoryEntity(Long id);
} 