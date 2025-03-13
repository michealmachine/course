package com.zhangziqi.online_course_mine.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 题目标签视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionTagVO {
    
    /**
     * 标签ID
     */
    private Long id;
    
    /**
     * 所属机构ID
     */
    private Long institutionId;
    
    /**
     * 标签名称
     */
    private String name;
    
    /**
     * 关联的题目数量
     */
    private Long questionCount;
    
    /**
     * 创建者ID
     */
    private Long creatorId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
} 