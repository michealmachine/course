package com.zhangziqi.online_course_mine.repository;

import com.zhangziqi.online_course_mine.model.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 订单数据访问接口
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
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
} 