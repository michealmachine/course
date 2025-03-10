package com.zhangziqi.online_course_mine.security;

import com.zhangziqi.online_course_mine.model.entity.User;
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
import java.util.stream.Collectors;

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
        log.debug("根据用户名查询用户: {}", username);

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
        
        // 添加角色（由于Spring Security的hasRole会自动添加ROLE_前缀，这里需要适配）
        user.getRoles().forEach(role -> {
            String roleCode = role.getCode();
            
            // 如果角色编码已经以ROLE_开头，需要处理以适配hasRole()
            if (roleCode.startsWith("ROLE_")) {
                // 直接添加带前缀的完整角色编码
                authorities.add(new SimpleGrantedAuthority(roleCode));
                
                // 同时添加不带前缀的版本，以适配hasRole()方法
                authorities.add(new SimpleGrantedAuthority(roleCode.substring("ROLE_".length())));
            } else {
                // 不以ROLE_开头，添加原始编码
                authorities.add(new SimpleGrantedAuthority(roleCode));
                // 添加带ROLE_前缀的版本
                authorities.add(new SimpleGrantedAuthority("ROLE_" + roleCode));
            }
            
            // 添加该角色拥有的所有权限
            role.getPermissions().forEach(permission -> 
                authorities.add(new SimpleGrantedAuthority(permission.getCode()))
            );
        });

        log.debug("用户 '{}' 拥有的权限: {}", username, authorities);
        
        // 构建UserDetails对象
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .accountExpired(false)
                .authorities(authorities)
                .build();
    }
} 