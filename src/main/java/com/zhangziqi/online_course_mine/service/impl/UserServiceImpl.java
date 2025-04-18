package com.zhangziqi.online_course_mine.service.impl;

import com.zhangziqi.online_course_mine.config.CacheConfig;
import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.model.dto.RegisterDTO;
import com.zhangziqi.online_course_mine.model.dto.UserDTO;
import com.zhangziqi.online_course_mine.model.dto.UserQueryDTO;
import com.zhangziqi.online_course_mine.model.entity.Role;
import com.zhangziqi.online_course_mine.model.entity.User;
import com.zhangziqi.online_course_mine.model.enums.RoleEnum;
import com.zhangziqi.online_course_mine.model.vo.UserVO;
import com.zhangziqi.online_course_mine.model.vo.UserStatsVO;
import com.zhangziqi.online_course_mine.model.vo.UserRoleDistributionVO;
import com.zhangziqi.online_course_mine.model.vo.UserGrowthStatsVO;
import com.zhangziqi.online_course_mine.model.vo.UserStatusStatsVO;
import com.zhangziqi.online_course_mine.model.vo.UserActivityStatsVO;
import com.zhangziqi.online_course_mine.repository.RoleRepository;
import com.zhangziqi.online_course_mine.repository.UserRepository;
import com.zhangziqi.online_course_mine.security.jwt.TokenBlacklistService;
import com.zhangziqi.online_course_mine.service.EmailService;
import com.zhangziqi.online_course_mine.service.MinioService;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.cache.annotation.Cacheable;

