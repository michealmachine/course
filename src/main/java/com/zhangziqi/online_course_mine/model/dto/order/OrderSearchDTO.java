package com.zhangziqi.online_course_mine.model.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单搜索数据传输对象
 * 用于封装订单搜索参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单搜索DTO")
public class OrderSearchDTO {
    
    /**
     * 订单号（模糊搜索）
     */
    @Schema(description = "订单号", example = "1652784531234")
    private String orderNo;
    
    /**
     * 交易号（模糊搜索）
     */
    @Schema(description = "支付宝交易号", example = "2021112222001498680501986123")
    private String tradeNo;
    
    /**
     * 订单状态
     */
    @Schema(description = "订单状态(0-待支付,1-已支付,2-已关闭,3-申请退款,4-已退款,5-退款失败)", example = "1")
    private Integer status;
    
    /**
     * 创建时间起始
     */
    @Schema(description = "创建时间起始", example = "2023-01-01T00:00:00")
    private LocalDateTime createdTimeStart;
    
    /**
     * 创建时间截止
     */
    @Schema(description = "创建时间截止", example = "2023-12-31T23:59:59")
    private LocalDateTime createdTimeEnd;
    
    /**
     * 支付时间起始
     */
    @Schema(description = "支付时间起始", example = "2023-01-01T00:00:00")
    private LocalDateTime paidTimeStart;
    
    /**
     * 支付时间截止
     */
    @Schema(description = "支付时间截止", example = "2023-12-31T23:59:59")
    private LocalDateTime paidTimeEnd;
    
    /**
     * 退款时间起始
     */
    @Schema(description = "退款时间起始", example = "2023-01-01T00:00:00")
    private LocalDateTime refundTimeStart;
    
    /**
     * 退款时间截止
     */
    @Schema(description = "退款时间截止", example = "2023-12-31T23:59:59")
    private LocalDateTime refundTimeEnd;
    
    /**
     * 订单金额最小值
     */
    @Schema(description = "订单金额最小值", example = "0")
    private BigDecimal minAmount;
    
    /**
     * 订单金额最大值
     */
    @Schema(description = "订单金额最大值", example = "1000")
    private BigDecimal maxAmount;
    
    /**
     * 课程ID
     */
    @Schema(description = "课程ID", example = "1")
    private Long courseId;
    
    /**
     * 用户ID（管理员查询使用）
     */
    @Schema(description = "用户ID（仅管理员使用）", example = "1")
    private Long userId;
    
    /**
     * 课程名称（模糊搜索）
     */
    @Schema(description = "课程名称", example = "Java入门")
    private String courseTitle;
    
    /**
     * 用户名称（模糊搜索）
     */
    @Schema(description = "用户名称", example = "张三")
    private String userName;
    
    /**
     * 每页条数
     */
    @Schema(description = "每页条数", example = "10")
    @Builder.Default
    private Integer pageSize = 10;
    
    /**
     * 页码（从1开始）
     */
    @Schema(description = "页码（从1开始）", example = "1")
    @Builder.Default
    private Integer pageNum = 1;
} 