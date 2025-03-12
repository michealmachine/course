package com.zhangziqi.online_course_mine.model.entity;

import com.zhangziqi.online_course_mine.model.enums.ContentType;
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
 * 课程小节实体类
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sections")
@EqualsAndHashCode(callSuper = true)
public class Section extends BaseEntity {

    /**
     * 小节标题
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * 小节描述
     */
    @Column(length = 1000)
    private String description;

    /**
     * 排序索引
     */
    private Integer orderIndex;

    /**
     * 内容类型
     */
    @Column(length = 20)
    private String contentType;

    /**
     * 所属章节
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;

    /**
     * 小节资源
     */
    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SectionResource> resources = new ArrayList<>();

    /**
     * 获取章节ID
     */
    public Long getChapterId() {
        return chapter != null ? chapter.getId() : null;
    }

    /**
     * 设置章节ID
     */
    public void setChapterId(Long chapterId) {
        if (chapterId != null) {
            this.chapter = Chapter.builder().id(chapterId).build();
        } else {
            this.chapter = null;
        }
    }
    
    /**
     * 获取内容类型枚举
     */
    @Transient
    public ContentType getContentTypeEnum() {
        return ContentType.getByCode(this.contentType);
    }
    
    /**
     * 设置内容类型枚举
     */
    public void setContentTypeEnum(ContentType contentType) {
        this.contentType = contentType != null ? contentType.getCode() : null;
    }
} 