package com.zhangziqi.online_course_mine.model.dto.section;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

/**
 * 小节题目组关联数据传输对象
 * 
 * @deprecated 此类已弃用，推荐使用 {@link com.zhangziqi.online_course_mine.service.SectionService#setQuestionGroup(Long, Long, SectionQuestionGroupConfigDTO)} 方法
 */
@Deprecated
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionQuestionGroupDTO {
    
    /**
     * 小节ID
     */
    @NotNull(message = "小节ID不能为空")
    private Long sectionId;
    
    /**
     * 题目组ID
     */
    @NotNull(message = "题目组ID不能为空")
    private Long questionGroupId;
    
    /**
     * 排序索引
     */
    @Min(value = 0, message = "排序索引不能为负数")
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
} 