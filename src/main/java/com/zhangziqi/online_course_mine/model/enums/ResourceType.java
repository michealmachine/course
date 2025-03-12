package com.zhangziqi.online_course_mine.model.enums;

import lombok.Getter;

/**
 * 课程资源类型枚举
 */
@Getter
public enum ResourceType {
    /**
     * 主要内容
     */
    PRIMARY("primary", "主要内容"),

    /**
     * 补充材料
     */
    SUPPLEMENTARY("supplementary", "补充材料"),

    /**
     * 课后作业
     */
    HOMEWORK("homework", "课后作业"),

    /**
     * 参考资料
     */
    REFERENCE("reference", "参考资料");

    /**
     * 资源类型编码
     */
    private final String code;

    /**
     * 资源类型描述
     */
    private final String description;

    ResourceType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据编码获取资源类型枚举
     *
     * @param code 编码
     * @return 资源类型枚举
     */
    public static ResourceType getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (ResourceType type : ResourceType.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
} 