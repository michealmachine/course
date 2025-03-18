package com.zhangziqi.online_course_mine.model.enums;

import lombok.Getter;

/**
 * 用户课程状态枚举
 */
@Getter
public enum UserCourseStatus {
    
    NORMAL(0, "正常"),
    EXPIRED(1, "已过期"),
    REFUNDED(2, "已退款");
    
    private final int value;
    private final String desc;
    
    UserCourseStatus(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
    
    public static UserCourseStatus valueOf(int value) {
        for (UserCourseStatus status : UserCourseStatus.values()) {
            if (status.getValue() == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的用户课程状态值: " + value);
    }
} 