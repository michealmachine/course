package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.model.dto.QuotaApplicationDTO;
import com.zhangziqi.online_course_mine.model.vo.QuotaApplicationVO;
import org.springframework.data.domain.Page;

/**
 * 存储配额申请服务
 */
public interface QuotaApplicationService {
    
    /**
     * 申请增加存储配额
     *
     * @param username 当前用户名
     * @param dto 申请信息DTO
     * @return 申请ID
     */
    String applyQuota(String username, QuotaApplicationDTO dto);
    
    /**
     * 获取申请状态
     *
     * @param username 当前用户名
     * @param applicationId 申请ID
     * @return 申请详情
     */
    QuotaApplicationVO getApplicationStatus(String username, String applicationId);
    
    /**
     * 获取用户的申请列表
     *
     * @param username 当前用户名
     * @param status 状态（可选）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 申请列表
     */
    Page<QuotaApplicationVO> getUserApplications(String username, Integer status, int pageNum, int pageSize);
    
    /**
     * 获取机构的申请列表（机构管理员可查看）
     *
     * @param username 当前用户名
     * @param status 状态（可选）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 申请列表
     */
    Page<QuotaApplicationVO> getInstitutionApplications(String username, Integer status, int pageNum, int pageSize);
    
    /**
     * 获取所有申请列表（管理员可查看）
     *
     * @param status 状态（可选）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 申请列表
     */
    Page<QuotaApplicationVO> getAllApplications(Integer status, int pageNum, int pageSize);
    
    /**
     * 获取申请详情
     *
     * @param id 申请ID
     * @return 申请详情
     */
    QuotaApplicationVO getApplicationDetail(Long id);
    
    /**
     * 审核通过申请
     *
     * @param id 申请ID
     * @param reviewerUsername 审核人用户名
     * @return 申请详情
     */
    QuotaApplicationVO approveApplication(Long id, String reviewerUsername);
    
    /**
     * 审核拒绝申请
     *
     * @param id 申请ID
     * @param reason 拒绝原因
     * @param reviewerUsername 审核人用户名
     */
    void rejectApplication(Long id, String reason, String reviewerUsername);
} 