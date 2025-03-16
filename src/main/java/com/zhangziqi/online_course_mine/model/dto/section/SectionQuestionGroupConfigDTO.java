package com.zhangziqi.online_course_mine.model.dto.section;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

/**
 * 小节题目组配置数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionQuestionGroupConfigDTO {
    
    /**
     * 是否随机题目顺序
     */
    @Builder.Default
    private Boolean randomOrder = false;
    
    /**
     * 是否按难度顺序排序
     */
    @Builder.Default
    private Boolean orderByDifficulty = false;
    
    /**
     * 是否显示答案解析
     */
    @Builder.Default
    private Boolean showAnalysis = true;
} 