package com.zhangziqi.online_course_mine.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单状态分布值对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusDistributionVO {
    
    /**
     * 订单状态值
     */
    private Integer status;
    
    /**
     * 订单状态名称
     */
    private String statusName;
    
    /**
     * 订单数量
     */
    private Integer count;
    
    /**
     * 百分比（0-100）
     */
    private Double percentage;
}
