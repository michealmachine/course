package com.zhangziqi.online_course_mine.service.impl;

import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.exception.ResourceNotFoundException;
import com.zhangziqi.online_course_mine.model.dto.QuestionGroupDTO;
import com.zhangziqi.online_course_mine.model.dto.QuestionGroupItemDTO;
import com.zhangziqi.online_course_mine.model.entity.*;
import com.zhangziqi.online_course_mine.model.vo.QuestionGroupItemVO;
import com.zhangziqi.online_course_mine.model.vo.QuestionGroupVO;
import com.zhangziqi.online_course_mine.model.vo.QuestionVO;
import com.zhangziqi.online_course_mine.repository.*;
import com.zhangziqi.online_course_mine.service.QuestionGroupService;
import com.zhangziqi.online_course_mine.service.QuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 题目组服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionGroupServiceImpl implements QuestionGroupService {

    private final QuestionGroupRepository groupRepository;
    private final QuestionGroupItemRepository groupItemRepository;
    private final QuestionRepository questionRepository;
    private final InstitutionRepository institutionRepository;
    private final QuestionService questionService;
    private final UserRepository userRepository;

    /**
     * 创建题目组
     */
    @Override
    @Transactional
    public QuestionGroupVO createGroup(QuestionGroupDTO groupDTO, Long creatorId) {
        // 验证机构是否存在
        Institution institution = institutionRepository.findById(groupDTO.getInstitutionId())
                .orElseThrow(() -> new ResourceNotFoundException("机构不存在"));
        
        try {
            // 创建题目组实体
            QuestionGroup group = QuestionGroup.builder()
                    .institution(institution)
                    .name(groupDTO.getName())
                    .description(groupDTO.getDescription())
                    .creatorId(creatorId)
                    .build();
            
            // 保存题目组
            QuestionGroup savedGroup = groupRepository.save(group);
            
            // 构建响应对象
            return buildGroupVO(savedGroup, new ArrayList<>(), 0L);
        } catch (DataIntegrityViolationException e) {
            log.error("创建题目组失败", e);
            throw new BusinessException("创建题目组失败：数据完整性异常");
        } catch (Exception e) {
            log.error("创建题目组失败", e);
            throw new BusinessException("创建题目组失败：" + e.getMessage());
        }
    }

    /**
     * 更新题目组
     */
    @Override
    @Transactional
    public QuestionGroupVO updateGroup(QuestionGroupDTO groupDTO) {
        // 验证题目组是否存在
        QuestionGroup existingGroup = groupRepository.findByIdAndInstitutionId(groupDTO.getId(), groupDTO.getInstitutionId())
                .orElseThrow(() -> new ResourceNotFoundException("题目组不存在"));
        
        try {
            // 更新题目组属性
            existingGroup.setName(groupDTO.getName());
            existingGroup.setDescription(groupDTO.getDescription());
            
            // 保存更新的题目组
            QuestionGroup updatedGroup = groupRepository.save(existingGroup);
            
            // 获取题目组的所有题目
            List<QuestionGroupItem> items = groupItemRepository.findByGroupId(updatedGroup.getId());
            List<QuestionGroupItemVO> itemVOs = buildGroupItemVOs(items);
            
            // 构建响应对象
            return buildGroupVO(updatedGroup, itemVOs, (long) items.size());
        } catch (Exception e) {
            log.error("更新题目组失败", e);
            throw new BusinessException("更新题目组失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID查询题目组详情
     */
    @Override
    public QuestionGroupVO getGroupById(Long groupId, Long institutionId, boolean includeItems) {
        // 获取题目组
        QuestionGroup group = groupRepository.findByIdAndInstitutionId(groupId, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("题目组不存在"));
        
        List<QuestionGroupItemVO> itemVOs = null;
        long questionCount = 0;
        
        if (includeItems) {
            // 获取题目组的所有题目
            List<QuestionGroupItem> items = groupItemRepository.findByGroupId(group.getId());
            itemVOs = buildGroupItemVOs(items);
            questionCount = items.size();
        } else {
            // 仅获取题目数量
            questionCount = groupItemRepository.countByGroupId(group.getId());
        }
        
        // 构建响应对象
        return buildGroupVO(group, itemVOs, questionCount);
    }

    /**
     * 删除题目组
     */
    @Override
    @Transactional
    public void deleteGroup(Long groupId, Long institutionId) {
        // 验证题目组是否存在
        QuestionGroup group = groupRepository.findByIdAndInstitutionId(groupId, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("题目组不存在"));
        
        try {
            
            // 删除题目组的所有题目项
            groupItemRepository.deleteByGroupId(groupId);
            
            // 删除题目组
            groupRepository.delete(group);
        } catch (Exception e) {
            log.error("删除题目组失败", e);
            throw new BusinessException("删除题目组失败：" + e.getMessage());
        }
    }

    /**
     * 分页查询题目组列表
     */
    @Override
    public Page<QuestionGroupVO> getGroups(Long institutionId, String keyword, Pageable pageable) {
        // 获取机构
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("机构不存在"));
        
        // 添加调试日志记录搜索关键词
        log.debug("搜索题组 - 机构ID: {}, 关键词: '{}', 页码: {}, 每页数量: {}", 
                institutionId, keyword, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<QuestionGroup> groupPage;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            // 根据关键词搜索
            log.debug("执行关键词搜索: '{}'", keyword.trim());
            groupPage = groupRepository.searchByKeyword(institution, keyword.trim(), pageable);
            log.debug("搜索结果数量: {}", groupPage.getTotalElements());
        } else {
            // 查询所有题目组
            log.debug("查询所有题目组");
            groupPage = groupRepository.findByInstitution(institution, pageable);
        }
        
        // 获取所有题目组的ID
        List<Long> groupIds = groupPage.getContent().stream()
                .map(QuestionGroup::getId)
                .collect(Collectors.toList());
        
        // 批量查询题目组的题目数量
        Map<Long, Long> questionCountMap = groupRepository.countQuestionsByGroupIds(groupIds)
                .stream()
                .collect(Collectors.toMap(
                        result -> (Long) result[0],
                        result -> (Long) result[1]
                ));
        
        // 转换为VO对象
        return groupPage.map(group -> {
            long questionCount = questionCountMap.getOrDefault(group.getId(), 0L);
            return buildGroupVO(group, null, questionCount);
        });
    }

    /**
     * 获取机构的所有题目组
     */
    @Override
    public List<QuestionGroupVO> getAllGroups(Long institutionId) {
        // 查询机构的所有题目组
        List<QuestionGroup> groups = groupRepository.findAllByInstitutionId(institutionId);
        
        // 获取所有题目组的ID
        List<Long> groupIds = groups.stream()
                .map(QuestionGroup::getId)
                .collect(Collectors.toList());
        
        // 批量查询题目组的题目数量
        Map<Long, Long> questionCountMap = groupRepository.countQuestionsByGroupIds(groupIds)
                .stream()
                .collect(Collectors.toMap(
                        result -> (Long) result[0],
                        result -> (Long) result[1],
                        (v1, v2) -> v1
                ));
        
        // 转换为VO对象
        return groups.stream()
                .map(group -> {
                    long questionCount = questionCountMap.getOrDefault(group.getId(), 0L);
                    return buildGroupVO(group, null, questionCount);
                })
                .collect(Collectors.toList());
    }

    /**
     * 为题目组添加题目
     */
    @Override
    @Transactional
    public QuestionGroupItemVO addQuestionToGroup(QuestionGroupItemDTO itemDTO) {
        // 验证题目组是否存在
        QuestionGroup group = groupRepository.findById(itemDTO.getGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("题目组不存在"));
        
        // 验证题目是否存在
        Question question = questionRepository.findById(itemDTO.getQuestionId())
                .orElseThrow(() -> new ResourceNotFoundException("题目不存在"));
        
        // 验证题目和题目组属于同一个机构
        if (!Objects.equals(group.getInstitutionId(), question.getInstitutionId())) {
            throw new BusinessException("题目和题目组必须属于同一个机构");
        }
        
        // 验证题目是否已在题目组中
        if (groupItemRepository.existsByGroupIdAndQuestionId(group.getId(), question.getId())) {
            throw new BusinessException("该题目已在题目组中");
        }
        
        try {
            // 创建题目组项实体
            QuestionGroupItem item = QuestionGroupItem.builder()
                    .group(group)
                    .question(question)
                    .orderIndex(itemDTO.getOrderIndex())
                    .difficulty(itemDTO.getDifficulty())
                    .score(itemDTO.getScore())
                    .build();
            
            // 保存题目组项
            QuestionGroupItem savedItem = groupItemRepository.save(item);
            
            // 获取题目详情
            QuestionVO questionVO = questionService.getQuestionById(question.getId(), question.getInstitutionId());
            
            // 构建响应对象
            return buildGroupItemVO(savedItem, questionVO);
        } catch (Exception e) {
            log.error("添加题目到题目组失败", e);
            throw new BusinessException("添加题目到题目组失败：" + e.getMessage());
        }
    }

    /**
     * 更新题目组项
     */
    @Override
    @Transactional
    public QuestionGroupItemVO updateGroupItem(QuestionGroupItemDTO itemDTO) {
        // 验证题目组项是否存在
        QuestionGroupItem existingItem = groupItemRepository.findById(itemDTO.getId())
                .orElseThrow(() -> new ResourceNotFoundException("题目组项不存在"));
        
        // 验证题目组ID是否一致
        if (!Objects.equals(existingItem.getGroup().getId(), itemDTO.getGroupId())) {
            throw new BusinessException("题目组ID不匹配");
        }
        
        try {
            // 更新题目组项属性
            existingItem.setOrderIndex(itemDTO.getOrderIndex());
            existingItem.setDifficulty(itemDTO.getDifficulty());
            existingItem.setScore(itemDTO.getScore());
            
            // 保存更新的题目组项
            QuestionGroupItem updatedItem = groupItemRepository.save(existingItem);
            
            // 获取题目详情
            QuestionVO questionVO = questionService.getQuestionById(updatedItem.getQuestion().getId(), updatedItem.getQuestion().getInstitutionId());
            
            // 构建响应对象
            return buildGroupItemVO(updatedItem, questionVO);
        } catch (Exception e) {
            log.error("更新题目组项失败", e);
            throw new BusinessException("更新题目组项失败：" + e.getMessage());
        }
    }

    /**
     * 从题目组移除题目
     */
    @Override
    @Transactional
    public boolean removeQuestionFromGroup(Long groupId, Long itemId, Long institutionId) {
        // 验证题目组是否存在
        groupRepository.findByIdAndInstitutionId(groupId, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("题目组不存在"));
        
        // 验证题目组项是否存在
        QuestionGroupItem item = groupItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("题目组项不存在"));
        
        // 验证题目组ID是否一致
        if (!Objects.equals(item.getGroup().getId(), groupId)) {
            throw new BusinessException("题目组ID不匹配");
        }
        
        try {
            // 删除题目组项
            groupItemRepository.delete(item);
            return true;
        } catch (Exception e) {
            log.error("从题目组移除题目失败", e);
            throw new BusinessException("从题目组移除题目失败：" + e.getMessage());
        }
    }

    /**
     * 获取题目组中的所有题目
     */
    @Override
    public List<QuestionGroupItemVO> getGroupItems(Long groupId, Long institutionId) {
        // 验证题目组是否存在
        groupRepository.findByIdAndInstitutionId(groupId, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("题目组不存在"));
        
        // 获取题目组的所有题目项
        List<QuestionGroupItem> items = groupItemRepository.findByGroupIdOrderByOrderIndex(groupId);
        
        // 转换为VO对象
        return buildGroupItemVOs(items);
    }

    /**
     * 更新题目组中题目的顺序
     */
    @Override
    @Transactional
    public boolean updateItemsOrder(Long groupId, List<QuestionGroupItemDTO> itemDTOs, Long institutionId) {
        // 验证题目组是否存在
        groupRepository.findByIdAndInstitutionId(groupId, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("题目组不存在"));
        
        try {
            // 获取题目组的所有题目项
            List<QuestionGroupItem> existingItems = groupItemRepository.findByGroupId(groupId);
            Map<Long, QuestionGroupItem> itemMap = existingItems.stream()
                    .collect(Collectors.toMap(QuestionGroupItem::getId, Function.identity()));
            
            // 更新题目项的顺序
            for (QuestionGroupItemDTO itemDTO : itemDTOs) {
                QuestionGroupItem item = itemMap.get(itemDTO.getId());
                if (item != null) {
                    item.setOrderIndex(itemDTO.getOrderIndex());
                    groupItemRepository.save(item);
                }
            }
            
            return true;
        } catch (Exception e) {
            log.error("更新题目顺序失败", e);
            throw new BusinessException("更新题目顺序失败：" + e.getMessage());
        }
    }

    /**
     * 批量添加题目到题目组
     */
    @Override
    @Transactional
    public List<QuestionGroupItemVO> addQuestionsToGroup(Long groupId, List<Long> questionIds, Long institutionId) {
        // 验证题目组是否存在
        QuestionGroup group = groupRepository.findByIdAndInstitutionId(groupId, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("题目组不存在"));
        
        // 获取当前题目组中的题目数量，用于设置顺序
        int currentItemCount = (int) groupItemRepository.countByGroupId(groupId);
        
        List<QuestionGroupItemVO> results = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();
        
        for (int i = 0; i < questionIds.size(); i++) {
            Long questionId = questionIds.get(i);
            try {
                // 验证题目是否存在
                Question question = questionRepository.findById(questionId)
                        .orElseThrow(() -> new ResourceNotFoundException("题目不存在: " + questionId));
                
                // 验证题目和题目组属于同一个机构
                if (!Objects.equals(group.getInstitutionId(), question.getInstitutionId())) {
                    errorMessages.add("题目ID: " + questionId + " - 题目和题目组必须属于同一个机构");
                    continue;
                }
                
                // 验证题目是否已在题目组中
                if (groupItemRepository.existsByGroupIdAndQuestionId(group.getId(), question.getId())) {
                    errorMessages.add("题目ID: " + questionId + " - 该题目已在题目组中");
                    continue;
                }
                
                // 创建题目组项实体
                QuestionGroupItem item = QuestionGroupItem.builder()
                        .group(group)
                        .question(question)
                        .orderIndex(currentItemCount + i) // 设置顺序
                        .difficulty(question.getDifficulty()) // 默认使用题目的难度
                        .score(question.getScore()) // 默认使用题目的分值
                        .build();
                
                // 保存题目组项
                QuestionGroupItem savedItem = groupItemRepository.save(item);
                
                // 获取题目详情
                QuestionVO questionVO = questionService.getQuestionById(question.getId(), question.getInstitutionId());
                
                // 构建响应对象并添加到结果列表
                results.add(buildGroupItemVO(savedItem, questionVO));
                
            } catch (Exception e) {
                log.error("添加题目到题目组失败, 题目ID: " + questionId, e);
                errorMessages.add("题目ID: " + questionId + " - " + e.getMessage());
            }
        }
        
        // 如果有错误消息，记录到日志中
        if (!errorMessages.isEmpty()) {
            log.warn("部分题目添加失败: {}", String.join("; ", errorMessages));
        }
        
        return results;
    }

    /**
     * 构建题目组视图对象
     */
    private QuestionGroupVO buildGroupVO(QuestionGroup group, List<QuestionGroupItemVO> items, Long questionCount) {
        QuestionGroupVO.QuestionGroupVOBuilder builder = QuestionGroupVO.builder()
                .id(group.getId())
                .institutionId(group.getInstitutionId())
                .name(group.getName())
                .description(group.getDescription())
                .questionCount(questionCount)
                .creatorId(group.getCreatorId())
                .createdTime(group.getCreatedTime())
                .updatedTime(group.getUpdatedTime());
        
        // 设置创建者名称
        if (group.getCreatorId() != null) {
            userRepository.findById(group.getCreatorId()).ifPresent(user -> 
                builder.creatorName(user.getNickname() != null ? user.getNickname() : user.getUsername())
            );
        }
        
        // 设置题目项列表
        if (items != null) {
            builder.items(items);
        }
        
        return builder.build();
    }

    /**
     * 构建题目组项视图对象列表
     */
    private List<QuestionGroupItemVO> buildGroupItemVOs(List<QuestionGroupItem> items) {
        if (items.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 获取所有题目ID
        List<Long> questionIds = items.stream()
                .map(item -> item.getQuestion().getId())
                .collect(Collectors.toList());
        
        // 批量获取题目详情
        Map<Long, QuestionVO> questionMap = questionService.getQuestionsByIds(questionIds)
                .stream()
                .collect(Collectors.toMap(QuestionVO::getId, Function.identity()));
        
        // 转换为VO对象
        return items.stream()
                .map(item -> {
                    QuestionVO questionVO = questionMap.get(item.getQuestion().getId());
                    return buildGroupItemVO(item, questionVO);
                })
                .collect(Collectors.toList());
    }

    /**
     * 构建题目组项视图对象
     */
    private QuestionGroupItemVO buildGroupItemVO(QuestionGroupItem item, QuestionVO question) {
        // 获取难度描述
        String difficultyDesc = "未知";
        switch (item.getDifficulty()) {
            case 1:
                difficultyDesc = "简单";
                break;
            case 2:
                difficultyDesc = "中等";
                break;
            case 3:
                difficultyDesc = "困难";
                break;
        }
        
        return QuestionGroupItemVO.builder()
                .id(item.getId())
                .groupId(item.getGroup().getId())
                .questionId(item.getQuestion().getId())
                .question(question)
                .orderIndex(item.getOrderIndex())
                .difficulty(item.getDifficulty())
                .difficultyDesc(difficultyDesc)
                .score(item.getScore())
                .build();
    }

    @Override
    public QuestionGroupVO getGroupByIdForPreview(Long groupId, boolean includeItems) {
        log.info("获取题目组(预览模式) - 题目组ID: {}, 包含题目: {}", groupId, includeItems);
        
        // 直接通过ID查找题组，不验证机构
        QuestionGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> {
                    log.error("题目组不存在, groupId: {}", groupId);
                    return new ResourceNotFoundException("题目组不存在，ID: " + groupId);
                });
        
        List<QuestionGroupItemVO> itemVOs = null;
        long questionCount = 0;
        
        if (includeItems) {
            // 获取题目组的所有题目
            List<QuestionGroupItem> items = groupItemRepository.findByGroupId(group.getId());
            itemVOs = buildGroupItemVOs(items);
            questionCount = items.size();
            log.info("已加载题目组的题目 - 题目组ID: {}, 题目数: {}", groupId, questionCount);
        } else {
            // 仅获取题目数量
            questionCount = groupItemRepository.countByGroupId(group.getId());
            log.info("已获取题目数量 - 题目组ID: {}, 题目数: {}", groupId, questionCount);
        }
        
        // 构建响应对象
        QuestionGroupVO result = buildGroupVO(group, itemVOs, questionCount);
        log.info("已构建题目组详情(预览模式) - 题目组名称: {}", result.getName());
        
        return result;
    }
} 