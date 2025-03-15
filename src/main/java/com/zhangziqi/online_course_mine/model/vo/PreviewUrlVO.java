package com.zhangziqi.online_course_mine.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 预览URL值对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreviewUrlVO {
    
    /**
     * 预览URL
     */
    private String url;
    
    /**
     * 过期时间
     */
    private LocalDateTime expireTime;
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 课程标题
     */
    private String courseTitle;
} 