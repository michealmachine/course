package com.zhangziqi.online_course_mine.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 学习进度更新DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningProgressUpdateDTO {

    /**
     * 当前学习章节ID
     */
    private Long chapterId;

    /**
     * 当前学习小节ID
     */
    private Long sectionId;

    /**
     * 当前小节学习进度（百分比）
     */
    private Integer sectionProgress;

    /**
     * 是否为复习模式
     * 复习模式下不更新总体进度
     */
    @Builder.Default
    private Boolean isReviewing = false;
}