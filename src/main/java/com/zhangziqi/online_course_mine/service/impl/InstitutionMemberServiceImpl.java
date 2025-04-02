package com.zhangziqi.online_course_mine.service.impl;

import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.model.entity.Role;
import com.zhangziqi.online_course_mine.model.entity.User;
import com.zhangziqi.online_course_mine.model.enums.RoleEnum;
import com.zhangziqi.online_course_mine.model.vo.UserVO;
import com.zhangziqi.online_course_mine.repository.RoleRepository;
import com.zhangziqi.online_course_mine.repository.UserRepository;
import com.zhangziqi.online_course_mine.service.InstitutionMemberService;
import com.zhangziqi.online_course_mine.service.InstitutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 机构成员管理服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InstitutionMemberServiceImpl implements InstitutionMemberService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final InstitutionService institutionService;
    
    private static final int MAX_MEMBERS = 5;
    
    @Override
    public Page<UserVO> getInstitutionMembers(Long institutionId, String keyword, Pageable pageable) {
        Page<User> members;
        
        if (StringUtils.hasText(keyword)) {
            // 有关键字时进行搜索
            members = userRepository.findByInstitutionIdAndKeyword(institutionId, keyword, pageable);
        } else {
            // 无关键字时获取全部
            members = userRepository.findByInstitutionId(institutionId, pageable);
        }
        
        // 将用户实体转换为VO
        return members.map(this::convertToUserVO);
    }
    
    @Override
    public long countInstitutionMembers(Long institutionId) {
        return userRepository.countByInstitutionId(institutionId);
    }
    
    @Override
    @Transactional
    public void removeMember(Long institutionId, Long userId, String operatorUsername) {
        log.info("开始移除机构成员: institutionId={}, userId={}, operator={}", institutionId, userId, operatorUsername);
        
        // 验证操作者是否为机构管理员
        if (!institutionService.isInstitutionAdmin(operatorUsername, institutionId)) {
            log.warn("非机构管理员尝试移除成员: operator={}, institutionId={}", operatorUsername, institutionId);
            throw new BusinessException("只有机构管理员才能移除成员");
        }
        
        // 获取要移除的用户
        User user = userRepository.findById(userId)
            .orElseThrow(() -> {
                log.warn("要移除的用户不存在: userId={}", userId);
                return new BusinessException("用户不存在");
            });
            
        // 验证用户是否属于该机构
        if (user.getInstitutionId() == null || !user.getInstitutionId().equals(institutionId)) {
            log.warn("用户不属于该机构: userId={}, userInstitutionId={}, targetInstitutionId={}", 
                userId, user.getInstitutionId(), institutionId);
            throw new BusinessException("该用户不属于您的机构");
        }
        
        // 检查操作者是否要移除自己
        User operator = userRepository.findByUsername(operatorUsername)
            .orElseThrow(() -> {
                log.warn("操作者不存在: username={}", operatorUsername);
                return new BusinessException("操作者不存在");
            });
            
        if (operator.getId().equals(userId)) {
            log.warn("管理员尝试移除自己: operatorId={}, userId={}", operator.getId(), userId);
            throw new BusinessException("管理员不能移除自己");
        }
        
        // 获取机构角色
        Role institutionRole = roleRepository.findByCode(RoleEnum.INSTITUTION.getCode())
            .orElseThrow(() -> {
                log.error("机构角色不存在: roleCode={}", RoleEnum.INSTITUTION.getCode());
                return new BusinessException("机构角色不存在");
            });
            
        log.info("准备移除用户的机构关联和角色: userId={}, username={}", user.getId(), user.getUsername());
        
        // 移除机构关联
        user.setInstitutionId(null);
        
        // 移除机构角色
        user.getRoles().remove(institutionRole);
        
        // 保存更新
        userRepository.save(user);
        log.info("成功将用户从机构中移除: userId={}, username={}, institutionId={}", 
            user.getId(), user.getUsername(), institutionId);
    }
    
    @Override
    public Map<String, Object> getMemberStats(Long institutionId) {
        long memberCount = countInstitutionMembers(institutionId);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", memberCount);
        stats.put("limit", MAX_MEMBERS);
        stats.put("available", Math.max(0, MAX_MEMBERS - memberCount));
        
        return stats;
    }
    
    /**
     * 将用户实体转换为VO（用于列表展示，不包含敏感信息）
     */
    private UserVO convertToUserVO(User user) {
        return UserVO.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .phone(user.getPhone())
            .nickname(user.getNickname())
            .avatar(user.getAvatar())
            .status(user.getStatus())
            .institutionId(user.getInstitutionId())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
    }
} 