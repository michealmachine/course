package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.model.dto.course.*;
import com.zhangziqi.online_course_mine.model.vo.CourseStructureVO;
import com.zhangziqi.online_course_mine.model.vo.CourseVO;
import com.zhangziqi.online_course_mine.model.vo.PreviewUrlVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

/**
 * 课程服务接口
 */
public interface CourseService {

    /**
     * 创建课程
     *
     * @param dto 课程创建DTO
     * @param creatorId 创建者ID
     * @param institutionId 机构ID
     * @return 创建的课程
     */
    CourseVO createCourse(CourseCreateDTO dto, Long creatorId, Long institutionId);

    /**
     * 更新课程
     *
     * @param id 课程ID
     * @param dto 课程更新DTO
     * @param institutionId 机构ID
     * @return 更新后的课程
     */
    CourseVO updateCourse(Long id, CourseCreateDTO dto, Long institutionId);

    /**
     * 获取课程详情
     *
     * @param id 课程ID
     * @return 课程
     */
    CourseVO getCourseById(Long id);
    
    /**
     * 获取课程完整结构（包括章节和小节）
     *
     * @param id 课程ID
     * @return 课程结构（含章节和小节）
     */
    CourseStructureVO getCourseStructure(Long id);

    /**
     * 获取机构下的课程列表
     *
     * @param institutionId 机构ID
     * @param pageable 分页参数
     * @return 课程分页
     */
    Page<CourseVO> getCoursesByInstitution(Long institutionId, Pageable pageable);

    /**
     * 删除课程
     *
     * @param id 课程ID
     */
    void deleteCourse(Long id);

    /**
     * 更新课程封面
     *
     * @param id 课程ID
     * @param file 封面图片文件
     * @return 更新后的课程
     * @throws IOException 如果文件处理出错
     */
    CourseVO updateCourseCover(Long id, MultipartFile file) throws IOException;

    /**
     * 提交课程审核
     *
     * @param id 课程ID
     * @return 更新后的课程
     */
    CourseVO submitForReview(Long id);

    /**
     * 开始审核课程
     *
     * @param id 课程ID
     * @param reviewerId 审核人ID
     * @return 更新后的课程
     */
    CourseVO startReview(Long id, Long reviewerId);

    /**
     * 通过课程审核
     *
     * @param id 课程ID
     * @param comment 审核意见
     * @param reviewerId 审核人ID
     * @return 更新后的课程
     */
    CourseVO approveCourse(Long id, String comment, Long reviewerId);

    /**
     * 拒绝课程审核
     *
     * @param id 课程ID
     * @param reason 拒绝原因
     * @param reviewerId 审核人ID
     * @return 更新后的课程
     */
    CourseVO rejectCourse(Long id, String reason, Long reviewerId);

    /**
     * 下线课程
     *
     * @param id 课程ID
     * @return 更新后的课程
     */
    CourseVO unpublishCourse(Long id);

    /**
     * 重新上线课程
     *
     * @param id 课程ID
     * @return 更新后的课程
     */
    CourseVO republishCourse(Long id);

    /**
     * 重新编辑被拒绝的课程
     *
     * @param id 课程ID
     * @return 更新后的课程
     */
    CourseVO reEditRejectedCourse(Long id);

    /**
     * 生成课程预览URL
     *
     * @param id 课程ID
     * @return 预览URL
     */
    PreviewUrlVO generatePreviewUrl(Long id);

    /**
     * 根据预览token获取课程
     *
     * @param token 预览token
     * @return 课程
     */
    CourseVO getCourseByPreviewToken(String token);

    /**
     * 通过预览令牌获取课程结构
     *
     * @param token 预览令牌
     * @return 课程结构（含章节和小节）
     */
    CourseStructureVO getCourseStructureByPreviewToken(String token);

    /**
     * 更新课程支付设置
     *
     * @param id 课程ID
     * @param paymentType 支付类型
     * @param price 价格
     * @param discountPrice 折扣价格
     * @return 更新后的课程
     */
    CourseVO updatePaymentSettings(Long id, Integer paymentType, 
        java.math.BigDecimal price, java.math.BigDecimal discountPrice);

    /**
     * 获取指定状态的课程列表
     *
     * @param status 课程状态
     * @param pageable 分页参数
     * @return 课程分页
     */
    Page<CourseVO> getCoursesByStatus(Integer status, Pageable pageable);
    
    /**
     * 获取指定状态且由特定审核员负责的课程列表
     *
     * @param status 课程状态
     * @param reviewerId 审核员ID
     * @param pageable 分页参数
     * @return 课程分页
     */
    Page<CourseVO> getCoursesByStatusAndReviewer(Integer status, Long reviewerId, Pageable pageable);

    /**
     * 获取机构的工作区课程列表（非发布版本）
     * 
     * @param institutionId 机构ID
     * @param pageable 分页参数
     * @return 课程VO分页
     */
    Page<CourseVO> getWorkspaceCoursesByInstitution(Long institutionId, Pageable pageable);

    /**
     * 获取机构的发布课程列表
     * 
     * @param institutionId 机构ID
     * @param pageable 分页参数
     * @return 课程VO分页
     */
    Page<CourseVO> getPublishedCoursesByInstitution(Long institutionId, Pageable pageable);
    
    /**
     * 根据工作区版本ID获取发布版本
     * 
     * @param workspaceId 工作区版本ID
     * @return 发布版本课程，如果不存在则返回null
     */
    CourseVO getPublishedVersionByWorkspaceId(Long workspaceId);
} 