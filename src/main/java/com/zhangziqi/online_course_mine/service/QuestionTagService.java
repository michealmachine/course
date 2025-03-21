package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.model.dto.QuestionTagDTO;
import com.zhangziqi.online_course_mine.model.vo.QuestionTagVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 题目标签服务接口
 */
public interface QuestionTagService {
    
    /**
     * 创建题目标签
     *
     * @param tagDTO 标签数据
     * @param creatorId 创建者ID
     * @return 创建后的标签信息
     */
    QuestionTagVO createTag(QuestionTagDTO tagDTO, Long creatorId);
    
    /**
     * 更新题目标签
     *
     * @param tagDTO 标签更新数据
     * @return 更新后的标签信息
     */
    QuestionTagVO updateTag(QuestionTagDTO tagDTO);
    
    /**
     * 根据ID查询标签详情
     *
     * @param tagId 标签ID
     * @param institutionId 机构ID
     * @return 标签详情
     */
    QuestionTagVO getTagById(Long tagId, Long institutionId);
    
    /**
     * 删除标签
     *
     * @param tagId 标签ID
     * @param institutionId 机构ID
     */
    void deleteTag(Long tagId, Long institutionId);
    
    /**
     * 分页查询标签列表
     *
     * @param institutionId 机构ID
     * @param keyword 关键词（可选）
     * @param pageable 分页参数
     * @return 分页标签列表
     */
    Page<QuestionTagVO> getTags(Long institutionId, String keyword, Pageable pageable);
    
    /**
     * 获取机构的所有标签
     *
     * @param institutionId 机构ID
     * @return 标签列表
     */
    List<QuestionTagVO> getAllTags(Long institutionId);
    
    /**
     * 根据题目ID获取相关标签
     *
     * @param questionId 题目ID
     * @return 标签列表
     */
    List<QuestionTagVO> getTagsByQuestionId(Long questionId);
    
    /**
     * 为题目添加标签
     *
     * @param questionId 题目ID
     * @param tagId 标签ID
     * @param institutionId 机构ID
     * @return 是否添加成功
     */
    boolean addTagToQuestion(Long questionId, Long tagId, Long institutionId);
    
    /**
     * 移除题目标签
     *
     * @param questionId 题目ID
     * @param tagId 标签ID
     * @param institutionId 机构ID
     * @return 是否移除成功
     */
    boolean removeTagFromQuestion(Long questionId, Long tagId, Long institutionId);
    
    /**
     * 根据名称获取标签
     *
     * @param institutionId 机构ID
     * @param name 标签名称
     * @return 标签视图对象
     */
    QuestionTagVO getTagByName(Long institutionId, String name);
    
    /**
     * 删除题目的所有标签映射
     *
     * @param questionId 题目ID
     */
    void deleteAllTagsByQuestionId(Long questionId);
} 