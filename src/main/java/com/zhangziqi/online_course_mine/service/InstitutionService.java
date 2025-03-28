package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.model.dto.InstitutionApplyDTO;
import com.zhangziqi.online_course_mine.model.dto.InstitutionApplicationQueryDTO;
import com.zhangziqi.online_course_mine.model.dto.InstitutionUpdateDTO;
import com.zhangziqi.online_course_mine.model.vo.InstitutionApplicationVO;
import com.zhangziqi.online_course_mine.model.vo.InstitutionVO;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 机构服务接口
 */
public interface InstitutionService {
    
    /**
     * 申请创建机构
     *
     * @param applyDTO 申请参数
     * @return 申请ID
     */
    String applyInstitution(InstitutionApplyDTO applyDTO);
    
    /**
     * 查询申请状态
     *
     * @param applicationId 申请ID
     * @param email 联系邮箱
     * @return 申请状态
     */
    InstitutionApplicationVO getApplicationStatus(String applicationId, String email);
    
    /**
     * 分页查询机构申请
     *
     * @param queryDTO 查询参数
     * @return 申请分页
     */
    Page<InstitutionApplicationVO> getApplications(InstitutionApplicationQueryDTO queryDTO);
    
    /**
     * 查询申请详情
     *
     * @param id 申请ID
     * @return 申请详情
     */
    InstitutionApplicationVO getApplicationDetail(Long id);
    
    /**
     * 审核通过申请
     *
     * @param id 申请ID
     * @param reviewerUsername 审核人用户名
     * @return 机构信息
     */
    InstitutionVO approveApplication(Long id, String reviewerUsername);
    
    /**
     * 拒绝申请
     *
     * @param id 申请ID
     * @param reason 拒绝原因
     * @param reviewerUsername 审核人用户名
     */
    void rejectApplication(Long id, String reason, String reviewerUsername);
    
    /**
     * 获取机构注册码
     *
     * @param username 用户名
     * @return 注册码
     */
    String getInstitutionRegisterCode(String username);
    
    /**
     * 检查用户是否为机构管理员
     * 通过比较用户邮箱和机构联系邮箱判断
     *
     * @param username 用户名
     * @param institutionId 机构ID
     * @return 是否为机构管理员
     */
    boolean isInstitutionAdmin(String username, Long institutionId);
    
    /**
     * 获取机构详情
     *
     * @param id 机构ID
     * @return 机构详情
     */
    InstitutionVO getInstitutionDetail(Long id);
    
    /**
     * 获取机构详情，可控制是否显示注册码
     *
     * @param id 机构ID
     * @param username 当前用户名
     * @return 机构详情
     */
    InstitutionVO getInstitutionDetail(Long id, String username);
    
    /**
     * 更新机构信息
     *
     * @param id 机构ID
     * @param updateDTO 更新参数
     * @param username 用户名
     * @return 更新后的机构信息
     */
    InstitutionVO updateInstitution(Long id, InstitutionUpdateDTO updateDTO, String username);
    
    /**
     * 更新机构Logo
     *
     * @param id 机构ID
     * @param file Logo文件
     * @param username 用户名
     * @return 更新后的机构信息
     * @throws IOException 文件处理异常
     */
    InstitutionVO updateInstitutionLogo(Long id, MultipartFile file, String username) throws IOException;
    
    /**
     * 重置机构注册码
     *
     * @param id 机构ID
     * @param username 用户名
     * @return 新的注册码
     */
    String resetInstitutionRegisterCode(Long id, String username);
} 