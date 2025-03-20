package com.zhangziqi.online_course_mine.listener;

import com.zhangziqi.online_course_mine.event.OrderTimeoutEvent;
import com.zhangziqi.online_course_mine.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 订单超时事件监听器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutEventListener {

    private final OrderService orderService;

    /**
     * 处理订单超时事件
     * 当订单超时时，将调用 OrderService 的 cancelOrder 方法将订单状态设置为已关闭
     * @param event 订单超时事件
     */
    @EventListener
    public void handleOrderTimeoutEvent(OrderTimeoutEvent event) {
        log.info("接收到订单超时事件，订单ID：{}，用户ID：{}, 订单号：{}", 
                event.getOrderId(), event.getUserId(), event.getOrderNo());
        
        try {
            // 调用服务取消订单
            orderService.cancelOrder(event.getOrderId(), event.getUserId());
            log.info("成功取消超时订单，订单ID：{}", event.getOrderId());
        } catch (Exception e) {
            log.error("取消超时订单失败，订单ID：{}", event.getOrderId(), e);
        }
    }
}
