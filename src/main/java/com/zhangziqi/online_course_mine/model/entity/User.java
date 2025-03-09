package com.zhangziqi.online_course_mine.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 用户实体类
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@Table(name = "users")
public class User extends BaseEntity {

    /**
     * 用户名
     */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /**
     * 密码
     */
    @Column(nullable = false)
    private String password;

    /**
     * 邮箱
     */
    @Column(unique = true)
    private String email;

    /**
     * 手机号
     */
    @Column(unique = true)
    private String phone;

    /**
     * 头像
     */
    @Column(length = 255)
    private String avatar;

    /**
     * 昵称
     */
    @Column(length = 50)
    private String nickname;

    /**
     * 状态（0-禁用，1-正常）
     */
    @Builder.Default
    private Integer status = 1;

    /**
     * 机构ID（仅机构用户）
     */
    private Long institutionId;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginAt;
    
    /**
     * 用户角色
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
} 