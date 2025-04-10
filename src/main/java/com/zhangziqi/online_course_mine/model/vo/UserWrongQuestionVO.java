package com.zhangziqi.online_course_mine.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户错题VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserWrongQuestionVO {
    
    /**
     * 错题ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 课程标题
     */
    private String courseTitle;
    
    /**
     * 小节ID
     */
    private Long sectionId;
    
    /**
     * 问题ID
     */
    private Long questionId;
    
    /**
     * 问题标题
     */
    private String questionTitle;
    
    /**
     * 问题类型
     */
    private String questionType;
    
    /**
     * 用户答案
     */
    private List<String> userAnswers;
    
    /**
     * 正确答案
     */
    private List<String> correctAnswers;
    
    /**
     * 状态：0-未解决，1-已解决
     */
    private Integer status;
    
    /**
     * 错误次数
     * 记录用户答错该题的次数
     */
    private Integer errorCount;
    
    /**
     * 相关学习记录ID
     */
    private Long learningRecordId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
} 