package com.zhangziqi.online_course_mine.model.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * 学习活动类型枚举
 * 用于标识不同类型的学习活动
 */
public enum LearningActivityType {
    
    VIDEO_WATCH("VIDEO_WATCH", "视频观看"),
    DOCUMENT_READ("DOCUMENT_READ", "文档阅读"),
    QUIZ_ATTEMPT("QUIZ_ATTEMPT", "测验尝试"),
    SECTION_START("SECTION_START", "小节开始"),
    SECTION_END("SECTION_END", "小节完成");
    
    private final String code;
    private final String description;
    
    // 用于代码到枚举的映射
    private static final Map<String, LearningActivityType> codeMap = new HashMap<>();
    
    static {
        for (LearningActivityType type : LearningActivityType.values()) {
            codeMap.put(type.getCode(), type);
        }
    }
    
    LearningActivityType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据代码获取枚举
     * @param code 代码
     * @return 对应的枚举，如果不存在则返回null
     */
    public static LearningActivityType getByCode(String code) {
        return codeMap.get(code);
    }
} 