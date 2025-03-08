package com.zhangziqi.online_course_mine.service.impl;

import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.model.dto.RegisterDTO;
import com.zhangziqi.online_course_mine.model.dto.UserDTO;
import com.zhangziqi.online_course_mine.model.dto.UserQueryDTO;
import com.zhangziqi.online_course_mine.model.entity.Role;
import com.zhangziqi.online_course_mine.model.entity.User;
import com.zhangziqi.online_course_mine.model.enums.RoleEnum;
import com.zhangziqi.online_course_mine.model.vo.UserVO;
import com.zhangziqi.online_course_mine.repository.RoleRepository;
import com.zhangziqi.online_course_mine.repository.UserRepository;
import com.zhangziqi.online_course_mine.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 注册用户
     *
     * @param registerDTO 注册请求
     * @return 用户信息
     */
    @Override
    @Transactional
    public User register(RegisterDTO registerDTO) {
        // 检查用户名是否存在
        if (userRepository.existsByUsername(registerDTO.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        // 检查邮箱是否存在
        if (registerDTO.getEmail() != null && userRepository.existsByEmail(registerDTO.getEmail())) {
            throw new BusinessException("邮箱已存在");
        }

        // 检查手机号是否存在
        if (registerDTO.getPhone() != null && userRepository.existsByPhone(registerDTO.getPhone())) {
            throw new BusinessException("手机号已存在");
        }

        // 获取普通用户角色
        Role userRole = roleRepository.findByCode(RoleEnum.USER.getCode())
                .orElseThrow(() -> new BusinessException("角色不存在"));

        // 创建用户
        User user = User.builder()
                .username(registerDTO.getUsername())
                .password(passwordEncoder.encode(registerDTO.getPassword()))
                .email(registerDTO.getEmail())
                .phone(registerDTO.getPhone())
                .status(1) // 正常状态
                .roles(Collections.singleton(userRole))
                .build();

        // 保存用户
        User savedUser = userRepository.save(user);
        log.info("用户注册成功: {}", savedUser.getUsername());
        return savedUser;
    }

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    @Override
    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return 是否存在
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * 检查邮箱是否存在
     *
     * @param email 邮箱
     * @return 是否存在
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * 检查手机号是否存在
     *
     * @param phone 手机号
     * @return 是否存在
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existsByPhone(String phone) {
        return userRepository.existsByPhone(phone);
    }

    /**
     * 更新最后登录时间
     *
     * @param username 用户名
     */
    @Override
    @Transactional
    public void updateLastLoginTime(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        log.debug("更新用户最后登录时间: {}", username);
    }
    
    /**
     * 分页查询用户列表
     *
     * @param queryDTO 查询条件
     * @return 用户列表（分页）
     */
    @Override
    @Transactional(readOnly = true)
    public Page<UserVO> getUserList(UserQueryDTO queryDTO) {
        Pageable pageable = PageRequest.of(queryDTO.getPageNum() - 1, queryDTO.getPageSize());
        
        // 构建查询条件
        Specification<User> spec = (root, query, cb) -> {
            var predicates = new HashSet<Predicate>();
            
            // 按用户名模糊查询
            if (StringUtils.hasText(queryDTO.getUsername())) {
                predicates.add(cb.like(root.get("username"), "%" + queryDTO.getUsername() + "%"));
            }
            
            // 按邮箱模糊查询
            if (StringUtils.hasText(queryDTO.getEmail())) {
                predicates.add(cb.like(root.get("email"), "%" + queryDTO.getEmail() + "%"));
            }
            
            // 按手机号模糊查询
            if (StringUtils.hasText(queryDTO.getPhone())) {
                predicates.add(cb.like(root.get("phone"), "%" + queryDTO.getPhone() + "%"));
            }
            
            // 按状态精确查询
            if (queryDTO.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), queryDTO.getStatus()));
            }
            
            // 按机构ID精确查询
            if (queryDTO.getInstitutionId() != null) {
                predicates.add(cb.equal(root.get("institutionId"), queryDTO.getInstitutionId()));
            }
            
            // 按角色ID查询
            if (queryDTO.getRoleId() != null) {
                var join = root.join("roles");
                predicates.add(cb.equal(join.get("id"), queryDTO.getRoleId()));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        // 执行查询
        Page<User> userPage = userRepository.findAll(spec, pageable);
        
        // 转换为VO
        List<UserVO> userVOList = userPage.getContent().stream()
                .map(this::convertToUserVO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(userVOList, pageable, userPage.getTotalElements());
    }
    
    /**
     * 获取用户详情
     *
     * @param id 用户ID
     * @return 用户详情
     */
    @Override
    @Transactional(readOnly = true)
    public UserVO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        return convertToUserVO(user);
    }
    
    /**
     * 创建用户
     *
     * @param userDTO 用户信息
     * @return 创建后的用户信息
     */
    @Override
    @Transactional
    public UserVO createUser(UserDTO userDTO) {
        // 检查用户名是否存在
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        
        // 检查邮箱是否存在
        if (userDTO.getEmail() != null && userRepository.existsByEmail(userDTO.getEmail())) {
            throw new BusinessException("邮箱已存在");
        }
        
        // 检查手机号是否存在
        if (userDTO.getPhone() != null && userRepository.existsByPhone(userDTO.getPhone())) {
            throw new BusinessException("手机号已存在");
        }
        
        // 获取角色
        Set<Role> roles = new HashSet<>();
        if (userDTO.getRoleIds() != null && !userDTO.getRoleIds().isEmpty()) {
            roles = userDTO.getRoleIds().stream()
                    .map(roleId -> roleRepository.findById(roleId)
                            .orElseThrow(() -> new BusinessException("角色不存在: " + roleId)))
                    .collect(Collectors.toSet());
        } else {
            // 默认分配普通用户角色
            Role userRole = roleRepository.findByCode(RoleEnum.USER.getCode())
                    .orElseThrow(() -> new BusinessException("角色不存在"));
            roles.add(userRole);
        }
        
        // 创建用户
        User user = User.builder()
                .username(userDTO.getUsername())
                .password(passwordEncoder.encode(userDTO.getPassword()))
                .email(userDTO.getEmail())
                .phone(userDTO.getPhone())
                .avatar(userDTO.getAvatar())
                .nickname(userDTO.getNickname())
                .status(userDTO.getStatus() != null ? userDTO.getStatus() : 1)
                .institutionId(userDTO.getInstitutionId())
                .roles(roles)
                .build();
        
        // 保存用户
        User savedUser = userRepository.save(user);
        log.info("创建用户成功: {}", savedUser.getUsername());
        return convertToUserVO(savedUser);
    }
    
    /**
     * 更新用户
     *
     * @param id 用户ID
     * @param userDTO 用户信息
     * @return 更新后的用户信息
     */
    @Override
    @Transactional
    public UserVO updateUser(Long id, UserDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        // 检查用户名是否存在
        if (StringUtils.hasText(userDTO.getUsername()) && 
                !user.getUsername().equals(userDTO.getUsername()) && 
                userRepository.existsByUsername(userDTO.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        
        // 检查邮箱是否存在
        if (StringUtils.hasText(userDTO.getEmail()) && 
                (user.getEmail() == null || !user.getEmail().equals(userDTO.getEmail())) && 
                userRepository.existsByEmail(userDTO.getEmail())) {
            throw new BusinessException("邮箱已存在");
        }
        
        // 检查手机号是否存在
        if (StringUtils.hasText(userDTO.getPhone()) && 
                (user.getPhone() == null || !user.getPhone().equals(userDTO.getPhone())) && 
                userRepository.existsByPhone(userDTO.getPhone())) {
            throw new BusinessException("手机号已存在");
        }
        
        // 更新角色
        if (userDTO.getRoleIds() != null && !userDTO.getRoleIds().isEmpty()) {
            Set<Role> roles = userDTO.getRoleIds().stream()
                    .map(roleId -> roleRepository.findById(roleId)
                            .orElseThrow(() -> new BusinessException("角色不存在: " + roleId)))
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }
        
        // 更新基本信息
        if (StringUtils.hasText(userDTO.getUsername())) {
            user.setUsername(userDTO.getUsername());
        }
        
        if (StringUtils.hasText(userDTO.getPassword())) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }
        
        if (StringUtils.hasText(userDTO.getEmail())) {
            user.setEmail(userDTO.getEmail());
        }
        
        if (StringUtils.hasText(userDTO.getPhone())) {
            user.setPhone(userDTO.getPhone());
        }
        
        if (StringUtils.hasText(userDTO.getAvatar())) {
            user.setAvatar(userDTO.getAvatar());
        }
        
        if (StringUtils.hasText(userDTO.getNickname())) {
            user.setNickname(userDTO.getNickname());
        }
        
        if (userDTO.getStatus() != null) {
            user.setStatus(userDTO.getStatus());
        }
        
        if (userDTO.getInstitutionId() != null) {
            user.setInstitutionId(userDTO.getInstitutionId());
        }
        
        // 保存用户
        User savedUser = userRepository.save(user);
        log.info("更新用户成功: {}", savedUser.getUsername());
        return convertToUserVO(savedUser);
    }
    
    /**
     * 删除用户
     *
     * @param id 用户ID
     */
    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        userRepository.delete(user);
        log.info("删除用户成功: {}", user.getUsername());
    }
    
    /**
     * 修改用户状态
     *
     * @param id 用户ID
     * @param status 状态（0-禁用，1-正常）
     * @return 更新后的用户信息
     */
    @Override
    @Transactional
    public UserVO updateUserStatus(Long id, Integer status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        user.setStatus(status);
        User savedUser = userRepository.save(user);
        log.info("更新用户状态成功: {}, 状态: {}", savedUser.getUsername(), status);
        return convertToUserVO(savedUser);
    }
    
    /**
     * 给用户分配角色
     *
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     * @return 更新后的用户信息
     */
    @Override
    @Transactional
    public UserVO assignRoles(Long userId, Set<Long> roleIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        if (roleIds == null || roleIds.isEmpty()) {
            throw new BusinessException("角色ID列表不能为空");
        }
        
        Set<Role> roles = roleIds.stream()
                .map(roleId -> roleRepository.findById(roleId)
                        .orElseThrow(() -> new BusinessException("角色不存在: " + roleId)))
                .collect(Collectors.toSet());
        
        user.setRoles(roles);
        User savedUser = userRepository.save(user);
        log.info("给用户分配角色成功: {}, 角色IDs: {}", savedUser.getUsername(), roleIds);
        return convertToUserVO(savedUser);
    }
    
    /**
     * 批量删除用户
     *
     * @param ids 用户ID列表
     */
    @Override
    @Transactional
    public void batchDeleteUsers(List<Long> ids) {
        for (Long id : ids) {
            deleteUser(id);
        }
    }
    
    /**
     * 转换为用户VO
     *
     * @param user 用户实体
     * @return 用户VO
     */
    private UserVO convertToUserVO(User user) {
        return UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .nickname(user.getNickname())
                .status(user.getStatus())
                .institutionId(user.getInstitutionId())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .roles(user.getRoles())
                .build();
    }
} 