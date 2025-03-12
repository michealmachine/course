 package com.zhangziqi.online_course_mine.service.impl;

import com.zhangziqi.online_course_mine.exception.ResourceNotFoundException;
import com.zhangziqi.online_course_mine.exception.ServiceException;
import com.zhangziqi.online_course_mine.model.dto.TagDTO;
import com.zhangziqi.online_course_mine.model.entity.Tag;
import com.zhangziqi.online_course_mine.model.vo.TagVO;
import com.zhangziqi.online_course_mine.repository.TagRepository;
import com.zhangziqi.online_course_mine.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 标签服务实现类
 */
@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;

    @Override
    @Transactional
    public Long createTag(TagDTO tagDTO) {
        // 校验标签名称唯一性
        if (!isNameAvailable(tagDTO.getName(), null)) {
            throw new ServiceException("标签名称已存在");
        }

        Tag tag = new Tag();
        BeanUtils.copyProperties(tagDTO, tag);
        Tag savedTag = tagRepository.save(tag);
        return savedTag.getId();
    }

    @Override
    @Transactional
    public boolean updateTag(Long id, TagDTO tagDTO) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("标签不存在"));

        // 校验标签名称唯一性
        if (!isNameAvailable(tagDTO.getName(), id)) {
            throw new ServiceException("标签名称已存在");
        }

        tag.setName(tagDTO.getName());
        tag.setDescription(tagDTO.getDescription());
        tagRepository.save(tag);
        return true;
    }

    @Override
    @Transactional
    public boolean deleteTag(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("标签不存在"));

        // 检查标签是否被课程使用
        if (tag.getCourses() != null && !tag.getCourses().isEmpty()) {
            throw new ServiceException("标签正在被课程使用，无法删除");
        }

        tagRepository.delete(tag);
        return true;
    }

    @Override
    public TagVO getTag(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("标签不存在"));
        return convertToTagVO(tag);
    }

    @Override
    public TagVO getTagByName(String name) {
        Tag tag = tagRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("标签不存在"));
        return convertToTagVO(tag);
    }

    @Override
    public Page<TagVO> listTags(String keyword, Pageable pageable) {
        Page<Tag> tagPage;
        
        if (StringUtils.hasText(keyword)) {
            // 创建动态查询条件
            Specification<Tag> spec = (root, query, criteriaBuilder) -> {
                String likePattern = "%" + keyword + "%";
                return criteriaBuilder.or(
                        criteriaBuilder.like(root.get("name"), likePattern),
                        criteriaBuilder.like(root.get("description"), likePattern)
                );
            };
            tagPage = tagRepository.findAll(spec, pageable);
        } else {
            tagPage = tagRepository.findAll(pageable);
        }
        
        return tagPage.map(this::convertToTagVO);
    }

    @Override
    public List<TagVO> getPopularTags(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Tag> popularTags = tagRepository.findTopByOrderByUseCountDesc(pageable);
        return popularTags.stream()
                .map(this::convertToTagVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Set<Tag> getOrCreateTags(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new HashSet<>();
        }

        Set<Tag> tags = new HashSet<>();
        for (String name : tagNames) {
            // 去除空格并验证
            String trimmedName = name.trim();
            if (!StringUtils.hasText(trimmedName)) {
                continue;
            }

            // 查找现有标签或创建新标签
            Optional<Tag> existingTag = tagRepository.findByName(trimmedName);
            if (existingTag.isPresent()) {
                tags.add(existingTag.get());
            } else {
                // 创建新标签
                Tag newTag = new Tag();
                newTag.setName(trimmedName);
                newTag.setUseCount(0);
                tags.add(tagRepository.save(newTag));
            }
        }
        return tags;
    }

    @Override
    public boolean isNameAvailable(String name, Long excludeId) {
        Optional<Tag> existingTag = tagRepository.findByName(name);
        return !existingTag.isPresent() || existingTag.get().getId().equals(excludeId);
    }

    @Override
    @Transactional
    public void incrementTagUseCount(Long tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("标签不存在"));
        tag.incrementUseCount();
        tagRepository.save(tag);
    }

    @Override
    @Transactional
    public void decrementTagUseCount(Long tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("标签不存在"));
        tag.decrementUseCount();
        tagRepository.save(tag);
    }

    @Override
    public Tag getTagEntity(Long id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("标签不存在"));
    }
    
    /**
     * 将标签实体转换为VO对象
     */
    private TagVO convertToTagVO(Tag tag) {
        TagVO vo = new TagVO();
        BeanUtils.copyProperties(tag, vo);
        
        // 设置关联课程数量
        if (tag.getCourses() != null) {
            vo.setCourseCount(tag.getCourses().size());
        } else {
            vo.setCourseCount(0);
        }
        
        return vo;
    }
}