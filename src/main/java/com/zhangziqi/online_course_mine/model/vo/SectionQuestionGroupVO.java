package com.zhangziqi.online_course_mine.model.vo;

import com.zhangziqi.online_course_mine.model.entity.SectionQuestionGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 小节题目组关联视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionQuestionGroupVO {
    
    /**
     * ID
     */
    private Long id;
    
    /**
     * 小节ID
     */
    private Long sectionId;
    
    /**
     * 题目组ID
     */
    private Long questionGroupId;
    
    /**
     * 题目组信息
     */
    private QuestionGroupVO questionGroup;
    
    /**
     * 排序索引
     */
    private Integer orderIndex;
    
    /**
     * 是否随机题目顺序
     */
    private Boolean randomOrder;
    
    /**
     * 是否按难度顺序排序
     */
    private Boolean orderByDifficulty;
    
    /**
     * 是否显示答案解析
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
     * 将实体转换为VO
     */
    public static SectionQuestionGroupVO fromEntity(SectionQuestionGroup entity) {
        if (entity == null) {
            return null;
        }
        
        QuestionGroupVO questionGroupVO = null;
        if (entity.getQuestionGroup() != null) {
            questionGroupVO = QuestionGroupVO.builder()
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
        }
        
        return SectionQuestionGroupVO.builder()
            .id(entity.getId())
            .sectionId(entity.getSectionId())
            .questionGroupId(entity.getQuestionGroupId())
            .questionGroup(questionGroupVO)
            .orderIndex(entity.getOrderIndex())
            .randomOrder(entity.getRandomOrder())
            .orderByDifficulty(entity.getOrderByDifficulty())
            .showAnalysis(entity.getShowAnalysis())
            .createdTime(entity.getCreatedAt() != null ? entity.getCreatedAt().toEpochSecond(java.time.ZoneOffset.UTC) : null)
            .updatedTime(entity.getUpdatedAt() != null ? entity.getUpdatedAt().toEpochSecond(java.time.ZoneOffset.UTC) : null)
            .build();
    }
} 