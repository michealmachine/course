package com.zhangziqi.online_course_mine.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

/**
 * 标签实体类
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tags")
@EqualsAndHashCode(callSuper = true)
public class Tag extends BaseEntity {

    /**
     * 标签名称
     */
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    /**
     * 标签描述
     */
    @Column(length = 255)
    private String description;

    /**
     * 使用次数
     */
    @Builder.Default
    private Integer useCount = 0;

    /**
     * 关联的课程
     */
    @JsonIgnore
    @ManyToMany(mappedBy = "tags")
    @Builder.Default
    private Set<Course> courses = new HashSet<>();
    
    /**
     * 增加使用次数
     */
    public void incrementUseCount() {
        if (this.useCount == null) {
            this.useCount = 1;
        } else {
            this.useCount++;
        }
    }
    
    /**
     * 减少使用次数
     */
    public void decrementUseCount() {
        if (this.useCount != null && this.useCount > 0) {
            this.useCount--;
        }
    }
} 