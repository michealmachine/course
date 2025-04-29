package com.zhangziqi.online_course_mine.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 平台收入统计值对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformIncomeStatsVO {
    
    /**
     * 总收入
     */
    private BigDecimal totalIncome;
    
    /**
     * 总退款
     */
    private BigDecimal totalRefund;
    
    /**
     * 净收入（总收入-总退款）
     */
    private BigDecimal netIncome;
    
    /**
     * 订单总数
     */
    private Integer orderCount;
    
    /**
     * 已支付订单数
     */
    private Integer paidOrderCount;
    
    /**
     * 退款订单数
     */
    private Integer refundOrderCount;
}
