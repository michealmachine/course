package com.zhangziqi.online_course_mine.model.vo;

import com.zhangziqi.online_course_mine.model.entity.UserCourse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户课程关系值对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCourseVO {
    
    /**
     * ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String userName;
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 课程标题
     */
    private String courseTitle;
    
    /**
     * 课程封面
     */
    private String courseCover;
    
    /**
     * 机构ID
     */
    private Long institutionId;
    
    /**
     * 机构名称
     */
    private String institutionName;
    
    /**
     * 购买时间
     */
    private LocalDateTime purchasedAt;
    
    /**
     * 订单ID
     */
    private Long orderId;
    
    /**
     * 订单号
     */
    private String orderNo;
    
    /**
     * 过期时间（如果有）
     */
    private LocalDateTime expireAt;
    
    /**
     * 学习进度（百分比）
     */
    private Integer progress;
    
    /**
     * 状态
     * 0: 正常学习
     * 1: 已过期
     * 2: 已退款
     */
    private Integer status;
    
    /**
     * 最后学习时间
     */
    private LocalDateTime lastLearnAt;
    
    /**
     * 学习总时长（秒）
     */
    private Integer learnDuration;
    
    /**
     * 当前学习章节ID
     */
    private Long currentChapterId;
    
    /**
     * 当前学习小节ID
     */
    private Long currentSectionId;
    
    /**
     * 当前小节学习进度（百分比）
     */
    private Integer currentSectionProgress;
    
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
    public static UserCourseVO fromEntity(UserCourse userCourse) {
        if (userCourse == null) {
            return null;
        }
        
        return UserCourseVO.builder()
                .id(userCourse.getId())
                .userId(userCourse.getUser().getId())
                .userName(userCourse.getUser().getUsername())
                .courseId(userCourse.getCourse().getId())
                .courseTitle(userCourse.getCourse().getTitle())
                .courseCover(userCourse.getCourse().getCoverImage())
                .institutionId(userCourse.getCourse().getInstitution().getId())
                .institutionName(userCourse.getCourse().getInstitution().getName())
                .purchasedAt(userCourse.getPurchasedAt())
                .orderId(userCourse.getOrder() != null ? userCourse.getOrder().getId() : null)
                .orderNo(userCourse.getOrder() != null ? userCourse.getOrder().getOrderNo() : null)
                .expireAt(userCourse.getExpireAt())
                .progress(userCourse.getProgress())
                .status(userCourse.getStatus())
                .lastLearnAt(userCourse.getLastLearnAt())
                .learnDuration(userCourse.getLearnDuration())
                .currentChapterId(userCourse.getCurrentChapterId())
                .currentSectionId(userCourse.getCurrentSectionId())
                .currentSectionProgress(userCourse.getCurrentSectionProgress())
                .createdAt(userCourse.getCreatedAt())
                .updatedAt(userCourse.getUpdatedAt())
                .build();
    }
    
    /**
     * 获取状态文本
     */
    public String getStatusText() {
        switch (status) {
            case 0:
                return "正常学习";
            case 1:
                return "已过期";
            case 2:
                return "已退款";
            default:
                return "未知状态";
        }
    }
    
    /**
     * 获取学习进度文本
     */
    public String getProgressText() {
        return progress + "%";
    }
    
    /**
     * 格式化学习时长
     */
    public String getFormattedDuration() {
        if (learnDuration == null || learnDuration == 0) {
            return "0小时0分钟";
        }
        
        int hours = learnDuration / 3600;
        int minutes = (learnDuration % 3600) / 60;
        
        return hours + "小时" + minutes + "分钟";
    }
} 