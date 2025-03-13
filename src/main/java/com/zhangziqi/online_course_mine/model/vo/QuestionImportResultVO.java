package com.zhangziqi.online_course_mine.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 试题导入结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionImportResultVO {

    /**
     * 总条目数
     */
    private int totalCount;

    /**
     * 成功导入数
     */
    private int successCount;

    /**
     * 失败数
     */
    private int failureCount;

    /**
     * 导入用时(毫秒)
     */
    private long duration;

    /**
     * 失败记录列表
     */
    @Builder.Default
    private List<FailureItem> failureItems = new ArrayList<>();

    /**
     * 失败记录项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FailureItem {
        
        /**
         * Excel行号(从1开始)
         */
        private int rowIndex;
        
        /**
         * 题目标题
         */
        private String title;
        
        /**
         * 错误信息
         */
        private String errorMessage;
    }
} 