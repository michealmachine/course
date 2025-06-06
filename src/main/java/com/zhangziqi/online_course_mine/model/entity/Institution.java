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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 教育机构实体类
 */
@Getter
@Setter
@ToString(exclude = {"courses", "users", "questions", "orders"})
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "institutions")
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Institution extends BaseEntity {

    /**
     * 机构名称
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 机构Logo
     */
    private String logo;

    /**
     * 机构描述
     */
    @Column(length = 500)
    private String description;

    /**
     * 状态（0-待审核，1-正常，2-禁用）
     */
    @Builder.Default
    private Integer status = 0;

    /**
     * 机构注册码
     */
    @Column(unique = true, length = 20)
    private String registerCode;

    /**
     * 联系人
     */
    @Column(length = 50)
    private String contactPerson;

    /**
     * 联系电话
     */
    @Column(length = 20)
    private String contactPhone;

    /**
     * 联系邮箱
     */
    private String contactEmail;

    /**
     * 地址
     */
    @Column(length = 255)
    private String address;
    
    /**
     * 机构媒体资源
     */
    @OneToMany(mappedBy = "institution", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Media> mediaList = new ArrayList<>();
    
    /**
     * 机构存储配额
     */
    @OneToMany(mappedBy = "institution", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<StorageQuota> quotas = new ArrayList<>();
    
    /**
     * 机构课程列表
     */
    @OneToMany(mappedBy = "institution", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Course> courses = new ArrayList<>();

    /**
     * 机构用户
     */
    @OneToMany(mappedBy = "institution", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<User> users = new HashSet<>();

    /**
     * 关联的题库
     */
    @OneToMany(mappedBy = "institution", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Question> questions = new HashSet<>();

    /**
     * 机构课程订单
     */
    @OneToMany(mappedBy = "institution", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Order> orders = new ArrayList<>();

    /**
     * 自定义equals方法，只比较ID
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Institution that = (Institution) o;
        return getId() != null && getId().equals(that.getId());
    }

    /**
     * 自定义hashCode方法，只使用ID
     */
    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }
} 