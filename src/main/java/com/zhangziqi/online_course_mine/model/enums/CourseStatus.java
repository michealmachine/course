package com.zhangziqi.online_course_mine.model.enums;

import lombok.Getter;

/**
 * 课程状态枚举
 */
@Getter
public enum CourseStatus {
    /**
     * 草稿
     */
    DRAFT(0, "草稿"),

    /**
     * 待审核
     */
    PENDING_REVIEW(1, "待审核"),

    /**
     * 审核中
     */
    REVIEWING(2, "审核中"),

    /**
     * 已拒绝
     */
    REJECTED(3, "已拒绝"),

    /**
     * 已发布
     */
    PUBLISHED(4, "已发布"),

    /**
     * 已下线
     */
    UNPUBLISHED(5, "已下线");

    /**
     * 状态值
     */
    private final Integer value;

    /**
     * 状态描述
     */
    private final String description;

    CourseStatus(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 根据状态值获取状态枚举
     *
     * @param value 状态值
     * @return 状态枚举
     */
    public static CourseStatus getByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (CourseStatus status : CourseStatus.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }
} 