package com.zhangziqi.online_course_mine.model.enums;

import lombok.Getter;

/**
 * 章节访问类型枚举
 */
@Getter
public enum ChapterAccessType {
    /**
     * 免费试学（无需付费即可访问的章节）
     */
    FREE_TRIAL(0, "免费试学"),

    /**
     * 付费访问（需要付费才能访问的章节）
     */
    PAID_ONLY(1, "付费访问");

    /**
     * 类型值
     */
    private final Integer value;

    /**
     * 类型描述
     */
    private final String description;

    ChapterAccessType(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 根据类型值获取类型枚举
     *
     * @param value 类型值
     * @return 类型枚举
     */
    public static ChapterAccessType getByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (ChapterAccessType type : ChapterAccessType.values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        return null;
    }
} 