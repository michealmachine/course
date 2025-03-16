package com.zhangziqi.online_course_mine.model.vo;

import com.zhangziqi.online_course_mine.model.entity.SectionResource;
import com.zhangziqi.online_course_mine.model.enums.ResourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 小节资源视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionResourceVO {
    
    /**
     * 资源ID
     */
    private Long id;
    
    /**
     * 小节ID
     */
    private Long sectionId;
    
    /**
     * 媒体资源ID
     */
    private Long mediaId;
    
    /**
     * 关联的媒体资源
     */
    private MediaVO media;
    
    /**
     * 资源类型(primary, supplementary, homework, reference)
     */
    private String resourceType;
    
    /**
     * 排序索引
     */
    private Integer orderIndex;
    
    /**
     * 创建时间
     */
    private Long createdTime;
    
    /**
     * 更新时间
     */
    private Long updatedTime;
    
    /**
     * 将实体转换为VO
     */
    public static SectionResourceVO fromEntity(SectionResource entity) {
        if (entity == null) {
            return null;
        }
        
        MediaVO mediaVO = null;
        if (entity.getMedia() != null) {
            mediaVO = MediaVO.builder()
                .id(entity.getMedia().getId())
                .title(entity.getMedia().getTitle())
                .description(entity.getMedia().getDescription())
                .type(entity.getMedia().getType() != null ? entity.getMedia().getType().name() : null)
                .size(entity.getMedia().getSize())
                .originalFilename(entity.getMedia().getOriginalFilename())
                .status(entity.getMedia().getStatus() != null ? entity.getMedia().getStatus().name() : null)
                .institutionId(entity.getMedia().getInstitution() != null ? entity.getMedia().getInstitution().getId() : null)
                .uploaderId(entity.getMedia().getUploaderId())
                .uploadTime(entity.getMedia().getUploadTime())
                .lastAccessTime(entity.getMedia().getLastAccessTime())
                .accessUrl(entity.getMedia().getStoragePath())
                .build();
        }
        
        return SectionResourceVO.builder()
            .id(entity.getId())
            .sectionId(entity.getSectionId())
            .mediaId(entity.getMediaId())
            .media(mediaVO)
            .resourceType(entity.getResourceType())
            .orderIndex(entity.getOrderIndex())
            .createdTime(entity.getCreatedAt() != null ? entity.getCreatedAt().toEpochSecond(java.time.ZoneOffset.UTC) : null)
            .updatedTime(entity.getUpdatedAt() != null ? entity.getUpdatedAt().toEpochSecond(java.time.ZoneOffset.UTC) : null)
            .build();
    }
} 