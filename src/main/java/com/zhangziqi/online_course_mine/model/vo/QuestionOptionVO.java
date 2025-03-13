package com.zhangziqi.online_course_mine.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 题目选项视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionOptionVO {
    
    /**
     * 选项ID
     */
    private Long id;
    
    /**
     * 所属题目ID
     */
    private Long questionId;
    
    /**
     * 选项内容
     */
    private String content;
    
    /**
     * 是否为正确选项
     */
    private Boolean isCorrect;
    
    /**
     * 选项顺序
     */
    private Integer orderIndex;
} 