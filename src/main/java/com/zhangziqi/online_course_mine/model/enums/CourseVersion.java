package com.zhangziqi.online_course_mine.model.enums;

import lombok.Getter;

/**
 * 课程版本类型枚举
 */
@Getter
public enum CourseVersion {
    /**
     * 草稿版本
     */
    DRAFT(0, "草稿"),

    /**
     * 审核版本
     */
    REVIEW(1, "审核版本"),

    /**
     * 发布版本
     */
    PUBLISHED(2, "发布版本");

    /**
     * 版本类型值
     */
    private final Integer value;

    /**
     * 版本类型描述
     */
    private final String description;

    CourseVersion(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 根据版本类型值获取版本类型枚举
     *
     * @param value 版本类型值
     * @return 版本类型枚举
     */
    public static CourseVersion getByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (CourseVersion version : CourseVersion.values()) {
            if (version.getValue().equals(value)) {
                return version;
            }
        }
        return null;
    }
} 