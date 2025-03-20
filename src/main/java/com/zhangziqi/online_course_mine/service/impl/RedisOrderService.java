package com.zhangziqi.online_course_mine.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * Redis订单服务实现
 * 处理订单超时计时、缓存等Redis相关操作
 * 注意：不再负责订单超时事件触发，仅提供计时功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisOrderService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    // Redis键前缀
    private static final String ORDER_TIMEOUT_PREFIX = "order:timeout:";
    
    /**
     * 设置订单超时计时
     * @param orderNo 订单号
     * @param userId 用户ID
     * @param orderId 订单ID
     */
    public void setOrderTimeout(String orderNo, Long userId, Long orderId) {
        String key = ORDER_TIMEOUT_PREFIX + orderNo;
        log.info("设置订单超时计时，订单号：{}，用户ID：{}，超时时间：29分钟", orderNo, userId);
        
        // 存储订单ID和用户ID，用于查询
        OrderTimeoutInfo timeoutInfo = new OrderTimeoutInfo(orderId, userId, orderNo);
        
        // 设置29分钟超时（比定时任务的30分钟稍短，确保在定时任务检查前Redis已过期）
        redisTemplate.opsForValue().set(key, timeoutInfo, 29, TimeUnit.MINUTES);
    }
    
    /**
     * 取消订单超时计时
     * @param orderNo 订单号
     */
    public void cancelOrderTimeout(String orderNo) {
        String key = ORDER_TIMEOUT_PREFIX + orderNo;
        log.info("取消订单超时计时，订单号：{}", orderNo);
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