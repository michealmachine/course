package com.zhangziqi.online_course_mine.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 用户实体类
 */
@Getter
@Setter
@ToString(exclude = {"favorites", "purchasedCourses", "orders", "roles", "institution"})
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
     * 姓名
     */
    @Column(length = 50)
    private String name;

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
     * 所属机构（仅机构用户）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id")
    private Institution institution;

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
    
    /**
     * 用户收藏的课程
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<UserFavorite> favorites = new HashSet<>();
    
    /**
     * 用户购买的课程
     */
    @OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Builder.Default
    private Set<UserCourse> purchasedCourses = new HashSet<>();

    /**
     * 用户的订单
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Order> orders = new ArrayList<>();

    /**
     * 获取机构ID
     */
    public Long getInstitutionId() {
        return institution != null ? institution.getId() : null;
    }

    /**
     * 设置机构ID
     */
    public void setInstitutionId(Long institutionId) {
        if (institutionId != null) {
            this.institution = Institution.builder().id(institutionId).build();
        } else {
            this.institution = null;
        }
    }

    /**
     * 判断用户是否启用
     */
    public boolean getEnabled() {
        return status != null && status == 1;
    }

    /**
     * 用于Builder模式的机构ID设置
     */
    public abstract static class UserBuilder<C extends User, B extends UserBuilder<C, B>> extends BaseEntity.BaseEntityBuilder<C, B> {
        /**
         * 设置机构ID
         */
        public B institutionId(Long institutionId) {
            if (institutionId != null) {
                this.institution(Institution.builder().id(institutionId).build());
            }
            return self();
        }
    }

    /**
     * 自定义equals方法，只比较ID
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return getId() != null && getId().equals(user.getId());
    }

    /**
     * 自定义hashCode方法，只使用ID
     */
    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }
} 