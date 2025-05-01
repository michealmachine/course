package com.zhangziqi.online_course_mine.model.enums;

import lombok.Getter;

/**
 * 审核结果枚举
 */
@Getter
public enum ReviewResult {
    /**
     * 通过
     */
    APPROVED(0, "通过"),

    /**
     * 拒绝
     */
    REJECTED(1, "拒绝");

    /**
     * 结果值
     */
    private final Integer value;

    /**
     * 结果描述
     */
    private final String description;

    ReviewResult(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 根据结果值获取结果枚举
     *
     * @param value 结果值
     * @return 结果枚举
     */
    public static ReviewResult getByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (ReviewResult result : ReviewResult.values()) {
            if (result.getValue().equals(value)) {
                return result;
            }
        }
        return null;
    }
}
