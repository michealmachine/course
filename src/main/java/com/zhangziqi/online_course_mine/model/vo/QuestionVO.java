package com.zhangziqi.online_course_mine.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 题目视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionVO {
    
    /**
     * 题目ID
     */
    private Long id;
    
    /**
     * 所属机构ID
     */
    private Long institutionId;
    
    /**
     * 题目标题
     */
    private String title;
    
    /**
     * 题目内容
     */
    private String content;
    
    /**
     * 题目类型：1-单选题，2-多选题
     */
    private Integer type;
    
    /**
     * 题目类型描述
     */
    private String typeDesc;
    
    /**
     * 难度级别：1-简单，2-中等，3-困难
     */
    private Integer difficulty;
    
    /**
     * 难度级别描述
     */
    private String difficultyDesc;
    
    /**
     * 分值
     */
    private Integer score;
    
    /**
     * 解析说明
     */
    private String analysis;
    
    /**
     * 正确答案（填空题必填，简答题可选）
     */
    private String answer;
    
    /**
     * 题目选项列表
     */
    private List<QuestionOptionVO> options;
    
    /**
     * 标签列表
     */
    private List<QuestionTagVO> tags;
    
    /**
     * 创建者ID
     */
    private Long creatorId;
    
    /**
     * 创建者名称
     */
    private String creatorName;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
} 