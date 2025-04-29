package com.zhangziqi.online_course_mine.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 收入趋势值对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncomeTrendVO {
    
    /**
     * 日期（格式：yyyy-MM-dd）
     */
    private String date;
    
    /**
     * 收入金额
     */
    private BigDecimal income;
    
    /**
     * 退款金额
     */
    private BigDecimal refund;
    
    /**
     * 净收入金额（收入-退款）
     */
    private BigDecimal netIncome;
}
