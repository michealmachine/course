package com.zhangziqi.online_course_mine.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

/**
 * 角色实体类
 */
@Getter
@Setter
@ToString(exclude = {"permissions"})
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "roles")
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Role extends BaseEntity {

    /**
     * 角色名称
     */
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    /**
     * 角色编码
     */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /**
     * 角色描述
     */
    @Column(length = 255)
    private String description;

    /**
     * 角色权限
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();

    /**
     * 自定义equals方法，只比较ID
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return getId() != null && getId().equals(role.getId());
    }

    /**
     * 自定义hashCode方法，只使用ID
     */
    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }
} 