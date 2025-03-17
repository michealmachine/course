package com.zhangziqi.online_course_mine.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户收藏课程视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFavoriteVO {
    
    /**
     * 收藏ID
     */
    private Long id;
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 课程标题
     */
    private String courseTitle;
    
    /**
     * 课程封面图片
     */
    private String courseCoverImage;
    
    /**
     * 课程价格
     */
    private String coursePrice;
    
    /**
     * 课程分类名称
     */
    private String categoryName;
    
    /**
     * 机构名称
     */
    private String institutionName;
    
    /**
     * 收藏时间
     */
    private LocalDateTime favoriteTime;
} 