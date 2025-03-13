package com.zhangziqi.online_course_mine.service.impl;

import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.exception.ResourceNotFoundException;
import com.zhangziqi.online_course_mine.model.dto.QuestionTagDTO;
import com.zhangziqi.online_course_mine.model.entity.Institution;
import com.zhangziqi.online_course_mine.model.entity.Question;
import com.zhangziqi.online_course_mine.model.entity.QuestionTag;
import com.zhangziqi.online_course_mine.model.entity.QuestionTagMapping;
import com.zhangziqi.online_course_mine.model.vo.QuestionTagVO;
import com.zhangziqi.online_course_mine.repository.InstitutionRepository;
import com.zhangziqi.online_course_mine.repository.QuestionRepository;
import com.zhangziqi.online_course_mine.repository.QuestionTagMappingRepository;
import com.zhangziqi.online_course_mine.repository.QuestionTagRepository;
import com.zhangziqi.online_course_mine.service.QuestionTagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 题目标签服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionTagServiceImpl implements QuestionTagService {

    private final QuestionTagRepository tagRepository;
    private final QuestionTagMappingRepository tagMappingRepository;
    private final QuestionRepository questionRepository;
    private final InstitutionRepository institutionRepository;

    /**
     * 创建题目标签
     */
    @Override
    @Transactional
    public QuestionTagVO createTag(QuestionTagDTO tagDTO, Long creatorId) {
        // 验证机构是否存在
        Institution institution = institutionRepository.findById(tagDTO.getInstitutionId())
                .orElseThrow(() -> new ResourceNotFoundException("机构不存在"));
        
        // 验证标签名称是否重复
        Optional<QuestionTag> existingTag = tagRepository.findByInstitutionAndName(institution, tagDTO.getName());
        if (existingTag.isPresent()) {
            throw new BusinessException("标签名称已存在");
        }
        
        try {
            // 创建标签实体
            QuestionTag tag = QuestionTag.builder()
                    .institution(institution)
                    .name(tagDTO.getName())
                    .creatorId(creatorId)
                    .build();
            
            // 保存标签
            QuestionTag savedTag = tagRepository.save(tag);
            
            // 构建响应对象
            return buildTagVO(savedTag, 0L);
        } catch (DataIntegrityViolationException e) {
            log.error("创建标签失败", e);
            throw new BusinessException("创建标签失败：数据完整性异常");
        } catch (Exception e) {
            log.error("创建标签失败", e);
            throw new BusinessException("创建标签失败：" + e.getMessage());
        }
    }

    /**
     * 更新题目标签
     */
    @Override
    @Transactional
    public QuestionTagVO updateTag(QuestionTagDTO tagDTO) {
        // 验证标签是否存在
        QuestionTag existingTag = tagRepository.findByIdAndInstitutionId(tagDTO.getId(), tagDTO.getInstitutionId())
                .orElseThrow(() -> new ResourceNotFoundException("标签不存在"));
        
        // 验证标签名称是否重复（排除自身）
        if (!existingTag.getName().equals(tagDTO.getName())) {
            Optional<QuestionTag> tagWithSameName = tagRepository.findByInstitutionAndName(existingTag.getInstitution(), tagDTO.getName());
            if (tagWithSameName.isPresent() && !tagWithSameName.get().getId().equals(existingTag.getId())) {
                throw new BusinessException("标签名称已存在");
            }
        }
        
        try {
            // 更新标签属性
            existingTag.setName(tagDTO.getName());
            
            // 保存更新的标签
            QuestionTag updatedTag = tagRepository.save(existingTag);
            
            // 获取标签关联的题目数量
            long questionCount = tagMappingRepository.countQuestionsByTagId(updatedTag.getId());
            
            // 构建响应对象
            return buildTagVO(updatedTag, questionCount);
        } catch (Exception e) {
            log.error("更新标签失败", e);
            throw new BusinessException("更新标签失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID查询标签详情
     */
    @Override
    public QuestionTagVO getTagById(Long tagId, Long institutionId) {
        // 获取标签
        QuestionTag tag = tagRepository.findByIdAndInstitutionId(tagId, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("标签不存在"));
        
        // 获取标签关联的题目数量
        long questionCount = tagMappingRepository.countQuestionsByTagId(tagId);
        
        // 构建响应对象
        return buildTagVO(tag, questionCount);
    }

    /**
     * 删除标签
     */
    @Override
    @Transactional
    public void deleteTag(Long tagId, Long institutionId) {
        // 验证标签是否存在
        QuestionTag tag = tagRepository.findByIdAndInstitutionId(tagId, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("标签不存在"));
        
        try {
            // 删除标签的所有映射
            tagMappingRepository.deleteByTagId(tagId);
            
            // 删除标签
            tagRepository.delete(tag);
        } catch (Exception e) {
            log.error("删除标签失败", e);
            throw new BusinessException("删除标签失败：" + e.getMessage());
        }
    }

    /**
     * 分页查询标签列表
     */
    @Override
    public Page<QuestionTagVO> getTags(Long institutionId, String keyword, Pageable pageable) {
        // 获取机构
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("机构不存在"));
        
        Page<QuestionTag> tagPage;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            // 根据关键词搜索
            tagPage = tagRepository.searchByKeyword(institution, keyword.trim(), pageable);
        } else {
            // 查询所有标签
            tagPage = tagRepository.findByInstitution(institution, pageable);
        }
        
        // 转换为VO对象
        return tagPage.map(tag -> {
            // 获取标签关联的题目数量
            long questionCount = tagMappingRepository.countQuestionsByTagId(tag.getId());
            return buildTagVO(tag, questionCount);
        });
    }

    /**
     * 获取机构的所有标签
     */
    @Override
    public List<QuestionTagVO> getAllTags(Long institutionId) {
        // 查询机构的所有标签
        List<QuestionTag> tags = tagRepository.findAllByInstitutionId(institutionId);
        
        // 转换为VO对象
        return tags.stream().map(tag -> {
            // 获取标签关联的题目数量
            long questionCount = tagMappingRepository.countQuestionsByTagId(tag.getId());
            return buildTagVO(tag, questionCount);
        }).collect(Collectors.toList());
    }

    /**
     * 根据题目ID获取相关标签
     */
    @Override
    public List<QuestionTagVO> getTagsByQuestionId(Long questionId) {
        // 查询题目的所有标签
        List<QuestionTag> tags = tagRepository.findByQuestionId(questionId);
        
        // 转换为VO对象
        return tags.stream().map(tag -> {
            // 获取标签关联的题目数量
            long questionCount = tagMappingRepository.countQuestionsByTagId(tag.getId());
            return buildTagVO(tag, questionCount);
        }).collect(Collectors.toList());
    }

    /**
     * 为题目添加标签
     */
    @Override
    @Transactional
    public boolean addTagToQuestion(Long questionId, Long tagId, Long institutionId) {
        try {
            // 验证题目是否存在
            Question question = questionRepository.findByIdAndInstitutionId(questionId, institutionId)
                    .orElseThrow(() -> new ResourceNotFoundException("题目不存在"));
            
            // 验证标签是否存在
            QuestionTag tag = tagRepository.findByIdAndInstitutionId(tagId, institutionId)
                    .orElseThrow(() -> new ResourceNotFoundException("标签不存在"));
            
            // 验证映射是否已存在
            Optional<QuestionTagMapping> existingMapping = tagMappingRepository.findByQuestionAndTag(question, tag);
            if (existingMapping.isPresent()) {
                // 映射已存在，无需重复添加
                return true;
            }
            
            // 创建并保存映射
            QuestionTagMapping mapping = QuestionTagMapping.builder()
                    .question(question)
                    .tag(tag)
                    .build();
            
            tagMappingRepository.save(mapping);
            
            return true;
        } catch (Exception e) {
            log.error("为题目添加标签失败", e);
            if (e instanceof ResourceNotFoundException) {
                throw e;
            }
            throw new BusinessException("为题目添加标签失败：" + e.getMessage());
        }
    }

    /**
     * 移除题目标签
     */
    @Override
    @Transactional
    public boolean removeTagFromQuestion(Long questionId, Long tagId, Long institutionId) {
        try {
            // 验证题目是否存在
            questionRepository.findByIdAndInstitutionId(questionId, institutionId)
                    .orElseThrow(() -> new ResourceNotFoundException("题目不存在"));
            
            // 验证标签是否存在
            tagRepository.findByIdAndInstitutionId(tagId, institutionId)
                    .orElseThrow(() -> new ResourceNotFoundException("标签不存在"));
            
            // 删除映射
            tagMappingRepository.deleteByQuestionIdAndTagId(questionId, tagId);
            
            return true;
        } catch (Exception e) {
            log.error("移除题目标签失败", e);
            if (e instanceof ResourceNotFoundException) {
                throw e;
            }
            throw new BusinessException("移除题目标签失败：" + e.getMessage());
        }
    }

    /**
     * 构建标签视图对象
     */
    private QuestionTagVO buildTagVO(QuestionTag tag, Long questionCount) {
        return QuestionTagVO.builder()
                .id(tag.getId())
                .institutionId(tag.getInstitutionId())
                .name(tag.getName())
                .questionCount(questionCount)
                .creatorId(tag.getCreatorId())
                .createdTime(tag.getCreatedTime())
                .updatedTime(tag.getUpdatedTime())
                .build();
    }
} 