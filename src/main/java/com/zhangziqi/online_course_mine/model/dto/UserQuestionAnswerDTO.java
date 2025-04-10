package com.zhangziqi.online_course_mine.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用户问题答案DTO
 * 用于提交用户对题目的回答
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserQuestionAnswerDTO {
    
    /**
     * 题目ID
     */
    private Long questionId;
    
    /**
     * 用户答案
     * - 单选题：包含一个选项ID
     * - 多选题：包含多个选项ID
     * - 判断题：包含一个值（true/false）
     * - 填空题：包含一个或多个文本
     * - 简答题：包含一个文本
     */
    private List<String> answers;
    
    /**
     * 正确答案（用于记录错题时）
     */
    private List<String> correctAnswers;
    
    /**
     * 题目类型
     */
    private String questionType;
    
    /**
     * 题目标题
     */
    private String questionTitle;
    
    /**
     * 用户回答用时（毫秒）
     */
    private Long duration;
    
    /**
     * 是否做错
     */
    private Boolean isWrong;
    
    /**
     * 学习记录ID
     * 关联的学习活动记录，用于链接错题和学习活动
     */
    private Long learningRecordId;
}