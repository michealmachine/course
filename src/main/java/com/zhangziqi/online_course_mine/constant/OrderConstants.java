package com.zhangziqi.online_course_mine.constant;

/**
 * 订单相关常量
 */
public class OrderConstants {
    
    /**
     * 订单来源
     */
    public static final String SOURCE_PC = "PC";
    public static final String SOURCE_MOBILE = "MOBILE";
    public static final String SOURCE_H5 = "H5";
    public static final String SOURCE_APP = "APP";
    
    /**
     * 支付方式
     */
    public static final String PAYMENT_METHOD_ALIPAY = "ALIPAY";
    public static final String PAYMENT_METHOD_WECHAT = "WECHAT";
    public static final String PAYMENT_METHOD_BALANCE = "BALANCE";
    public static final String PAYMENT_METHOD_FREE = "FREE";
    
    /**
     * 订单类型
     */
    public static final String TYPE_COURSE = "COURSE";
    public static final String TYPE_VIP = "VIP";
    public static final String TYPE_RECHARGE = "RECHARGE";
    
    /**
     * 支付宝交易状态
     */
    public static final String ALIPAY_TRADE_SUCCESS = "TRADE_SUCCESS";
    public static final String ALIPAY_TRADE_FINISHED = "TRADE_FINISHED";
    public static final String ALIPAY_TRADE_CLOSED = "TRADE_CLOSED";
    
    /**
     * 回调成功响应
     */
    public static final String NOTIFY_SUCCESS = "success";
    public static final String NOTIFY_FAIL = "fail";
    
    /**
     * 订单退款原因
     */
    public static final String REFUND_REASON_COURSE_UNPUBLISHED = "课程已下架，系统自动退款";
    public static final String REFUND_REASON_USER_REQUEST = "用户申请退款";
    public static final String REFUND_REASON_ADMIN_OPERATION = "管理员操作退款";
    
    private OrderConstants() {
        // 私有构造函数防止实例化
    }
} 