package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.model.dto.QuestionGroupDTO;
import com.zhangziqi.online_course_mine.model.dto.QuestionGroupItemDTO;
import com.zhangziqi.online_course_mine.model.vo.QuestionGroupItemVO;
import com.zhangziqi.online_course_mine.model.vo.QuestionGroupVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 题目组服务接口
 */
public interface QuestionGroupService {
    
    /**
     * 创建题目组
     *
     * @param groupDTO 题目组数据
     * @param creatorId 创建者ID
     * @return 创建后的题目组信息
     */
    QuestionGroupVO createGroup(QuestionGroupDTO groupDTO, Long creatorId);
    
    /**
     * 更新题目组
     *
     * @param groupDTO 题目组更新数据
     * @return 更新后的题目组信息
     */
    QuestionGroupVO updateGroup(QuestionGroupDTO groupDTO);
    
    /**
     * 根据ID查询题目组详情
     *
     * @param groupId 题目组ID
     * @param institutionId 机构ID
     * @param includeItems 是否包含题目项
     * @return 题目组详情
     */
    QuestionGroupVO getGroupById(Long groupId, Long institutionId, boolean includeItems);
    
    /**
     * 删除题目组
     *
     * @param groupId 题目组ID
     * @param institutionId 机构ID
     */
    void deleteGroup(Long groupId, Long institutionId);
    
    /**
     * 分页查询题目组列表
     *
     * @param institutionId 机构ID
     * @param keyword 关键词（可选）
     * @param pageable 分页参数
     * @return 分页题目组列表
     */
    Page<QuestionGroupVO> getGroups(Long institutionId, String keyword, Pageable pageable);
    
    /**
     * 获取机构的所有题目组
     *
     * @param institutionId 机构ID
     * @return 题目组列表
     */
    List<QuestionGroupVO> getAllGroups(Long institutionId);
    
    /**
     * 添加题目到题目组
     *
     * @param itemDTO 题目组项数据
     * @return 添加后的题目组项信息
     */
    QuestionGroupItemVO addQuestionToGroup(QuestionGroupItemDTO itemDTO);
    
    /**
     * 更新题目组项
     *
     * @param itemDTO 题目组项更新数据
     * @return 更新后的题目组项信息
     */
    QuestionGroupItemVO updateGroupItem(QuestionGroupItemDTO itemDTO);
    
    /**
     * 从题目组中移除题目
     *
     * @param groupId 题目组ID
     * @param itemId 题目项ID
     * @param institutionId 机构ID
     * @return 是否移除成功
     */
    boolean removeQuestionFromGroup(Long groupId, Long itemId, Long institutionId);
    
    /**
     * 获取题目组中的所有题目
     *
     * @param groupId 题目组ID
     * @param institutionId 机构ID
     * @return 题目组项列表
     */
    List<QuestionGroupItemVO> getGroupItems(Long groupId, Long institutionId);
    
    /**
     * 批量更新题目组项的顺序
     *
     * @param groupId 题目组ID
     * @param itemDTOs 题目项列表
     * @param institutionId 机构ID 
     * @return 是否更新成功
     */
    boolean updateItemsOrder(Long groupId, List<QuestionGroupItemDTO> itemDTOs, Long institutionId);
    
    /**
     * 根据课程小节ID获取相关题目组
     *
     * @param sectionId 课程小节ID
     * @param institutionId 机构ID
     * @return 题目组列表
     */
    List<QuestionGroupVO> getGroupsBySectionId(Long sectionId, Long institutionId);
    
    /**
     * 关联题目组到课程小节
     *
     * @param groupId 题目组ID
     * @param sectionId 课程小节ID
     * @param institutionId 机构ID
     * @return 是否关联成功
     */
    boolean associateGroupToSection(Long groupId, Long sectionId, Long institutionId);
    
    /**
     * 解除题目组与课程小节的关联
     *
     * @param sectionId 课程小节ID
     * @param groupId 题目组ID
     * @param institutionId 机构ID
     * @return 是否解除成功
     */
    boolean dissociateGroupFromSection(Long groupId, Long sectionId, Long institutionId);
    
    /**
     * 批量添加题目到题目组
     *
     * @param groupId 题目组ID
     * @param questionIds 题目ID列表
     * @param institutionId 机构ID
     * @return 添加后的题目组项信息列表
     */
    List<QuestionGroupItemVO> addQuestionsToGroup(Long groupId, List<Long> questionIds, Long institutionId);
} 