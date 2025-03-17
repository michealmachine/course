package com.zhangziqi.online_course_mine.model.vo;

import com.zhangziqi.online_course_mine.model.entity.CourseReview;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

/**
 * 课程评论VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewVO {
    
    /**
     * 评论ID
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
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 用户头像
     */
    private String userAvatar;
    
    /**
     * 评分 (1-5)
     */
    private Integer rating;
    
    /**
     * 评论内容
     */
    private String content;
    
    /**
     * 点赞数
     */
    private Integer likeCount;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 从实体转换为VO
     */
    public static ReviewVO fromEntity(CourseReview review) {
        ReviewVO vo = new ReviewVO();
        BeanUtils.copyProperties(review, vo);
        
        if (review.getCourse() != null) {
            vo.setCourseId(review.getCourse().getId());
            vo.setCourseTitle(review.getCourse().getTitle());
        }
        
        return vo;
    }
    
    /**
     * 从实体转换为VO，并填充用户信息
     */
    public static ReviewVO fromEntity(CourseReview review, String username, String userAvatar) {
        ReviewVO vo = fromEntity(review);
        vo.setUsername(username);
        vo.setUserAvatar(userAvatar);
        return vo;
    }
} 