import jakarta.persistence.criteria.Predicate;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
    private final EmailService emailService;
    private final MinioService minioService;
    private final TokenBlacklistService tokenBlacklistService;

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
        return convertToUserVO(user, true);
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
        // 获取用户
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 更新状态
        user.setStatus(status);
        User savedUser = userRepository.save(user);

        // 如果用户被禁用，使其所有token失效
        if (status != 1) {
            tokenBlacklistService.invalidateUserTokens(user.getUsername());
            log.info("用户 {} 已被禁用，所有token已失效", user.getUsername());
        }

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
     * 转换为用户VO（不包含角色信息）
     *
     * @param user 用户实体
     * @return 用户VO
     */
    private UserVO convertToUserVO(User user) {
        return convertToUserVO(user, false);
    }

    /**
     * 转换为用户VO
     *
     * @param user 用户实体
     * @param includeRoles 是否包含角色信息
     * @return 用户VO
     */
    private UserVO convertToUserVO(User user, boolean includeRoles) {
        UserVO.UserVOBuilder builder = UserVO.builder()
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
                .lastLoginAt(user.getLastLoginAt());

        if (includeRoles) {
            builder.roles(user.getRoles());
        }

        return builder.build();
    }

    /**
     * 获取当前登录用户信息
     *
     * @param username 当前登录用户名
     * @return 用户详细信息
     */
    @Override
    @Transactional(readOnly = true)
    public UserVO getCurrentUser(String username) {
        User user = getUserByUsername(username);
        // 创建基本的UserVO（不包含角色信息）
        UserVO.UserVOBuilder builder = UserVO.builder()
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
                .lastLoginAt(user.getLastLoginAt());

        // 从SecurityContext获取当前用户的角色信息
        org.springframework.security.core.Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            // 直接从Authentication中获取角色信息
            Set<Role> roles = authentication.getAuthorities().stream()
                .filter(authority -> authority.getAuthority().startsWith("ROLE_"))
                .map(authority -> {
                    String roleName = authority.getAuthority().substring(5); // 去掉"ROLE_"前缀
                    return Role.builder()
                            .name(roleName)
                            .code(roleName)
                            .build();
                })
                .collect(java.util.stream.Collectors.toSet());
            builder.roles(roles);
        }

        return builder.build();
    }
    
    /**
     * 更新当前用户个人信息
     *
     * @param username 当前登录用户名
     * @param nickname 昵称
     * @param phone 手机号
     * @return 更新后的用户信息
     */
    @Override
    @Transactional
    public UserVO updateCurrentUserProfile(String username, String nickname, String phone) {
        User user = getUserByUsername(username);
        
        // 检查手机号是否已被其他用户使用
        if (StringUtils.hasText(phone) && !phone.equals(user.getPhone()) && existsByPhone(phone)) {
            throw new BusinessException("手机号已存在");
        }
        
        // 更新用户信息
        if (StringUtils.hasText(nickname)) {
            user.setNickname(nickname);
        }
        if (StringUtils.hasText(phone)) {
            user.setPhone(phone);
        }
        
        User updatedUser = userRepository.save(user);
        log.info("用户个人信息更新成功: {}", username);
        return convertToUserVO(updatedUser);
    }
    
    /**
     * 修改当前用户密码
     *
     * @param username 当前登录用户名
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否修改成功
     */
    @Override
    @Transactional
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        User user = getUserByUsername(username);
        
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("旧密码不正确");
        }
        
        // 验证新旧密码不能相同
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new BusinessException("新密码不能与旧密码相同");
        }
        
        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("用户密码修改成功: {}", username);
        return true;
    }
    
    /**
     * 更新当前用户头像
     *
     * @param username 当前登录用户名
     * @param avatarUrl 头像URL
     * @return 更新后的用户信息
     */
    @Override
    @Transactional
    public UserVO updateAvatar(String username, String avatarUrl) {
        User user = getUserByUsername(username);
        
        // 获取旧头像URL
        String oldAvatarUrl = user.getAvatar();
        
        // 更新头像
        user.setAvatar(avatarUrl);
        User updatedUser = userRepository.save(user);
        log.info("用户头像更新成功: {}", username);
        
        // 删除旧头像
        if (oldAvatarUrl != null && !oldAvatarUrl.isEmpty()) {
            try {
                // 从URL中提取对象名
                String objectName = extractObjectNameFromUrl(oldAvatarUrl);
                if (objectName != null) {
                    boolean deleted = minioService.deleteFile(objectName);
                    if (deleted) {
                        log.info("删除旧头像成功: {}", objectName);
                    } else {
                        log.warn("删除旧头像失败: {}", objectName);
                    }
                }
            } catch (Exception e) {
                log.error("删除旧头像出错: {}", e.getMessage(), e);
                // 继续执行，不影响头像更新
            }
        }
        
        return convertToUserVO(updatedUser);
    }
    
    /**
     * 从URL中提取对象名
     * 例如：http://localhost:8999/media/avatars/username/uuid-filename.jpg
     * 提取为：avatars/username/uuid-filename.jpg
     */
    private String extractObjectNameFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        
        try {
            // 查找桶名在URL中的位置
            String bucketName = "media"; // MinIO配置中的桶名
            int bucketIndex = url.indexOf("/" + bucketName + "/");
            
            if (bucketIndex != -1) {
                // +桶名长度+2，是为了跳过"/桶名/"
                return url.substring(bucketIndex + bucketName.length() + 2);
            }
            
            // 如果使用特殊格式，尝试直接从路径中提取
            String[] parts = url.split("/");
            if (parts.length >= 2) {
                // 假设最后两部分是路径，如：avatars/username/uuid-filename.jpg
                return String.join("/", parts[parts.length - 3], parts[parts.length - 2], parts[parts.length - 1]);
            }
            
            log.warn("无法从URL中提取对象名: {}", url);
            return null;
        } catch (Exception e) {
            log.error("提取对象名出错: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 上传并更新用户头像
     */
    @Override
    @Transactional
    public Map<String, String> uploadAndUpdateAvatar(String username, MultipartFile file) throws IOException {
        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(400, "只支持上传图片文件");
        }
        
        // 检查文件大小（最大2MB）
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new BusinessException(400, "文件大小不能超过2MB");
        }
        
        // 生成唯一的对象名
        String objectName = "avatars/" + username + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();
        
        // 上传到MinIO
        String avatarUrl = minioService.uploadFile(objectName, file.getInputStream(), file.getContentType());
        
        // 更新用户头像
        updateAvatar(username, avatarUrl);
        
        Map<String, String> result = new HashMap<>();
        result.put("avatarUrl", avatarUrl);
        return result;
    }
    
    /**
     * 更新当前用户邮箱
     *
     * @param username 当前登录用户名
     * @param newEmail 新邮箱
     * @param emailCode 邮箱验证码
     * @param password 当前密码 (用于安全验证)
     * @return 更新后的用户信息
     */
    @Override
    @Transactional
    public UserVO updateEmail(String username, String newEmail, String emailCode, String password) {
        User user = getUserByUsername(username);
        
        // 验证用户密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("密码不正确");
        }
        
        // 检查邮箱是否已被其他用户使用
        if (existsByEmail(newEmail)) {
            throw new BusinessException("邮箱已存在");
        }
        
        // 验证邮箱验证码
        boolean isValid = emailService.validateVerificationCode(newEmail, emailCode);
        if (!isValid) {
            throw new BusinessException("邮箱验证码不正确或已过期");
        }
        
        // 更新邮箱
        user.setEmail(newEmail);
        User updatedUser = userRepository.save(user);
        log.info("用户邮箱更新成功: {}, 新邮箱: {}", username, newEmail);
        return convertToUserVO(updatedUser);
    }
    
    /**
     * 获取用户基本信息（用于前端展示）
     *
     * @param userId 用户ID
     * @return 用户基本信息
     */
    @Override
    @Transactional(readOnly = true)
    public UserVO getBasicUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        // 创建基本信息VO，不包含敏感信息
        return UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .build();
    }

    /**
     * 更新用户角色
     *
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     */
    @Override
    @Transactional
    public void updateUserRoles(Long userId, Set<Long> roleIds) {
        // 获取用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 获取角色
        Set<Role> roles = roleIds.stream()
                .map(roleId -> roleRepository.findById(roleId)
                        .orElseThrow(() -> new BusinessException("角色不存在: " + roleId)))
                .collect(Collectors.toSet());

        // 更新用户角色
        user.setRoles(roles);
        userRepository.save(user);

        // 使该用户的所有token失效
        tokenBlacklistService.invalidateUserTokens(user.getUsername());
        log.info("用户 {} 的角色已更新，所有token已失效", user.getUsername());
    }

    /**
     * 更新用户密码
     *
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    @Override
    @Transactional
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        // 获取用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // 使该用户的所有token失效
        tokenBlacklistService.invalidateUserTokens(user.getUsername());
        log.info("用户 {} 的密码已更新，所有token已失效", user.getUsername());
    }

    /**
     * 获取用户统计数据
     *
     * @return 用户统计数据
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.USER_STATS_CACHE, key = "'all'")
    public UserStatsVO getUserStats() {
        // 创建包含所有统计数据的综合VO
        return UserStatsVO.builder()
                .roleDistribution(getUserRoleDistribution())
                .growthStats(getUserGrowthStats())
                .statusStats(getUserStatusStats())
                .activityStats(getUserActivityStats())
                .build();
    }

    /**
     * 获取用户角色分布统计
     *
     * @return 用户角色分布统计数据
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.USER_STATS_CACHE, key = "'roleDistribution'")
    public UserRoleDistributionVO getUserRoleDistribution() {
        // 获取总用户数
        long totalUserCount = userRepository.count();
        
        // 获取所有角色
        List<Role> allRoles = roleRepository.findAll();
        
        // 计算每个角色的用户分布
        List<UserRoleDistributionVO.RoleDistribution> roleDistributions = new ArrayList<>();
        
        for (Role role : allRoles) {
            // 统计该角色的用户数量
            long userCount = userRepository.countByRoleId(role.getId());
            
            // 计算百分比
            double percentage = totalUserCount > 0 
                ? (double) userCount / totalUserCount * 100 
                : 0;
            
            // 创建角色分布对象
            UserRoleDistributionVO.RoleDistribution distribution = UserRoleDistributionVO.RoleDistribution.builder()
                    .roleId(role.getId())
                    .roleName(role.getName())
                    .roleCode(role.getCode())
                    .userCount(userCount)
                    .percentage(Math.round(percentage * 100) / 100.0) // 保留两位小数
                    .build();
            
            roleDistributions.add(distribution);
        }
        
        // 创建并返回角色分布VO
        return UserRoleDistributionVO.builder()
                .totalUserCount(totalUserCount)
                .roleDistributions(roleDistributions)
                .build();
    }

    /**
     * 获取用户增长统计数据
     *
     * @return 用户增长统计数据
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.USER_STATS_CACHE, key = "'growthStats'")
    public UserGrowthStatsVO getUserGrowthStats() {
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        
        // 计算今日、本周、本月的开始时间
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
        LocalDateTime weekStart = now.toLocalDate().minusDays(now.getDayOfWeek().getValue() - 1).atStartOfDay();
        LocalDateTime monthStart = now.toLocalDate().withDayOfMonth(1).atStartOfDay();
        
        // 计算前一天、前一周、前一月的开始时间（用于计算增长率）
        LocalDateTime yesterdayStart = todayStart.minusDays(1);
        LocalDateTime lastWeekStart = weekStart.minusWeeks(1);
        LocalDateTime lastMonthStart = monthStart.minusMonths(1);
        
        // 获取总用户数
        long totalUserCount = userRepository.count();
        
        // 获取今日、本周、本月新增用户数
        long todayNewUsers = userRepository.countByCreatedAtBetween(todayStart, now);
        long weekNewUsers = userRepository.countByCreatedAtBetween(weekStart, now);
        long monthNewUsers = userRepository.countByCreatedAtBetween(monthStart, now);
        
        // 获取前一天、前一周、前一月的新增用户数
        long yesterdayNewUsers = userRepository.countByCreatedAtBetween(yesterdayStart, todayStart);
        long lastWeekNewUsers = userRepository.countByCreatedAtBetween(lastWeekStart, weekStart);
        long lastMonthNewUsers = userRepository.countByCreatedAtBetween(lastMonthStart, monthStart);
        
        // 计算增长率
        double dailyGrowthRate = yesterdayNewUsers > 0 
                ? (double) (todayNewUsers - yesterdayNewUsers) / yesterdayNewUsers * 100
                : (todayNewUsers > 0 ? 100 : 0);
        
        double weeklyGrowthRate = lastWeekNewUsers > 0 
                ? (double) (weekNewUsers - lastWeekNewUsers) / lastWeekNewUsers * 100
                : (weekNewUsers > 0 ? 100 : 0);
        
        double monthlyGrowthRate = lastMonthNewUsers > 0 
                ? (double) (monthNewUsers - lastMonthNewUsers) / lastMonthNewUsers * 100
                : (monthNewUsers > 0 ? 100 : 0);
        
        // 获取过去30天的用户注册数据
        LocalDateTime thirtyDaysAgo = now.minusDays(30);
        List<Object[]> registrationData = userRepository.countUserRegistrationsByDateRange(thirtyDaysAgo, now);
        
        List<UserGrowthStatsVO.DailyRegistration> dailyRegistrations = registrationData.stream()
                .map(data -> UserGrowthStatsVO.DailyRegistration.builder()
                        .date((String) data[0])
                        .count(((Number) data[1]).longValue())
                        .build())
                .collect(Collectors.toList());
        
        // 创建并返回用户增长统计VO
        return UserGrowthStatsVO.builder()
                .totalUserCount(totalUserCount)
                .todayNewUsers(todayNewUsers)
                .weekNewUsers(weekNewUsers)
                .monthNewUsers(monthNewUsers)
                .dailyGrowthRate(Math.round(dailyGrowthRate * 100) / 100.0) // 保留两位小数
                .weeklyGrowthRate(Math.round(weeklyGrowthRate * 100) / 100.0)
                .monthlyGrowthRate(Math.round(monthlyGrowthRate * 100) / 100.0)
                .dailyRegistrations(dailyRegistrations)
                .build();
    }

    /**
     * 获取用户状态统计数据
     *
     * @return 用户状态统计数据
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.USER_STATS_CACHE, key = "'statusStats'")
    public UserStatusStatsVO getUserStatusStats() {
        // 获取总用户数
        long totalUserCount = userRepository.count();
        
        // 获取活跃用户数（状态为1）
        long activeUserCount = userRepository.countByStatus(1);
        
        // 获取禁用用户数（状态为0）
        long disabledUserCount = userRepository.countByStatus(0);
        
        // 计算百分比
        double activeUserPercentage = totalUserCount > 0 
                ? (double) activeUserCount / totalUserCount * 100 
                : 0;
        
        double disabledUserPercentage = totalUserCount > 0 
                ? (double) disabledUserCount / totalUserCount * 100 
                : 0;
        
        // 创建并返回用户状态统计VO
        return UserStatusStatsVO.builder()
                .totalUserCount(totalUserCount)
                .activeUserCount(activeUserCount)
                .disabledUserCount(disabledUserCount)
                .activeUserPercentage(Math.round(activeUserPercentage * 100) / 100.0) // 保留两位小数
                .disabledUserPercentage(Math.round(disabledUserPercentage * 100) / 100.0)
                .build();
    }

    /**
     * 获取用户活跃度统计数据
     *
     * @return 用户活跃度统计数据
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.USER_STATS_CACHE, key = "'activityStats'")
    public UserActivityStatsVO getUserActivityStats() {
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        
        // 计算过去一天、一周、一个月的时间
        LocalDateTime oneDayAgo = now.minusDays(1);
        LocalDateTime oneWeekAgo = now.minusWeeks(1);
        LocalDateTime oneMonthAgo = now.minusMonths(1);
        
        // 获取总用户数
        long totalUserCount = userRepository.count();
        
        // 获取活跃用户数（过去30天有登录记录）
        long activeUserCount = userRepository.countByLastLoginAtAfter(oneMonthAgo);
        
        // 计算非活跃用户数
        long inactiveUserCount = totalUserCount - activeUserCount;
        
        // 获取今日、最近7天、最近30天活跃用户数
        long todayActiveUsers = userRepository.countByLastLoginAtAfter(oneDayAgo);
        long weekActiveUsers = userRepository.countByLastLoginAtAfter(oneWeekAgo);
        long monthActiveUsers = activeUserCount;
        
        // 计算活跃用户占比
        double activeUserPercentage = totalUserCount > 0 
                ? (double) activeUserCount / totalUserCount * 100 
                : 0;
        
        // 获取过去30天的用户活跃数据
        List<Object[]> activityData = userRepository.countUserActivityByDateRange(oneMonthAgo, now);
        
        List<UserActivityStatsVO.DailyActiveUsers> dailyActiveUsers = activityData.stream()
                .map(data -> UserActivityStatsVO.DailyActiveUsers.builder()
                        .date((String) data[0])
                        .count(((Number) data[1]).longValue())
                        .build())
                .collect(Collectors.toList());
        
        // 获取每小时的用户活跃分布（过去7天）
        Map<Integer, Long> hourlyActiveDistribution = new HashMap<>();
        List<Object[]> hourlyData = userRepository.countUserLoginByHourOfDay(oneWeekAgo, now);
        
        for (Object[] data : hourlyData) {
            Integer hour = ((Number) data[0]).intValue();
            Long count = ((Number) data[1]).longValue();
            hourlyActiveDistribution.put(hour, count);
        }
        
        // 获取每周几的用户活跃分布（过去30天）
        Map<Integer, Long> weekdayActiveDistribution = new HashMap<>();
        List<Object[]> weekdayData = userRepository.countUserLoginByDayOfWeek(oneMonthAgo, now);
        
        for (Object[] data : weekdayData) {
            Integer weekday = ((Number) data[0]).intValue();
            Long count = ((Number) data[1]).longValue();
            weekdayActiveDistribution.put(weekday, count);
        }
        
        // 创建并返回用户活跃度统计VO
        return UserActivityStatsVO.builder()
                .totalUserCount(totalUserCount)
                .activeUserCount(activeUserCount)
                .inactiveUserCount(inactiveUserCount)
                .todayActiveUsers(todayActiveUsers)
                .weekActiveUsers(weekActiveUsers)
                .monthActiveUsers(monthActiveUsers)
                .activeUserPercentage(Math.round(activeUserPercentage * 100) / 100.0) // 保留两位小数
                .dailyActiveUsers(dailyActiveUsers)
                .hourlyActiveDistribution(hourlyActiveDistribution)
                .weekdayActiveDistribution(weekdayActiveDistribution)
                .build();
    }
} 