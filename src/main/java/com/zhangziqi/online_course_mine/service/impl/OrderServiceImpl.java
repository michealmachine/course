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
import com.zhangziqi.online_course_mine.model.dto.order.OrderSearchDTO;
import com.zhangziqi.online_course_mine.model.entity.Course;
import com.zhangziqi.online_course_mine.model.entity.*;
import com.zhangziqi.online_course_mine.model.enums.UserCourseStatus;
import com.zhangziqi.online_course_mine.model.vo.OrderVO;
import com.zhangziqi.online_course_mine.repository.CourseRepository;
import com.zhangziqi.online_course_mine.repository.OrderRepository;
import com.zhangziqi.online_course_mine.repository.UserCourseRepository;
import com.zhangziqi.online_course_mine.repository.UserRepository;
import com.zhangziqi.online_course_mine.service.OrderService;
import com.zhangziqi.online_course_mine.service.impl.RedisOrderService;
import com.zhangziqi.online_course_mine.service.UserCourseService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Objects;
import java.util.Random;
import java.util.ArrayList;
import com.zhangziqi.online_course_mine.model.enums.OrderStatus;
import com.zhangziqi.online_course_mine.constant.OrderConstants;
import java.util.Optional;

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
    private final RedisOrderService redisOrderService;

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
            
            // 设置订单金额 - 考虑折扣价格
            if (course.getDiscountPrice() != null && course.getDiscountPrice().compareTo(BigDecimal.ZERO) > 0) {
                order.setAmount(course.getDiscountPrice());
                log.info("使用折扣价格：{}", course.getDiscountPrice());
            } else {
                order.setAmount(course.getPrice());
                log.info("使用原价：{}", course.getPrice());
            }
            
            order.setStatus(OrderStatus.PENDING.getValue());
            order.setCreatedAt(LocalDateTime.now());
            order.setTitle(course.getTitle());
            
            orderRepository.save(order);
            log.info("付费课程订单创建成功：{}", order.getOrderNo());
            
            // 设置订单超时（30分钟后自动取消）
            redisOrderService.setOrderTimeout(order.getOrderNo(), userId, order.getId());
            
            // 生成支付链接
            String payLink = generatePayLink(order);
            
            OrderVO orderVO = OrderVO.fromEntity(order);
            // 获取订单剩余支付时间并设置
            long remainingTime = redisOrderService.getOrderRemainingTime(order.getOrderNo());
            orderVO.setRemainingTime(remainingTime);
            
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
        
        OrderVO orderVO = OrderVO.fromEntity(order);
        
        // 如果是待支付订单，设置剩余支付时间
        if (OrderStatus.PENDING.getValue() == order.getStatus()) {
            long remainingTime = redisOrderService.getOrderRemainingTime(order.getOrderNo());
            orderVO.setRemainingTime(remainingTime);
        }
        
        return orderVO;
    }

    @Override
    @Transactional(readOnly = true)
    public OrderVO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("订单不存在，ID: " + id));
        
        OrderVO orderVO = OrderVO.fromEntity(order);
        
        // 如果是待支付订单，设置剩余支付时间
        if (OrderStatus.PENDING.getValue() == order.getStatus()) {
            long remainingTime = redisOrderService.getOrderRemainingTime(order.getOrderNo());
            orderVO.setRemainingTime(remainingTime);
        }
        
        return orderVO;
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
        log.info("生成支付链接，订单号：{}，金额：{}", order.getOrderNo(), order.getAmount());
        
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
        // 查询状态为"已支付"和"申请退款"的订单，因为申请退款中的订单仍然应该计入收入
        List<Order> validOrders = orderRepository.findByInstitution_IdAndStatusIn(
                institutionId, 
                List.of(OrderStatus.PAID.getValue(), OrderStatus.REFUNDING.getValue())
        );
        
        return validOrders.stream()
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
        // 只统计状态为"已退款"的订单
        List<Order> refundedOrders = orderRepository.findByInstitution_IdAndStatus(
                institutionId, OrderStatus.REFUNDED.getValue());
        
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
        order.setTitle(course.getTitle());
        
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
        
        // 检查是否存在退款记录
        Optional<UserCourse> refundedRecord = userCourseService.findByUserIdAndCourseIdAndStatus(
                order.getUser().getId(), 
                order.getCourse().getId(), 
                UserCourseStatus.REFUNDED.getValue());
        
        if (refundedRecord.isPresent()) {
            // 如果存在退款记录，更新为正常状态
            UserCourse userCourse = refundedRecord.get();
            userCourse.setStatusEnum(UserCourseStatus.NORMAL);
            userCourse.setOrder(order);
            userCourse.setPurchasedAt(LocalDateTime.now());
            userCourseService.save(userCourse);
            log.info("更新退款记录为正常状态，用户课程ID：{}", userCourse.getId());
        } else {
            // 如果不存在退款记录，创建新的用户课程关系
            userCourseService.createUserCourseRelation(
                    order.getUser().getId(), 
                    order.getCourse().getId(), 
                    order.getId(), 
                    true);
            log.info("创建新的用户课程关系");
        }
        
        // 取消订单超时
        redisOrderService.cancelOrderTimeout(orderNo);
        
        log.info("支付成功处理完成，订单号：{}", orderNo);
    }

    /**
     * 取消订单
     *
     * @param id 订单ID
     * @param userId 用户ID
     * @return 取消后的订单VO
     */
    @Override
    @Transactional
    public OrderVO cancelOrder(Long id, Long userId) {
        log.info("取消订单，订单ID：{}，用户ID：{}", id, userId);
        
        // 查询订单
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("订单不存在，ID: " + id));
        
        // 验证用户权限
        if (!order.getUser().getId().equals(userId)) {
            throw new BusinessException(403, "无权操作此订单");
        }
        
        // 检查订单状态
        if (order.getStatus() != OrderStatus.PENDING.getValue()) {
            throw new BusinessException(400, "当前订单状态不允许取消");
        }
        
        // 更新订单状态
        order.setStatus(OrderStatus.CLOSED.getValue());
        orderRepository.save(order);
        
        // 取消Redis中的订单超时
        if (order.getOrderNo() != null) {
            redisOrderService.cancelOrderTimeout(order.getOrderNo());
        }
        
        log.info("订单取消成功，订单ID：{}", id);
        
        return OrderVO.fromEntity(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderVO> searchUserOrders(OrderSearchDTO searchDTO, Long userId) {
        log.info("搜索用户订单，用户ID: {}, 搜索条件: {}", userId, searchDTO);
        
        // 创建分页对象
        Pageable pageable = PageRequest.of(searchDTO.getPageNum() - 1, searchDTO.getPageSize());
        
        // 构建查询条件
        Specification<Order> spec = buildOrderSpecification(searchDTO);
        
        // 添加用户ID的查询条件
        spec = spec.and((root, query, cb) -> cb.equal(root.get("user").get("id"), userId));
        
        // 执行查询
        Page<Order> orderPage = orderRepository.findAll(spec, pageable);
        
        log.info("用户订单搜索结果: 共{}条记录", orderPage.getTotalElements());
        
        // 转换为VO并为待支付订单设置剩余支付时间
        Page<OrderVO> result = orderPage.map(order -> {
            OrderVO orderVO = OrderVO.fromEntity(order);
            
            // 如果是待支付订单，设置剩余支付时间
            if (OrderStatus.PENDING.getValue() == order.getStatus()) {
                long remainingTime = redisOrderService.getOrderRemainingTime(order.getOrderNo());
                orderVO.setRemainingTime(remainingTime);
            }
            
            return orderVO;
        });
        
        return result;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<OrderVO> searchInstitutionOrders(OrderSearchDTO searchDTO, Long institutionId) {
        log.info("搜索机构订单，机构ID: {}, 搜索条件: {}", institutionId, searchDTO);
        
        // 创建分页对象
        Pageable pageable = PageRequest.of(searchDTO.getPageNum() - 1, searchDTO.getPageSize());
        
        // 构建查询条件
        Specification<Order> spec = buildOrderSpecification(searchDTO);
        
        // 添加机构ID的查询条件
        spec = spec.and((root, query, cb) -> cb.equal(root.get("institution").get("id"), institutionId));
        
        // 执行查询
        Page<Order> orderPage = orderRepository.findAll(spec, pageable);
        
        log.info("机构订单搜索结果: 共{}条记录", orderPage.getTotalElements());
        
        // 转换为VO并设置剩余支付时间
        Page<OrderVO> result = orderPage.map(order -> {
            OrderVO orderVO = OrderVO.fromEntity(order);
            
            // 如果是待支付订单，设置剩余支付时间
            if (OrderStatus.PENDING.getValue() == order.getStatus()) {
                long remainingTime = redisOrderService.getOrderRemainingTime(order.getOrderNo());
                orderVO.setRemainingTime(remainingTime);
            }
            
            return orderVO;
        });
        
        return result;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<OrderVO> searchAllOrders(OrderSearchDTO searchDTO) {
        log.info("搜索所有订单，搜索条件: {}", searchDTO);
        
        // 创建分页对象
        Pageable pageable = PageRequest.of(searchDTO.getPageNum() - 1, searchDTO.getPageSize());
        
        // 构建查询条件
        Specification<Order> spec = buildOrderSpecification(searchDTO);
        
        // 执行查询
        Page<Order> orderPage = orderRepository.findAll(spec, pageable);
        
        log.info("所有订单搜索结果: 共{}条记录", orderPage.getTotalElements());
        
        // 转换为VO并设置剩余支付时间
        Page<OrderVO> result = orderPage.map(order -> {
            OrderVO orderVO = OrderVO.fromEntity(order);
            
            // 如果是待支付订单，设置剩余支付时间
            if (OrderStatus.PENDING.getValue() == order.getStatus()) {
                long remainingTime = redisOrderService.getOrderRemainingTime(order.getOrderNo());
                orderVO.setRemainingTime(remainingTime);
            }
            
            return orderVO;
        });
        
        return result;
    }
    
    /**
     * 获取机构待处理退款申请
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderVO> getInstitutionPendingRefunds(Long institutionId) {
        log.info("获取机构待处理退款申请, 机构ID: {}", institutionId);
        
        // 查询状态为"退款中"的订单
        List<Order> pendingRefunds = orderRepository.findByInstitution_IdAndStatus(
                institutionId, OrderStatus.REFUNDING.getValue());
        
        log.info("机构待处理退款申请数量: {}", pendingRefunds.size());
        
        // 转换为VO
        return pendingRefunds.stream()
                .map(order -> {
                    OrderVO orderVO = OrderVO.fromEntity(order);
                    
                    // 如果是待支付订单，设置剩余支付时间
                    if (OrderStatus.PENDING.getValue() == order.getStatus()) {
                        long remainingTime = redisOrderService.getOrderRemainingTime(order.getOrderNo());
                        orderVO.setRemainingTime(remainingTime);
                    }
                    
                    return orderVO;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 构建订单查询条件
     */
    private Specification<Order> buildOrderSpecification(OrderSearchDTO searchDTO) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 订单号模糊查询
            if (StringUtils.hasText(searchDTO.getOrderNo())) {
                predicates.add(cb.like(root.get("orderNo"), "%" + searchDTO.getOrderNo() + "%"));
            }
            
            // 交易号模糊查询
            if (StringUtils.hasText(searchDTO.getTradeNo())) {
                predicates.add(cb.like(root.get("tradeNo"), "%" + searchDTO.getTradeNo() + "%"));
            }
            
            // 订单状态查询
            if (searchDTO.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), searchDTO.getStatus()));
            }
            
            // 创建时间范围查询
            if (searchDTO.getCreatedTimeStart() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), searchDTO.getCreatedTimeStart()));
            }
            if (searchDTO.getCreatedTimeEnd() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), searchDTO.getCreatedTimeEnd()));
            }
            
            // 支付时间范围查询
            if (searchDTO.getPaidTimeStart() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("paidAt"), searchDTO.getPaidTimeStart()));
            }
            if (searchDTO.getPaidTimeEnd() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("paidAt"), searchDTO.getPaidTimeEnd()));
            }
            
            // 退款时间范围查询
            if (searchDTO.getRefundTimeStart() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("refundedAt"), searchDTO.getRefundTimeStart()));
            }
            if (searchDTO.getRefundTimeEnd() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("refundedAt"), searchDTO.getRefundTimeEnd()));
            }
            
            // 订单金额范围查询
            if (searchDTO.getMinAmount() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), searchDTO.getMinAmount()));
            }
            if (searchDTO.getMaxAmount() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("amount"), searchDTO.getMaxAmount()));
            }
            
            // 课程ID查询
            if (searchDTO.getCourseId() != null) {
                predicates.add(cb.equal(root.get("course").get("id"), searchDTO.getCourseId()));
            }
            
            // 用户ID查询（仅管理员使用）
            if (searchDTO.getUserId() != null) {
                predicates.add(cb.equal(root.get("user").get("id"), searchDTO.getUserId()));
            }
            
            // 课程名称模糊查询
            if (StringUtils.hasText(searchDTO.getCourseTitle())) {
                predicates.add(cb.like(root.get("course").get("title"), "%" + searchDTO.getCourseTitle() + "%"));
            }
            
            // 用户名称模糊查询
            if (StringUtils.hasText(searchDTO.getUserName())) {
                predicates.add(cb.like(root.get("user").get("username"), "%" + searchDTO.getUserName() + "%"));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * 生成支付表单
     */
    @Override
    public String generatePaymentForm(String orderNo) {
        log.info("生成订单支付表单，订单号：{}", orderNo);
        
        // 查询订单
        Order order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new ResourceNotFoundException("订单不存在，订单号: " + orderNo));
        
        // 验证订单状态是否为待支付
        if (order.getStatus() != OrderStatus.PENDING.getValue()) {
            throw new BusinessException(400, "当前订单状态不支持支付");
        }
        
        // 检查订单是否已超时
        Long remainingTime = redisOrderService.getOrderRemainingTime(orderNo);
        if (remainingTime <= 0) {
            // 更新订单状态为已关闭
            order.setStatus(OrderStatus.CLOSED.getValue());
            orderRepository.save(order);
            throw new BusinessException(400, "订单已超时，请重新下单");
        }
        
        // 生成支付表单
        return generatePayLink(order);
    }
}