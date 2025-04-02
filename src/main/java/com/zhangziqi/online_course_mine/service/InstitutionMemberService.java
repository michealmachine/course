package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.model.vo.UserVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 机构成员管理服务接口
 */
public interface InstitutionMemberService {
    
    /**
     * 获取机构成员列表
     *
     * @param institutionId 机构ID
     * @param keyword 关键字（用户名或邮箱）
     * @param pageable 分页参数
     * @return 成员列表
     */
    Page<UserVO> getInstitutionMembers(Long institutionId, String keyword, Pageable pageable);
    
    /**
     * 统计机构成员数量
     *
     * @param institutionId 机构ID
     * @return 成员数量
     */
    long countInstitutionMembers(Long institutionId);
    
    /**
     * 移除机构成员
     *
     * @param institutionId 机构ID
     * @param userId 用户ID
     * @param operatorUsername 操作者用户名
     */
    void removeMember(Long institutionId, Long userId, String operatorUsername);
    
    /**
     * 获取机构成员统计信息
     *
     * @param institutionId 机构ID
     * @return 统计信息
     */
    java.util.Map<String, Object> getMemberStats(Long institutionId);
} 