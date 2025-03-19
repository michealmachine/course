package com.zhangziqi.online_course_mine.controller;

import com.zhangziqi.online_course_mine.model.dto.order.OrderCreateDTO;
import com.zhangziqi.online_course_mine.model.dto.order.OrderRefundDTO;
import com.zhangziqi.online_course_mine.model.dto.order.OrderSearchDTO;
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
     * 根据订单号查询订单状态（前端轮询支付状态用）
     */
    @GetMapping("/query")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "根据订单号查询订单", description = "前端根据订单号轮询查询订单状态")
    public Result<OrderVO> queryOrderByOrderNo(@Parameter(description = "订单号") @RequestParam String orderNo) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("查询订单状态, 订单号: {}, 用户ID: {}", orderNo, userId);
        
        OrderVO orderVO = orderService.getOrderByOrderNo(orderNo);
        
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

    /**
     * 高级搜索个人订单
     */
    @PostMapping("/my/search")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "高级搜索用户订单", description = "根据条件高级搜索已登录用户的订单")
    public Result<Page<OrderVO>> searchUserOrders(@Valid @RequestBody OrderSearchDTO searchDTO) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("高级搜索用户订单, 用户ID: {}, 搜索条件: {}", userId, searchDTO);
        
        Page<OrderVO> orderPage = orderService.searchUserOrders(searchDTO, userId);
        return Result.success(orderPage);
    }

    /**
     * 高级搜索机构订单（机构管理员）
     */
    @PostMapping("/institution/search")
    @PreAuthorize("hasAuthority('ROLE_INSTITUTION')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "高级搜索机构订单", description = "根据条件高级搜索机构的所有订单")
    public Result<Page<OrderVO>> searchInstitutionOrders(@Valid @RequestBody OrderSearchDTO searchDTO) {
        Long institutionId = SecurityUtil.getCurrentInstitutionId();
        log.info("高级搜索机构订单, 机构ID: {}, 搜索条件: {}", institutionId, searchDTO);
        
        Page<OrderVO> orderPage = orderService.searchInstitutionOrders(searchDTO, institutionId);
        return Result.success(orderPage);
    }

    /**
     * 高级搜索所有订单（平台管理员）
     */
    @PostMapping("/admin/search")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "高级搜索所有订单", description = "平台管理员根据条件高级搜索所有订单")
    public Result<Page<OrderVO>> searchAllOrders(@Valid @RequestBody OrderSearchDTO searchDTO) {
        log.info("管理员高级搜索所有订单, 搜索条件: {}", searchDTO);
        
        Page<OrderVO> orderPage = orderService.searchAllOrders(searchDTO);
        return Result.success(orderPage);
    }
} 