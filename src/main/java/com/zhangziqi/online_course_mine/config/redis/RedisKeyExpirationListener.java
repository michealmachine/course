package com.zhangziqi.online_course_mine.config.redis;

import com.zhangziqi.online_course_mine.service.impl.RedisOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

/**
 * Redis键过期监听器
 * 用于监听订单超时事件
 */
@Slf4j
@Component
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {

    private final RedisOrderService redisOrderService;
    
    // 订单超时键前缀
    private static final String ORDER_TIMEOUT_PREFIX = "order:timeout:";

    public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer,
                                     RedisOrderService redisOrderService) {
        super(listenerContainer);
        this.redisOrderService = redisOrderService;
    }

    /**
     * 监听Redis键过期事件
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        log.debug("Redis键过期：{}", expiredKey);
        
        // 处理订单超时
        if (expiredKey.startsWith(ORDER_TIMEOUT_PREFIX)) {
            String orderNo = expiredKey.substring(ORDER_TIMEOUT_PREFIX.length());
            log.info("检测到订单超时，订单号：{}", orderNo);
            try {
                redisOrderService.handleOrderTimeout(orderNo);
            } catch (Exception e) {
                log.error("处理订单超时失败，订单号：{}，错误信息：{}", orderNo, e.getMessage());
            }
        }
    }
}