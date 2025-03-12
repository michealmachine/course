package com.zhangziqi.online_course_mine.model.entity;

import com.zhangziqi.online_course_mine.model.enums.ResourceType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 小节资源实体类
 */
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "section_resources")
@EqualsAndHashCode(callSuper = true)
public class SectionResource extends BaseEntity {

    /**
     * 所属小节
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    /**
     * 关联的媒体资源
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_id", nullable = false)
    private Media media;

    /**
     * 资源类型
     */
    @Column(length = 20)
    private String resourceType;

    /**
     * 排序索引
     */
    private Integer orderIndex;

    /**
     * 获取小节ID
     */
    public Long getSectionId() {
        return section != null ? section.getId() : null;
    }

    /**
     * 设置小节ID
     */
    public void setSectionId(Long sectionId) {
        if (sectionId != null) {
            this.section = Section.builder().id(sectionId).build();
        } else {
            this.section = null;
        }
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
        } else {
            this.media = null;
        }
    }
    
    /**
     * 获取资源类型枚举
     */
    @Transient
    public ResourceType getResourceTypeEnum() {
        return ResourceType.getByCode(this.resourceType);
    }
    
    /**
     * 设置资源类型枚举
     */
    public void setResourceTypeEnum(ResourceType resourceType) {
        this.resourceType = resourceType != null ? resourceType.getCode() : null;
    }
} 