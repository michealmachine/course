package com.zhangziqi.online_course_mine.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * 课程收入排行值对象
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CourseIncomeRankingVO {

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 课程标题
     */
    private String courseTitle;

    /**
     * 课程封面
     */
    private String courseCover;

    /**
     * 收入金额
     */
    private BigDecimal income;
}
