package com.zhangziqi.online_course_mine.model.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 订单退款数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRefundDTO {
    
    /**
     * 退款金额（默认全额退款）
     */
    private BigDecimal refundAmount;
    
    /**
     * 退款原因
     */
    @NotBlank(message = "退款原因不能为空")
    @Size(max = 500, message = "退款原因不能超过500个字符")
    private String refundReason;
} 