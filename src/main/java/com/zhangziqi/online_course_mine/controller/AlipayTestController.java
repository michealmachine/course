package com.zhangziqi.online_course_mine.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.zhangziqi.online_course_mine.config.AlipayConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/alipay")
@RequiredArgsConstructor
public class AlipayTestController {

    private final AlipayClient alipayClient;
    private final AlipayConfig alipayConfig;

    /**
     * 生成支付宝支付链接
     */
    @GetMapping("/pay")
    public String pay(@RequestParam(defaultValue = "0.01") BigDecimal amount) {
        try {
            // 创建API对应的request
            AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
            
            // 设置回调地址
            alipayRequest.setNotifyUrl(alipayConfig.getNotifyUrl());
            alipayRequest.setReturnUrl("http://localhost:8080/api/alipay/return");
            
            // 生成订单号
            String outTradeNo = UUID.randomUUID().toString().replace("-", "");
            
            // 设置请求参数
            Map<String, Object> bizContent = new HashMap<>();
            bizContent.put("out_trade_no", outTradeNo);
            bizContent.put("total_amount", amount);
            bizContent.put("subject", "测试商品");
            bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
            
            alipayRequest.setBizContent(com.alibaba.fastjson.JSON.toJSONString(bizContent));
            
            // 调用SDK生成支付链接
            String form = alipayClient.pageExecute(alipayRequest).getBody();
            log.info("生成支付表单: {}", form);
            
            return form;
        } catch (AlipayApiException e) {
            log.error("支付宝支付错误", e);
            return "支付宝接口调用异常: " + e.getMessage();
        }
    }

    /**
     * 支付宝同步回调接口
     */
    @GetMapping("/return")
    public String returnUrl(@RequestParam Map<String, String> params) {
        log.info("支付宝同步回调参数: {}", params);
        try {
            // 验证签名
            boolean signVerified = AlipaySignature.rsaCheckV1(
                params, 
                alipayConfig.getAlipayPublicKey(), 
                "UTF-8", 
                "RSA2"
            );
            
            if (signVerified) {
                String outTradeNo = params.get("out_trade_no");
                String tradeNo = params.get("trade_no");
                String totalAmount = params.get("total_amount");
                
                log.info("验签成功，订单号: {}, 支付宝交易号: {}, 金额: {}", outTradeNo, tradeNo, totalAmount);
                return "支付成功！订单号: " + outTradeNo;
            } else {
                log.warn("验签失败");
                return "验签失败";
            }
        } catch (AlipayApiException e) {
            log.error("支付宝回调验签异常", e);
            return "验签异常: " + e.getMessage();
        }
    }

    /**
     * 支付宝异步通知接口
     */
    @PostMapping("/notify")
    public String notifyUrl(@RequestParam Map<String, String> params) {
        log.info("支付宝异步通知参数: {}", params);
        try {
            // 验证签名
            boolean signVerified = AlipaySignature.rsaCheckV1(
                params, 
                alipayConfig.getAlipayPublicKey(), 
                "UTF-8", 
                "RSA2"
            );
            
            if (signVerified) {
                // 验签成功
                String outTradeNo = params.get("out_trade_no");
                String tradeNo = params.get("trade_no");
                String tradeStatus = params.get("trade_status");
                String totalAmount = params.get("total_amount");
                
                log.info("异步通知验签成功，订单号: {}, 支付宝交易号: {}, 交易状态: {}, 金额: {}", 
                         outTradeNo, tradeNo, tradeStatus, totalAmount);
                
                // 判断交易状态
                if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                    // 支付成功，执行业务逻辑
                    log.info("订单 {} 支付成功，金额: {}", outTradeNo, totalAmount);
                    // TODO: 更新订单状态，创建支付记录等
                }
                
                return "success";  // 要返回success，表示接收成功
            } else {
                log.warn("异步通知验签失败");
                return "fail";
            }
        } catch (AlipayApiException e) {
            log.error("支付宝异步通知验签异常", e);
            return "fail";
        }
    }
} 