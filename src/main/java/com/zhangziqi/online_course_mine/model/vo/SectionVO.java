package com.zhangziqi.online_course_mine.model.vo;

import com.zhangziqi.online_course_mine.model.entity.Section;
import com.zhangziqi.online_course_mine.model.enums.ContentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 小节视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionVO {
    
    /**
     * 小节ID
     */
    private Long id;
    
    /**
     * 小节标题
     */
    private String title;
    
    /**
     * 小节描述
     */
    private String description;
    
    /**
     * 排序索引
     */
    private Integer orderIndex;
    
    /**
     * 内容类型
     */
    private String contentType;
    
    /**
     * 所属章节ID
     */
    private Long chapterId;
    
    /**
     * 章节标题（可选）
     */
    private String chapterTitle;
    
    /**
     * 资源类型鉴别器：MEDIA, QUESTION_GROUP, NONE
     */
    private String resourceTypeDiscriminator;
    
    /**
     * 直接关联的媒体资源（仅当resourceTypeDiscriminator为MEDIA时有效）
     */
    private MediaVO media;
    
    /**
     * 媒体资源ID（仅当resourceTypeDiscriminator为MEDIA时有效）
     */
    private Long mediaId;
    
    /**
     * 媒体资源类型(primary, supplementary, homework, reference)
     */
    private String mediaResourceType;
    
    /**
     * 直接关联的题目组（仅当resourceTypeDiscriminator为QUESTION_GROUP时有效）
     */
    private QuestionGroupVO questionGroup;
    
    /**
     * 题目组ID（仅当resourceTypeDiscriminator为QUESTION_GROUP时有效）
     */
    private Long questionGroupId;
    
    /**
     * 是否随机题目顺序（仅当resourceTypeDiscriminator为QUESTION_GROUP时有效）
     */
    private Boolean randomOrder;
    
    /**
     * 是否按难度顺序排序（仅当resourceTypeDiscriminator为QUESTION_GROUP时有效）
     */
    private Boolean orderByDifficulty;
    
    /**
     * 是否显示答案解析（仅当resourceTypeDiscriminator为QUESTION_GROUP时有效）
     */
    private Boolean showAnalysis;
    


    
    /**
     * 创建时间
     */
    private Long createdTime;
    
    /**
     * 更新时间
     */
    private Long updatedTime;
    
    /**
     * 将实体转换为VO（基本信息）
     */
    public static SectionVO fromEntity(Section entity) {
        if (entity == null) {
            return null;
        }
        
        SectionVO.SectionVOBuilder builder = SectionVO.builder()
            .id(entity.getId())
            .title(entity.getTitle())
            .description(entity.getDescription())
            .orderIndex(entity.getOrderIndex())
            .contentType(entity.getContentType())
            .chapterId(entity.getChapterId())
            .chapterTitle(entity.getChapter() != null ? entity.getChapter().getTitle() : null)
            .resourceTypeDiscriminator(entity.getResourceTypeDiscriminator())
            .mediaId(entity.getMediaId())
            .mediaResourceType(entity.getMediaResourceType())
            .questionGroupId(entity.getQuestionGroupId())
            .randomOrder(entity.getRandomOrder())
            .orderByDifficulty(entity.getOrderByDifficulty())
            .showAnalysis(entity.getShowAnalysis())
            .createdTime(entity.getCreatedAt() != null ? entity.getCreatedAt().toEpochSecond(java.time.ZoneOffset.UTC) : null)
            .updatedTime(entity.getUpdatedAt() != null ? entity.getUpdatedAt().toEpochSecond(java.time.ZoneOffset.UTC) : null);
        
        // 处理媒体资源
        if (entity.getMedia() != null) {
            MediaVO mediaVO = MediaVO.builder()
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
            builder.media(mediaVO);
        }
        
        // 处理题目组
        if (entity.getQuestionGroup() != null) {
            QuestionGroupVO questionGroupVO = QuestionGroupVO.builder()
                .id(entity.getQuestionGroup().getId())
                .name(entity.getQuestionGroup().getName())
                .description(entity.getQuestionGroup().getDescription())
                .institutionId(entity.getQuestionGroup().getInstitutionId())
                .questionCount((long) (entity.getQuestionGroup().getItems() != null ? entity.getQuestionGroup().getItems().size() : 0))
                .creatorId(entity.getQuestionGroup().getCreatorId())
                .creatorName(entity.getQuestionGroup().getCreatorName())
                .createdTime(entity.getQuestionGroup().getCreatedTime())
                .updatedTime(entity.getQuestionGroup().getUpdatedTime())
                .build();
            builder.questionGroup(questionGroupVO);
        }
        
        return builder.build();
    }

    
    /**
     * 获取内容类型枚举
     */
    public ContentType getContentTypeEnum() {
        return ContentType.getByCode(this.contentType);
    }
} 