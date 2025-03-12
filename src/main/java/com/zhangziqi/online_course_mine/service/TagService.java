package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.model.dto.TagDTO;
import com.zhangziqi.online_course_mine.model.entity.Tag;
import com.zhangziqi.online_course_mine.model.vo.TagVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

/**
 * 标签服务接口
 */
public interface TagService {
    
    /**
     * 创建标签
     * 
     * @param tagDTO 标签信息
     * @return 创建的标签ID
     */
    Long createTag(TagDTO tagDTO);
    
    /**
     * 更新标签
     * 
     * @param id 标签ID
     * @param tagDTO 标签信息
     * @return 是否更新成功
     */
    boolean updateTag(Long id, TagDTO tagDTO);
    
    /**
     * 删除标签
     * 
     * @param id 标签ID
     * @return 是否删除成功
     */
    boolean deleteTag(Long id);
    
    /**
     * 获取标签详情
     * 
     * @param id 标签ID
     * @return 标签详情
     */
    TagVO getTag(Long id);
    
    /**
     * 根据名称获取标签
     * 
     * @param name 标签名称
     * @return 标签详情
     */
    TagVO getTagByName(String name);
    
    /**
     * 分页查询标签
     * 
     * @param keyword 关键词
     * @param pageable 分页参数
     * @return 标签分页结果
     */
    Page<TagVO> listTags(String keyword, Pageable pageable);
    
    /**
     * 获取热门标签
     * 
     * @param limit 数量限制
     * @return 热门标签列表
     */
    List<TagVO> getPopularTags(int limit);
    
    /**
     * 根据名称列表获取或创建标签
     * 
     * @param tagNames 标签名称列表
     * @return 标签列表
     */
    Set<Tag> getOrCreateTags(List<String> tagNames);
    
    /**
     * 校验标签名称是否可用
     * 
     * @param name 标签名称
     * @param excludeId 排除的ID（更新时使用）
     * @return 名称是否可用
     */
    boolean isNameAvailable(String name, Long excludeId);
    
    /**
     * 增加标签使用次数
     * 
     * @param tagId 标签ID
     */
    void incrementTagUseCount(Long tagId);
    
    /**
     * 减少标签使用次数
     * 
     * @param tagId 标签ID
     */
    void decrementTagUseCount(Long tagId);
    
    /**
     * 获取标签实体
     * 
     * @param id 标签ID
     * @return 标签实体
     */
    Tag getTagEntity(Long id);
} 