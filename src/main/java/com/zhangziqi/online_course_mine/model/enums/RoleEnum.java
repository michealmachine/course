package com.zhangziqi.online_course_mine.model.enums;

import lombok.Getter;

/**
 * 角色枚举
 */
@Getter
public enum RoleEnum {

    /**
     * 普通用户
     */
    USER("普通用户", "ROLE_USER"),

    /**
     * 管理员
     */
    ADMIN("管理员", "ROLE_ADMIN"),

    /**
     * 审核人员
     */
    REVIEWER("审核人员", "ROLE_REVIEWER"),

    /**
     * 机构用户
     */
    INSTITUTION("机构用户", "ROLE_INSTITUTION");

    /**
     * 角色名称
     */
    private final String name;

    /**
     * 角色编码
     */
    private final String code;

    RoleEnum(String name, String code) {
        this.name = name;
        this.code = code;
    }

    /**
     * 根据编码获取角色枚举
     *
     * @param code 编码
     * @return 角色枚举
     */
    public static RoleEnum getByCode(String code) {
        for (RoleEnum role : RoleEnum.values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        return null;
    }
} 