package com.zhangziqi.online_course_mine.service.impl;

import com.zhangziqi.online_course_mine.exception.ResourceNotFoundException;
import com.zhangziqi.online_course_mine.exception.ServiceException;
import com.zhangziqi.online_course_mine.model.dto.CategoryDTO;
import com.zhangziqi.online_course_mine.model.entity.Category;
import com.zhangziqi.online_course_mine.model.vo.CategoryTreeVO;
import com.zhangziqi.online_course_mine.model.vo.CategoryVO;
import com.zhangziqi.online_course_mine.repository.CategoryRepository;
import com.zhangziqi.online_course_mine.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 分类服务实现类
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public Long createCategory(CategoryDTO categoryDTO) {
        // 校验分类编码唯一性
        if (!isCodeAvailable(categoryDTO.getCode(), null)) {
            throw new ServiceException("分类编码已存在");
        }

        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);

        // 设置父分类
        if (categoryDTO.getParentId() != null) {
            Category parent = categoryRepository.findById(categoryDTO.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("父分类不存在"));
            category.setParent(parent);
            // 设置层级为父分类层级+1
            category.setLevel(parent.getLevel() + 1);
        } else {
            // 根分类层级为1
            category.setLevel(1);
        }

        // 如果未设置排序索引，获取同级分类中最大的排序索引+1
        if (category.getOrderIndex() == null) {
            Integer maxOrderIndex = 0;
            if (categoryDTO.getParentId() != null) {
                List<Category> siblings = categoryRepository.findByParentId(categoryDTO.getParentId());
                maxOrderIndex = siblings.stream()
                        .map(Category::getOrderIndex)
                        .filter(index -> index != null)
                        .max(Integer::compareTo)
                        .orElse(0);
            } else {
                List<Category> rootCategories = categoryRepository.findByParentIsNull();
                maxOrderIndex = rootCategories.stream()
                        .map(Category::getOrderIndex)
                        .filter(index -> index != null)
                        .max(Integer::compareTo)
                        .orElse(0);
            }
            category.setOrderIndex(maxOrderIndex + 1);
        }

        Category savedCategory = categoryRepository.save(category);
        return savedCategory.getId();
    }

    @Override
    @Transactional
    public boolean updateCategory(Long id, CategoryDTO categoryDTO) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("分类不存在"));

        // 校验分类编码唯一性
        if (!isCodeAvailable(categoryDTO.getCode(), id)) {
            throw new ServiceException("分类编码已存在");
        }

        // 检查是否修改了父分类
        Long newParentId = categoryDTO.getParentId();
        Long oldParentId = category.getParent() != null ? category.getParent().getId() : null;
        boolean parentChanged = (newParentId == null && oldParentId != null) ||
                (newParentId != null && !newParentId.equals(oldParentId));

        // 如果修改了父分类，需要检查是否形成循环依赖
        if (parentChanged && newParentId != null) {
            // 不能将自己或子分类设为父分类
            if (id.equals(newParentId) || isChildCategory(id, newParentId)) {
                throw new ServiceException("不能将自己或子分类设为父分类");
            }

            Category parent = categoryRepository.findById(newParentId)
                    .orElseThrow(() -> new ResourceNotFoundException("父分类不存在"));
            category.setParent(parent);
            // 更新层级为父分类层级+1
            category.setLevel(parent.getLevel() + 1);

            // 需要递归更新所有子分类的层级
            updateChildrenLevel(category);
        } else if (parentChanged) {
            // 父分类设为null，即变为根分类
            category.setParent(null);
            category.setLevel(1);

            // 需要递归更新所有子分类的层级
            updateChildrenLevel(category);
        }

        // 复制其他属性
        category.setName(categoryDTO.getName());
        category.setCode(categoryDTO.getCode());
        category.setDescription(categoryDTO.getDescription());
        category.setOrderIndex(categoryDTO.getOrderIndex());
        category.setEnabled(categoryDTO.getEnabled());
        category.setIcon(categoryDTO.getIcon());

        categoryRepository.save(category);
        return true;
    }

    /**
     * 递归更新子分类的层级
     */
    private void updateChildrenLevel(Category parent) {
        List<Category> children = categoryRepository.findByParentId(parent.getId());
        if (!children.isEmpty()) {
            for (Category child : children) {
                child.setLevel(parent.getLevel() + 1);
                categoryRepository.save(child);
                updateChildrenLevel(child);
            }
        }
    }

    /**
     * 检查targetId是否为sourceId的子分类（任意层级）
     */
    private boolean isChildCategory(Long sourceId, Long targetId) {
        List<Category> children = categoryRepository.findByParentId(sourceId);
        if (children.isEmpty()) {
            return false;
        }

        for (Category child : children) {
            if (child.getId().equals(targetId) || isChildCategory(child.getId(), targetId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    @Transactional
    public boolean deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("分类不存在"));

        // 检查是否有子分类
        long childCount = categoryRepository.countByParentId(id);
        if (childCount > 0) {
            throw new ServiceException("存在子分类，无法删除");
        }

        // 检查是否有关联的课程
        long courseCount = categoryRepository.countCoursesByCategoryId(id);
        if (courseCount > 0) {
            throw new ServiceException("分类下存在课程，无法删除");
        }

        categoryRepository.delete(category);
        return true;
    }

    @Override
    public CategoryVO getCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("分类不存在"));
        return convertToCategoryVO(category);
    }

    @Override
    public CategoryVO getCategoryByCode(String code) {
        Category category = categoryRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("分类不存在"));
        return convertToCategoryVO(category);
    }

    @Override
    public Page<CategoryVO> listCategories(String keyword, Pageable pageable) {
        Page<Category> categoryPage;

        if (StringUtils.hasText(keyword)) {
            // 创建动态查询条件
            Specification<Category> spec = (root, query, criteriaBuilder) -> {
                String likePattern = "%" + keyword + "%";
                return criteriaBuilder.or(
                        criteriaBuilder.like(root.get("name"), likePattern),
                        criteriaBuilder.like(root.get("code"), likePattern),
                        criteriaBuilder.like(root.get("description"), likePattern)
                );
            };
            categoryPage = categoryRepository.findAll(spec, pageable);
        } else {
            categoryPage = categoryRepository.findAll(pageable);
        }

        return categoryPage.map(this::convertToCategoryVO);
    }

    @Override
    public List<CategoryVO> listRootCategories() {
        List<Category> rootCategories = categoryRepository.findByParentIsNull();
        return rootCategories.stream()
                .map(this::convertToCategoryVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryVO> listChildCategories(Long parentId) {
        List<Category> children = categoryRepository.findByParentId(parentId);
        return children.stream()
                .map(this::convertToCategoryVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryTreeVO> getCategoryTree() {
        // 获取所有根分类
        List<Category> rootCategories = categoryRepository.findByParentIsNull();
        // 递归构建分类树
        return rootCategories.stream()
                .map(this::buildCategoryTreeVO)
                .collect(Collectors.toList());
    }

    /**
     * 递归构建分类树
     */
    private CategoryTreeVO buildCategoryTreeVO(Category category) {
        CategoryTreeVO treeVO = new CategoryTreeVO();
        BeanUtils.copyProperties(category, treeVO);
        treeVO.setFullPath(category.getFullPath());

        // 获取已发布课程数量
        long courseCount = categoryRepository.countPublishedCoursesByCategoryId(category.getId());
        treeVO.setCourseCount(courseCount);

        // 递归获取子分类
        List<Category> children = categoryRepository.findByParentId(category.getId());
        if (!children.isEmpty()) {
            List<CategoryTreeVO> childrenVOs = children.stream()
                    .map(this::buildCategoryTreeVO)
                    .collect(Collectors.toList());
            treeVO.setChildren(childrenVOs);
        }

        return treeVO;
    }

    @Override
    public boolean isCodeAvailable(String code, Long excludeId) {
        Optional<Category> existingCategory = categoryRepository.findByCode(code);
        return !existingCategory.isPresent() || existingCategory.get().getId().equals(excludeId);
    }

    @Override
    @Transactional
    public boolean updateCategoryStatus(Long id, boolean enabled) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("分类不存在"));
        category.setEnabled(enabled);
        categoryRepository.save(category);
        return true;
    }

    @Override
    @Transactional
    public boolean updateCategoryOrder(Long id, Integer orderIndex) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("分类不存在"));
        category.setOrderIndex(orderIndex);
        categoryRepository.save(category);
        return true;
    }

    @Override
    public Category getCategoryEntity(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("分类不存在"));
    }

    /**
     * 将分类实体转换为VO对象
     */
    private CategoryVO convertToCategoryVO(Category category) {
        CategoryVO vo = new CategoryVO();
        BeanUtils.copyProperties(category, vo);

        // 设置父分类信息
        if (category.getParent() != null) {
            vo.setParentId(category.getParent().getId());
            vo.setParentName(category.getParent().getName());
        }

        // 获取已发布课程数量
        long courseCount = categoryRepository.countPublishedCoursesByCategoryId(category.getId());
        vo.setCourseCount((int) courseCount);

        // 获取子分类数量
        long childrenCount = categoryRepository.countByParentId(category.getId());
        vo.setChildrenCount((int) childrenCount);

        return vo;
    }
}