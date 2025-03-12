package com.zhangziqi.online_course_mine.model.enums;

import lombok.Getter;

/**
 * 内容类型枚举
 */
@Getter
public enum ContentType {
    /**
     * 视频内容
     */
    VIDEO("video", "视频"),

    /**
     * 文档内容
     */
    DOCUMENT("document", "文档"),

    /**
     * 音频内容
     */
    AUDIO("audio", "音频"),

    /**
     * 文本内容
     */
    TEXT("text", "文本"),

    /**
     * 图片内容
     */
    IMAGE("image", "图片"),

    /**
     * 混合内容
     */
    MIXED("mixed", "混合内容");

    /**
     * 内容类型编码
     */
    private final String code;

    /**
     * 内容类型描述
     */
    private final String description;

    ContentType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据编码获取内容类型枚举
     *
     * @param code 编码
     * @return 内容类型枚举
     */
    public static ContentType getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (ContentType type : ContentType.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
} 