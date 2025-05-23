package com.zhangziqi.online_course_mine.repository;

import com.zhangziqi.online_course_mine.model.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 订单数据访问接口
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    /**
     * 根据订单号查询订单
     */
    Optional<Order> findByOrderNo(String orderNo);

    /**
     * 根据支付宝交易号查询订单
     */
    Optional<Order> findByTradeNo(String tradeNo);

    /**
     * 根据用户ID查询所有订单
     */
    List<Order> findByUser_Id(Long userId);

    /**
     * 分页查询用户订单
     */
    Page<Order> findByUser_Id(Long userId, Pageable pageable);

    /**
     * 根据机构ID查询所有订单
     */
    List<Order> findByInstitution_Id(Long institutionId);

    /**
     * 分页查询机构订单
     */
    Page<Order> findByInstitution_Id(Long institutionId, Pageable pageable);

    /**
     * 根据课程ID查询所有订单
     */
    List<Order> findByCourse_Id(Long courseId);

    /**
     * 分页查询课程订单
     */
    Page<Order> findByCourse_Id(Long courseId, Pageable pageable);

    /**
     * 根据订单状态查询订单
     */
    List<Order> findByStatus(Integer status);

    /**
     * 分页查询订单状态
     */
    Page<Order> findByStatus(Integer status, Pageable pageable);

    /**
     * 根据用户ID和订单状态查询订单
     */
    List<Order> findByUser_IdAndStatus(Long userId, Integer status);

    /**
     * 分页查询用户订单状态
     */
    Page<Order> findByUser_IdAndStatus(Long userId, Integer status, Pageable pageable);

    /**
     * 根据机构ID和订单状态查询订单
     */
    List<Order> findByInstitution_IdAndStatus(Long institutionId, Integer status);

    /**
     * 根据机构ID和多个订单状态查询订单
     */
    List<Order> findByInstitution_IdAndStatusIn(Long institutionId, List<Integer> statuses);

    /**
     * 分页查询机构订单状态
     */
    Page<Order> findByInstitution_IdAndStatus(Long institutionId, Integer status, Pageable pageable);

    /**
     * 根据课程ID和订单状态查询订单
     */
    List<Order> findByCourse_IdAndStatus(Long courseId, Integer status);

    /**
     * 分页查询课程订单状态
     */
    Page<Order> findByCourse_IdAndStatus(Long courseId, Integer status, Pageable pageable);

    /**
     * 统计用户订单总数
     */
    long countByUser_Id(Long userId);

    /**
     * 统计机构订单总数
     */
    long countByInstitution_Id(Long institutionId);

    /**
     * 统计课程订单总数
     */
    long countByCourse_Id(Long courseId);

    /**
     * 统计订单状态总数
     */
    long countByStatus(Integer status);

    List<Order> findByUser_IdAndCourse_IdAndStatus(Long userId, Long courseId, Integer status);

    /**
     * 查询创建时间早于指定时间的待支付订单
     */
    List<Order> findByStatusAndCreatedAtBefore(Integer status, LocalDateTime createdTime);

    /**
     * 根据机构ID、多个订单状态和时间范围查询订单
     */
    List<Order> findByInstitution_IdAndStatusInAndCreatedAtBetween(
            Long institutionId, List<Integer> statuses,
            LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据机构ID、订单状态和时间范围查询订单
     */
    List<Order> findByInstitution_IdAndStatusAndCreatedAtBetween(
            Long institutionId, Integer status,
            LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据机构ID、多个订单状态和时间范围查询订单（按支付时间）
     */
    List<Order> findByInstitution_IdAndStatusInAndPaidAtBetween(
            Long institutionId, List<Integer> statuses,
            LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据机构ID、订单状态和时间范围查询订单（按支付时间）
     */
    List<Order> findByInstitution_IdAndStatusAndPaidAtBetween(
            Long institutionId, Integer status,
            LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据机构ID、订单状态和时间范围查询订单（按退款时间）
     */
    List<Order> findByInstitution_IdAndStatusAndRefundedAtBetween(
            Long institutionId, Integer status,
            LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据多个订单状态查询订单
     */
    List<Order> findByStatusIn(List<Integer> statuses);

    /**
     * 根据多个订单状态和支付时间范围查询订单
     */
    List<Order> findByStatusInAndPaidAtBetween(
            List<Integer> statuses,
            LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据订单状态和退款时间范围查询订单
     */
    List<Order> findByStatusAndRefundedAtBetween(
            Integer status,
            LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据创建时间范围查询订单
     */
    List<Order> findByCreatedAtBetween(
            LocalDateTime startTime, LocalDateTime endTime);
}