package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.model.entity.Course;
import com.zhangziqi.online_course_mine.model.entity.Institution;
import com.zhangziqi.online_course_mine.model.entity.Order;
import com.zhangziqi.online_course_mine.model.entity.User;
import com.zhangziqi.online_course_mine.model.enums.OrderStatus;
import com.zhangziqi.online_course_mine.model.vo.*;
import com.zhangziqi.online_course_mine.model.vo.AdminCourseIncomeRankingVO;
import com.zhangziqi.online_course_mine.repository.OrderRepository;
import com.zhangziqi.online_course_mine.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceStatsTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Institution institution;
    private Course course1;
    private Course course2;
    private User user;
    private List<Order> paidOrders;
    private List<Order> refundedOrders;
    private List<Order> allOrders;

    @BeforeEach
    void setUp() {
        // 创建测试数据
        institution = new Institution();
        institution.setId(1L);
        institution.setName("测试机构");

        course1 = new Course();
        course1.setId(1L);
        course1.setTitle("测试课程1");
        course1.setCoverImage("cover1.jpg");
        course1.setInstitution(institution);

        course2 = new Course();
        course2.setId(2L);
        course2.setTitle("测试课程2");
        course2.setCoverImage("cover2.jpg");
        course2.setInstitution(institution);

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        // 创建已支付订单
        paidOrders = new ArrayList<>();
        Order paidOrder1 = new Order();
        paidOrder1.setId(1L);
        paidOrder1.setOrderNo("ORDER001");
        paidOrder1.setUser(user);
        paidOrder1.setCourse(course1);
        paidOrder1.setInstitution(institution);
        paidOrder1.setAmount(new BigDecimal("100.00"));
        paidOrder1.setStatus(OrderStatus.PAID.getValue());
        paidOrder1.setCreatedAt(LocalDateTime.now().minusDays(5));
        paidOrder1.setPaidAt(LocalDateTime.now().minusDays(5));
        paidOrders.add(paidOrder1);

        Order paidOrder2 = new Order();
        paidOrder2.setId(2L);
        paidOrder2.setOrderNo("ORDER002");
        paidOrder2.setUser(user);
        paidOrder2.setCourse(course2);
        paidOrder2.setInstitution(institution);
        paidOrder2.setAmount(new BigDecimal("200.00"));
        paidOrder2.setStatus(OrderStatus.PAID.getValue());
        paidOrder2.setCreatedAt(LocalDateTime.now().minusDays(3));
        paidOrder2.setPaidAt(LocalDateTime.now().minusDays(3));
        paidOrders.add(paidOrder2);

        // 创建已退款订单
        refundedOrders = new ArrayList<>();
        Order refundedOrder = new Order();
        refundedOrder.setId(3L);
        refundedOrder.setOrderNo("ORDER003");
        refundedOrder.setUser(user);
        refundedOrder.setCourse(course1);
        refundedOrder.setInstitution(institution);
        refundedOrder.setAmount(new BigDecimal("100.00"));
        refundedOrder.setRefundAmount(new BigDecimal("100.00"));
        refundedOrder.setStatus(OrderStatus.REFUNDED.getValue());
        refundedOrder.setCreatedAt(LocalDateTime.now().minusDays(2));
        refundedOrder.setPaidAt(LocalDateTime.now().minusDays(2));
        refundedOrder.setRefundedAt(LocalDateTime.now().minusDays(1));
        refundedOrders.add(refundedOrder);

        // 所有订单
        allOrders = new ArrayList<>();
        allOrders.addAll(paidOrders);
        allOrders.addAll(refundedOrders);

        // 添加一个待支付订单
        Order pendingOrder = new Order();
        pendingOrder.setId(4L);
        pendingOrder.setOrderNo("ORDER004");
        pendingOrder.setUser(user);
        pendingOrder.setCourse(course2);
        pendingOrder.setInstitution(institution);
        pendingOrder.setAmount(new BigDecimal("150.00"));
        pendingOrder.setStatus(OrderStatus.PENDING.getValue());
        pendingOrder.setCreatedAt(LocalDateTime.now().minusHours(2));
        allOrders.add(pendingOrder);
    }

    @Test
    void testGetInstitutionOrderStatusDistribution() {
        // 设置Mock行为
        when(orderRepository.findByInstitution_Id(eq(1L))).thenReturn(allOrders);

        // 调用被测试方法
        List<OrderStatusDistributionVO> result = orderService.getInstitutionOrderStatusDistribution(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(6, result.size()); // 应该有6种状态

        // 验证已支付订单的统计
        OrderStatusDistributionVO paidStatus = result.stream()
                .filter(vo -> vo.getStatus().equals(OrderStatus.PAID.getValue()))
                .findFirst()
                .orElse(null);

        assertNotNull(paidStatus);
        assertEquals(2, paidStatus.getCount());
        assertEquals(50.0, paidStatus.getPercentage()); // 2/4 = 50%

        // 验证已退款订单的统计
        OrderStatusDistributionVO refundedStatus = result.stream()
                .filter(vo -> vo.getStatus().equals(OrderStatus.REFUNDED.getValue()))
                .findFirst()
                .orElse(null);

        assertNotNull(refundedStatus);
        assertEquals(1, refundedStatus.getCount());
        assertEquals(25.0, refundedStatus.getPercentage()); // 1/4 = 25%

        // 验证待支付订单的统计
        OrderStatusDistributionVO pendingStatus = result.stream()
                .filter(vo -> vo.getStatus().equals(OrderStatus.PENDING.getValue()))
                .findFirst()
                .orElse(null);

        assertNotNull(pendingStatus);
        assertEquals(1, pendingStatus.getCount());
        assertEquals(25.0, pendingStatus.getPercentage()); // 1/4 = 25%
    }

    @Test
    void testGetInstitutionCourseIncomeRanking() {
        // 设置Mock行为
        when(orderRepository.findByInstitution_IdAndStatusIn(eq(1L), any())).thenReturn(paidOrders);
        when(orderRepository.findByInstitution_IdAndStatus(eq(1L), eq(OrderStatus.REFUNDED.getValue()))).thenReturn(refundedOrders);

        // 调用被测试方法
        List<CourseIncomeRankingVO> result = orderService.getInstitutionCourseIncomeRanking(1L, 10);

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());

        // 验证排序（按收入降序）
        assertEquals(course2.getId(), result.get(0).getCourseId());
        assertEquals(new BigDecimal("200.00"), result.get(0).getIncome());

        assertEquals(course1.getId(), result.get(1).getCourseId());
        assertEquals(new BigDecimal("0.00"), result.get(1).getIncome()); // 100 - 100 = 0
    }

    @Test
    void testGetPlatformIncomeStats() {
        // 设置Mock行为
        LocalDateTime startDate = LocalDateTime.now().minusDays(10);
        LocalDateTime endDate = LocalDateTime.now();
        when(orderRepository.findByCreatedAtBetween(eq(startDate), eq(endDate))).thenReturn(allOrders);

        // 调用被测试方法
        PlatformIncomeStatsVO result = orderService.getPlatformIncomeStats(startDate, endDate);

        // 验证结果
        assertNotNull(result);
        assertEquals(new BigDecimal("300.00"), result.getTotalIncome()); // 100 + 200 = 300
        assertEquals(new BigDecimal("100.00"), result.getTotalRefund()); // 100
        assertEquals(new BigDecimal("200.00"), result.getNetIncome()); // 300 - 100 = 200
        assertEquals(4, result.getOrderCount()); // 总共4个订单
        assertEquals(2, result.getPaidOrderCount()); // 2个已支付订单
        assertEquals(1, result.getRefundOrderCount()); // 1个已退款订单
    }

    @Test
    void testGetInstitutionIncomeTrend() {
        // 设置Mock行为

        when(orderRepository.findByInstitution_IdAndStatusInAndPaidAtBetween(
                eq(1L), any(), any(), any())).thenReturn(paidOrders);
        when(orderRepository.findByInstitution_IdAndStatusAndRefundedAtBetween(
                eq(1L), eq(OrderStatus.REFUNDED.getValue()), any(), any())).thenReturn(refundedOrders);

        // 调用被测试方法
        List<IncomeTrendVO> result = orderService.getInstitutionIncomeTrend(1L, "7d", "day");

        // 验证结果
        assertNotNull(result);
        assertTrue(result.size() >= 7); // 应该有至少7天的数据（包括填充的空日期）

        // 验证总收入和净收入
        BigDecimal totalIncome = result.stream()
                .map(IncomeTrendVO::getIncome)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(new BigDecimal("300.00"), totalIncome);

        BigDecimal totalRefund = result.stream()
                .map(IncomeTrendVO::getRefund)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(new BigDecimal("100.00"), totalRefund);
    }

    @Test
    void testGetPlatformIncomeTrend() {
        // 设置Mock行为

        when(orderRepository.findByStatusInAndPaidAtBetween(
                any(), any(), any())).thenReturn(paidOrders);
        when(orderRepository.findByStatusAndRefundedAtBetween(
                eq(OrderStatus.REFUNDED.getValue()), any(), any())).thenReturn(refundedOrders);

        // 调用被测试方法
        List<IncomeTrendVO> result = orderService.getPlatformIncomeTrend("7d", "day");

        // 验证结果
        assertNotNull(result);
        assertTrue(result.size() >= 7); // 应该有至少7天的数据（包括填充的空日期）

        // 验证总收入和净收入
        BigDecimal totalIncome = result.stream()
                .map(IncomeTrendVO::getIncome)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(new BigDecimal("300.00"), totalIncome);

        BigDecimal totalRefund = result.stream()
                .map(IncomeTrendVO::getRefund)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(new BigDecimal("100.00"), totalRefund);
    }

    @Test
    void testGetPlatformOrderStatusDistribution() {
        // 设置Mock行为
        when(orderRepository.findAll()).thenReturn(allOrders);

        // 调用被测试方法
        List<OrderStatusDistributionVO> result = orderService.getPlatformOrderStatusDistribution();

        // 验证结果
        assertNotNull(result);
        assertEquals(6, result.size()); // 应该有6种状态

        // 验证已支付订单的统计
        OrderStatusDistributionVO paidStatus = result.stream()
                .filter(vo -> vo.getStatus().equals(OrderStatus.PAID.getValue()))
                .findFirst()
                .orElse(null);

        assertNotNull(paidStatus);
        assertEquals(2, paidStatus.getCount());
        assertEquals(50.0, paidStatus.getPercentage()); // 2/4 = 50%

        // 验证已退款订单的统计
        OrderStatusDistributionVO refundedStatus = result.stream()
                .filter(vo -> vo.getStatus().equals(OrderStatus.REFUNDED.getValue()))
                .findFirst()
                .orElse(null);

        assertNotNull(refundedStatus);
        assertEquals(1, refundedStatus.getCount());
        assertEquals(25.0, refundedStatus.getPercentage()); // 1/4 = 25%
    }

    @Test
    void testGetPlatformCourseIncomeRanking() {
        // 设置Mock行为
        when(orderRepository.findByStatusIn(any())).thenReturn(paidOrders);
        when(orderRepository.findByStatus(eq(OrderStatus.REFUNDED.getValue()))).thenReturn(refundedOrders);

        // 调用被测试方法
        List<AdminCourseIncomeRankingVO> result = orderService.getPlatformCourseIncomeRanking(10);

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());

        // 验证排序（按收入降序）
        assertEquals(course2.getId(), result.get(0).getCourseId());
        assertEquals(new BigDecimal("200.00"), result.get(0).getIncome());
        assertEquals(institution.getId(), result.get(0).getInstitutionId());
        assertEquals(institution.getName(), result.get(0).getInstitutionName());

        assertEquals(course1.getId(), result.get(1).getCourseId());
        assertEquals(new BigDecimal("0.00"), result.get(1).getIncome()); // 100 - 100 = 0
        assertEquals(institution.getId(), result.get(1).getInstitutionId());
        assertEquals(institution.getName(), result.get(1).getInstitutionName());
    }
}
