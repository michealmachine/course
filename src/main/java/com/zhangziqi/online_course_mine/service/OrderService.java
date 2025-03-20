package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.model.dto.order.OrderCreateDTO;
import com.zhangziqi.online_course_mine.model.dto.order.OrderRefundDTO;
import com.zhangziqi.online_course_mine.model.dto.order.OrderSearchDTO;
import com.zhangziqi.online_course_mine.model.vo.OrderVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * 订单服务接口
 */
public interface OrderService {

    /**
     * 创建订单
     *
     * @param courseId 课程ID
     * @param userId 用户ID
     * @return 创建的订单VO
     */
    OrderVO createOrder(Long courseId, Long userId);

    /**
     * 处理支付宝同步回调
     *
     * @param params 支付宝回调参数
     * @return 处理结果
     */
    String handleAlipayReturn(Map<String, String> params);

    /**
     * 处理支付宝异步通知
     *
     * @param params 支付宝通知参数
     * @return 处理结果
     */
    String handleAlipayNotify(Map<String, String> params);

    /**
     * 根据订单号查询订单
     *
     * @param orderNo 订单号
     * @return 订单VO
     */
    OrderVO getOrderByOrderNo(String orderNo);

    /**
     * 根据ID查询订单
     *
     * @param id 订单ID
     * @return 订单VO
     */
    OrderVO getOrderById(Long id);
    
    /**
     * 取消订单
     *
     * @param id 订单ID
     * @param userId 用户ID
     * @return 取消后的订单VO
     */
    OrderVO cancelOrder(Long id, Long userId);
    
    /**
     * 申请退款
     *
     * @param id 订单ID
     * @param dto 退款信息
     * @param userId 用户ID
     * @return 更新后的订单VO
     */
    OrderVO refundOrder(Long id, OrderRefundDTO dto, Long userId);
    
    /**
     * 处理退款（机构管理员或平台管理员）
     *
     * @param id 订单ID
     * @param approved 是否批准
     * @param operatorId 操作人ID
     * @return 更新后的订单VO
     */
    OrderVO processRefund(Long id, boolean approved, Long operatorId);
    
    /**
     * 统计机构总收入（不含退款）
     *
     * @param institutionId 机构ID
     * @return 总收入金额
     */
    java.math.BigDecimal calculateInstitutionTotalIncome(Long institutionId);
    
    /**
     * 统计机构总退款
     *
     * @param institutionId 机构ID
     * @return 总退款金额
     */
    java.math.BigDecimal calculateInstitutionTotalRefund(Long institutionId);
    
    /**
     * 获取机构净收入
     *
     * @param institutionId 机构ID
     * @return 净收入金额（总收入-总退款）
     */
    java.math.BigDecimal calculateInstitutionNetIncome(Long institutionId);
    
    /**
     * 执行支付宝退款
     *
     * @param orderNo 订单号
     * @param refundAmount 退款金额
     * @param refundReason 退款原因
     * @return 退款结果，true表示成功，false表示失败
     */
    boolean executeAlipayRefund(String orderNo, java.math.BigDecimal refundAmount, String refundReason);
    
    /**
     * 处理支付成功回调
     *
     * @param orderNo 订单号
     */
    void handlePaymentSuccess(String orderNo);

    /**
     * 根据搜索条件查询用户订单
     *
     * @param searchDTO 搜索条件
     * @param userId 用户ID
     * @return 分页订单VO
     */
    Page<OrderVO> searchUserOrders(OrderSearchDTO searchDTO, Long userId);
    
    /**
     * 根据搜索条件查询机构订单
     *
     * @param searchDTO 搜索条件
     * @param institutionId 机构ID
     * @return 分页订单VO
     */
    Page<OrderVO> searchInstitutionOrders(OrderSearchDTO searchDTO, Long institutionId);
    
    /**
     * 根据搜索条件查询所有订单（管理员）
     *
     * @param searchDTO 搜索条件
     * @return 分页订单VO
     */
    Page<OrderVO> searchAllOrders(OrderSearchDTO searchDTO);
    
    /**
     * 获取机构待处理退款申请
     *
     * @param institutionId 机构ID
     * @return 待处理退款申请列表
     */
    List<OrderVO> getInstitutionPendingRefunds(Long institutionId);
    
    /**
     * 生成支付表单
     *
     * @param orderNo 订单号
     * @return 支付表单HTML字符串
     */
    String generatePaymentForm(String orderNo);
}