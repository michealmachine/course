package com.zhangziqi.online_course_mine.model.enums;

import lombok.Getter;

/**
 * 题目类型枚举
 */
@Getter
public enum QuestionType {
    /**
     * 单选题
     */
    SINGLE_CHOICE(0, "单选题"),

    /**
     * 多选题
     */
    MULTIPLE_CHOICE(1, "多选题"),
    
    /**
     * 判断题
     */
    TRUE_FALSE(2, "判断题"),
    
    /**
     * 填空题
     */
    FILL_BLANK(3, "填空题"),
    
    /**
     * 简答题
     */
    SHORT_ANSWER(4, "简答题");

    /**
     * 类型值
     */
    private final Integer value;

    /**
     * 类型描述
     */
    private final String description;

    QuestionType(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 根据类型值获取类型枚举
     *
     * @param value 类型值
     * @return 类型枚举
     */
    public static QuestionType getByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (QuestionType type : QuestionType.values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        return null;
    }
} 