package com.zhangziqi.online_course_mine.repository;

import com.zhangziqi.online_course_mine.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
} 