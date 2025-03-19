package com.zhangziqi.online_course_mine.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.zhangziqi.online_course_mine.config.AlipayConfig;
import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.exception.ResourceNotFoundException;
import com.zhangziqi.online_course_mine.model.dto.order.OrderRefundDTO;
import com.zhangziqi.online_course_mine.model.dto.order.OrderSearchDTO;
import com.zhangziqi.online_course_mine.model.entity.Course;
import com.zhangziqi.online_course_mine.model.entity.Institution;
import com.zhangziqi.online_course_mine.model.entity.Order;
import com.zhangziqi.online_course_mine.model.entity.User;
import com.zhangziqi.online_course_mine.model.enums.CoursePaymentType;
import com.zhangziqi.online_course_mine.model.enums.CourseStatus;
import com.zhangziqi.online_course_mine.model.enums.OrderStatus;
import com.zhangziqi.online_course_mine.model.vo.OrderVO;
import com.zhangziqi.online_course_mine.repository.CourseRepository;
import com.zhangziqi.online_course_mine.repository.OrderRepository;
import com.zhangziqi.online_course_mine.repository.UserCourseRepository;
import com.zhangziqi.online_course_mine.repository.UserRepository;
import com.zhangziqi.online_course_mine.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserCourseRepository userCourseRepository;

    @Mock
    private UserCourseService userCourseService;

    @Mock
    private AlipayClient alipayClient;

    @Mock
    private AlipayConfig alipayConfig;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User testUser;
    private Course testCourse;
    private Order testOrder;
    private Institution testInstitution;

    @BeforeEach
    void setUp() {
        // 创建测试机构
        testInstitution = Institution.builder()
                .id(1L)
                .name("测试机构")
                .build();

        // 创建测试用户
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();

        // 创建测试课程
        testCourse = Course.builder()
                .id(1L)
                .title("测试课程")
                .description("这是一个测试课程")
                .institution(testInstitution)
                .creatorId(1L)
                .status(CourseStatus.PUBLISHED.getValue())
                .paymentType(CoursePaymentType.PAID.getValue())
                .price(BigDecimal.valueOf(99.99))
                .publishedVersionId(1L)
                .studentCount(10)
                .build();

        // 创建测试订单
        testOrder = Order.builder()
                .id(1L)
                .orderNo("TEST12345678")
                .user(testUser)
                .course(testCourse)
                .institution(testInstitution)
                .amount(BigDecimal.valueOf(99.99))
                .status(OrderStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .build();

        // 创建OrderService实例
        orderService = new OrderServiceImpl(
                orderRepository,
                userRepository,
                courseRepository,
                userCourseRepository,
                userCourseService,
                alipayClient,
                alipayConfig
        );
    }

    @Test
    @DisplayName("创建订单 - 付费课程成功")
    void createOrder_PaidCourseSuccess() throws AlipayApiException {
        // 修改课程为付费课程
        testCourse.setPaymentType(CoursePaymentType.PAID.getValue());
        testCourse.setPrice(new BigDecimal("100.00"));
        
        // 修改订单状态为待支付
        testOrder.setStatus(OrderStatus.PENDING.getValue());
        testOrder.setPaidAt(null);
        
        // 模拟支付宝支付链接生成
        AlipayTradePagePayResponse mockResponse = mock(AlipayTradePagePayResponse.class);
        lenient().when(mockResponse.isSuccess()).thenReturn(true);
        when(mockResponse.getBody()).thenReturn("https://example.com/pay");
        when(alipayClient.pageExecute(any(AlipayTradePagePayRequest.class))).thenReturn(mockResponse);
        
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(testCourse));
        when(userCourseService.hasPurchasedCourse(anyLong(), anyLong())).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setOrderNo("TEST12345678");
            return testOrder;
        });
        
        // 执行方法
        OrderVO result = orderService.createOrder(testCourse.getId(), testUser.getId());
        
        // 验证结果
        assertNotNull(result);
        assertEquals("TEST12345678", result.getOrderNo());
        assertEquals(testUser.getId(), result.getUserId());
        assertEquals(testCourse.getId(), result.getCourseId());
        assertEquals(new BigDecimal("100.00"), result.getAmount());
        assertEquals(OrderStatus.PENDING.getValue(), result.getStatus());
        
        // 验证方法调用
        verify(userRepository).findById(testUser.getId());
        verify(courseRepository).findById(testCourse.getId());
        verify(userCourseService).hasPurchasedCourse(testUser.getId(), testCourse.getId());
        verify(orderRepository).save(any(Order.class));
        verify(userCourseService, never()).createUserCourseRelation(anyLong(), anyLong(), any(), anyBoolean());
        verify(alipayClient).pageExecute(any(AlipayTradePagePayRequest.class));
    }

    @Test
    @DisplayName("创建订单 - 免费课程成功")
    void createOrder_FreeCourseSuccess() {
        // 修改课程为免费课程
        testCourse.setPaymentType(CoursePaymentType.FREE.getValue());
        testCourse.setPrice(BigDecimal.ZERO);
        
        // 修改订单状态为已支付
        testOrder.setStatus(OrderStatus.PAID.getValue());
        testOrder.setPaidAt(LocalDateTime.now());
        
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(testCourse));
        when(userCourseService.hasPurchasedCourse(anyLong(), anyLong())).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setOrderNo("TEST12345678");
            return testOrder;
        });
        when(userCourseService.createUserCourseRelation(anyLong(), anyLong(), any(), anyBoolean())).thenReturn(null);
        
        // 执行方法
        OrderVO result = orderService.createOrder(testCourse.getId(), testUser.getId());
        
        // 验证结果
        assertNotNull(result);
        assertEquals("TEST12345678", result.getOrderNo());
        assertEquals(testUser.getId(), result.getUserId());
        assertEquals(testCourse.getId(), result.getCourseId());
        assertEquals(BigDecimal.ZERO, result.getAmount());
        assertEquals(OrderStatus.PAID.getValue(), result.getStatus());
        
        // 验证方法调用
        verify(userRepository).findById(testUser.getId());
        verify(courseRepository).findById(testCourse.getId());
        verify(userCourseService).hasPurchasedCourse(testUser.getId(), testCourse.getId());
        verify(orderRepository).save(any(Order.class));
        verify(userCourseService).createUserCourseRelation(eq(testUser.getId()), eq(testCourse.getId()), any(), eq(true));
    }

    @Test
    @DisplayName("创建订单 - 用户不存在")
    void createOrder_UserNotFound() {
        // 准备测试数据
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        // 验证抛出异常
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, 
                () -> orderService.createOrder(testCourse.getId(), testUser.getId()));
        
        assertTrue(exception.getMessage().contains("用户不存在"));
        
        // 验证方法调用
        verify(userRepository).findById(testUser.getId());
        verify(courseRepository, never()).findById(anyLong());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("创建订单 - 课程不存在")
    void createOrder_CourseNotFound() {
        // 准备测试数据
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(courseRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        // 验证抛出异常
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, 
                () -> orderService.createOrder(testCourse.getId(), testUser.getId()));
        
        assertTrue(exception.getMessage().contains("课程不存在"));
        
        // 验证方法调用
        verify(userRepository).findById(testUser.getId());
        verify(courseRepository).findById(testCourse.getId());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("创建订单 - 课程未发布")
    void createOrder_CourseNotPublished() {
        // 准备测试数据
        testCourse.setPublishedVersionId(null);
        
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(testCourse));
        
        // 验证抛出异常
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> orderService.createOrder(testCourse.getId(), testUser.getId()));
        
        assertTrue(exception.getMessage().contains("课程未发布，无法购买"));
        
        // 验证方法调用
        verify(userRepository).findById(testUser.getId());
        verify(courseRepository).findById(testCourse.getId());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("创建订单 - 用户已购买该课程")
    void createOrder_UserAlreadyPurchased() {
        // 准备测试数据
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(testCourse));
        when(userCourseService.hasPurchasedCourse(anyLong(), anyLong())).thenReturn(true);
        
        // 验证抛出异常
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> orderService.createOrder(testCourse.getId(), testUser.getId()));
        
        assertTrue(exception.getMessage().contains("您已购买该课程，请勿重复购买"));
        
        // 验证方法调用
        verify(userRepository).findById(testUser.getId());
        verify(courseRepository).findById(testCourse.getId());
        verify(userCourseService).hasPurchasedCourse(testUser.getId(), testCourse.getId());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("根据订单号查询订单 - 成功")
    void getOrderByOrderNo_Success() {
        // 准备测试数据
        when(orderRepository.findByOrderNo(anyString())).thenReturn(Optional.of(testOrder));
        
        // 执行方法
        OrderVO result = orderService.getOrderByOrderNo(testOrder.getOrderNo());
        
        // 验证结果
        assertNotNull(result);
        assertEquals(testOrder.getId(), result.getId());
        assertEquals(testOrder.getOrderNo(), result.getOrderNo());
        assertEquals(testUser.getId(), result.getUserId());
        assertEquals(testCourse.getId(), result.getCourseId());
        
        // 验证方法调用
        verify(orderRepository).findByOrderNo(testOrder.getOrderNo());
    }

    @Test
    @DisplayName("根据订单号查询订单 - 订单不存在")
    void getOrderByOrderNo_OrderNotFound() {
        // 准备测试数据
        when(orderRepository.findByOrderNo(anyString())).thenReturn(Optional.empty());
        
        // 验证抛出异常
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, 
                () -> orderService.getOrderByOrderNo("NONEXIST123"));
        
        assertTrue(exception.getMessage().contains("订单不存在"));
        
        // 验证方法调用
        verify(orderRepository).findByOrderNo("NONEXIST123");
    }

    @Test
    @DisplayName("申请退款 - 成功")
    void refundOrder_Success() {
        // 准备测试数据
        testOrder.setStatus(OrderStatus.PAID.getValue());
        
        OrderRefundDTO refundDTO = new OrderRefundDTO();
        refundDTO.setRefundAmount(testOrder.getAmount());
        refundDTO.setRefundReason("不想学习了");
        
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        
        // 执行方法
        OrderVO result = orderService.refundOrder(testOrder.getId(), refundDTO, testUser.getId());
        
        // 验证结果
        assertNotNull(result);
        assertEquals(OrderStatus.REFUNDING.getValue(), result.getStatus());
        assertEquals(refundDTO.getRefundAmount(), result.getRefundAmount());
        assertEquals(refundDTO.getRefundReason(), result.getRefundReason());
        
        // 验证方法调用
        verify(orderRepository).findById(testOrder.getId());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("申请退款 - 订单不存在")
    void refundOrder_OrderNotFound() {
        // 准备测试数据
        OrderRefundDTO refundDTO = new OrderRefundDTO();
        refundDTO.setRefundAmount(testOrder.getAmount());
        refundDTO.setRefundReason("不想学习了");
        
        when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        // 验证抛出异常
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, 
                () -> orderService.refundOrder(999L, refundDTO, testUser.getId()));
        
        assertTrue(exception.getMessage().contains("订单不存在"));
        
        // 验证方法调用
        verify(orderRepository).findById(999L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("申请退款 - 非订单所有者")
    void refundOrder_NotOrderOwner() {
        // 准备测试数据
        OrderRefundDTO refundDTO = new OrderRefundDTO();
        refundDTO.setRefundAmount(testOrder.getAmount());
        refundDTO.setRefundReason("不想学习了");
        
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(testOrder));
        
        // 验证抛出异常
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> orderService.refundOrder(testOrder.getId(), refundDTO, 999L));
        
        assertTrue(exception.getMessage().contains("无权操作此订单"));
        
        // 验证方法调用
        verify(orderRepository).findById(testOrder.getId());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("申请退款 - 订单状态不支持")
    void refundOrder_InvalidOrderStatus() {
        // 准备测试数据
        testOrder.setStatus(OrderStatus.CREATED);
        
        OrderRefundDTO refundDTO = new OrderRefundDTO();
        refundDTO.setRefundAmount(testOrder.getAmount());
        refundDTO.setRefundReason("不想学习了");
        
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(testOrder));
        
        // 验证抛出异常
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> orderService.refundOrder(testOrder.getId(), refundDTO, testUser.getId()));
        
        assertTrue(exception.getMessage().contains("当前订单状态不支持退款"));
        
        // 验证方法调用
        verify(orderRepository).findById(testOrder.getId());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("处理退款 - 批准成功")
    void processRefund_ApproveSuccess() throws AlipayApiException {
        // 准备测试数据
        testOrder.setStatus(OrderStatus.REFUNDING.getValue());
        testOrder.setRefundAmount(testOrder.getAmount());
        testOrder.setRefundReason("不想学习了");
        
        // 模拟支付宝退款成功
        AlipayTradeRefundResponse mockResponse = mock(AlipayTradeRefundResponse.class);
        when(mockResponse.isSuccess()).thenReturn(true);
        
        // 模拟支付宝客户端调用
        when(alipayClient.execute(any(AlipayTradeRefundRequest.class))).thenReturn(mockResponse);
        
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(testOrder));
        when(orderRepository.findByOrderNo(anyString())).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setStatus(OrderStatus.REFUNDED.getValue());
            return savedOrder;
        });
        
        // 执行方法
        OrderVO result = orderService.processRefund(testOrder.getId(), true, 2L);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(OrderStatus.REFUNDED.getValue(), result.getStatus());
        
        // 验证方法调用
        verify(orderRepository).findById(testOrder.getId());
        verify(orderRepository).findByOrderNo(testOrder.getOrderNo());
        verify(alipayClient).execute(any(AlipayTradeRefundRequest.class));
        verify(orderRepository, times(2)).save(any(Order.class));
        verify(userCourseService).updateUserCourseRefunded(testOrder.getId());
    }

    @Test
    @DisplayName("处理退款 - 拒绝退款")
    void processRefund_Reject() throws AlipayApiException {
        // 准备测试数据
        testOrder.setStatus(OrderStatus.REFUNDING.getValue());
        
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        
        // 执行方法
        OrderVO result = orderService.processRefund(testOrder.getId(), false, 2L);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(OrderStatus.REFUND_FAILED.getValue(), result.getStatus());
        
        // 验证方法调用
        verify(orderRepository).findById(testOrder.getId());
        verify(alipayClient, never()).execute(any(AlipayTradeRefundRequest.class));
        verify(orderRepository).save(any(Order.class));
        verify(userCourseService, never()).updateUserCourseRefunded(anyLong());
    }

    @Test
    @DisplayName("处理退款 - 订单状态不是申请退款")
    void processRefund_InvalidOrderStatus() throws AlipayApiException {
        // 准备测试数据
        testOrder.setStatus(OrderStatus.PAID.getValue());
        
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(testOrder));
        
        // 验证抛出异常
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> orderService.processRefund(testOrder.getId(), true, 2L));
        
        assertTrue(exception.getMessage().contains("当前订单状态不支持处理退款"));
        
        // 验证方法调用
        verify(orderRepository).findById(testOrder.getId());
        verify(alipayClient, never()).execute(any(AlipayTradeRefundRequest.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("计算机构总收入 - 成功")
    void calculateInstitutionTotalIncome_Success() {
        // 准备测试数据
        List<Order> paidOrders = Arrays.asList(
            Order.builder().amount(BigDecimal.valueOf(100)).build(),
            Order.builder().amount(BigDecimal.valueOf(200)).build(),
            Order.builder().amount(BigDecimal.valueOf(300)).build()
        );
        
        when(orderRepository.findByInstitution_IdAndStatus(anyLong(), anyInt())).thenReturn(paidOrders);
        
        // 执行方法
        BigDecimal result = orderService.calculateInstitutionTotalIncome(testInstitution.getId());
        
        // 验证结果
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(600), result);
        
        // 验证方法调用
        verify(orderRepository).findByInstitution_IdAndStatus(testInstitution.getId(), OrderStatus.PAID.getValue());
    }

    @Test
    @DisplayName("计算机构总退款 - 成功")
    void calculateInstitutionTotalRefund_Success() {
        // 准备测试数据
        List<Order> refundedOrders = Arrays.asList(
            Order.builder().refundAmount(BigDecimal.valueOf(50)).build(),
            Order.builder().refundAmount(BigDecimal.valueOf(100)).build()
        );
        
        when(orderRepository.findByInstitution_IdAndStatus(anyLong(), anyInt())).thenReturn(refundedOrders);
        
        // 执行方法
        BigDecimal result = orderService.calculateInstitutionTotalRefund(testInstitution.getId());
        
        // 验证结果
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(150), result);
        
        // 验证方法调用
        verify(orderRepository).findByInstitution_IdAndStatus(testInstitution.getId(), OrderStatus.REFUNDED.getValue());
    }

    @Test
    @DisplayName("计算机构净收入 - 成功")
    void calculateInstitutionNetIncome_Success() {
        // 准备测试数据
        List<Order> paidOrders = Arrays.asList(
            Order.builder().amount(BigDecimal.valueOf(100)).build(),
            Order.builder().amount(BigDecimal.valueOf(200)).build()
        );
        
        List<Order> refundedOrders = Collections.singletonList(
            Order.builder().refundAmount(BigDecimal.valueOf(50)).build()
        );
        
        when(orderRepository.findByInstitution_IdAndStatus(eq(testInstitution.getId()), eq(OrderStatus.PAID.getValue())))
            .thenReturn(paidOrders);
        when(orderRepository.findByInstitution_IdAndStatus(eq(testInstitution.getId()), eq(OrderStatus.REFUNDED.getValue())))
            .thenReturn(refundedOrders);
        
        // 执行方法
        BigDecimal result = orderService.calculateInstitutionNetIncome(testInstitution.getId());
        
        // 验证结果
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(250), result);
        
        // 验证方法调用
        verify(orderRepository).findByInstitution_IdAndStatus(testInstitution.getId(), OrderStatus.PAID.getValue());
        verify(orderRepository).findByInstitution_IdAndStatus(testInstitution.getId(), OrderStatus.REFUNDED.getValue());
    }

    @Test
    @DisplayName("处理支付成功回调 - 成功")
    void handlePaymentSuccess_Success() {
        // 准备测试数据
        testOrder.setStatus(OrderStatus.PENDING.getValue());
        
        when(orderRepository.findByOrderNo(anyString())).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        
        // 执行方法
        orderService.handlePaymentSuccess(testOrder.getOrderNo());
        
        // 验证结果
        assertEquals(OrderStatus.PAID.getValue(), testOrder.getStatus());
        assertNotNull(testOrder.getPaidAt());
        
        // 验证方法调用
        verify(orderRepository).findByOrderNo(testOrder.getOrderNo());
        verify(orderRepository).save(testOrder);
        verify(userCourseService).createUserCourseRelation(
                testOrder.getUser().getId(), 
                testOrder.getCourse().getId(), 
                testOrder.getId(), 
                true);
    }

    @Test
    @DisplayName("处理支付成功回调 - 订单已支付")
    void handlePaymentSuccess_OrderAlreadyPaid() {
        // 准备测试数据
        testOrder.setStatus(OrderStatus.PAID.getValue());
        testOrder.setPaidAt(LocalDateTime.now().minusDays(1)); // 设置为昨天支付
        
        when(orderRepository.findByOrderNo(anyString())).thenReturn(Optional.of(testOrder));
        
        // 执行方法
        orderService.handlePaymentSuccess(testOrder.getOrderNo());
        
        // 验证订单状态和支付时间没有被更新
        assertEquals(OrderStatus.PAID.getValue(), testOrder.getStatus());
        assertTrue(testOrder.getPaidAt().isBefore(LocalDateTime.now().minusHours(23)));
        
        // 验证方法调用
        verify(orderRepository).findByOrderNo(testOrder.getOrderNo());
        verify(orderRepository, never()).save(any(Order.class));
        verify(userCourseService, never()).createUserCourseRelation(anyLong(), anyLong(), anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("用户订单高级搜索 - 基本搜索")
    void searchUserOrders_BasicSearch() {
        // 准备测试数据
        OrderSearchDTO searchDTO = OrderSearchDTO.builder()
                .pageNum(1)
                .pageSize(10)
                .build();
        
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(List.of(testOrder), pageable, 1);
        
        // 模拟repository调用
        when(orderRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(orderPage);
        
        // 执行方法
        Page<OrderVO> result = orderService.searchUserOrders(searchDTO, userId);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testOrder.getId(), result.getContent().get(0).getId());
        
        // 验证方法调用
        verify(orderRepository).findAll(any(Specification.class), eq(pageable));
    }
    
    @Test
    @DisplayName("用户订单高级搜索 - 带条件")
    void searchUserOrders_WithConditions() {
        // 准备测试数据
        OrderSearchDTO searchDTO = OrderSearchDTO.builder()
                .pageNum(1)
                .pageSize(10)
                .orderNo("TEST")
                .status(OrderStatus.PAID.getValue())
                .minAmount(BigDecimal.valueOf(50))
                .maxAmount(BigDecimal.valueOf(200))
                .courseTitle("测试")
                .build();
        
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(List.of(testOrder), pageable, 1);
        
        // 模拟repository调用
        when(orderRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(orderPage);
        
        // 执行方法
        Page<OrderVO> result = orderService.searchUserOrders(searchDTO, userId);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testOrder.getId(), result.getContent().get(0).getId());
        
        // 验证方法调用
        verify(orderRepository).findAll(any(Specification.class), eq(pageable));
    }
    
    @Test
    @DisplayName("用户订单高级搜索 - 日期范围")
    void searchUserOrders_DateRange() {
        // 准备测试数据
        LocalDateTime start = LocalDateTime.now().minusDays(30);
        LocalDateTime end = LocalDateTime.now();
        
        OrderSearchDTO searchDTO = OrderSearchDTO.builder()
                .pageNum(1)
                .pageSize(10)
                .createdTimeStart(start)
                .createdTimeEnd(end)
                .build();
        
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(List.of(testOrder), pageable, 1);
        
        // 模拟repository调用
        when(orderRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(orderPage);
        
        // 执行方法
        Page<OrderVO> result = orderService.searchUserOrders(searchDTO, userId);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        
        // 验证方法调用
        verify(orderRepository).findAll(any(Specification.class), eq(pageable));
    }
    
    @Test
    @DisplayName("机构订单高级搜索 - 基本搜索")
    void searchInstitutionOrders_BasicSearch() {
        // 准备测试数据
        OrderSearchDTO searchDTO = OrderSearchDTO.builder()
                .pageNum(1)
                .pageSize(10)
                .build();
        
        Long institutionId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(List.of(testOrder), pageable, 1);
        
        // 模拟repository调用
        when(orderRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(orderPage);
        
        // 执行方法
        Page<OrderVO> result = orderService.searchInstitutionOrders(searchDTO, institutionId);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testOrder.getId(), result.getContent().get(0).getId());
        
        // 验证方法调用
        verify(orderRepository).findAll(any(Specification.class), eq(pageable));
    }
    
    @Test
    @DisplayName("机构订单高级搜索 - 带条件")
    void searchInstitutionOrders_WithConditions() {
        // 准备测试数据
        OrderSearchDTO searchDTO = OrderSearchDTO.builder()
                .pageNum(1)
                .pageSize(10)
                .status(OrderStatus.PAID.getValue())
                .userName("test")
                .build();
        
        Long institutionId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(List.of(testOrder), pageable, 1);
        
        // 模拟repository调用
        when(orderRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(orderPage);
        
        // 执行方法
        Page<OrderVO> result = orderService.searchInstitutionOrders(searchDTO, institutionId);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        
        // 验证方法调用
        verify(orderRepository).findAll(any(Specification.class), eq(pageable));
    }
    
    @Test
    @DisplayName("管理员订单高级搜索 - 基本搜索")
    void searchAllOrders_BasicSearch() {
        // 准备测试数据
        OrderSearchDTO searchDTO = OrderSearchDTO.builder()
                .pageNum(1)
                .pageSize(10)
                .build();
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(List.of(testOrder), pageable, 1);
        
        // 模拟repository调用
        when(orderRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(orderPage);
        
        // 执行方法
        Page<OrderVO> result = orderService.searchAllOrders(searchDTO);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testOrder.getId(), result.getContent().get(0).getId());
        
        // 验证方法调用
        verify(orderRepository).findAll(any(Specification.class), eq(pageable));
    }
    
    @Test
    @DisplayName("管理员订单高级搜索 - 复杂条件")
    void searchAllOrders_ComplexConditions() {
        // 准备测试数据
        LocalDateTime start = LocalDateTime.now().minusDays(30);
        LocalDateTime end = LocalDateTime.now();
        
        OrderSearchDTO searchDTO = OrderSearchDTO.builder()
                .pageNum(1)
                .pageSize(10)
                .orderNo("TEST")
                .status(OrderStatus.PAID.getValue())
                .createdTimeStart(start)
                .createdTimeEnd(end)
                .minAmount(BigDecimal.valueOf(50))
                .maxAmount(BigDecimal.valueOf(200))
                .courseId(1L)
                .userId(1L)
                .courseTitle("测试")
                .userName("test")
                .build();
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(List.of(testOrder), pageable, 1);
        
        // 模拟repository调用
        when(orderRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(orderPage);
        
        // 执行方法
        Page<OrderVO> result = orderService.searchAllOrders(searchDTO);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        
        // 验证方法调用
        verify(orderRepository).findAll(any(Specification.class), eq(pageable));
    }
    
    @Test
    @DisplayName("管理员订单高级搜索 - 空结果")
    void searchAllOrders_EmptyResult() {
        // 准备测试数据
        OrderSearchDTO searchDTO = OrderSearchDTO.builder()
                .pageNum(1)
                .pageSize(10)
                .status(OrderStatus.REFUNDED.getValue()) // 不存在的状态
                .build();
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        
        // 模拟repository调用
        when(orderRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(emptyPage);
        
        // 执行方法
        Page<OrderVO> result = orderService.searchAllOrders(searchDTO);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
        
        // 验证方法调用
        verify(orderRepository).findAll(any(Specification.class), eq(pageable));
    }
    
    @Test
    @DisplayName("根据订单号查询订单 - 支付成功情况")
    void getOrderByOrderNo_PaymentSuccessful() {
        // 准备测试数据 - 模拟已支付订单
        testOrder.setStatus(OrderStatus.PAID.getValue());
        testOrder.setPaidAt(LocalDateTime.now());
        testOrder.setTradeNo("ALIPAY12345678");
        
        when(orderRepository.findByOrderNo(anyString())).thenReturn(Optional.of(testOrder));
        
        // 执行方法
        OrderVO result = orderService.getOrderByOrderNo(testOrder.getOrderNo());
        
        // 验证结果
        assertNotNull(result);
        assertEquals(testOrder.getOrderNo(), result.getOrderNo());
        assertEquals(OrderStatus.PAID.getValue(), result.getStatus());
        assertNotNull(result.getPaidAt());
        assertEquals("ALIPAY12345678", result.getTradeNo());
        
        // 验证方法调用
        verify(orderRepository).findByOrderNo(testOrder.getOrderNo());
    }
    
    @Test
    @DisplayName("根据订单号查询订单 - 待支付情况")
    void getOrderByOrderNo_PendingPayment() {
        // 准备测试数据 - 模拟待支付订单
        testOrder.setStatus(OrderStatus.PENDING.getValue());
        testOrder.setPaidAt(null);
        testOrder.setTradeNo(null);
        
        when(orderRepository.findByOrderNo(anyString())).thenReturn(Optional.of(testOrder));
        
        // 执行方法
        OrderVO result = orderService.getOrderByOrderNo(testOrder.getOrderNo());
        
        // 验证结果
        assertNotNull(result);
        assertEquals(testOrder.getOrderNo(), result.getOrderNo());
        assertEquals(OrderStatus.PENDING.getValue(), result.getStatus());
        assertNull(result.getPaidAt());
        assertNull(result.getTradeNo());
        
        // 验证方法调用
        verify(orderRepository).findByOrderNo(testOrder.getOrderNo());
    }

    @Test
    @DisplayName("获取机构待处理退款申请 - 有申请")
    void getInstitutionPendingRefunds_WithRefunds() {
        // 准备测试数据
        testOrder.setStatus(OrderStatus.REFUNDING.getValue());
        testOrder.setRefundReason("课程内容不符合预期");
        testOrder.setRefundAmount(BigDecimal.valueOf(99.99));
        
        List<Order> pendingRefunds = List.of(testOrder);
        
        when(orderRepository.findByInstitution_IdAndStatus(
                anyLong(), eq(OrderStatus.REFUNDING.getValue())))
                .thenReturn(pendingRefunds);
        
        // 执行方法
        List<OrderVO> result = orderService.getInstitutionPendingRefunds(testInstitution.getId());
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testOrder.getId(), result.get(0).getId());
        assertEquals(OrderStatus.REFUNDING.getValue(), result.get(0).getStatus());
        assertEquals("课程内容不符合预期", result.get(0).getRefundReason());
        assertEquals(BigDecimal.valueOf(99.99), result.get(0).getRefundAmount());
        
        // 验证方法调用
        verify(orderRepository).findByInstitution_IdAndStatus(
                testInstitution.getId(), OrderStatus.REFUNDING.getValue());
    }
    
    @Test
    @DisplayName("获取机构待处理退款申请 - 无申请")
    void getInstitutionPendingRefunds_NoRefunds() {
        // 准备测试数据 - 返回空列表
        when(orderRepository.findByInstitution_IdAndStatus(
                anyLong(), eq(OrderStatus.REFUNDING.getValue())))
                .thenReturn(Collections.emptyList());
        
        // 执行方法
        List<OrderVO> result = orderService.getInstitutionPendingRefunds(testInstitution.getId());
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        // 验证方法调用
        verify(orderRepository).findByInstitution_IdAndStatus(
                testInstitution.getId(), OrderStatus.REFUNDING.getValue());
    }
} 