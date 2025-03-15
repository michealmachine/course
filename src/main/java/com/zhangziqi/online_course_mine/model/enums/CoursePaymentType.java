package com.zhangziqi.online_course_mine.model.enums;

import lombok.Getter;

/**
 * 课程付费类型枚举
 */
@Getter
public enum CoursePaymentType {
    /**
     * 免费课程
     */
    FREE(0, "免费"),

    /**
     * 付费课程
     */
    PAID(1, "付费");

    /**
     * 类型值
     */
    private final Integer value;

    /**
     * 类型描述
     */
    private final String description;

    CoursePaymentType(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 根据类型值获取类型枚举
     *
     * @param value 类型值
     * @return 类型枚举
     */
    public static CoursePaymentType getByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (CoursePaymentType type : CoursePaymentType.values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        return null;
    }
} 