package com.zhangziqi.online_course_mine.task;

import com.zhangziqi.online_course_mine.model.entity.Order;
import com.zhangziqi.online_course_mine.model.enums.OrderStatus;
import com.zhangziqi.online_course_mine.repository.OrderRepository;
import com.zhangziqi.online_course_mine.service.OrderService;
import com.zhangziqi.online_course_mine.service.impl.RedisOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单超时定时任务
 * 定期扫描数据库中的待支付订单，关闭超时订单
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutTask {

    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final RedisOrderService redisOrderService;

    /**
     * 处理超时订单
     * 每隔30秒检查一次，关闭超过30分钟未支付的订单
     */
    @Scheduled(fixedRate = 30000) // 30秒执行一次
    @Transactional
    public void handleTimeoutOrders() {
        log.info("开始处理超时订单...");
        
        // 查找超过30分钟未支付的订单
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(30);
        List<Order> timeoutOrders = orderRepository.findByStatusAndCreatedAtBefore(
                OrderStatus.PENDING.getValue(), timeoutThreshold);
        
        log.info("发现{}个超时订单需要处理", timeoutOrders.size());
        
        for (Order order : timeoutOrders) {
            try {
                // 先检查Redis中订单是否真的已超时（双重检查，防止定时任务与Redis的时间差）
                long remainingTime = redisOrderService.getOrderRemainingTime(order.getOrderNo());
                
                // 如果Redis中显示已经超时或者Redis中已没有这个订单记录
                if (remainingTime <= 0) {
                    // 通过OrderService关闭订单，保证业务逻辑统一
                    orderService.cancelOrder(order.getId(), order.getUser().getId());
                    log.info("成功关闭超时订单：{}，订单号：{}", order.getId(), order.getOrderNo());
                } else {
                    // Redis中还有时间，可能是数据不一致，记录一下日志
                    log.warn("订单数据不一致：订单{}在数据库中已超过30分钟，但Redis中还有{}秒", 
                            order.getOrderNo(), remainingTime);
                }
            } catch (Exception e) {
                log.error("关闭超时订单失败：{}，订单号：{}", order.getId(), order.getOrderNo(), e);
            }
        }
        
        log.info("超时订单处理完成");
    }
} 