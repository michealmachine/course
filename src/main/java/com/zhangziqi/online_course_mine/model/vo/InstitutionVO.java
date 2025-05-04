package com.zhangziqi.online_course_mine.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 机构VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "机构信息")
public class InstitutionVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "机构名称")
    private String name;

    @Schema(description = "机构Logo")
    private String logo;

    @Schema(description = "机构描述")
    private String description;

    @Schema(description = "状态：0-待审核，1-正常，2-禁用")
    private Integer status;

    @Schema(description = "机构注册码")
    private String registerCode;

    @Schema(description = "联系人")
    private String contactPerson;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "联系邮箱")
    private String contactEmail;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    /**
     * 机构统计数据VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "机构统计数据")
    public static class InstitutionStatsVO {

        /**
         * 用户数量
         */
        @Schema(description = "用户数量")
        private Long userCount;

        /**
         * 课程数量
         */
        @Schema(description = "课程数量")
        private Integer courseCount;

        /**
         * 已发布课程数量
         */
        @Schema(description = "已发布课程数量")
        private Integer publishedCourseCount;

        /**
         * 总学习人数
         */
        @Schema(description = "总学习人数")
        private Long totalLearners;

        /**
         * 总学习时长（秒）
         */
        @Schema(description = "总学习时长（秒）")
        private Long totalLearningDuration;

        /**
         * 总收入（分）
         */
        @Schema(description = "总收入（分）")
        private Long totalIncome;

        /**
         * 本月收入（分）
         */
        @Schema(description = "本月收入（分）")
        private Long monthIncome;
    }
}