package com.zhangziqi.online_course_mine.security;

import com.zhangziqi.online_course_mine.model.entity.User;
import com.zhangziqi.online_course_mine.model.entity.Role;
import com.zhangziqi.online_course_mine.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户详情服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * 根据用户名加载用户
     *
     * @param username 用户名
     * @return 用户详情
     * @throws UsernameNotFoundException 用户名不存在
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("开始加载用户: {}", username);

        // 查询用户
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

        // 状态检查
        if (user.getStatus() != 1) {
            log.warn("用户已被禁用: {}", username);
            throw new UsernameNotFoundException("用户已被禁用");
        }

        // 获取用户的角色和权限
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        
        // 只处理第一个角色（每个用户只使用一个主要角色）
        if (!user.getRoles().isEmpty()) {
            Role role = user.getRoles().iterator().next();
            String roleCode = role.getCode();
            
            // 添加角色
            authorities.add(new SimpleGrantedAuthority(roleCode));
            
            // 添加该角色拥有的所有权限
            role.getPermissions().forEach(permission -> 
                authorities.add(new SimpleGrantedAuthority(permission.getCode()))
            );
            
            log.debug("用户 '{}' 使用角色: {}", username, roleCode);
        }

        log.debug("用户 '{}' 拥有的权限: {}", username, authorities);
        
        // 构建标准的Spring Security User对象，只使用标准属性
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(user.getStatus() != 1)
                .build();
    }
} 