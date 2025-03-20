package com.zhangziqi.online_course_mine.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户错题状态枚举
 */
@Getter
@AllArgsConstructor
public enum UserWrongQuestionStatus {
    
    /**
     * 未解决
     */
    UNRESOLVED(0, "未解决"),
    
    /**
     * 已解决
     */
    RESOLVED(1, "已解决");
    
    private final Integer value;
    private final String description;
    
    /**
     * 根据值获取枚举
     */
    public static UserWrongQuestionStatus fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        
        for (UserWrongQuestionStatus status : UserWrongQuestionStatus.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        
        return null;
    }
} 