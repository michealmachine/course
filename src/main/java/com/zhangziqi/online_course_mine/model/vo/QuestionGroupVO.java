package com.zhangziqi.online_course_mine.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 题目组视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionGroupVO {
    
    /**
     * 题目组ID
     */
    private Long id;
    
    /**
     * 所属机构ID
     */
    private Long institutionId;
    
    /**
     * 题目组名称
     */
    private String name;
    
    /**
     * 题目组描述
     */
    private String description;
    
    /**
     * 题目数量
     */
    private Long questionCount;
    
    /**
     * 题目项列表（可选，详情查询时返回）
     */
    private List<QuestionGroupItemVO> items;
    
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