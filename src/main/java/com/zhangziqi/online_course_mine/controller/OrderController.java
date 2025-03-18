package com.zhangziqi.online_course_mine.controller;

import com.zhangziqi.online_course_mine.model.dto.order.OrderCreateDTO;
import com.zhangziqi.online_course_mine.model.dto.order.OrderRefundDTO;
import com.zhangziqi.online_course_mine.model.vo.OrderVO;
import com.zhangziqi.online_course_mine.model.vo.Result;
import com.zhangziqi.online_course_mine.security.SecurityUtil;
import com.zhangziqi.online_course_mine.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 订单控制器
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
@Tag(name = "订单接口", description = "订单创建、查询、退款等接口")
public class OrderController {

    private final OrderService orderService;

    /**
     * 创建订单
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "创建订单", description = "创建课程订单")
    public Result<OrderVO> createOrder(@Valid @RequestBody OrderCreateDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("创建订单, 用户ID: {}, 课程ID: {}", userId, dto.getCourseId());
        
        OrderVO orderVO = orderService.createOrder(dto.getCourseId(), userId);
        return Result.success(orderVO);
    }

    /**
     * 获取用户订单列表
     */
    @GetMapping("/my")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "获取当前用户订单列表", description = "获取已登录用户的所有订单")
    public Result<List<OrderVO>> getUserOrders() {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("获取用户订单列表, 用户ID: {}", userId);
        
        List<OrderVO> orders = orderService.getUserOrders(userId);
        return Result.success(orders);
    }

    /**
     * 分页获取用户订单
     */
    @GetMapping("/my/page")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "分页获取当前用户订单", description = "分页获取已登录用户的订单")
    public Result<Page<OrderVO>> getUserOrdersPage(
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int size) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("分页获取用户订单, 用户ID: {}, 页码: {}, 每页条数: {}", userId, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderVO> orderPage = orderService.getUserOrders(userId, pageable);
        return Result.success(orderPage);
    }

    /**
     * 根据ID获取订单详情
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "获取订单详情", description = "根据订单ID获取订单详情")
    public Result<OrderVO> getOrderById(@Parameter(description = "订单ID") @PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("获取订单详情, 订单ID: {}, 用户ID: {}", id, userId);
        
        OrderVO orderVO = orderService.getOrderById(id);
        
        // 验证订单所有者或管理员权限
        if (!orderVO.getUserId().equals(userId) && !SecurityUtil.isAdmin()) {
            return Result.fail(403, "无权访问此订单");
        }
        
        return Result.success(orderVO);
    }

    /**
     * 申请退款
     */
    @PostMapping("/{id}/refund")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "申请退款", description = "对指定订单申请退款")
    public Result<OrderVO> refundOrder(
            @Parameter(description = "订单ID") @PathVariable Long id, 
            @Valid @RequestBody OrderRefundDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("申请退款, 订单ID: {}, 用户ID: {}, 退款原因: {}", id, userId, dto.getRefundReason());
        
        OrderVO orderVO = orderService.refundOrder(id, dto, userId);
        return Result.success(orderVO);
    }

    /**
     * 支付宝异步通知
     */
    @PostMapping("/alipay/notify")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "支付宝异步通知", description = "处理支付宝异步通知")
    public String alipayNotify(@RequestParam Map<String, String> params) {
        log.info("接收到支付宝异步通知: {}", params);
        return orderService.handleAlipayNotify(params);
    }

    /**
     * 获取机构订单列表（机构管理员）
     */
    @GetMapping("/institution")
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "获取机构订单列表", description = "获取当前用户所属机构的所有订单")
    public Result<List<OrderVO>> getInstitutionOrders() {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        log.info("获取机构订单列表, 机构ID: {}", institutionId);
        
        List<OrderVO> orders = orderService.getInstitutionOrders(institutionId);
        return Result.success(orders);
    }

    /**
     * 分页获取机构订单（机构管理员）
     */
    @GetMapping("/institution/page")
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "分页获取机构订单", description = "分页获取当前用户所属机构的订单")
    public Result<Page<OrderVO>> getInstitutionOrdersPage(
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int size) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        log.info("分页获取机构订单, 机构ID: {}, 页码: {}, 每页条数: {}", institutionId, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderVO> orderPage = orderService.getInstitutionOrders(institutionId, pageable);
        return Result.success(orderPage);
    }
    
    /**
     * 获取机构收入统计（机构管理员）
     */
    @GetMapping("/institution/income")
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "获取机构收入统计", description = "获取当前用户所属机构的收入统计")
    public Result<Map<String, BigDecimal>> getInstitutionIncome() {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        log.info("获取机构收入统计, 机构ID: {}", institutionId);
        
        BigDecimal totalIncome = orderService.calculateInstitutionTotalIncome(institutionId);
        BigDecimal totalRefund = orderService.calculateInstitutionTotalRefund(institutionId);
        BigDecimal netIncome = orderService.calculateInstitutionNetIncome(institutionId);
        
        Map<String, BigDecimal> incomeStats = new HashMap<>();
        incomeStats.put("totalIncome", totalIncome);
        incomeStats.put("totalRefund", totalRefund);
        incomeStats.put("netIncome", netIncome);
        
        return Result.success(incomeStats);
    }

    /**
     * 获取所有订单（平台管理员）
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "获取所有订单", description = "平台管理员获取所有订单")
    public Result<List<OrderVO>> getAllOrders() {
        log.info("管理员获取所有订单");
        List<OrderVO> orders = orderService.getAllOrders();
        return Result.success(orders);
    }

    /**
     * 分页获取所有订单（平台管理员）
     */
    @GetMapping("/admin/page")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "分页获取所有订单", description = "平台管理员分页获取所有订单")
    public Result<Page<OrderVO>> getAllOrdersPage(
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int size) {
        log.info("管理员分页获取所有订单, 页码: {}, 每页条数: {}", page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderVO> orderPage = orderService.getAllOrders(pageable);
        return Result.success(orderPage);
    }

    /**
     * 处理退款申请（机构管理员或平台管理员）
     */
    @PostMapping("/admin/{id}/process-refund")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'INSTITUTION_ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "处理退款申请", description = "处理订单退款申请")
    public Result<OrderVO> processRefund(
            @Parameter(description = "订单ID") @PathVariable Long id,
            @Parameter(description = "是否批准") @RequestParam boolean approved) {
        Long operatorId = SecurityUtil.getCurrentUserId();
        log.info("处理退款申请, 订单ID: {}, 操作人ID: {}, 是否批准: {}", id, operatorId, approved);
        
        OrderVO orderVO = orderService.processRefund(id, approved, operatorId);
        return Result.success(orderVO);
    }
} 