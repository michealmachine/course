package com.zhangziqi.online_course_mine.model.enums;

import lombok.Getter;

/**
 * 订单状态枚举
 */
@Getter
public enum OrderStatus {
    
    PENDING(0, "待支付"),
    PAID(1, "已支付"),
    CLOSED(2, "已关闭"),
    REFUNDING(3, "申请退款"),
    REFUNDED(4, "已退款"),
    REFUND_FAILED(5, "退款失败");
    
    // 添加常量而不是新的枚举值，因为CREATED与PENDING是相同的状态值
    public static final int CREATED = 0; // 创建（同待支付）
    
    private final int value;
    private final String desc;
    
    OrderStatus(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
    
    /**
     * 根据值获取枚举
     */
    public static OrderStatus valueOf(int value) {
        for (OrderStatus status : OrderStatus.values()) {
            if (status.getValue() == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的订单状态值: " + value);
    }
} 