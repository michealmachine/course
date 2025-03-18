package com.zhangziqi.online_course_mine.model.vo;

import com.zhangziqi.online_course_mine.model.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单值对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderVO {
    
    /**
     * 订单ID
     */
    private Long id;
    
    /**
     * 订单号
     */
    private String orderNo;
    
    /**
     * 订单标题
     */
    private String title;
    
    /**
     * 订单描述
     */
    private String description;
    
    /**
     * 订单金额
     */
    private BigDecimal amount;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户姓名
     */
    private String userName;
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 课程标题
     */
    private String courseTitle;
    
    /**
     * 课程封面
     */
    private String courseCover;
    
    /**
     * 机构ID
     */
    private Long institutionId;
    
    /**
     * 机构名称
     */
    private String institutionName;
    
    /**
     * 支付宝交易号
     */
    private String tradeNo;
    
    /**
     * 支付时间
     */
    private LocalDateTime paidAt;
    
    /**
     * 订单状态
     * 0: 待支付
     * 1: 已支付
     * 2: 已关闭
     * 3: 申请退款
     * 4: 已退款
     * 5: 退款失败
     */
    private Integer status;
    
    /**
     * 退款时间
     */
    private LocalDateTime refundedAt;
    
    /**
     * 退款金额
     */
    private BigDecimal refundAmount;
    
    /**
     * 退款原因
     */
    private String refundReason;
    
    /**
     * 退款交易号
     */
    private String refundTradeNo;
    
    /**
     * 支付链接（仅在创建付费订单时返回）
     */
    private String payUrl;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 从实体转换为VO
     */
    public static OrderVO fromEntity(Order order) {
        if (order == null) {
            return null;
        }
        
        OrderVO.OrderVOBuilder builder = OrderVO.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .title(order.getTitle())
                .description(order.getDescription())
                .amount(order.getAmount())
                .userId(order.getUser().getId())
                .userName(order.getUser().getUsername())
                .courseId(order.getCourse().getId())
                .courseTitle(order.getCourse().getTitle())
                .courseCover(order.getCourse().getCoverImage())
                .tradeNo(order.getTradeNo())
                .paidAt(order.getPaidAt())
                .status(order.getStatus())
                .refundedAt(order.getRefundedAt())
                .refundAmount(order.getRefundAmount())
                .refundReason(order.getRefundReason())
                .refundTradeNo(order.getRefundTradeNo())
                .payUrl(order.getPayUrl())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt());
        
        // 检查Institution是否为空
        if (order.getInstitution() != null) {
            builder.institutionId(order.getInstitution().getId())
                  .institutionName(order.getInstitution().getName());
        }
        
        return builder.build();
    }
    
    /**
     * 获取状态文本
     */
    public String getStatusText() {
        return com.zhangziqi.online_course_mine.model.enums.OrderStatus.valueOf(status).getDesc();
    }
}