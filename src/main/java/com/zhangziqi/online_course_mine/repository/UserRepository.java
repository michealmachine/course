package com.zhangziqi.online_course_mine.repository;

import com.zhangziqi.online_course_mine.model.entity.User;
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
 * 用户Repository
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    /**
     * 根据用户名查找用户
     *
     * @param username 用户名
     * @return 用户
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据邮箱查找用户
     *
     * @param email 邮箱
     * @return 用户
     */
    Optional<User> findByEmail(String email);

    /**
     * 根据手机号查找用户
     *
     * @param phone 手机号
     * @return 用户
     */
    Optional<User> findByPhone(String phone);

    /**
     * 判断用户名是否存在
     *
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 判断邮箱是否存在
     *
     * @param email 邮箱
     * @return 是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 判断手机号是否存在
     *
     * @param phone 手机号
     * @return 是否存在
     */
    boolean existsByPhone(String phone);

    /**
     * 统计机构成员数量
     *
     * @param institutionId 机构ID
     * @return 成员数量
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.institution.id = :institutionId")
    long countByInstitutionId(@Param("institutionId") Long institutionId);

    /**
     * 根据机构ID查询所有用户
     *
     * @param institutionId 机构ID
     * @return 用户列表
     */
    @Query("SELECT u FROM User u WHERE u.institution.id = :institutionId")
    List<User> findByInstitutionId(@Param("institutionId") Long institutionId);

    /**
     * 根据机构ID分页查询用户
     *
     * @param institutionId 机构ID
     * @param pageable 分页参数
     * @return 分页用户列表
     */
    @Query("SELECT u FROM User u WHERE u.institution.id = :institutionId")
    Page<User> findByInstitutionId(@Param("institutionId") Long institutionId, Pageable pageable);

    /**
     * 根据机构ID和关键字搜索用户（用户名或邮箱包含关键字）
     *
     * @param institutionId 机构ID
     * @param keyword 关键字（用于搜索用户名或邮箱）
     * @param pageable 分页参数
     * @return 分页用户列表
     */
    @Query("SELECT u FROM User u WHERE u.institution.id = :institutionId " +
           "AND (LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<User> findByInstitutionIdAndKeyword(
            @Param("institutionId") Long institutionId,
            @Param("keyword") String keyword,
            Pageable pageable);

    /**
     * 根据机构ID和用户名模糊查询或邮箱模糊查询
     *
     * @param institutionId 机构ID
     * @param username 用户名关键词
     * @param institutionId2 机构ID（重复参数，与institutionId相同）
     * @param email 邮箱关键词
     * @param pageable 分页参数
     * @return 分页用户列表
     */
    @Query("SELECT u FROM User u WHERE (u.institution.id = :institutionId1 AND LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%'))) " +
           "OR (u.institution.id = :institutionId2 AND LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%')))")
    Page<User> findByInstitutionIdAndUsernameContainingIgnoreCaseOrInstitutionIdAndEmailContainingIgnoreCase(
            @Param("institutionId1") Long institutionId1,
            @Param("username") String username,
            @Param("institutionId2") Long institutionId2,
            @Param("email") String email,
            Pageable pageable);

    /**
     * 根据用户角色ID查询用户数量
     *
     * @param roleId 角色ID
     * @return 用户数量
     */
    @Query("SELECT COUNT(DISTINCT u) FROM User u JOIN u.roles r WHERE r.id = :roleId")
    long countByRoleId(@Param("roleId") Long roleId);

    /**
     * 统计指定时间范围内创建的用户数量
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 用户数量
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt BETWEEN :startTime AND :endTime")
    long countByCreatedAtBetween(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定状态的用户数量
     *
     * @param status 用户状态
     * @return 用户数量
     */
    long countByStatus(Integer status);

    /**
     * 统计最后登录时间在指定时间之后的用户数量
     *
     * @param lastLoginTime 指定时间
     * @return 用户数量
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.lastLoginAt IS NOT NULL AND u.lastLoginAt >= :lastLoginTime")
    long countByLastLoginAtAfter(@Param("lastLoginTime") LocalDateTime lastLoginTime);

    /**
     * 统计最后登录时间在指定时间范围内的用户数量
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 用户数量
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.lastLoginAt IS NOT NULL AND u.lastLoginAt BETWEEN :startTime AND :endTime")
    long countByLastLoginAtBetween(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定日期每个小时的用户登录分布
     *
     * @param dateStart 日期开始时间
     * @param dateEnd 日期结束时间
     * @return 小时 -> 用户数量 的键值对列表
     */
    @Query("SELECT HOUR(u.lastLoginAt) as hour, COUNT(u) as count FROM User u " +
           "WHERE u.lastLoginAt BETWEEN :dateStart AND :dateEnd " +
           "GROUP BY HOUR(u.lastLoginAt) ORDER BY hour")
    List<Object[]> countUserLoginByHourOfDay(
            @Param("dateStart") LocalDateTime dateStart,
            @Param("dateEnd") LocalDateTime dateEnd);

    /**
     * 统计最近一段时间内每个星期几的用户登录分布
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 星期几(1-7) -> 用户数量 的键值对列表
     */
    @Query("SELECT FUNCTION('DAYOFWEEK', u.lastLoginAt) as weekday, COUNT(u) as count FROM User u " +
           "WHERE u.lastLoginAt BETWEEN :startTime AND :endTime " +
           "GROUP BY FUNCTION('DAYOFWEEK', u.lastLoginAt) ORDER BY weekday")
    List<Object[]> countUserLoginByDayOfWeek(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 按日期统计用户注册数量
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 日期 -> 注册用户数 的键值对列表
     */
    @Query("SELECT FUNCTION('DATE_FORMAT', u.createdAt, '%Y-%m-%d') as date, COUNT(u) as count FROM User u " +
           "WHERE u.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY FUNCTION('DATE_FORMAT', u.createdAt, '%Y-%m-%d') ORDER BY date")
    List<Object[]> countUserRegistrationsByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 按日期统计用户活跃数量
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 日期 -> 活跃用户数 的键值对列表
     */
    @Query("SELECT FUNCTION('DATE_FORMAT', u.lastLoginAt, '%Y-%m-%d') as date, COUNT(u) as count FROM User u " +
           "WHERE u.lastLoginAt BETWEEN :startDate AND :endDate " +
           "GROUP BY FUNCTION('DATE_FORMAT', u.lastLoginAt, '%Y-%m-%d') ORDER BY date")
    List<Object[]> countUserActivityByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}