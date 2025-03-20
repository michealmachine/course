package com.zhangziqi.online_course_mine.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 订单超时事件
 */
@Getter
public class OrderTimeoutEvent extends ApplicationEvent {
    
    private final Long orderId;
    private final Long userId;
    private final String orderNo;
    
    public OrderTimeoutEvent(Object source, Long orderId, Long userId, String orderNo) {
        super(source);
        this.orderId = orderId;
        this.userId = userId;
        this.orderNo = orderNo;
    }
}
