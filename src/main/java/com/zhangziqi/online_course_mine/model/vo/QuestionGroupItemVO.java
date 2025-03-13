package com.zhangziqi.online_course_mine.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 题目组项视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionGroupItemVO {
    
    /**
     * 题目组项ID
     */
    private Long id;
    
    /**
     * 题目组ID
     */
    private Long groupId;
    
    /**
     * 题目ID
     */
    private Long questionId;
    
    /**
     * 题目信息（可选，详情查询时返回）
     */
    private QuestionVO question;
    
    /**
     * 在组中的顺序
     */
    private Integer orderIndex;
    
    /**
     * 难度级别（可覆盖题目原始难度）
     */
    private Integer difficulty;
    
    /**
     * 实际难度描述
     */
    private String difficultyDesc;
    
    /**
     * 分值（可覆盖题目原始分值）
     */
    private Integer score;
} 