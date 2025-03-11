package com.zhangziqi.online_course_mine.service.impl;

import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.model.dto.InstitutionRegisterDTO;
import com.zhangziqi.online_course_mine.model.entity.Institution;
import com.zhangziqi.online_course_mine.model.entity.Role;
import com.zhangziqi.online_course_mine.model.entity.User;
import com.zhangziqi.online_course_mine.model.enums.RoleEnum;
import com.zhangziqi.online_course_mine.repository.InstitutionRepository;
import com.zhangziqi.online_course_mine.repository.RoleRepository;
import com.zhangziqi.online_course_mine.repository.UserRepository;
import com.zhangziqi.online_course_mine.service.EmailService;
import com.zhangziqi.online_course_mine.service.InstitutionAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

/**
 * 机构用户注册服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InstitutionAuthServiceImpl implements InstitutionAuthService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final InstitutionRepository institutionRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    
    @Override
    @Transactional
    public void registerWithInstitutionCode(InstitutionRegisterDTO registerDTO) {
        log.info("开始处理机构用户注册流程: username={}, email={}, institutionCode={}", 
                registerDTO.getUsername(), registerDTO.getEmail(), registerDTO.getInstitutionCode());
        
        // 验证机构注册码
        log.debug("正在验证机构注册码: {}", registerDTO.getInstitutionCode());
        Institution institution = institutionRepository.findByRegisterCode(registerDTO.getInstitutionCode())
                .orElseThrow(() -> {
                    log.warn("机构注册码无效: {}", registerDTO.getInstitutionCode());
                    return new BusinessException("机构注册码无效");
                });
        log.info("机构注册码验证成功: code={}, institutionId={}, institutionName={}", 
                registerDTO.getInstitutionCode(), institution.getId(), institution.getName());
        
        if (institution.getStatus() != 1) {
            log.warn("机构状态异常，无法注册: institutionId={}, status={}", institution.getId(), institution.getStatus());
            throw new BusinessException("机构状态异常，无法注册");
        }
        
        // 检查用户名是否存在
        log.debug("检查用户名是否已存在: {}", registerDTO.getUsername());
        boolean usernameExists = userRepository.existsByUsername(registerDTO.getUsername());
        if (usernameExists) {
            log.warn("用户名已存在: {}", registerDTO.getUsername());
            throw new BusinessException("用户名已存在");
        }
        
        // 检查邮箱是否存在
        log.debug("检查邮箱是否已被注册: {}", registerDTO.getEmail());
        boolean emailExists = userRepository.existsByEmail(registerDTO.getEmail());
        if (emailExists) {
            log.warn("邮箱已被注册: {}", registerDTO.getEmail());
            throw new BusinessException("邮箱已被注册");
        }
        
        log.info("用户验证通过，开始创建用户: username={}, email={}, institutionId={}", 
                registerDTO.getUsername(), registerDTO.getEmail(), institution.getId());
        
        // 创建用户
        User user = User.builder()
                .username(registerDTO.getUsername())
                .password(passwordEncoder.encode(registerDTO.getPassword()))
                .email(registerDTO.getEmail())
                .phone(registerDTO.getPhone())
                .institutionId(institution.getId()) // 设置机构ID
                .status(1) // 正常状态
                .roles(new HashSet<>())
                .build();
        
        // 获取角色
        log.debug("正在获取机构角色: {}", RoleEnum.INSTITUTION.getCode());
        Role institutionRole = roleRepository.findByCode(RoleEnum.INSTITUTION.getCode())
                .orElseThrow(() -> {
                    log.error("机构角色不存在: {}", RoleEnum.INSTITUTION.getCode());
                    return new BusinessException("机构角色不存在");
                });
        
        // 设置角色 - 只分配机构角色，不再额外分配USER角色
        user.getRoles().add(institutionRole);
        
        log.debug("正在保存用户信息...");
        try {
            User savedUser = userRepository.save(user);
            log.info("机构用户注册成功: userId={}, username={}, institutionId={}", 
                    savedUser.getId(), savedUser.getUsername(), savedUser.getInstitutionId());
        } catch (Exception e) {
            log.error("保存用户信息失败: {}", e.getMessage(), e);
            throw new BusinessException("注册失败: " + e.getMessage());
        }
    }
} 