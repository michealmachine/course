package com.zhangziqi.online_course_mine.service.impl;

import com.zhangziqi.online_course_mine.event.OrderTimeoutEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * Redis订单服务实现
 * 处理订单超时、缓存等Redis相关操作
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisOrderService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final ApplicationEventPublisher eventPublisher;
    
    // Redis键前缀
    private static final String ORDER_TIMEOUT_PREFIX = "order:timeout:";
    
    /**
     * 设置订单超时
     * @param orderNo 订单号
     * @param userId 用户ID
     * @param orderId 订单ID
     */
    public void setOrderTimeout(String orderNo, Long userId, Long orderId) {
        String key = ORDER_TIMEOUT_PREFIX + orderNo;
        log.info("设置订单超时，订单号：{}，用户ID：{}，超时时间：30分钟", orderNo, userId);
        
        // 存储订单ID和用户ID，用于超时后自动取消
        OrderTimeoutInfo timeoutInfo = new OrderTimeoutInfo(orderId, userId, orderNo);
        
        // 设置30分钟超时
        redisTemplate.opsForValue().set(key, timeoutInfo, 30, TimeUnit.MINUTES);
    }
    
    /**
     * 取消订单超时
     * @param orderNo 订单号
     */
    public void cancelOrderTimeout(String orderNo) {
        String key = ORDER_TIMEOUT_PREFIX + orderNo;
        log.info("取消订单超时，订单号：{}", orderNo);
        redisTemplate.delete(key);
    }
    
    /**
     * 获取订单剩余支付时间（秒）
     * @param orderNo 订单号
     * @return 剩余秒数，如果不存在则返回0
     */
    public long getOrderRemainingTime(String orderNo) {
        String key = ORDER_TIMEOUT_PREFIX + orderNo;
        Long remainingTime = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return remainingTime != null && remainingTime > 0 ? remainingTime : 0;
    }
    
    /**
     * 处理订单超时（由Redis Key过期监听器触发）
     * @param orderNo 订单号
     */
    public void handleOrderTimeout(String orderNo) {
        log.info("处理订单超时，订单号：{}", orderNo);
        try {
            // 从Redis中获取超时信息（如果已经被处理，可能获取不到）
            String key = ORDER_TIMEOUT_PREFIX + orderNo;
            OrderTimeoutInfo timeoutInfo = (OrderTimeoutInfo) redisTemplate.opsForValue().get(key);
            
            if (timeoutInfo != null) {
                log.info("发布订单超时事件，订单ID：{}，用户ID：{}", timeoutInfo.getOrderId(), timeoutInfo.getUserId());
                // 发布订单超时事件，由对应的监听器处理
                eventPublisher.publishEvent(new OrderTimeoutEvent(
                        this, 
                        timeoutInfo.getOrderId(), 
                        timeoutInfo.getUserId(),
                        timeoutInfo.getOrderNo()));
            } else {
                log.info("订单超时信息不存在，可能已被处理，订单号：{}", orderNo);
            }
        } catch (Exception e) {
            log.error("处理订单超时异常，订单号：{}", orderNo, e);
        }
    }
    
    /**
     * 订单超时信息
     */
    static class OrderTimeoutInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private Long orderId;
        private Long userId;
        private String orderNo;
        
        public OrderTimeoutInfo() {
        }
        
        public OrderTimeoutInfo(Long orderId, Long userId, String orderNo) {
            this.orderId = orderId;
            this.userId = userId;
            this.orderNo = orderNo;
        }
        
        public Long getOrderId() {
            return orderId;
        }
        
        public void setOrderId(Long orderId) {
            this.orderId = orderId;
        }
        
        public Long getUserId() {
            return userId;
        }
        
        public void setUserId(Long userId) {
            this.userId = userId;
        }
        
        public String getOrderNo() {
            return orderNo;
        }
        
        public void setOrderNo(String orderNo) {
            this.orderNo = orderNo;
        }
    }
}