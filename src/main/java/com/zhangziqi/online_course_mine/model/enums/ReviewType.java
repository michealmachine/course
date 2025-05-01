package com.zhangziqi.online_course_mine.model.enums;

import lombok.Getter;

/**
 * 审核类型枚举
 */
@Getter
public enum ReviewType {
    /**
     * 课程审核
     */
    COURSE(0, "课程审核"),

    /**
     * 机构审核
     */
    INSTITUTION(1, "机构审核");

    /**
     * 类型值
     */
    private final Integer value;

    /**
     * 类型描述
     */
    private final String description;

    ReviewType(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 根据类型值获取类型枚举
     *
     * @param value 类型值
     * @return 类型枚举
     */
    public static ReviewType getByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (ReviewType type : ReviewType.values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        return null;
    }
}
