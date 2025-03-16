package com.zhangziqi.online_course_mine.model.entity;

import com.zhangziqi.online_course_mine.model.enums.ContentType;
import com.zhangziqi.online_course_mine.model.enums.ResourceType;
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
     * 资源类型鉴别器：MEDIA, QUESTION_GROUP, NONE
     */
    @Column(name = "resource_type_discriminator", length = 20)
    private String resourceTypeDiscriminator;
    
    /**
     * 媒体资源（仅当resourceTypeDiscriminator为MEDIA时有效）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_id")
    private Media media;
    
    /**
     * 媒体资源类型(primary, supplementary, homework, reference)
     */
    @Column(name = "media_resource_type", length = 20)
    private String mediaResourceType;
    
    /**
     * 题目组（仅当resourceTypeDiscriminator为QUESTION_GROUP时有效）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_group_id")
    private QuestionGroup questionGroup;
    
    /**
     * 是否随机题目顺序（仅当resourceTypeDiscriminator为QUESTION_GROUP时有效）
     */
    @Builder.Default
    private Boolean randomOrder = false;
    
    /**
     * 是否按难度顺序排序（仅当resourceTypeDiscriminator为QUESTION_GROUP时有效）
     */
    @Builder.Default
    private Boolean orderByDifficulty = false;
    
    /**
     * 是否显示答案解析（仅当resourceTypeDiscriminator为QUESTION_GROUP时有效）
     */
    @Builder.Default
    private Boolean showAnalysis = true;

    /**
     * 小节资源 (已弃用，保留向后兼容)
     * @deprecated 使用直接关联的媒体资源或题目组替代
     */
    @Deprecated
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
    
    /**
     * 获取媒体ID
     */
    public Long getMediaId() {
        return media != null ? media.getId() : null;
    }
    
    /**
     * 设置媒体ID
     */
    public void setMediaId(Long mediaId) {
        if (mediaId != null) {
            this.media = Media.builder().id(mediaId).build();
            this.resourceTypeDiscriminator = "MEDIA";
        } else if ("MEDIA".equals(this.resourceTypeDiscriminator)) {
            this.media = null;
            this.resourceTypeDiscriminator = "NONE";
        }
    }
    
    /**
     * 获取题目组ID
     */
    public Long getQuestionGroupId() {
        return questionGroup != null ? questionGroup.getId() : null;
    }
    
    /**
     * 设置题目组ID
     */
    public void setQuestionGroupId(Long questionGroupId) {
        if (questionGroupId != null) {
            this.questionGroup = QuestionGroup.builder().id(questionGroupId).build();
            this.resourceTypeDiscriminator = "QUESTION_GROUP";
        } else if ("QUESTION_GROUP".equals(this.resourceTypeDiscriminator)) {
            this.questionGroup = null;
            this.resourceTypeDiscriminator = "NONE";
        }
    }
    
    /**
     * 获取媒体资源类型枚举
     */
    @Transient
    public ResourceType getMediaResourceTypeEnum() {
        return ResourceType.getByCode(this.mediaResourceType);
    }
    
    /**
     * 设置媒体资源类型枚举
     */
    public void setMediaResourceTypeEnum(ResourceType resourceType) {
        this.mediaResourceType = resourceType != null ? resourceType.getCode() : null;
    }
} 