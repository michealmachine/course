package com.zhangziqi.online_course_mine.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.zhangziqi.online_course_mine.config.AlipayConfig;
import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.exception.ResourceNotFoundException;
import com.zhangziqi.online_course_mine.model.dto.order.OrderRefundDTO;
import com.zhangziqi.online_course_mine.model.entity.Course;
import com.zhangziqi.online_course_mine.model.entity.*;
import com.zhangziqi.online_course_mine.model.vo.OrderVO;
import com.zhangziqi.online_course_mine.repository.CourseRepository;
import com.zhangziqi.online_course_mine.repository.OrderRepository;
import com.zhangziqi.online_course_mine.repository.UserCourseRepository;
import com.zhangziqi.online_course_mine.repository.UserRepository;
import com.zhangziqi.online_course_mine.service.OrderService;
import com.zhangziqi.online_course_mine.service.UserCourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Objects;
import java.util.Random;
import com.zhangziqi.online_course_mine.model.enums.OrderStatus;
import com.zhangziqi.online_course_mine.constant.OrderConstants;

/**
 * 订单服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final UserCourseRepository userCourseRepository;
    private final UserCourseService userCourseService;
    private final AlipayClient alipayClient;
    private final AlipayConfig alipayConfig;

    @Override
    @Transactional
    public OrderVO createOrder(Long courseId, Long userId) {
        log.info("创建订单，课程ID：{}，用户ID：{}", courseId, userId);
        
        // 获取用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        
        // 查询课程
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在"));
        
        // 验证课程是否已发布
        if (course.getPublishedVersionId() == null) {
            throw new BusinessException(400, "课程未发布，无法购买");
        }
        
        // 验证用户是否已购买该课程
        if (userCourseService.hasPurchasedCourse(userId, courseId)) {
            throw new BusinessException(400, "您已购买该课程，请勿重复购买");
        }
        
        Order order;
        
        // 根据课程价格处理
        if (course.getPrice().compareTo(BigDecimal.ZERO) == 0) {
            // 免费课程直接创建已支付订单
            order = createFreeCourseOrder(user, course);
            return OrderVO.fromEntity(order);
        } else {
            // 创建付费订单
            order = new Order();
            order.setOrderNo(generateOrderNo());
            order.setUser(user);
            order.setCourse(course);
            order.setInstitution(course.getInstitution());
            order.setAmount(course.getPrice());
            order.setStatus(OrderStatus.CREATED);
            order.setCreatedAt(LocalDateTime.now());
            
            orderRepository.save(order);
            log.info("付费课程订单创建成功：{}", order.getOrderNo());
            
            // 生成支付链接
            String payLink = generatePayLink(order);
            
            OrderVO orderVO = OrderVO.fromEntity(order);
            orderVO.setPayUrl(payLink);
            
            return orderVO;
        }
    }

    @Override
    @Transactional
    public String handleAlipayReturn(Map<String, String> params) {
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
                
                // 处理订单状态
                Order order = orderRepository.findByOrderNo(outTradeNo)
                        .orElseThrow(() -> new ResourceNotFoundException("订单不存在，订单号: " + outTradeNo));
                
                // 如果订单未支付，标记为已支付
                if (order.getStatus() == OrderStatus.PENDING.getValue()) {
                    order.setStatus(OrderStatus.PAID.getValue());
                    order.setTradeNo(tradeNo);
                    order.setPaidAt(LocalDateTime.now());
                    orderRepository.save(order);
                    
                    // 创建用户课程关系
                    userCourseService.createUserCourseRelation(
                            order.getUser().getId(),
                            order.getCourse().getId(),
                            order.getId(),
                            true);
                }
                
                return "支付成功！订单号: " + outTradeNo;
            } else {
                log.warn("支付宝同步回调验签失败");
                return "验签失败";
            }
        } catch (AlipayApiException e) {
            log.error("支付宝同步回调验签异常", e);
            return "验签异常: " + e.getMessage();
        }
    }

    @Override
    @Transactional
    public String handleAlipayNotify(Map<String, String> params) {
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
                // 交易状态
                String tradeStatus = params.get("trade_status");
                // 订单号
                String outTradeNo = params.get("out_trade_no");
                // 支付宝交易号
                String tradeNo = params.get("trade_no");
                // 总金额
                String totalAmount = params.get("total_amount");
                
                log.info("交易状态:{}, 订单号:{}, 支付宝交易号:{}, 金额:{}",
                        tradeStatus, outTradeNo, tradeNo, totalAmount);
                
                // 查询订单
                Order order = orderRepository.findByOrderNo(outTradeNo)
                        .orElseThrow(() -> new ResourceNotFoundException("订单不存在，订单号: " + outTradeNo));
                
                // 根据交易状态处理订单
                if (OrderConstants.ALIPAY_TRADE_SUCCESS.equals(tradeStatus) 
                        || OrderConstants.ALIPAY_TRADE_FINISHED.equals(tradeStatus)) {
                    // 如果订单未支付，标记为已支付
                    if (order.getStatus() == OrderStatus.PENDING.getValue()) {
                        order.setStatus(OrderStatus.PAID.getValue());
                        order.setTradeNo(tradeNo);
                        order.setPaidAt(LocalDateTime.now());
                        orderRepository.save(order);
                        
                        // 创建用户课程关系
                        userCourseService.createUserCourseRelation(
                                order.getUser().getId(),
                                order.getCourse().getId(),
                                order.getId(),
                                true);
                    }
                } else if (OrderConstants.ALIPAY_TRADE_CLOSED.equals(tradeStatus)) {
                    // 交易关闭
                    if (order.getStatus() == OrderStatus.PENDING.getValue()) {
                        order.setStatus(OrderStatus.CLOSED.getValue());
                        orderRepository.save(order);
                    }
                }
                
                return OrderConstants.NOTIFY_SUCCESS;
            } else {
                log.warn("支付宝异步通知验签失败");
                return OrderConstants.NOTIFY_FAIL;
            }
        } catch (AlipayApiException e) {
            log.error("支付宝异步通知验签异常", e);
            return OrderConstants.NOTIFY_FAIL;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public OrderVO getOrderByOrderNo(String orderNo) {
        Order order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new ResourceNotFoundException("订单不存在，订单号: " + orderNo));
        
        return OrderVO.fromEntity(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderVO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("订单不存在，ID: " + id));
        
        return OrderVO.fromEntity(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderVO> getUserOrders(Long userId) {
        List<Order> orders = orderRepository.findByUser_Id(userId);
        
        return orders.stream()
                .map(OrderVO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderVO> getUserOrders(Long userId, Pageable pageable) {
        Page<Order> orderPage = orderRepository.findByUser_Id(userId, pageable);
        
        return orderPage.map(OrderVO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderVO> getInstitutionOrders(Long institutionId) {
        List<Order> orders = orderRepository.findByInstitution_Id(institutionId);
        
        return orders.stream()
                .map(OrderVO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderVO> getInstitutionOrders(Long institutionId, Pageable pageable) {
        Page<Order> orderPage = orderRepository.findByInstitution_Id(institutionId, pageable);
        
        return orderPage.map(OrderVO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderVO> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        
        return orders.stream()
                .map(OrderVO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderVO> getAllOrders(Pageable pageable) {
        Page<Order> orderPage = orderRepository.findAll(pageable);
        
        return orderPage.map(OrderVO::fromEntity);
    }

    @Override
    @Transactional
    public OrderVO refundOrder(Long id, OrderRefundDTO dto, Long userId) {
        // 查询订单
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("订单不存在，ID: " + id));
        
        // 验证是否为订单所有者
        if (!order.getUser().getId().equals(userId)) {
            throw new BusinessException(403, "无权操作此订单");
        }
        
        // 验证订单状态是否为已支付
        if (order.getStatus() != OrderStatus.PAID.getValue()) {
            throw new BusinessException(400, "当前订单状态不支持退款");
        }
        
        // 设置退款金额（如果未指定则默认全额退款）
        BigDecimal refundAmount = dto.getRefundAmount();
        if (refundAmount == null) {
            refundAmount = order.getAmount();
        }
        
        // 验证退款金额不超过订单金额
        if (refundAmount.compareTo(order.getAmount()) > 0) {
            throw new BusinessException(400, "退款金额不能超过订单金额");
        }
        
        // 更新订单状态为申请退款
        order.setStatus(OrderStatus.REFUNDING.getValue());
        order.setRefundAmount(refundAmount);
        order.setRefundReason(dto.getRefundReason());
        
        Order updatedOrder = orderRepository.save(order);
        
        return OrderVO.fromEntity(updatedOrder);
    }

    @Override
    @Transactional
    public OrderVO processRefund(Long id, boolean approved, Long operatorId) {
        // 查询订单
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("订单不存在，ID: " + id));
        
        // 验证订单状态是否为申请退款
        if (order.getStatus() != OrderStatus.REFUNDING.getValue()) {
            throw new BusinessException(400, "当前订单状态不支持处理退款");
        }
        
        if (approved) {
            log.info("管理员(ID:{})批准订单(ID:{})的退款申请", operatorId, id);
            
            // 调用支付宝退款接口
            boolean refundSuccess = executeAlipayRefund(
                    order.getOrderNo(), 
                    order.getRefundAmount(), 
                    order.getRefundReason());
            
            if (!refundSuccess) {
                // 退款失败
                order.setStatus(OrderStatus.REFUND_FAILED.getValue());
                log.warn("订单(ID:{})退款失败", id);
            } else {
                // 退款成功，状态已在executeAlipayRefund方法中更新
                log.info("订单(ID:{})退款成功", id);
            }
        } else {
            // 拒绝退款
            log.info("管理员(ID:{})拒绝订单(ID:{})的退款申请", operatorId, id);
            order.setStatus(OrderStatus.REFUND_FAILED.getValue());
        }
        
        Order updatedOrder = orderRepository.save(order);
        
        return OrderVO.fromEntity(updatedOrder);
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        // 使用时间戳+随机数生成订单号
        return System.currentTimeMillis() + String.format("%04d", new Random().nextInt(10000));
    }

    /**
     * 生成支付宝支付链接
     */
    private String generatePayLink(Order order) {
        log.info("生成支付链接，订单号：{}", order.getOrderNo());
        
        try {
            // 创建API对应的request
            AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
            
            // 设置回调地址
            alipayRequest.setNotifyUrl(alipayConfig.getNotifyUrl());
            
            // 设置请求参数
            Map<String, Object> bizContent = new HashMap<>();
            bizContent.put("out_trade_no", order.getOrderNo());
            bizContent.put("total_amount", order.getAmount().toString());
            bizContent.put("subject", order.getCourse().getTitle());
            bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
            
            alipayRequest.setBizContent(com.alibaba.fastjson.JSON.toJSONString(bizContent));
            
            // 调用API
            String form = alipayClient.pageExecute(alipayRequest).getBody();
            log.info("支付宝支付链接生成成功");
            
            return form;
        } catch (AlipayApiException e) {
            log.error("生成支付链接异常", e);
            throw new BusinessException(500, "生成支付链接失败：" + e.getMessage());
        }
    }

    /**
     * 统计机构总收入（不含退款）
     */
    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateInstitutionTotalIncome(Long institutionId) {
        List<Order> paidOrders = orderRepository.findByInstitution_IdAndStatus(institutionId, OrderStatus.PAID.getValue());
        
        return paidOrders.stream()
                .map(Order::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 统计机构总退款
     */
    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateInstitutionTotalRefund(Long institutionId) {
        List<Order> refundedOrders = orderRepository.findByInstitution_IdAndStatus(institutionId, OrderStatus.REFUNDED.getValue());
        
        return refundedOrders.stream()
                .map(Order::getRefundAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 获取机构净收入
     */
    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateInstitutionNetIncome(Long institutionId) {
        BigDecimal totalIncome = calculateInstitutionTotalIncome(institutionId);
        BigDecimal totalRefund = calculateInstitutionTotalRefund(institutionId);
        
        return totalIncome.subtract(totalRefund);
    }
    
    /**
     * 执行支付宝退款
     */
    @Override
    @Transactional
    public boolean executeAlipayRefund(String orderNo, BigDecimal refundAmount, String refundReason) {
        log.info("执行支付宝退款，订单号：{}，退款金额：{}，退款原因：{}", orderNo, refundAmount, refundReason);
        
        try {
            // 查询订单
            Order order = orderRepository.findByOrderNo(orderNo)
                    .orElseThrow(() -> new ResourceNotFoundException("订单不存在，订单号: " + orderNo));
            
            // 验证订单状态
            if (order.getStatus() != OrderStatus.PAID.getValue() && order.getStatus() != OrderStatus.REFUNDING.getValue()) {
                log.warn("订单状态不适合退款，订单号：{}，当前状态：{}", orderNo, order.getStatus());
                return false;
            }
            
            // 验证退款金额
            if (refundAmount.compareTo(order.getAmount()) > 0) {
                log.warn("退款金额大于订单金额，订单号：{}，订单金额：{}，退款金额：{}", 
                        orderNo, order.getAmount(), refundAmount);
                return false;
            }
            
            // 设置退款参数并调用支付宝退款接口
            // 创建API对应的request
            AlipayTradeRefundRequest alipayRequest = new AlipayTradeRefundRequest();
            
            // 设置请求参数
            Map<String, Object> bizContent = new HashMap<>();
            bizContent.put("out_trade_no", orderNo);
            bizContent.put("refund_amount", refundAmount.toString());
            bizContent.put("refund_reason", refundReason);
            
            alipayRequest.setBizContent(com.alibaba.fastjson.JSON.toJSONString(bizContent));
            
            // 调用API
            try {
                AlipayTradeRefundResponse response = alipayClient.execute(alipayRequest);
                if (response.isSuccess()) {
                    log.info("支付宝退款成功，订单号：{}，退款金额：{}", orderNo, refundAmount);
                    
                    // 更新订单状态
                    order.setStatus(OrderStatus.REFUNDED.getValue());
                    order.setRefundAmount(refundAmount);
                    order.setRefundReason(refundReason);
                    order.setRefundedAt(LocalDateTime.now());
                    orderRepository.save(order);
                    
                    // 更新用户课程状态为已退款
                    userCourseService.updateUserCourseRefunded(order.getId());
                    
                    return true;
                } else {
                    log.error("支付宝退款接口调用失败，订单号：{}，错误码：{}，错误信息：{}", 
                            orderNo, response.getCode(), response.getMsg());
                    return false;
                }
            } catch (AlipayApiException e) {
                log.error("调用支付宝退款接口异常", e);
                return false;
            }
        } catch (Exception e) {
            log.error("执行支付宝退款过程发生异常", e);
            return false;
        }
    }

    /**
     * 创建免费课程订单
     */
    private Order createFreeCourseOrder(User user, Course course) {
        log.info("创建免费课程订单，用户：{}，课程：{}", user.getId(), course.getId());
        
        // 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUser(user);
        order.setCourse(course);
        order.setInstitution(course.getInstitution());
        order.setAmount(BigDecimal.ZERO);
        order.setStatus(OrderStatus.PAID.getValue()); // 免费课程直接标记为已支付
        order.setCreatedAt(LocalDateTime.now());
        order.setPaidAt(LocalDateTime.now());
        
        // 保存订单
        orderRepository.save(order);
        
        // 创建用户课程关系
        userCourseService.createUserCourseRelation(user.getId(), course.getId(), order.getId(), true);
        
        log.info("免费课程订单创建成功：{}", order.getOrderNo());
        return order;
    }
    
    /**
     * 处理支付成功回调
     */
    @Override
    @Transactional
    public void handlePaymentSuccess(String orderNo) {
        log.info("处理支付成功回调，订单号：{}", orderNo);
        
        // 查询订单
        Order order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new ResourceNotFoundException("订单不存在，订单号: " + orderNo));
        
        // 检查订单状态
        if (order.getStatus() == OrderStatus.PAID.getValue()) {
            log.info("订单已是支付状态，无需重复处理，订单号：{}", orderNo);
            return;
        }
        
        // 更新订单状态
        order.setStatus(OrderStatus.PAID.getValue());
        order.setPaidAt(LocalDateTime.now());
        orderRepository.save(order);
        
        // 创建用户课程关系并标记为已支付
        userCourseService.createUserCourseRelation(
                order.getUser().getId(), 
                order.getCourse().getId(), 
                order.getId(), 
                true);
        
        log.info("支付成功处理完成，订单号：{}", orderNo);
    }
} 