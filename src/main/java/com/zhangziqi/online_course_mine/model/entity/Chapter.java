package com.zhangziqi.online_course_mine.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * 课程章节实体类
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "chapters")
@EqualsAndHashCode(callSuper = true)
public class Chapter extends BaseEntity {

    /**
     * 章节标题
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * 章节描述
     */
    @Column(length = 1000)
    private String description;

    /**
     * 排序索引
     */
    private Integer orderIndex;

    /**
     * 所属课程
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    /**
     * 章节小节
     */
    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<Section> sections = new ArrayList<>();

    /**
     * 获取课程ID
     */
    public Long getCourseId() {
        return course != null ? course.getId() : null;
    }

    /**
     * 设置课程ID
     */
    public void setCourseId(Long courseId) {
        if (courseId != null) {
            this.course = Course.builder().id(courseId).build();
        } else {
            this.course = null;
        }
    }
} 